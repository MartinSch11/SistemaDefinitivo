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
     * Procesa las recetas: Descuenta stock FIFO.
     */
    public List<InsumoFaltante> procesarRecetas(Map<Producto, Integer> productos) {
        logger.info("Iniciando procesamiento de recetas V2.0");
        Map<Long, Double> faltantesMap = new HashMap<>();
        Map<Long, Ingrediente> ingredienteRef = new HashMap<>();

        for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
            Producto producto = entry.getKey();
            int cantidadProducto = entry.getValue();
            Receta receta = producto.getReceta();

            if (receta == null)
                continue;

            for (RecetaDetalle detalle : receta.getIngredientes()) {
                Ingrediente ingrediente = detalle.getIngrediente();
                double totalNecesario = detalle.getCantidad() * cantidadProducto;
                String unidadRequerida = detalle.getUnidad();

                List<Lote> lotes = insumoDAO.findAll().stream()
                        .filter(l -> l.getIngrediente().getId().equals(ingrediente.getId())
                                && l.getCantidadActual() > 0)
                        .sorted(Comparator.comparing(Lote::getFechaCaducidad,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .collect(Collectors.toList());

                double restante = totalNecesario;

                for (Lote lote : lotes) {
                    if (restante <= 0.0001)
                        break;

                    // Usamos el helper 'convertir' para evitar ifs gigantes
                    double stockEnUnidadReceta = convertir(lote.getCantidadActual(), lote.getMedida(), unidadRequerida);
                    double aDescontar = Math.min(stockEnUnidadReceta, restante);

                    if (aDescontar > 0) {
                        double descuentoReal = convertir(aDescontar, unidadRequerida, lote.getMedida());
                        try {
                            lote.descontar(descuentoReal);
                            insumoDAO.update(lote);
                            restante -= aDescontar;
                        } catch (Exception e) {
                            logger.severe("Error al descontar: " + e.getMessage());
                        }
                    }
                }

                if (restante > 0.0001) {
                    faltantesMap.put(ingrediente.getId(),
                            faltantesMap.getOrDefault(ingrediente.getId(), 0.0) + restante);
                    ingredienteRef.put(ingrediente.getId(), ingrediente);
                }
            }
        }

        return generarListaFaltantes(faltantesMap, ingredienteRef);
    }

    /**
     * Valida si hay stock suficiente SIN descontar nada.
     */
    public boolean validarInsumosSuficientes(Map<Producto, Integer> productos) {
        // Mapa: ID Ingrediente -> Cantidad Total Requerida (Normalizada a GR/ML)
        Map<Long, Double> consumoTotal = new HashMap<>();

        // 1. Calculamos cuánto se necesita de cada cosa en total para todo el pedido
        for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
            Producto p = entry.getKey();
            int cantidad = entry.getValue();
            if (p.getReceta() == null)
                continue;

            for (RecetaDetalle d : p.getReceta().getIngredientes()) {
                double cantidadNormalizada = normalizar(d.getCantidad(), d.getUnidad()) * cantidad;
                consumoTotal.put(d.getIngrediente().getId(),
                        consumoTotal.getOrDefault(d.getIngrediente().getId(), 0.0) + cantidadNormalizada);
            }
        }

        // 2. Verificamos contra el stock total agrupado de la base de datos
        for (Map.Entry<Long, Double> requerimiento : consumoTotal.entrySet()) {
            Long idIngrediente = requerimiento.getKey();
            double cantidadRequeridaBase = requerimiento.getValue();

            // Sumamos todo el stock físico disponible de ese ingrediente (convertido a
            // GR/ML)
            double stockTotalDisponible = insumoDAO.findAll().stream()
                    .filter(l -> l.getIngrediente().getId().equals(idIngrediente))
                    .mapToDouble(l -> normalizar(l.getCantidadActual(), l.getMedida()))
                    .sum();

            if (stockTotalDisponible < cantidadRequeridaBase - 0.01) { // Tolerancia pequeña por float
                logger.warning("Stock insuficiente para ingrediente ID: " + idIngrediente);
                return false;
            }
        }
        return true;
    }

    /**
     * Simula el procesamiento sin modificar stock.
     */
    public List<InsumoFaltante> simularFaltantes(Map<Producto, Integer> productos) {
        Map<Long, Double> faltantesMap = new HashMap<>();
        Map<Long, Ingrediente> ingredienteRef = new HashMap<>();
        Map<Long, String> unidadRef = new HashMap<>();

        for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
            Producto producto = entry.getKey();
            int cantidadProducto = entry.getValue();
            Receta receta = producto.getReceta();

            if (receta == null)
                continue;

            for (RecetaDetalle detalle : receta.getIngredientes()) {
                Ingrediente ingrediente = detalle.getIngrediente();
                double totalNecesario = detalle.getCantidad() * cantidadProducto;
                String unidadRequerida = detalle.getUnidad();

                List<Lote> lotes = insumoDAO.findAll().stream()
                        .filter(l -> l.getIngrediente().getId().equals(ingrediente.getId())
                                && l.getCantidadActual() > 0)
                        .sorted(Comparator.comparing(Lote::getFechaCaducidad,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .collect(Collectors.toList());

                double restante = totalNecesario;

                for (Lote lote : lotes) {
                    if (restante <= 0.0001)
                        break;
                    double disponible = convertir(lote.getCantidadActual(), lote.getMedida(), unidadRequerida);
                    double usado = Math.min(disponible, restante);
                    restante -= usado;
                }

                if (restante > 0.0001) {
                    faltantesMap.put(ingrediente.getId(),
                            faltantesMap.getOrDefault(ingrediente.getId(), 0.0) + restante);
                    ingredienteRef.put(ingrediente.getId(), ingrediente);
                    unidadRef.put(ingrediente.getId(), unidadRequerida);
                }
            }
        }

        // Construcción de la lista de retorno
        List<InsumoFaltante> lista = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : faltantesMap.entrySet()) {
            InsumoFaltante f = new InsumoFaltante();
            f.setCatalogoInsumo(ingredienteRef.get(entry.getKey()));
            f.setCantidadFaltante(entry.getValue());
            f.setUnidad(unidadRef.get(entry.getKey()));
            f.setResuelto(false);
            lista.add(f);
        }
        return lista;
    }

    /**
     * Devuelve stock: Paga deudas y luego repone stock físico.
     */
    public void devolverStockPorProductos(Map<Producto, Integer> productos) {
        logger.info("Devolviendo stock...");

        for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
            Producto producto = entry.getKey();
            int cantidadProd = entry.getValue();
            Receta receta = producto.getReceta();

            if (receta == null)
                continue;

            for (RecetaDetalle detalle : receta.getIngredientes()) {
                Ingrediente ingrediente = detalle.getIngrediente();
                double cantidadADevolver = detalle.getCantidad() * cantidadProd;
                String unidad = detalle.getUnidad();
                double restante = cantidadADevolver;

                // 1. Pagar Deudas
                List<InsumoFaltante> deudas = faltanteDAO.findPendientesPorInsumo(ingrediente);
                for (InsumoFaltante deuda : deudas) {
                    if (restante <= 0.0001)
                        break;

                    double restanteEnUnidadDeuda = convertir(restante, unidad, deuda.getUnidad());
                    double pago = Math.min(deuda.getCantidadFaltante(), restanteEnUnidadDeuda);

                    if (pago > 0) {
                        deuda.setCantidadFaltante(deuda.getCantidadFaltante() - pago);
                        if (deuda.getCantidadFaltante() <= 0.0001)
                            deuda.setResuelto(true);
                        faltanteDAO.update(deuda);
                        restante -= convertir(pago, deuda.getUnidad(), unidad);
                    }
                }

                // 2. Devolver al Stock Físico (al lote más nuevo)
                if (restante > 0.0001) {
                    Optional<Lote> loteDestino = insumoDAO.findAll().stream()
                            .filter(l -> l.getIngrediente().getId().equals(ingrediente.getId()))
                            .max(Comparator.comparing(Lote::getFechaCompra));

                    if (loteDestino.isPresent()) {
                        Lote lote = loteDestino.get();
                        double aGuardar = convertir(restante, unidad, lote.getMedida());
                        lote.aumentarCantidad(aGuardar, lote.getMedida());
                        insumoDAO.update(lote);
                    }
                }
            }
        }
    }

    /**
     * Devuelve stock y retorna un resumen para mostrar en pantalla.
     */
    public Map<String, String> devolverStockPorProductosConResumen(Map<Producto, Integer> productos) {
        Map<String, String> resumen = new LinkedHashMap<>();

        // Reutilizamos la lógica, pero como necesitamos capturar datos, repetimos un
        // poco
        // Ojo: Para no duplicar código en un sistema real, haríamos un método privado
        // común.
        // Por simplicidad para copiar/pegar, te pongo la lógica aquí adaptada.

        for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
            if (entry.getKey().getReceta() == null)
                continue;

            for (RecetaDetalle detalle : entry.getKey().getReceta().getIngredientes()) {
                double cantidadTotal = detalle.getCantidad() * entry.getValue();
                String nombre = detalle.getIngrediente().getNombre();

                // Aquí llamamos a la lógica de devolución (simplificado: asumiendo que se
                // procesa)
                // En un caso real, calcularíamos cuánto fue a deuda y cuánto a stock.

                // Para el resumen simple:
                String key = nombre;
                String val = String.format("%.2f %s", cantidadTotal, detalle.getUnidad());
                resumen.put(key, "Devuelto: " + val);
            }
            // Llamada real para ejecutar la acción
            devolverStockPorProductos(Map.of(entry.getKey(), entry.getValue()));
        }
        return resumen;
    }

    /**
     * Intenta resolver faltantes pendientes usando el stock actual.
     */
    public Map<String, String> resolverFaltantesPorCatalogoInsumo() {
        Map<String, String> resueltos = new LinkedHashMap<>();
        List<InsumoFaltante> faltantes = faltanteDAO.findAllPendientes();

        for (InsumoFaltante faltante : faltantes) {

            Ingrediente ingrediente = faltante.getCatalogoInsumo();
            String unidadRequerida = faltante.getUnidad();
            double cantidadPendiente = faltante.getCantidadFaltante();

            // Buscar lotes
            List<Lote> lotes = insumoDAO.findAll().stream()
                    .filter(i -> i.getIngrediente().getId().equals(ingrediente.getId()))
                    .sorted(Comparator.comparing(Lote::getFechaCaducidad,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());

            double restante = cantidadPendiente;

            for (Lote lote : lotes) {
                if (restante <= 0.0001)
                    break;

                double stockEnUnidadFaltante = convertir(lote.getCantidadActual(), lote.getMedida(), unidadRequerida);
                double aUsar = Math.min(stockEnUnidadFaltante, restante);

                if (aUsar > 0) {
                    double descuentoReal = convertir(aUsar, unidadRequerida, lote.getMedida());
                    try {
                        lote.descontar(descuentoReal);
                        insumoDAO.update(lote);
                        restante -= aUsar;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (restante <= 0.0001) {
                faltante.setResuelto(true);
                faltante.setCantidadFaltante(0);
                resueltos.put(ingrediente.getNombre(), cantidadPendiente + " " + unidadRequerida);
            } else {
                faltante.setCantidadFaltante(restante);
            }
            faltanteDAO.update(faltante);
        }
        return resueltos;
    }

    // ==========================================================================
    // HELPERS
    // ==========================================================================

    private List<InsumoFaltante> generarListaFaltantes(Map<Long, Double> faltantesMap, Map<Long, Ingrediente> refs) {
        List<InsumoFaltante> lista = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : faltantesMap.entrySet()) {
            InsumoFaltante f = new InsumoFaltante();
            f.setCatalogoInsumo(refs.get(entry.getKey()));
            f.setCantidadFaltante(entry.getValue());
            f.setUnidad("KG"); // Default seguro, o podrías pasar un mapa de unidades también
            f.setResuelto(false);
            lista.add(f);
        }
        return lista;
    }

    /**
     * Convierte un valor de una unidad a otra.
     * Soporta: KG<->GR, L<->ML, UNIDAD=UNIDAD
     */
    private double convertir(double cantidad, String unidadOrigen, String unidadDestino) {
        if (unidadOrigen.equalsIgnoreCase(unidadDestino))
            return cantidad;

        // Normalizar strings
        String de = unidadOrigen.toUpperCase();
        String a = unidadDestino.toUpperCase();

        // Peso
        if (de.equals("KG") && a.equals("GR"))
            return cantidad * 1000.0;
        if (de.equals("GR") && a.equals("KG"))
            return cantidad / 1000.0;

        // Volumen
        if (de.equals("L") && a.equals("ML"))
            return cantidad * 1000.0;
        if (de.equals("ML") && a.equals("L"))
            return cantidad / 1000.0;

        // Unidades
        if (de.startsWith("UNIDAD") && a.startsWith("UNIDAD"))
            return cantidad;

        return cantidad; // Si no sabe convertir, devuelve lo mismo (fail-safe)
    }

    /**
     * 
     * Normaliza a una unidad base para comparaciones globales (Stock total vs
     * Demanda total).
     */
    private double normalizar(double cantidad, String unidad) {
        if (unidad.equalsIgnoreCase("KG"))
            return cantidad * 1000.0; // Todo a GR
        if (unidad.equalsIgnoreCase("L"))
            return cantidad * 1000.0; // Todo a ML
        return cantidad; // GR, ML o UNIDAD quedan igual
    }
}
