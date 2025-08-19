package service;

import lombok.Getter;
import model.*;
import persistence.dao.ClienteDAO;
import persistence.dao.PedidoDAO;
import persistence.dao.TrabajadorDAO;
import utilities.RecetaProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PedidoService {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    private final RecetaProcessor recetaProcessor = new RecetaProcessor();

    public PedidoConFaltantes crearPedido(String dniCliente, String nombreEmpleado, String formaEntrega,
            LocalDate fechaEntrega, Map<Producto, Integer> productosSeleccionados) throws Exception {

        if (dniCliente == null || dniCliente.isEmpty())
            throw new Exception("El DNI del cliente es obligatorio.");
        if (nombreEmpleado == null || nombreEmpleado.isEmpty())
            throw new Exception("Debe seleccionar un empleado.");
        if (formaEntrega == null || formaEntrega.isEmpty())
            throw new Exception("Debe seleccionar una forma de entrega.");
        if (fechaEntrega == null || fechaEntrega.isBefore(LocalDate.now()))
            throw new Exception("La fecha de entrega no es válida.");
        if (productosSeleccionados == null || productosSeleccionados.isEmpty())
            throw new Exception("Debe seleccionar al menos un producto.");

        Cliente cliente = clienteDAO.findByDni(dniCliente);
        if (cliente == null)
            throw new Exception("Cliente no encontrado.");

        Trabajador empleado = trabajadorDAO.findByNombre(nombreEmpleado);
        if (empleado == null)
            throw new Exception("Empleado no encontrado.");

        System.out.println("[DEBUG PedidoService] crearPedido productosSeleccionados:");
        productosSeleccionados.forEach(
                (p, c) -> System.out.println("  - " + p.getId() + " | " + p.getNombre() + " | cantidad: " + c));
        // Procesar recetas y obtener faltantes (esto descuenta stock SOLO aquí)
        List<InsumoFaltante> faltantes = recetaProcessor.simularFaltantes(productosSeleccionados);
        // 💬 Mensaje para mostrar al usuario si hay insumos faltantes
        String mensajeFaltantes = "";
        if (!faltantes.isEmpty()) {
            // Agrupar por insumo y sumar cantidades
            Map<String, Double> faltantesAgrupados = new LinkedHashMap<>();
            Map<String, String> unidadesAgrupadas = new HashMap<>();
            for (InsumoFaltante f : faltantes) {
                String nombre = f.getCatalogoInsumo().getNombre();
                double cantidad = f.getCantidadFaltante();
                faltantesAgrupados.put(nombre, faltantesAgrupados.getOrDefault(nombre, 0.0) + cantidad);
                unidadesAgrupadas.put(nombre, f.getUnidad());
            }
            mensajeFaltantes = "⚠️ Pedido creado, pero hay insumos faltantes:\n"
                    + faltantesAgrupados.entrySet().stream().map(e -> {
                        double cantidad = e.getValue();
                        String unidad = unidadesAgrupadas.get(e.getKey());
                        String cantidadStr;
                        String unidadStr = unidad;
                        if (unidad != null && unidad.equalsIgnoreCase("GR") && cantidad >= 1000) {
                            cantidadStr = String.format(Locale.ROOT, "%.2f", cantidad / 1000);
                            unidadStr = "KG";
                        } else if (unidad != null && unidad.equalsIgnoreCase("ML") && cantidad >= 1000) {
                            cantidadStr = String.format(Locale.ROOT, "%.2f", cantidad / 1000);
                            unidadStr = "L";
                        } else if (unidad != null && unidad.equalsIgnoreCase("UNIDAD") && cantidad > 1) {
                            cantidadStr = String.format(Locale.ROOT, "%.0f", cantidad);
                            unidadStr = "UNIDADES";
                        } else {
                            cantidadStr = (cantidad == Math.floor(cantidad))
                                    ? String.format(Locale.ROOT, "%.0f", cantidad)
                                    : String.format(Locale.ROOT, "%.2f", cantidad);
                        }
                        return "- " + e.getKey() + ": faltan " + cantidadStr + " " + unidadStr;
                    }).collect(java.util.stream.Collectors.joining("\n"));
        }

        BigDecimal totalPedido = calcularTotalPedido(productosSeleccionados, null);

        Pedido pedido = new Pedido(null, cliente, empleado, formaEntrega, fechaEntrega, "Sin empezar", "", totalPedido);

        // pedidoDAO.save(pedido); // Eliminado: solo se debe guardar después de setear
        // los productos

        List<PedidoProducto> pedidoProductos = new ArrayList<>();
        for (Map.Entry<Producto, Integer> entry : productosSeleccionados.entrySet()) {
            Producto producto = entry.getKey();
            int cantidad = entry.getValue();

            PedidoProducto pedidoProducto = new PedidoProducto(pedido, producto, cantidad);
            pedidoProductos.add(pedidoProducto);
        }

        pedido.setPedidoProductos(pedidoProductos);

        pedidoDAO.save(pedido); // Persistimos productos vía cascade

        return new PedidoConFaltantes(pedido, mensajeFaltantes);
    }

    // Unifica el cálculo del total para ambos métodos, siempre suma productos y
    // combos
    private BigDecimal calcularTotalPedido(Map<Producto, Integer> productos, Map<Combo, Integer> combos) {
        BigDecimal total = BigDecimal.ZERO;
        if (productos != null) {
            for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
                if (entry.getKey() != null && entry.getKey().getPrecio() != null) {
                    total = total.add(entry.getKey().getPrecio().multiply(BigDecimal.valueOf(entry.getValue())));
                }
            }
        }
        if (combos != null) {
            for (Map.Entry<Combo, Integer> entry : combos.entrySet()) {
                if (entry.getKey() != null && entry.getKey().getPrecio() != null) {
                    total = total.add(entry.getKey().getPrecio().multiply(BigDecimal.valueOf(entry.getValue())));
                }
            }
        }
        return total;
    }

    public PedidoConFaltantes actualizarPedido(Pedido pedidoOriginal, String dniCliente, String nombreEmpleado,
            String formaEntrega, LocalDate fechaEntrega, Map<Producto, Integer> productosSeleccionados)
            throws Exception {
        if (pedidoOriginal == null || pedidoOriginal.getNumeroPedido() == null)
            throw new Exception("El pedido a actualizar no es válido.");
        if (dniCliente == null || dniCliente.isEmpty())
            throw new Exception("El DNI del cliente es obligatorio.");
        if (nombreEmpleado == null || nombreEmpleado.isEmpty())
            throw new Exception("Debe seleccionar un empleado.");
        if (formaEntrega == null || formaEntrega.isEmpty())
            throw new Exception("Debe seleccionar una forma de entrega.");
        if (fechaEntrega == null || fechaEntrega.isBefore(LocalDate.now()))
            throw new Exception("La fecha de entrega no es válida.");
        if (productosSeleccionados == null || productosSeleccionados.isEmpty())
            throw new Exception("Debe seleccionar al menos un producto.");

        Pedido managed = pedidoDAO.findByNumeroPedido(pedidoOriginal.getNumeroPedido());

        // Reconstruimos los productos originales del pedido actual
        Map<Producto, Integer> originales = managed.getPedidoProductos().stream()
                .collect(Collectors.toMap(
                        PedidoProducto::getProducto,
                        PedidoProducto::getCantidad,
                        Integer::sum));

        // Si no se modificó nada, evitamos el guardado innecesario
        boolean mismoCliente = managed.getCliente().getDni().equals(dniCliente);
        boolean mismoEmp = managed.getEmpleadoAsignado().getNombre().equals(nombreEmpleado);
        boolean mismaForma = managed.getFormaEntrega().equals(formaEntrega);
        boolean mismaFecha = managed.getFechaEntrega().equals(fechaEntrega);
        boolean mismosProds = originales.equals(productosSeleccionados);

        if (mismoCliente && mismoEmp && mismaForma && mismaFecha && mismosProds) {
            return new PedidoConFaltantes(managed, "", "");
        }

        Cliente cliente = clienteDAO.findByDni(dniCliente);
        if (cliente == null)
            throw new Exception("Cliente no encontrado.");
        Trabajador empleado = trabajadorDAO.findByNombre(nombreEmpleado);
        if (empleado == null)
            throw new Exception("Empleado no encontrado.");

        // Devolver al stock productos eliminados o reducidos
        Map<Producto, Integer> productosParaDevolver = new HashMap<>();
        for (PedidoProducto pp : managed.getPedidoProductos()) {
            int antigua = pp.getCantidad();
            int nueva = productosSeleccionados.getOrDefault(pp.getProducto(), 0);
            if (antigua > nueva) {
                productosParaDevolver.put(pp.getProducto(), antigua - nueva);
            }
        }

        String mensajeDevolucion = "";
        if (!productosParaDevolver.isEmpty()) {
            Map<String, String> resumen = recetaProcessor.devolverStockPorProductosConResumen(productosParaDevolver);
            mensajeDevolucion = "Insumos devueltos al stock por modificación del pedido:\n" +
                    resumen.entrySet().stream()
                            .map(e -> {
                                String valor = e.getValue();
                                try {
                                    double cantidad = Double
                                            .parseDouble(valor.replaceAll("[^0-9.,]", "").replace(",", "."));
                                    String cantidadStr = (cantidad == Math.floor(cantidad))
                                            ? String.format(Locale.ROOT, "%.0f", cantidad)
                                            : String.format(Locale.ROOT, "%.2f", cantidad);
                                    return "- " + e.getKey() + ": " +
                                            valor.replaceFirst("[0-9]+([.,][0-9]+)?", cantidadStr);
                                } catch (Exception ex) {
                                    return "- " + e.getKey() + ": " + valor;
                                }
                            })
                            .collect(Collectors.joining("\n"));
        }

        // Preparar faltantes: diferencias de productos o nuevos
        Map<Producto, Integer> paraDescontar = new HashMap<>();
        for (PedidoProducto pp : managed.getPedidoProductos()) {
            int antigua = pp.getCantidad();
            int nueva = productosSeleccionados.getOrDefault(pp.getProducto(), 0);
            if (nueva > antigua) {
                paraDescontar.put(pp.getProducto(), nueva - antigua);
            }
        }
        for (Map.Entry<Producto, Integer> entry : productosSeleccionados.entrySet()) {
            if (!originales.containsKey(entry.getKey())) {
                paraDescontar.put(entry.getKey(), entry.getValue());
            }
        }

        List<InsumoFaltante> faltantes = recetaProcessor.simularFaltantes(paraDescontar);
        String mensajeFaltantes = "";
        if (!faltantes.isEmpty()) {
            mensajeFaltantes = "⚠️ Pedido actualizado, pero hay insumos faltantes:\n" +
                    faltantes.stream()
                            .collect(Collectors.groupingBy(f -> f.getCatalogoInsumo().getNombre()))
                            .entrySet().stream()
                            .map(e -> {
                                double sum = e.getValue().stream().mapToDouble(InsumoFaltante::getCantidadFaltante)
                                        .sum();
                                String unidad = e.getValue().get(0).getUnidad();
                                String cantidadStr = (sum == Math.floor(sum))
                                        ? String.format(Locale.ROOT, "%.0f", sum)
                                        : String.format(Locale.ROOT, "%.2f", sum);
                                return "- " + e.getKey() + ": faltan " + cantidadStr + " " + unidad;
                            })
                            .collect(Collectors.joining("\n"));
        }

        // Actualizar datos del pedido
        managed.setCliente(cliente);
        managed.setEmpleadoAsignado(empleado);
        managed.setFormaEntrega(formaEntrega);
        managed.setFechaEntrega(fechaEntrega);

        // Reconstruir lista de productos del pedido
        managed.getPedidoProductos().removeIf(pp -> productosSeleccionados.getOrDefault(pp.getProducto(), 0) == 0);
        for (PedidoProducto pp : managed.getPedidoProductos()) {
            pp.setCantidad(productosSeleccionados.get(pp.getProducto()));
        }

        Set<Long> existentes = managed.getPedidoProductos().stream()
                .map(pp -> pp.getProducto().getId()).collect(Collectors.toSet());
        for (Map.Entry<Producto, Integer> entry : productosSeleccionados.entrySet()) {
            if (entry.getValue() > 0 && !existentes.contains(entry.getKey().getId())) {
                managed.getPedidoProductos().add(new PedidoProducto(managed, entry.getKey(), entry.getValue()));
            }
        }

        // Actualizar total y guardar
        managed.setTotalPedido(calcularTotalPedido(productosSeleccionados, null));
        pedidoDAO.update(managed);

        return new PedidoConFaltantes(managed, mensajeFaltantes, mensajeDevolucion);
    }

    // --- NUEVO: Crear pedido con productos y combos ---
    public PedidoConFaltantes crearPedido(String dniCliente, String nombreEmpleado, String formaEntrega,
            LocalDate fechaEntrega, Map<Producto, Integer> productosSeleccionados,
            Map<Combo, Integer> combosSeleccionados) throws Exception {
        // Si no hay combos, usar el método original
        if (combosSeleccionados == null || combosSeleccionados.isEmpty()) {
            return crearPedido(dniCliente, nombreEmpleado, formaEntrega, fechaEntrega, productosSeleccionados);
        }
        // Validaciones básicas (igual que el método original)
        if (dniCliente == null || dniCliente.isEmpty())
            throw new Exception("El DNI del cliente es obligatorio.");
        if (nombreEmpleado == null || nombreEmpleado.isEmpty())
            throw new Exception("Debe seleccionar un empleado.");
        if (formaEntrega == null || formaEntrega.isEmpty())
            throw new Exception("Debe seleccionar una forma de entrega.");
        if (fechaEntrega == null || fechaEntrega.isBefore(LocalDate.now()))
            throw new Exception("La fecha de entrega no es válida.");
        if ((productosSeleccionados == null || productosSeleccionados.isEmpty())
                && (combosSeleccionados == null || combosSeleccionados.isEmpty()))
            throw new Exception("Debe seleccionar al menos un producto o combo.");

        Cliente cliente = clienteDAO.findByDni(dniCliente);
        if (cliente == null)
            throw new Exception("Cliente no encontrado.");
        Trabajador empleado = trabajadorDAO.findByNombre(nombreEmpleado);
        if (empleado == null)
            throw new Exception("Empleado no encontrado.");

        // --- Desglosar combos en productos para el descuento de stock ---
        Map<Producto, Integer> productosTotales = new HashMap<>();
        if (productosSeleccionados != null) {
            productosTotales.putAll(productosSeleccionados);
        }
        if (combosSeleccionados != null) {
            for (Map.Entry<Combo, Integer> entry : combosSeleccionados.entrySet()) {
                Combo combo = entry.getKey();
                int cantidadCombo = entry.getValue();
                for (ComboProducto cp : combo.getProductos()) {
                    productosTotales.merge(cp.getProducto(), cp.getCantidad() * cantidadCombo, Integer::sum);
                }
            }
        }
        // Procesar recetas y obtener faltantes (NO descontar stock aquí)
        List<InsumoFaltante> faltantes = recetaProcessor.simularFaltantes(productosTotales); // <-- CORREGIDO: usar
                                                                                             // productosTotales
        String mensajeFaltantes = "";
        if (!faltantes.isEmpty()) {
            // Agrupar por insumo y sumar cantidades
            Map<String, Double> faltantesAgrupados = new LinkedHashMap<>();
            Map<String, String> unidadesAgrupadas = new HashMap<>();
            for (InsumoFaltante f : faltantes) {
                String nombre = f.getCatalogoInsumo().getNombre();
                double cantidad = f.getCantidadFaltante();
                faltantesAgrupados.put(nombre, faltantesAgrupados.getOrDefault(nombre, 0.0) + cantidad);
                unidadesAgrupadas.put(nombre, f.getUnidad());
            }
            mensajeFaltantes = "⚠️ Pedido creado, pero hay insumos faltantes:\n"
                    + faltantesAgrupados.entrySet().stream().map(e -> {
                        double cantidad = e.getValue();
                        String unidad = unidadesAgrupadas.get(e.getKey());
                        String cantidadStr;
                        String unidadStr = unidad;
                        if (unidad != null && unidad.equalsIgnoreCase("GR") && cantidad >= 1000) {
                            cantidadStr = String.format(Locale.ROOT, "%.2f", cantidad / 1000);
                            unidadStr = "KG";
                        } else if (unidad != null && unidad.equalsIgnoreCase("ML") && cantidad >= 1000) {
                            cantidadStr = String.format(Locale.ROOT, "%.2f", cantidad / 1000);
                            unidadStr = "L";
                        } else if (unidad != null && unidad.equalsIgnoreCase("UNIDAD") && cantidad > 1) {
                            cantidadStr = String.format(Locale.ROOT, "%.0f", cantidad);
                            unidadStr = "UNIDADES";
                        } else {
                            cantidadStr = (cantidad == Math.floor(cantidad))
                                    ? String.format(Locale.ROOT, "%.0f", cantidad)
                                    : String.format(Locale.ROOT, "%.2f", cantidad);
                        }
                        return "- " + e.getKey() + ": faltan " + cantidadStr + " " + unidadStr;
                    }).collect(java.util.stream.Collectors.joining("\n"));
        }
        // Calcular total sumando productos y combos
        java.math.BigDecimal totalPedido = java.math.BigDecimal.ZERO;
        if (productosSeleccionados != null) {
            totalPedido = totalPedido.add(calcularTotalPedido(productosSeleccionados, null));
        }
        if (combosSeleccionados != null) {
            for (Map.Entry<Combo, Integer> entry : combosSeleccionados.entrySet()) {
                totalPedido = totalPedido
                        .add(entry.getKey().getPrecio().multiply(java.math.BigDecimal.valueOf(entry.getValue())));
            }
        }
        Pedido pedido = new Pedido(null, cliente, empleado, formaEntrega, fechaEntrega, "Sin empezar", "", totalPedido);
        // --- Asociar productos ---
        java.util.List<PedidoProducto> pedidoProductos = new java.util.ArrayList<>();
        if (productosSeleccionados != null) {
            for (Map.Entry<Producto, Integer> entry : productosSeleccionados.entrySet()) {
                PedidoProducto pedidoProducto = new PedidoProducto(pedido, entry.getKey(), entry.getValue());
                pedidoProductos.add(pedidoProducto);
            }
        }
        pedido.setPedidoProductos(pedidoProductos);
        // --- Asociar combos ---
        java.util.List<PedidoCombo> pedidoCombos = new java.util.ArrayList<>();
        if (combosSeleccionados != null) {
            for (Map.Entry<Combo, Integer> entry : combosSeleccionados.entrySet()) {
                PedidoCombo pedidoCombo = new PedidoCombo(pedido, entry.getKey(), entry.getValue());
                pedidoCombos.add(pedidoCombo);
            }
        }
        pedido.setPedidoCombos(pedidoCombos);
        pedidoDAO.save(pedido); // Persistimos productos y combos vía cascade
        return new PedidoConFaltantes(pedido, mensajeFaltantes);
    }

    // --- NUEVO: Actualizar pedido con productos y combos ---
    public PedidoConFaltantes actualizarPedido(
            Pedido pedidoOriginal,
            String dniCliente,
            String nombreEmpleado,
            String formaEntrega,
            LocalDate fechaEntrega,
            Map<Producto, Integer> productosSeleccionados,
            Map<Combo, Integer> combosSeleccionados) throws Exception {
        if (pedidoOriginal == null || pedidoOriginal.getNumeroPedido() == null)
            throw new Exception("El pedido a actualizar no es válido.");
        if (dniCliente == null || dniCliente.isEmpty())
            throw new Exception("El DNI del cliente es obligatorio.");
        if (nombreEmpleado == null || nombreEmpleado.isEmpty())
            throw new Exception("Debe seleccionar un empleado.");
        if (formaEntrega == null || formaEntrega.isEmpty())
            throw new Exception("Debe seleccionar una forma de entrega.");
        if (fechaEntrega == null || fechaEntrega.isBefore(LocalDate.now()))
            throw new Exception("La fecha de entrega no es válida.");
        if ((productosSeleccionados == null || productosSeleccionados.isEmpty())
                && (combosSeleccionados == null || combosSeleccionados.isEmpty()))
            throw new Exception("Debe seleccionar al menos un producto o combo.");

        Pedido managed = pedidoDAO.findByNumeroPedido(pedidoOriginal.getNumeroPedido());

        // --- Reconstruir productos y combos originales
        Map<Producto, Integer> productosOriginales = managed.getPedidoProductos().stream()
                .collect(Collectors.toMap(PedidoProducto::getProducto, PedidoProducto::getCantidad, Integer::sum));

        Map<Combo, Integer> combosOriginales = managed.getPedidoCombos().stream()
                .collect(Collectors.toMap(PedidoCombo::getCombo, PedidoCombo::getCantidad, Integer::sum));

        boolean mismoCliente = managed.getCliente().getDni().equals(dniCliente);
        boolean mismoEmp = managed.getEmpleadoAsignado().getNombre().equals(nombreEmpleado);
        boolean mismaForma = managed.getFormaEntrega().equals(formaEntrega);
        boolean mismaFecha = managed.getFechaEntrega().equals(fechaEntrega);
        boolean mismosProds = productosOriginales.equals(productosSeleccionados);
        boolean mismosCombos = combosOriginales.equals(combosSeleccionados);

        if (mismoCliente && mismoEmp && mismaForma && mismaFecha && mismosProds && mismosCombos) {
            return new PedidoConFaltantes(managed, "", "");
        }

        Cliente cliente = clienteDAO.findByDni(dniCliente);
        if (cliente == null)
            throw new Exception("Cliente no encontrado.");

        Trabajador empleado = trabajadorDAO.findByNombre(nombreEmpleado);
        if (empleado == null)
            throw new Exception("Empleado no encontrado.");

        // --- Devolver insumos al stock (productos y combos)
        Map<Producto, Integer> productosParaDevolver = new HashMap<>();

        for (PedidoProducto pp : managed.getPedidoProductos()) {
            Producto p = pp.getProducto();
            int diff = pp.getCantidad() - productosSeleccionados.getOrDefault(p, 0);
            if (diff > 0)
                productosParaDevolver.put(p, diff);
        }

        for (PedidoCombo pc : managed.getPedidoCombos()) {
            Combo combo = pc.getCombo();
            int diff = pc.getCantidad() - combosSeleccionados.getOrDefault(combo, 0);
            if (diff > 0) {
                for (ComboProducto cp : combo.getProductos()) {
                    productosParaDevolver.merge(cp.getProducto(), cp.getCantidad() * diff, Integer::sum);
                }
            }
        }

        String mensajeDevolucion = "";
        if (!productosParaDevolver.isEmpty()) {
            Map<String, String> resumen = recetaProcessor.devolverStockPorProductosConResumen(productosParaDevolver);
            mensajeDevolucion = "Insumos devueltos:\n" + resumen.entrySet().stream()
                    .map(e -> "- " + e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining("\n"));
        }

        // --- Faltantes: preparar solo diferencia a descontar
        Map<Producto, Integer> productosParaDescontar = new HashMap<>();

        for (PedidoProducto pp : managed.getPedidoProductos()) {
            Producto p = pp.getProducto();
            int nueva = productosSeleccionados.getOrDefault(p, 0);
            int diff = nueva - pp.getCantidad();
            if (diff > 0)
                productosParaDescontar.put(p, diff);
        }

        for (Map.Entry<Producto, Integer> entry : productosSeleccionados.entrySet()) {
            if (!productosOriginales.containsKey(entry.getKey())) {
                productosParaDescontar.put(entry.getKey(), entry.getValue());
            }
        }

        for (PedidoCombo pc : managed.getPedidoCombos()) {
            Combo combo = pc.getCombo();
            int nueva = combosSeleccionados.getOrDefault(combo, 0);
            int diff = nueva - pc.getCantidad();
            if (diff > 0) {
                for (ComboProducto cp : combo.getProductos()) {
                    productosParaDescontar.merge(cp.getProducto(), cp.getCantidad() * diff, Integer::sum);
                }
            }
        }

        for (Map.Entry<Combo, Integer> entry : combosSeleccionados.entrySet()) {
            if (!combosOriginales.containsKey(entry.getKey())) {
                for (ComboProducto cp : entry.getKey().getProductos()) {
                    productosParaDescontar.merge(cp.getProducto(), cp.getCantidad() * entry.getValue(), Integer::sum);
                }
            }
        }

        List<InsumoFaltante> faltantes = recetaProcessor.simularFaltantes(productosParaDescontar);
        String mensajeFaltantes = "";
        if (!faltantes.isEmpty()) {
            mensajeFaltantes = faltantes.stream()
                    .collect(Collectors.groupingBy(f -> f.getCatalogoInsumo().getNombre()))
                    .entrySet().stream()
                    .map(e -> {
                        double sum = e.getValue().stream().mapToDouble(InsumoFaltante::getCantidadFaltante).sum();
                        String unidad = e.getValue().get(0).getUnidad();
                        return "- " + e.getKey() + ": faltan " + sum + " " + unidad;
                    })
                    .collect(Collectors.joining("\n", "⚠️ Faltantes:\n", ""));
        }

        // --- Actualizar datos
        managed.setCliente(cliente);
        managed.setEmpleadoAsignado(empleado);
        managed.setFormaEntrega(formaEntrega);
        managed.setFechaEntrega(fechaEntrega);

        // --- Actualizar productos
        managed.getPedidoProductos().removeIf(pp -> productosSeleccionados.getOrDefault(pp.getProducto(), 0) == 0);
        for (PedidoProducto pp : managed.getPedidoProductos()) {
            pp.setCantidad(productosSeleccionados.get(pp.getProducto()));
        }
        Set<Long> idsExistentes = managed.getPedidoProductos().stream()
                .map(pp -> pp.getProducto().getId())
                .collect(Collectors.toSet());
        for (Map.Entry<Producto, Integer> entry : productosSeleccionados.entrySet()) {
            if (entry.getValue() > 0 && !idsExistentes.contains(entry.getKey().getId())) {
                managed.getPedidoProductos().add(new PedidoProducto(managed, entry.getKey(), entry.getValue()));
            }
        }

        // --- Actualizar combos
        managed.getPedidoCombos().removeIf(pc -> combosSeleccionados.getOrDefault(pc.getCombo(), 0) == 0);
        for (PedidoCombo pc : managed.getPedidoCombos()) {
            pc.setCantidad(combosSeleccionados.get(pc.getCombo()));
        }
        Set<Long> combosExistentes = managed.getPedidoCombos().stream()
                .map(pc -> pc.getCombo().getId())
                .collect(Collectors.toSet());
        for (Map.Entry<Combo, Integer> entry : combosSeleccionados.entrySet()) {
            if (entry.getValue() > 0 && !combosExistentes.contains(entry.getKey().getId())) {
                managed.getPedidoCombos().add(new PedidoCombo(managed, entry.getKey(), entry.getValue()));
            }
        }

        managed.setTotalPedido(calcularTotalPedido(productosSeleccionados, combosSeleccionados));
        pedidoDAO.update(managed);

        return new PedidoConFaltantes(managed, mensajeFaltantes, mensajeDevolucion);
    }

    /**
     * Valida si hay stock suficiente para los productos seleccionados, sin
     * descontar stock real.
     * Devuelve lista de faltantes simulados, pero NO descuenta stock.
     */
    public List<InsumoFaltante> validarStockPedido(Map<Producto, Integer> productosSeleccionados) {
        return recetaProcessor.simularFaltantes(productosSeleccionados);
    }

    // Clase auxiliar que incluye el pedido y los faltantes

    @Getter
    public static class PedidoConFaltantes {
        private final Pedido pedido;
        private final String mensajeFaltantes;
        private final String mensajeDevolucion;

        public PedidoConFaltantes(Pedido pedido, String mensajeFaltantes) {
            this.pedido = pedido;
            this.mensajeFaltantes = mensajeFaltantes;
            this.mensajeDevolucion = null;
        }

        public PedidoConFaltantes(Pedido pedido, String mensajeFaltantes, String mensajeDevolucion) {
            this.pedido = pedido;
            this.mensajeFaltantes = mensajeFaltantes;
            this.mensajeDevolucion = mensajeDevolucion;
        }

        public Pedido getPedido() {
            return pedido;
        }

        public String getMensajeFaltantes() {
            return mensajeFaltantes;
        }

        public String getMensajeDevolucion() {
            return mensajeDevolucion;
        }
    }

}
