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
            mensajeFaltantes = " Pedido creado, pero hay insumos faltantes:\n" + faltantes.stream().map(f -> "- " + f.getCatalogoInsumo().getNombre() + ": faltan " + String.format(Locale.ROOT, "%.2f", f.getCantidadFaltante()) + " " + f.getUnidad()).collect(Collectors.joining("\n"));
        }

        BigDecimal totalPedido = calcularTotalPedido(productosSeleccionados);

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

    private BigDecimal calcularTotalPedido(Map<Producto, Integer> productos) {
        return productos.entrySet().stream().map(entry -> entry.getKey().getPrecio().multiply(BigDecimal.valueOf(entry.getValue()))).reduce(BigDecimal.ZERO, BigDecimal::add);
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
                            .map(f -> "- " + f.getCatalogoInsumo().getNombre() + ": faltan " +
                                    String.format(Locale.ROOT, "%.2f", f.getCantidadFaltante()) + " " + f.getUnidad())
                            .collect(Collectors.joining("\n"));
        }

        // 📝 Actualizar datos básicos
        // Asegurarse de trabajar sobre el objeto gestionado por la sesión
        pedidoOriginal = pedidoDAO.findByNumeroPedido(pedidoOriginal.getNumeroPedido());

        pedidoOriginal.setCliente(cliente);
        pedidoOriginal.setEmpleadoAsignado(empleado);
        pedidoOriginal.setFormaEntrega(formaEntrega);
        pedidoOriginal.setFechaEntrega(fechaEntrega);
        pedidoOriginal.setTotalPedido(calcularTotalPedido(productosSeleccionados));

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
        // ✅ Guardar cambios
        pedidoDAO.update(pedidoOriginal);

        // --- NUEVO: Componer mensaje profesional de insumos devueltos ---
        String mensajeDevolucion = "";
        if (resumenDevolucion != null && !resumenDevolucion.isEmpty()) {
            mensajeDevolucion = "Insumos devueltos al stock por modificación del pedido:\n" +
                    resumenDevolucion.entrySet().stream()
                            .map(e -> "- " + e.getKey() + ": " + e.getValue())
                            .collect(Collectors.joining("\n"));
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
