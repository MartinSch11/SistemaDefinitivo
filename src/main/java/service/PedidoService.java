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

    public PedidoConFaltantes crearPedido(String dniCliente, String nombreEmpleado, String formaEntrega, LocalDate fechaEntrega, Map<Producto, Integer> productosSeleccionados) throws Exception {

        if (dniCliente == null || dniCliente.isEmpty()) throw new Exception("El DNI del cliente es obligatorio.");
        if (nombreEmpleado == null || nombreEmpleado.isEmpty()) throw new Exception("Debe seleccionar un empleado.");
        if (formaEntrega == null || formaEntrega.isEmpty())
            throw new Exception("Debe seleccionar una forma de entrega.");
        if (fechaEntrega == null || fechaEntrega.isBefore(LocalDate.now()))
            throw new Exception("La fecha de entrega no es válida.");
        if (productosSeleccionados == null || productosSeleccionados.isEmpty())
            throw new Exception("Debe seleccionar al menos un producto.");

        Cliente cliente = clienteDAO.findByDni(dniCliente);
        if (cliente == null) throw new Exception("Cliente no encontrado.");

        Trabajador empleado = trabajadorDAO.findByNombre(nombreEmpleado);
        if (empleado == null) throw new Exception("Empleado no encontrado.");

        System.out.println("[DEBUG PedidoService] crearPedido productosSeleccionados:");
        productosSeleccionados.forEach((p, c) -> System.out.println("  - " + p.getId() + " | " + p.getNombre() + " | cantidad: " + c));
        // Procesar recetas y obtener faltantes (esto descuenta stock SOLO aquí)
        List<InsumoFaltante> faltantes = recetaProcessor.procesarRecetas(productosSeleccionados);

        // 💬 Mensaje para mostrar al usuario si hay insumos faltantes
        String mensajeFaltantes = "";
        if (!faltantes.isEmpty()) {
            mensajeFaltantes = " Pedido creado, pero hay insumos faltantes:\n" + faltantes.stream().map(f -> {
                double cantidad = f.getCantidadFaltante();
                String cantidadStr = (cantidad == Math.floor(cantidad)) ? String.format(Locale.ROOT, "%.0f", cantidad) : String.format(Locale.ROOT, "%.2f", cantidad);
                return "- " + f.getCatalogoInsumo().getNombre() + ": faltan " + cantidadStr + " " + f.getUnidad();
            }).collect(Collectors.joining("\n"));
        }

        BigDecimal totalPedido = calcularTotalPedido(productosSeleccionados, null);

        Pedido pedido = new Pedido(null, cliente, empleado, formaEntrega, fechaEntrega, "Sin empezar", "", totalPedido);

        // pedidoDAO.save(pedido); // Eliminado: solo se debe guardar después de setear los productos

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

    // Unifica el cálculo del total para ambos métodos, siempre suma productos y combos
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

    public PedidoConFaltantes actualizarPedido(
            Pedido pedidoOriginal,
            String dniCliente,
            String nombreEmpleado,
            String formaEntrega,
            LocalDate fechaEntrega,
            Map<Producto, Integer> productosSeleccionados) throws Exception {

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

        Cliente cliente = clienteDAO.findByDni(dniCliente);
        if (cliente == null) throw new Exception("Cliente no encontrado.");

        Trabajador empleado = trabajadorDAO.findByNombre(nombreEmpleado);
        if (empleado == null) throw new Exception("Empleado no encontrado.");

        // --- NUEVO: Devolver insumos al stock si se reduce cantidad o elimina producto ---
        Map<Producto, Integer> productosParaDevolver = new HashMap<>();
        for (PedidoProducto pp : pedidoOriginal.getPedidoProductos()) {
            Producto producto = pp.getProducto();
            int cantidadAntigua = pp.getCantidad();
            int cantidadNueva = productosSeleccionados.getOrDefault(producto, 0);
            int diferencia = cantidadAntigua - cantidadNueva;
            if (diferencia > 0) {
                productosParaDevolver.put(producto, diferencia);
            } else if (cantidadNueva == 0 && cantidadAntigua > 0) {
                productosParaDevolver.put(producto, cantidadAntigua);
            }
        }
        Map<String, String> resumenDevolucion = null;
        if (!productosParaDevolver.isEmpty()) {
            resumenDevolucion = recetaProcessor.devolverStockPorProductosConResumen(productosParaDevolver);
        }
        // --- FIN DEVOLUCIÓN ---

        // 🔍 Preparar para procesar insumos faltantes (solo para aumentos o nuevos productos)
        Map<Producto, Integer> productosParaDescontar = new HashMap<>();
        for (PedidoProducto pp : pedidoOriginal.getPedidoProductos()) {
            Producto producto = pp.getProducto();
            int cantidadAntigua = pp.getCantidad();
            int cantidadNueva = productosSeleccionados.getOrDefault(producto, 0);
            int diferencia = cantidadNueva - cantidadAntigua;
            if (diferencia > 0) {
                productosParaDescontar.put(producto, diferencia);
            }
        }
        for (Map.Entry<Producto, Integer> entry : productosSeleccionados.entrySet()) {
            boolean esNuevo = pedidoOriginal.getPedidoProductos().stream()
                    .noneMatch(pp -> pp.getProducto().equals(entry.getKey()));
            if (esNuevo) {
                productosParaDescontar.put(entry.getKey(), entry.getValue());
            }
        }
        // 🔥 Filtrar productos con cantidad > 0
        productosSeleccionados = productosSeleccionados.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        System.out.println("[DEBUG PedidoService] actualizarPedido productosParaDescontar:");
        productosParaDescontar.forEach((p, c) -> System.out.println("  - " + p.getId() + " | " + p.getNombre() + " | cantidad: " + c));
        // 🧪 Procesar faltantes
        List<InsumoFaltante> faltantes = recetaProcessor.procesarRecetas(productosParaDescontar);
        String mensajeFaltantes = "";
        if (!faltantes.isEmpty()) {
            mensajeFaltantes = "⚠️ Pedido actualizado, pero hay insumos faltantes:\n" +
                    faltantes.stream()
                            .map(f -> {
                                double cantidad = f.getCantidadFaltante();
                                String cantidadStr = (cantidad == Math.floor(cantidad)) ? String.format(Locale.ROOT, "%.0f", cantidad) : String.format(Locale.ROOT, "%.2f", cantidad);
                                return "- " + f.getCatalogoInsumo().getNombre() + ": faltan " + cantidadStr + " " + f.getUnidad();
                            })
                            .collect(Collectors.joining("\n"));
        }

        // 📝 Actualizar datos básicos
        // Asegurarse de trabajar sobre el objeto gestionado por la sesión
        pedidoOriginal = pedidoDAO.findByNumeroPedido(pedidoOriginal.getNumeroPedido());

        pedidoOriginal.setCliente(cliente);
        pedidoOriginal.setEmpleadoAsignado(empleado);
        pedidoOriginal.setFormaEntrega(formaEntrega);
        pedidoOriginal.setFechaEntrega(fechaEntrega);
        // Solo productos, combos es null aquí
        pedidoOriginal.setTotalPedido(calcularTotalPedido(productosSeleccionados, null));

        // 🔁 Actualizar lista de productos correctamente
        Iterator<PedidoProducto> it = pedidoOriginal.getPedidoProductos().iterator();
        while (it.hasNext()) {
            PedidoProducto pp = it.next();
            Integer nuevaCantidad = productosSeleccionados.get(pp.getProducto());
            if (nuevaCantidad == null || nuevaCantidad <= 0) {
                it.remove();
            }
        }
        // Ahora actualiza o agrega los que quedan
        Map<Long, Integer> nuevosProductos = productosSeleccionados.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getId(), Map.Entry::getValue));
        Set<Long> idsExistentes = pedidoOriginal.getPedidoProductos().stream()
                .map(pp -> pp.getProducto().getId())
                .collect(Collectors.toSet());
        for (Map.Entry<Producto, Integer> entry : productosSeleccionados.entrySet()) {
            if (entry.getValue() > 0 && !idsExistentes.contains(entry.getKey().getId())) {
                PedidoProducto nuevoPP = new PedidoProducto(pedidoOriginal, entry.getKey(), entry.getValue());
                pedidoOriginal.getPedidoProductos().add(nuevoPP);
            } else {
                // Si ya existe, actualiza la cantidad
                for (PedidoProducto pp : pedidoOriginal.getPedidoProductos()) {
                    if (pp.getProducto().getId().equals(entry.getKey().getId())) {
                        pp.setCantidad(entry.getValue());
                    }
                }
            }
        }
        // --- NUEVO: Componer mensaje profesional de insumos devueltos ---
        String mensajeDevolucion = "";
        if (resumenDevolucion != null && !resumenDevolucion.isEmpty()) {
            mensajeDevolucion = "Insumos devueltos al stock por modificación del pedido:\n" +
                    resumenDevolucion.entrySet().stream()
                            .map(e -> {
                                String valor = e.getValue();
                                // Buscar si el valor es un número flotante y formatear si es entero
                                try {
                                    double cantidad = Double.parseDouble(valor.replaceAll("[^0-9.,]", "").replace(",", "."));
                                    String cantidadStr = (cantidad == Math.floor(cantidad)) ? String.format(Locale.ROOT, "%.0f", cantidad) : String.format(Locale.ROOT, "%.2f", cantidad);
                                    // Reemplazar solo el número al inicio del string
                                    return "- " + e.getKey() + ": " + valor.replaceFirst("[0-9]+([.,][0-9]+)?", cantidadStr);
                                } catch (Exception ex) {
                                    return "- " + e.getKey() + ": " + valor;
                                }
                            })
                            .collect(Collectors.joining("\n"));
        }

        return new PedidoConFaltantes(pedidoOriginal, mensajeFaltantes, mensajeDevolucion);
    }

    // --- NUEVO: Crear pedido con productos y combos ---
    public PedidoConFaltantes crearPedido(String dniCliente, String nombreEmpleado, String formaEntrega, LocalDate fechaEntrega, Map<Producto, Integer> productosSeleccionados, Map<Combo, Integer> combosSeleccionados) throws Exception {
        // Si no hay combos, usar el método original
        if (combosSeleccionados == null || combosSeleccionados.isEmpty()) {
            return crearPedido(dniCliente, nombreEmpleado, formaEntrega, fechaEntrega, productosSeleccionados);
        }
        // Validaciones básicas (igual que el método original)
        if (dniCliente == null || dniCliente.isEmpty()) throw new Exception("El DNI del cliente es obligatorio.");
        if (nombreEmpleado == null || nombreEmpleado.isEmpty()) throw new Exception("Debe seleccionar un empleado.");
        if (formaEntrega == null || formaEntrega.isEmpty())
            throw new Exception("Debe seleccionar una forma de entrega.");
        if (fechaEntrega == null || fechaEntrega.isBefore(LocalDate.now()))
            throw new Exception("La fecha de entrega no es válida.");
        if ((productosSeleccionados == null || productosSeleccionados.isEmpty()) && (combosSeleccionados == null || combosSeleccionados.isEmpty()))
            throw new Exception("Debe seleccionar al menos un producto o combo.");

        Cliente cliente = clienteDAO.findByDni(dniCliente);
        if (cliente == null) throw new Exception("Cliente no encontrado.");
        Trabajador empleado = trabajadorDAO.findByNombre(nombreEmpleado);
        if (empleado == null) throw new Exception("Empleado no encontrado.");

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
        // Procesar recetas y obtener faltantes (esto descuenta stock SOLO aquí)
        List<InsumoFaltante> faltantes = recetaProcessor.procesarRecetas(productosTotales);
        String mensajeFaltantes = "";
        if (!faltantes.isEmpty()) {
            mensajeFaltantes = " Pedido creado, pero hay insumos faltantes:\n" + faltantes.stream().map(f -> {
                double cantidad = f.getCantidadFaltante();
                String cantidadStr = (cantidad == Math.floor(cantidad)) ? String.format(Locale.ROOT, "%.0f", cantidad) : String.format(Locale.ROOT, "%.2f", cantidad);
                return "- " + f.getCatalogoInsumo().getNombre() + ": faltan " + cantidadStr + " " + f.getUnidad();
            }).collect(java.util.stream.Collectors.joining("\n"));
        }
        // Calcular total sumando productos y combos
        java.math.BigDecimal totalPedido = java.math.BigDecimal.ZERO;
        if (productosSeleccionados != null) {
            totalPedido = totalPedido.add(calcularTotalPedido(productosSeleccionados, null));
        }
        if (combosSeleccionados != null) {
            for (Map.Entry<Combo, Integer> entry : combosSeleccionados.entrySet()) {
                totalPedido = totalPedido.add(entry.getKey().getPrecio().multiply(java.math.BigDecimal.valueOf(entry.getValue())));
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
    public PedidoConFaltantes actualizarPedido(Pedido pedidoOriginal, String dniCliente, String nombreEmpleado, String formaEntrega, LocalDate fechaEntrega, Map<Producto, Integer> productosSeleccionados, Map<Combo, Integer> combosSeleccionados) throws Exception {
        // Si no hay combos, usar el método original
        if (combosSeleccionados == null || combosSeleccionados.isEmpty()) {
            return actualizarPedido(pedidoOriginal, dniCliente, nombreEmpleado, formaEntrega, fechaEntrega, productosSeleccionados);
        }
        // Validaciones básicas (igual que el método original)
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
        if ((productosSeleccionados == null || productosSeleccionados.isEmpty()) && (combosSeleccionados == null || combosSeleccionados.isEmpty()))
            throw new Exception("Debe seleccionar al menos un producto o combo.");
        Cliente cliente = clienteDAO.findByDni(dniCliente);
        if (cliente == null) throw new Exception("Cliente no encontrado.");
        Trabajador empleado = trabajadorDAO.findByNombre(nombreEmpleado);
        if (empleado == null) throw new Exception("Empleado no encontrado.");
        // --- Devolver insumos al stock si se reduce cantidad o elimina producto/combo ---
        Map<Producto, Integer> productosParaDevolver = new HashMap<>();
        // De productos
        for (PedidoProducto pp : pedidoOriginal.getPedidoProductos()) {
            Producto producto = pp.getProducto();
            int cantidadAntigua = pp.getCantidad();
            int cantidadNueva = productosSeleccionados.getOrDefault(producto, 0);
            int diferencia = cantidadAntigua - cantidadNueva;
            if (diferencia > 0) {
                productosParaDevolver.put(producto, diferencia);
            } else if (cantidadNueva == 0 && cantidadAntigua > 0) {
                productosParaDevolver.put(producto, cantidadAntigua);
            }
        }
        // De combos
        for (PedidoCombo pc : pedidoOriginal.getPedidoCombos()) {
            Combo combo = pc.getCombo();
            int cantidadAntigua = pc.getCantidad();
            int cantidadNueva = combosSeleccionados.getOrDefault(combo, 0);
            int diferencia = cantidadAntigua - cantidadNueva;
            if (diferencia > 0) {
                for (ComboProducto cp : combo.getProductos()) {
                    productosParaDevolver.merge(cp.getProducto(), cp.getCantidad() * diferencia, Integer::sum);
                }
            } else if (cantidadNueva == 0 && cantidadAntigua > 0) {
                for (ComboProducto cp : combo.getProductos()) {
                    productosParaDevolver.merge(cp.getProducto(), cp.getCantidad() * cantidadAntigua, Integer::sum);
                }
            }
        }
        Map<String, String> resumenDevolucion = null;
        if (!productosParaDevolver.isEmpty()) {
            resumenDevolucion = recetaProcessor.devolverStockPorProductosConResumen(productosParaDevolver);
        }
        // --- FIN DEVOLUCIÓN ---
        // --- Preparar para procesar insumos faltantes (solo para aumentos o nuevos productos/combos) ---
        Map<Producto, Integer> productosParaDescontar = new HashMap<>();
        // Productos
        for (PedidoProducto pp : pedidoOriginal.getPedidoProductos()) {
            Producto producto = pp.getProducto();
            int cantidadAntigua = pp.getCantidad();
            int cantidadNueva = productosSeleccionados.getOrDefault(producto, 0);
            int diferencia = cantidadNueva - cantidadAntigua;
            if (diferencia > 0) {
                productosParaDescontar.put(producto, diferencia);
            }
        }
        for (Map.Entry<Producto, Integer> entry : productosSeleccionados.entrySet()) {
            boolean esNuevo = pedidoOriginal.getPedidoProductos().stream()
                    .noneMatch(pp -> pp.getProducto().equals(entry.getKey()));
            if (esNuevo) {
                productosParaDescontar.put(entry.getKey(), entry.getValue());
            }
        }
        // Combos
        for (PedidoCombo pc : pedidoOriginal.getPedidoCombos()) {
            Combo combo = pc.getCombo();
            int cantidadAntigua = pc.getCantidad();
            int cantidadNueva = combosSeleccionados.getOrDefault(combo, 0);
            int diferencia = cantidadNueva - cantidadAntigua;
            if (diferencia > 0) {
                for (ComboProducto cp : combo.getProductos()) {
                    productosParaDescontar.merge(cp.getProducto(), cp.getCantidad() * diferencia, Integer::sum);
                }
            }
        }
        for (Map.Entry<Combo, Integer> entry : combosSeleccionados.entrySet()) {
            boolean esNuevo = pedidoOriginal.getPedidoCombos().stream()
                    .noneMatch(pc -> pc.getCombo().equals(entry.getKey()));
            if (esNuevo) {
                for (ComboProducto cp : entry.getKey().getProductos()) {
                    productosParaDescontar.merge(cp.getProducto(), cp.getCantidad() * entry.getValue(), Integer::sum);
                }
            }
        }
        // 🔥 Filtrar productos con cantidad > 0
        productosSeleccionados = productosSeleccionados.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        combosSeleccionados = combosSeleccionados.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 🧪 Procesar faltantes
        List<InsumoFaltante> faltantes = recetaProcessor.procesarRecetas(productosParaDescontar);
        String mensajeFaltantes = "";
        if (!faltantes.isEmpty()) {
            mensajeFaltantes = "⚠️ Pedido actualizado, pero hay insumos faltantes:\n" +
                    faltantes.stream()
                            .map(f -> {
                                double cantidad = f.getCantidadFaltante();
                                String cantidadStr = (cantidad == Math.floor(cantidad)) ? String.format(Locale.ROOT, "%.0f", cantidad) : String.format(Locale.ROOT, "%.2f", cantidad);
                                return "- " + f.getCatalogoInsumo().getNombre() + ": faltan " + cantidadStr + " " + f.getUnidad();
                            })
                            .collect(java.util.stream.Collectors.joining("\n"));
        }
        // 📝 Actualizar datos básicos
        pedidoOriginal = pedidoDAO.findByNumeroPedido(pedidoOriginal.getNumeroPedido());
        pedidoOriginal.setCliente(cliente);
        pedidoOriginal.setEmpleadoAsignado(empleado);
        pedidoOriginal.setFormaEntrega(formaEntrega);
        pedidoOriginal.setFechaEntrega(fechaEntrega);
        pedidoOriginal.setTotalPedido(calcularTotalPedido(productosSeleccionados, null));
        // 🔁 Actualizar lista de productos correctamente
        java.util.Iterator<PedidoProducto> it = pedidoOriginal.getPedidoProductos().iterator();
        while (it.hasNext()) {
            PedidoProducto pp = it.next();
            Integer nuevaCantidad = productosSeleccionados.get(pp.getProducto());
            if (nuevaCantidad == null || nuevaCantidad <= 0) {
                it.remove();
            }
        }
        // Ahora actualiza o agrega los que quedan
        java.util.Set<Long> idsExistentes = pedidoOriginal.getPedidoProductos().stream()
                .map(pp -> pp.getProducto().getId())
                .collect(java.util.stream.Collectors.toSet());
        for (Map.Entry<Producto, Integer> entry : productosSeleccionados.entrySet()) {
            if (entry.getValue() > 0 && !idsExistentes.contains(entry.getKey().getId())) {
                PedidoProducto nuevoPP = new PedidoProducto(pedidoOriginal, entry.getKey(), entry.getValue());
                pedidoOriginal.getPedidoProductos().add(nuevoPP);
            } else {
                // Si ya existe, actualiza la cantidad
                for (PedidoProducto pp : pedidoOriginal.getPedidoProductos()) {
                    if (pp.getProducto().getId().equals(entry.getKey().getId())) {
                        pp.setCantidad(entry.getValue());
                    }
                }
            }
        }
        // --- Actualizar lista de combos correctamente ---
        java.util.Iterator<PedidoCombo> itCombo = pedidoOriginal.getPedidoCombos().iterator();
        while (itCombo.hasNext()) {
            PedidoCombo pc = itCombo.next();
            Integer nuevaCantidad = combosSeleccionados.get(pc.getCombo());
            if (nuevaCantidad == null || nuevaCantidad <= 0) {
                itCombo.remove();
            }
        }
        java.util.Set<Long> idsCombosExistentes = pedidoOriginal.getPedidoCombos().stream()
                .map(pc -> pc.getCombo().getId())
                .collect(java.util.stream.Collectors.toSet());
        for (Map.Entry<Combo, Integer> entry : combosSeleccionados.entrySet()) {
            if (entry.getValue() > 0 && !idsCombosExistentes.contains(entry.getKey().getId())) {
                PedidoCombo nuevoPC = new PedidoCombo(pedidoOriginal, entry.getKey(), entry.getValue());
                pedidoOriginal.getPedidoCombos().add(nuevoPC);
            } else {
                // Si ya existe, actualiza la cantidad
                for (PedidoCombo pc : pedidoOriginal.getPedidoCombos()) {
                    if (pc.getCombo().getId().equals(entry.getKey().getId())) {
                        pc.setCantidad(entry.getValue());
                    }
                }
            }
        }
        // ✅ Guardar cambios
        pedidoDAO.update(pedidoOriginal);
        // --- NUEVO: Componer mensaje profesional de insumos devueltos ---
        String mensajeDevolucion = "";
        if (resumenDevolucion != null && !resumenDevolucion.isEmpty()) {
            mensajeDevolucion = "Insumos devueltos al stock por modificación del pedido:\n" +
                    resumenDevolucion.entrySet().stream()
                            .map(e -> {
                                String valor = e.getValue();
                                try {
                                    double cantidad = Double.parseDouble(valor.replaceAll("[^0-9.,]", "").replace(",", "."));
                                    String cantidadStr = (cantidad == Math.floor(cantidad)) ? String.format(Locale.ROOT, "%.0f", cantidad) : String.format(Locale.ROOT, "%.2f", cantidad);
                                    return "- " + e.getKey() + ": " + valor.replaceFirst("[0-9]+([.,][0-9]+)?", cantidadStr);
                                } catch (Exception ex) {
                                    return "- " + e.getKey() + ": " + valor;
                                }
                            })
                            .collect(java.util.stream.Collectors.joining("\n"));
        }
        return new PedidoConFaltantes(pedidoOriginal, mensajeFaltantes, mensajeDevolucion);
    }

    /**
     * Valida si hay stock suficiente para los productos seleccionados, sin descontar stock real.
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
