package utilities;

import model.*;
import persistence.dao.InsumoDAO;
import persistence.dao.InsumoFaltanteDAO;
import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.Logger;

public class RecetaProcessor {

    private static final Logger logger = Logger.getLogger(RecetaProcessor.class.getName());
    private final InsumoDAO insumoDAO = new InsumoDAO();
    private final InsumoFaltanteDAO faltanteDAO = new InsumoFaltanteDAO();

    /**
     * Procesa las recetas de productos, descuenta stock y guarda insumos faltantes si no alcanza.
     * @param productos productos seleccionados con su cantidad
     * @return lista de insumos faltantes
     */
    public List<InsumoFaltante> procesarRecetas(Map<Producto, Integer> productos) {
        logger.info("Iniciando procesamiento de recetas para productos: " + productos);
        Map<String, Double> faltantesAcumulados = new HashMap<>();
        Map<String, InsumoFaltante> faltanteInfo = new HashMap<>();
        Map<String, String> unidadPreferida = new HashMap<>();

        for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
            Producto producto = entry.getKey();
            int cantidadProducto = entry.getValue();
            Receta receta = producto.getReceta();

            logger.info("Procesando producto: " + producto.getNombre() + ", cantidad: " + cantidadProducto);

            if (receta == null) {
                logger.warning("Producto sin receta: " + producto.getNombre());
                continue;
            }

            for (InsumoReceta insumoReceta : receta.getInsumosReceta()) {
                String nombreInsumo = insumoReceta.getInsumo().getNombre();
                String unidadRequerida = insumoReceta.getUnidad();
                double totalNecesario = insumoReceta.getCantidadUtilizada() * cantidadProducto;

                logger.info("  Insumo requerido: " + nombreInsumo + ", unidad: " + unidadRequerida + ", total necesario: " + totalNecesario);

                List<Insumo> lotes = insumoDAO.findAll().stream()
                        .filter(i -> i.getNombre().equalsIgnoreCase(nombreInsumo))
                        .sorted(Comparator.comparing(Insumo::getFechaCaducidad))
                        .collect(Collectors.toList());

                double restante = totalNecesario;

                for (Insumo lote : lotes) {
                    if (restante <= 0.0001) break;
                    double disponible = lote.convertirUnidad(lote.getCantidad(), lote.getMedida(), unidadRequerida);
                    double usado = Math.min(disponible, restante);
                    logger.info("    Lote: " + lote + ", disponible: " + disponible + ", usado: " + usado);
                    if (usado > 0) {
                        lote.reducirCantidad(usado, unidadRequerida);
                        insumoDAO.update(lote);
                        logger.info("    Se descuenta " + usado + " " + unidadRequerida + " del lote. Restante: " + (restante - usado));
                        restante -= usado;
                    }
                }

                if (restante > 0.0001) {
                    logger.warning("  Faltante detectado para insumo: " + nombreInsumo + ", cantidad: " + restante + " " + unidadRequerida);
                    Long idCatalogo = insumoReceta.getInsumo().getCatalogoInsumo().getId();
                    String clave = idCatalogo + "|" + producto.getId();
                    double cantidadBase = restante;
                    String unidadBase = unidadRequerida;
                    if (unidadBase.equalsIgnoreCase("L")) {
                        cantidadBase = restante * 1000.0;
                        unidadBase = "ML";
                    } else if (unidadBase.equalsIgnoreCase("KG")) {
                        cantidadBase = restante * 1000.0;
                        unidadBase = "GR";
                    }
                    faltantesAcumulados.put(clave, faltantesAcumulados.getOrDefault(clave, 0.0) + cantidadBase);
                    if (!faltanteInfo.containsKey(clave)) {
                        InsumoFaltante faltante = new InsumoFaltante();
                        faltante.setCatalogoInsumo(insumoReceta.getInsumo().getCatalogoInsumo());
                        faltante.setResuelto(false);
                        faltanteInfo.put(clave, faltante);
                        unidadPreferida.put(clave, unidadBase);
                    }
                }
            }
        }

        List<InsumoFaltante> faltantes = new ArrayList<>();
        for (Map.Entry<String, Double> entry : faltantesAcumulados.entrySet()) {
            String clave = entry.getKey();
            double cantidadMLGR = entry.getValue();
            String unidad = unidadPreferida.get(clave);
            InsumoFaltante faltante = faltanteInfo.get(clave);
            logger.info("Procesando faltante: " + faltante.getCatalogoInsumo().getNombre() + ", cantidad: " + cantidadMLGR + " " + unidad);
            // Conversión automática de unidades
            if (unidad.equalsIgnoreCase("ML") && cantidadMLGR >= 1000) {
                faltante.setUnidad("L");
                faltante.setCantidadFaltante(cantidadMLGR / 1000.0);
            } else if (unidad.equalsIgnoreCase("GR") && cantidadMLGR >= 1000) {
                faltante.setUnidad("KG");
                faltante.setCantidadFaltante(cantidadMLGR / 1000.0);
            } else if ((unidad.equalsIgnoreCase("UNIDAD") || unidad.equalsIgnoreCase("UNIDADES"))) {
                if (cantidadMLGR == 1) {
                    faltante.setUnidad("UNIDAD");
                } else {
                    faltante.setUnidad("UNIDADES");
                }
                faltante.setCantidadFaltante(cantidadMLGR);
            } else {
                faltante.setUnidad(unidad);
                faltante.setCantidadFaltante(cantidadMLGR);
            }
            // Buscar si ya existe un faltante pendiente para este insumo (ignorando unidad)
            List<InsumoFaltante> pendientes = faltanteDAO.findPendientesPorInsumo(faltante.getCatalogoInsumo());
            if (!pendientes.isEmpty()) {
                logger.info("  Ya existe faltante pendiente para insumo: " + faltante.getCatalogoInsumo().getNombre() + ". Se suma cantidad.");
                // Siempre sumamos en la unidad base (ML o GR)
                InsumoFaltante existente = pendientes.get(0);
                double existenteBase = existente.getUnidad().equalsIgnoreCase("L") ? existente.getCantidadFaltante() * 1000.0 :
                        existente.getUnidad().equalsIgnoreCase("KG") ? existente.getCantidadFaltante() * 1000.0 :
                                existente.getCantidadFaltante();
                double nuevoBase = faltante.getUnidad().equalsIgnoreCase("L") ? faltante.getCantidadFaltante() * 1000.0 :
                        faltante.getUnidad().equalsIgnoreCase("KG") ? faltante.getCantidadFaltante() * 1000.0 :
                                faltante.getCantidadFaltante();
                double suma = existenteBase + nuevoBase;
                // Si supera 1000, mostrar en L/KG, si no en ML/GR
                if (unidad.equalsIgnoreCase("ML") || unidad.equalsIgnoreCase("L")) {
                    if (suma >= 1000) {
                        existente.setUnidad("L");
                        existente.setCantidadFaltante(suma / 1000.0);
                    } else {
                        existente.setUnidad("ML");
                        existente.setCantidadFaltante(suma);
                    }
                } else if (unidad.equalsIgnoreCase("GR") || unidad.equalsIgnoreCase("KG")) {
                    if (suma >= 1000) {
                        existente.setUnidad("KG");
                        existente.setCantidadFaltante(suma / 1000.0);
                    } else {
                        existente.setUnidad("GR");
                        existente.setCantidadFaltante(suma);
                    }
                } else if (unidad.equalsIgnoreCase("UNIDAD") || unidad.equalsIgnoreCase("UNIDADES")) {
                    if (suma == 1) {
                        existente.setUnidad("UNIDAD");
                    } else {
                        existente.setUnidad("UNIDADES");
                    }
                    existente.setCantidadFaltante(suma);
                } else {
                    existente.setUnidad(unidad);
                    existente.setCantidadFaltante(suma);
                }
                faltanteDAO.update(existente);
                faltantes.add(existente);
            } else {
                logger.info("  Guardando nuevo faltante para insumo: " + faltante.getCatalogoInsumo().getNombre());
                faltanteDAO.save(faltante);
                faltantes.add(faltante);
            }
        }
        logger.info("Procesamiento de recetas finalizado. Faltantes: " + faltantes);
        return faltantes;
    }

    /**
     * Simula el stock para validar si alcanza para todos los productos.
     * @param productos mapa de productos y sus cantidades
     * @return true si alcanza el stock, false si falta alguno
     */
    public boolean validarInsumosSuficientes(Map<Producto, Integer> productos) {
        logger.info("Validando insumos suficientes para productos: " + productos);
        Map<String, Double> stockPorNombre = new HashMap<>();
        for (Insumo insumo : insumoDAO.findAll()) {
            String nombre = insumo.getNombre();
            double acumulado = stockPorNombre.getOrDefault(nombre, 0.0);
            stockPorNombre.put(nombre, acumulado + insumo.getCantidad());
        }
        for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
            Producto producto = entry.getKey();
            int cantidad = entry.getValue();
            if (producto.getReceta() == null) {
                logger.warning("Producto sin receta: " + producto.getNombre());
                continue;
            }
            for (InsumoReceta insumoReceta : producto.getReceta().getInsumosReceta()) {
                String nombre = insumoReceta.getInsumo().getNombre();
                String unidadReceta = insumoReceta.getUnidad();
                double requerido = insumoReceta.getCantidadUtilizada() * cantidad;
                double disponible = 0.0;
                for (Insumo insumo : insumoDAO.findAll()) {
                    if (insumo.getNombre().equalsIgnoreCase(nombre)) {
                        try {
                            disponible += insumo.convertirUnidad(insumo.getCantidad(), insumo.getMedida(), unidadReceta);
                        } catch (Exception e) {
                            logger.warning("No se pudo convertir unidad para insumo: " + nombre + ", lote: " + insumo + ". Error: " + e.getMessage());
                        }
                    }
                }
                logger.info("  Insumo: " + nombre + ", requerido: " + requerido + " " + unidadReceta + ", disponible: " + disponible);
                if (disponible < requerido) {
                    logger.warning("Stock insuficiente para insumo: " + nombre + ". Requerido: " + requerido + ", disponible: " + disponible);
                    return false;
                }
            }
        }
        logger.info("Stock suficiente para todos los productos.");
        return true;
    }

    /**
     * Simula el procesamiento de recetas: calcula insumos faltantes sin descontar stock ni modificar la base.
     * @param productos productos seleccionados con su cantidad
     * @return lista de insumos faltantes simulados
     */
    public List<InsumoFaltante> simularFaltantes(Map<Producto, Integer> productos) {
        Map<String, Double> faltantesAcumulados = new HashMap<>();
        Map<String, InsumoFaltante> faltanteInfo = new HashMap<>();
        Map<String, String> unidadPreferida = new HashMap<>();

        for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
            Producto producto = entry.getKey();
            int cantidadProducto = entry.getValue();
            Receta receta = producto.getReceta();
            if (receta == null) continue;
            for (InsumoReceta insumoReceta : receta.getInsumosReceta()) {
                String nombreInsumo = insumoReceta.getInsumo().getNombre();
                String unidadRequerida = insumoReceta.getUnidad();
                double totalNecesario = insumoReceta.getCantidadUtilizada() * cantidadProducto;
                List<Insumo> lotes = insumoDAO.findAll().stream()
                        .filter(i -> i.getNombre().equalsIgnoreCase(nombreInsumo))
                        .sorted(Comparator.comparing(Insumo::getFechaCaducidad))
                        .collect(Collectors.toList());
                double restante = totalNecesario;
                for (Insumo lote : lotes) {
                    if (restante <= 0.0001) break;
                    double disponible = lote.convertirUnidad(lote.getCantidad(), lote.getMedida(), unidadRequerida);
                    double usado = Math.min(disponible, restante);
                    restante -= usado;
                }
                if (restante > 0.0001) {
                    Long idCatalogo = insumoReceta.getInsumo().getCatalogoInsumo().getId();
                    String clave = idCatalogo + "|" + producto.getId();
                    double cantidadBase = restante;
                    String unidadBase = unidadRequerida;
                    if (unidadBase.equalsIgnoreCase("L")) {
                        cantidadBase = restante * 1000.0;
                        unidadBase = "ML";
                    } else if (unidadBase.equalsIgnoreCase("KG")) {
                        cantidadBase = restante * 1000.0;
                        unidadBase = "GR";
                    }
                    faltantesAcumulados.put(clave, faltantesAcumulados.getOrDefault(clave, 0.0) + cantidadBase);
                    if (!faltanteInfo.containsKey(clave)) {
                        InsumoFaltante faltante = new InsumoFaltante();
                        faltante.setCatalogoInsumo(insumoReceta.getInsumo().getCatalogoInsumo());
                        faltante.setResuelto(false);
                        faltanteInfo.put(clave, faltante);
                        unidadPreferida.put(clave, unidadBase);
                    }
                }
            }
        }
        List<InsumoFaltante> faltantes = new ArrayList<>();
        for (Map.Entry<String, Double> entry : faltantesAcumulados.entrySet()) {
            String clave = entry.getKey();
            double cantidadMLGR = entry.getValue();
            String unidad = unidadPreferida.get(clave);
            InsumoFaltante faltante = faltanteInfo.get(clave);
            // Conversión automática de unidades
            if (unidad.equalsIgnoreCase("ML") && cantidadMLGR >= 1000) {
                faltante.setUnidad("L");
                faltante.setCantidadFaltante(cantidadMLGR / 1000.0);
            } else if (unidad.equalsIgnoreCase("GR") && cantidadMLGR >= 1000) {
                faltante.setUnidad("KG");
                faltante.setCantidadFaltante(cantidadMLGR / 1000.0);
            } else if ((unidad.equalsIgnoreCase("UNIDAD") || unidad.equalsIgnoreCase("UNIDADES"))) {
                if (cantidadMLGR == 1) {
                    faltante.setUnidad("UNIDAD");
                } else {
                    faltante.setUnidad("UNIDADES");
                }
                faltante.setCantidadFaltante(cantidadMLGR);
            } else {
                faltante.setUnidad(unidad);
                faltante.setCantidadFaltante(cantidadMLGR);
            }
            faltantes.add(faltante);
        }
        return faltantes;
    }

    /**
     * Devuelve al stock los insumos utilizados por los productos indicados (por receta y por lote, FEFO).
     * @param productos productos y cantidades a devolver
     */
    public void devolverStockPorProductos(Map<Producto, Integer> productos) {
        logger.info("Devolviendo stock por productos: " + productos);
        for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
            Producto producto = entry.getKey();
            int cantidadADevolver = entry.getValue();
            Receta receta = producto.getReceta();
            if (receta == null) continue;
            for (InsumoReceta insumoReceta : receta.getInsumosReceta()) {
                String nombreInsumo = insumoReceta.getInsumo().getNombre();
                String unidadRequerida = insumoReceta.getUnidad();
                double totalADevolver = insumoReceta.getCantidadUtilizada() * cantidadADevolver;
                // Buscar lotes del insumo, ordenados por fecha de caducidad inversa (LIFO para devolución)
                List<Insumo> lotes = insumoDAO.findAll().stream()
                        .filter(i -> i.getNombre().equalsIgnoreCase(nombreInsumo))
                        .sorted(Comparator.comparing(Insumo::getFechaCaducidad).reversed())
                        .collect(Collectors.toList());
                double restante = totalADevolver;
                for (Insumo lote : lotes) {
                    if (restante <= 0.0001) break;
                    double capacidadLote = lote.convertirUnidad(lote.getCapacidadOriginal(), lote.getMedida(), unidadRequerida);
                    double cantidadActual = lote.convertirUnidad(lote.getCantidad(), lote.getMedida(), unidadRequerida);
                    double maxADevolver = capacidadLote - cantidadActual;
                    if (maxADevolver <= 0) continue; // Lote lleno
                    double aDevolver = Math.min(maxADevolver, restante);
                    lote.aumentarCantidad(aDevolver, unidadRequerida);
                    insumoDAO.update(lote);
                    logger.info("  Se devuelve " + aDevolver + " " + unidadRequerida + " al lote: " + lote);
                    restante -= aDevolver;
                }
                if (restante > 0.0001) {
                    logger.warning("No se pudo devolver toda la cantidad de insumo " + nombreInsumo + ". Restante: " + restante + " " + unidadRequerida);
                }
            }
        }
    }

    /**
     * Devuelve insumos al stock y retorna un resumen por nombre de insumo, cantidad devuelta y unidad (en unidad base).
     * Si la cantidad es entera, no muestra decimales.
     * @param productos productos y cantidades a devolver
     * @return Map<String, String> nombre insumo -> "cantidad unidad"
     */
    public Map<String, String> devolverStockPorProductosConResumen(Map<Producto, Integer> productos) {
        Map<String, Double> resumenCantidad = new LinkedHashMap<>();
        Map<String, String> resumenUnidad = new LinkedHashMap<>();
        logger.info("Devolviendo stock por productos (con resumen): " + productos);
        for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
            Producto producto = entry.getKey();
            int cantidadADevolver = entry.getValue();
            Receta receta = producto.getReceta();
            if (receta == null) continue;
            for (InsumoReceta insumoReceta : receta.getInsumosReceta()) {
                String nombreInsumo = insumoReceta.getInsumo().getNombre();
                String unidadRequerida = insumoReceta.getUnidad();
                double totalADevolver = insumoReceta.getCantidadUtilizada() * cantidadADevolver;
                // Buscar lotes del insumo, ordenados por fecha de caducidad inversa (LIFO para devolución)
                List<Insumo> lotes = insumoDAO.findAll().stream()
                        .filter(i -> i.getNombre().equalsIgnoreCase(nombreInsumo))
                        .sorted(Comparator.comparing(Insumo::getFechaCaducidad).reversed())
                        .collect(Collectors.toList());
                double restante = totalADevolver;
                double devuelto = 0.0;
                for (Insumo lote : lotes) {
                    if (restante <= 0.0001) break;
                    double capacidadLote = lote.convertirUnidad(lote.getCapacidadOriginal(), lote.getMedida(), unidadRequerida);
                    double cantidadActual = lote.convertirUnidad(lote.getCantidad(), lote.getMedida(), unidadRequerida);
                    double maxADevolver = capacidadLote - cantidadActual;
                    if (maxADevolver <= 0) continue; // Lote lleno
                    double aDevolver = Math.min(maxADevolver, restante);
                    lote.aumentarCantidad(aDevolver, unidadRequerida);
                    insumoDAO.update(lote);
                    logger.info("  Se devuelve " + aDevolver + " " + unidadRequerida + " al lote: " + lote);
                    devuelto += aDevolver;
                    restante -= aDevolver;
                }
                // Determinar unidad base para mostrar
                String unidadMostrar = unidadRequerida;
                double cantidadMostrar = devuelto;
                if (unidadRequerida.equalsIgnoreCase("ML") && devuelto >= 1000) {
                    unidadMostrar = "L";
                    cantidadMostrar = devuelto / 1000.0;
                } else if (unidadRequerida.equalsIgnoreCase("GR") && devuelto >= 1000) {
                    unidadMostrar = "KG";
                    cantidadMostrar = devuelto / 1000.0;
                } else if (unidadRequerida.equalsIgnoreCase("UNIDAD") || unidadRequerida.equalsIgnoreCase("UNIDADES")) {
                    unidadMostrar = (devuelto == 1) ? "UNIDAD" : "UNIDADES";
                }
                resumenCantidad.put(nombreInsumo, resumenCantidad.getOrDefault(nombreInsumo, 0.0) + cantidadMostrar);
                resumenUnidad.put(nombreInsumo, unidadMostrar);
                if (restante > 0.0001) {
                    logger.warning("No se pudo devolver toda la cantidad de insumo " + nombreInsumo + ". Restante: " + restante + " " + unidadRequerida);
                }
            }
        }
        // Unir cantidad y unidad para mostrar
        Map<String, String> resumenFinal = new LinkedHashMap<>();
        for (String insumo : resumenCantidad.keySet()) {
            double cantidad = resumenCantidad.get(insumo);
            String unidad = resumenUnidad.get(insumo);
            String cantidadStr = (cantidad == Math.floor(cantidad)) ? String.format("%.0f", cantidad) : String.format(java.util.Locale.ROOT, "%.2f", cantidad);
            resumenFinal.put(insumo, cantidadStr + " " + unidad);
        }
        return resumenFinal;
    }

    /**
     * Intenta resolver automáticamente todos los insumos faltantes pendientes usando el stock actual.
     * Recorre todos los faltantes pendientes y descuenta stock si es posible.
     * Devuelve un resumen de los faltantes resueltos (nombre y cantidad).
     */
    public Map<String, String> resolverFaltantesPorCatalogoInsumo() {
        Map<String, String> resueltos = new LinkedHashMap<>();
        List<InsumoFaltante> faltantes = faltanteDAO.findAllPendientes();
        for (InsumoFaltante faltante : faltantes) {
            CatalogoInsumo catalogo = faltante.getCatalogoInsumo();
            String nombre = catalogo.getNombre();
            String unidad = faltante.getUnidad();
            double cantidad = faltante.getCantidadFaltante();
            // Buscar lotes disponibles de ese insumo
            List<Insumo> lotes = insumoDAO.findAll().stream()
                    .filter(i -> i.getCatalogoInsumo() != null && i.getCatalogoInsumo().getId().equals(catalogo.getId()))
                    .sorted(Comparator.comparing(Insumo::getFechaCaducidad))
                    .collect(Collectors.toList());
            double restante = cantidad;
            for (Insumo lote : lotes) {
                if (restante <= 0.0001) break;
                double disponible = lote.convertirUnidad(lote.getCantidad(), lote.getMedida(), unidad);
                double usado = Math.min(disponible, restante);
                if (usado > 0) {
                    lote.reducirCantidad(usado, unidad);
                    insumoDAO.update(lote);
                    restante -= usado;
                }
            }
            if (restante <= 0.0001) {
                faltante.setResuelto(true);
                faltanteDAO.update(faltante);
                // Formatear cantidad
                String cantidadStr = (cantidad == Math.floor(cantidad)) ? String.format("%.0f", cantidad) : String.format(java.util.Locale.ROOT, "%.2f", cantidad);
                resueltos.put(nombre, cantidadStr + " " + unidad);
            } else {
                // Si no se pudo resolver completamente, actualiza la cantidad faltante
                faltante.setCantidadFaltante(restante);
                faltanteDAO.update(faltante);
            }
        }
        return resueltos;
    }
}
