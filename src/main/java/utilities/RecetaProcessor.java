package utilities;

import model.*;
import persistence.dao.InsumoDAO;
import persistence.dao.InsumoFaltanteDAO;
import java.util.*;
import java.util.stream.Collectors;

public class RecetaProcessor {

    private final InsumoDAO insumoDAO = new InsumoDAO();
    private final InsumoFaltanteDAO faltanteDAO = new InsumoFaltanteDAO();

    /**
     * Procesa las recetas de productos, descuenta stock y guarda insumos faltantes si no alcanza.
     * @param productos productos seleccionados con su cantidad
     * @return lista de insumos faltantes
     */
    public List<InsumoFaltante> procesarRecetas(Map<Producto, Integer> productos) {
        // Nuevo mapa: clave = idInsumo|idProducto, valor = cantidad en ML/GR (si corresponde) y unidad base
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
                    if (usado > 0) {
                        lote.reducirCantidad(usado, unidadRequerida);
                        insumoDAO.update(lote);
                        restante -= usado;
                    }
                }

                if (restante > 0.0001) {
                    // Clave única por insumo y producto (sin unidad)
                    String clave = insumoReceta.getInsumo().getId() + "|" + producto.getId();
                    double cantidadBase = restante;
                    String unidadBase = unidadRequerida;
                    // Si la unidad es L/KG, convertir a ML/GR para sumar correctamente
                    if (unidadBase.equalsIgnoreCase("L")) {
                        cantidadBase = restante * 1000.0;
                        unidadBase = "ML";
                    } else if (unidadBase.equalsIgnoreCase("KG")) {
                        cantidadBase = restante * 1000.0;
                        unidadBase = "GR";
                    }
                    faltantesAcumulados.put(clave, faltantesAcumulados.getOrDefault(clave, 0.0) + cantidadBase);
                    // Guardar info para crear el objeto InsumoFaltante luego
                    if (!faltanteInfo.containsKey(clave)) {
                        InsumoFaltante faltante = new InsumoFaltante();
                        faltante.setInsumo(insumoReceta.getInsumo());
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
            } else {
                faltante.setUnidad(unidad);
                faltante.setCantidadFaltante(cantidadMLGR);
            }
            // Buscar si ya existe un faltante pendiente para este insumo (ignorando unidad)
            List<InsumoFaltante> pendientes = faltanteDAO.findPendientesPorInsumo(faltante.getInsumo());
            if (!pendientes.isEmpty()) {
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
                } else {
                    existente.setUnidad(unidad);
                    existente.setCantidadFaltante(suma);
                }
                faltanteDAO.update(existente);
                faltantes.add(existente);
            } else {
                faltanteDAO.save(faltante);
                faltantes.add(faltante);
            }
        }
        return faltantes;
    }

    /**
     * Simula el stock para validar si alcanza para todos los productos.
     * @param productos mapa de productos y sus cantidades
     * @return true si alcanza el stock, false si falta alguno
     */
    public boolean validarInsumosSuficientes(Map<Producto, Integer> productos) {
        Map<String, Double> stockPorNombre = new HashMap<>();

        // Acumula el stock de cada insumo en la unidad de la receta (no siempre GR)
        for (Insumo insumo : insumoDAO.findAll()) {
            String nombre = insumo.getNombre();
            double acumulado = stockPorNombre.getOrDefault(nombre, 0.0);
            // Buscamos la unidad que se usará en la receta para este insumo
            // Por defecto, usamos la unidad del insumo
            stockPorNombre.put(nombre, acumulado + insumo.getCantidad());
        }

        for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
            Producto producto = entry.getKey();
            int cantidad = entry.getValue();

            if (producto.getReceta() == null) continue;

            for (InsumoReceta insumoReceta : producto.getReceta().getInsumosReceta()) {
                String nombre = insumoReceta.getInsumo().getNombre();
                String unidadReceta = insumoReceta.getUnidad();
                double requerido = insumoReceta.getCantidadUtilizada() * cantidad;
                double disponible = 0.0;

                // Sumar el stock total disponible en la unidad de la receta
                for (Insumo insumo : insumoDAO.findAll()) {
                    if (insumo.getNombre().equalsIgnoreCase(nombre)) {
                        try {
                            disponible += insumo.convertirUnidad(insumo.getCantidad(), insumo.getMedida(), unidadReceta);
                        } catch (Exception e) {
                            // Si no se puede convertir, se ignora ese lote
                        }
                    }
                }

                if (disponible < requerido) {
                    return false;
                }
            }
        }

        return true;
    }
}
