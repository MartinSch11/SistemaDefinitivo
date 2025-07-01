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

    public PedidoConFaltantes crearPedido(
            String dniCliente,
            String nombreEmpleado,
            String formaEntrega,
            LocalDate fechaEntrega,
            Map<Producto, Integer> productosSeleccionados
    ) throws Exception {

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

        // Procesar recetas y obtener faltantes
        List<InsumoFaltante> faltantes = recetaProcessor.procesarRecetas(productosSeleccionados);

        // 💬 Mensaje para mostrar al usuario si hay insumos faltantes
        String mensajeFaltantes = "";
        if (!faltantes.isEmpty()) {
            mensajeFaltantes = "⚠️ Pedido creado, pero hay insumos faltantes:\n" +
                    faltantes.stream()
                            .map(f -> "- " + f.getInsumo().getNombre() + ": faltan " +
                                    String.format(Locale.ROOT, "%.2f", f.getCantidadFaltante()) + " " + f.getUnidad())
                            .collect(Collectors.joining("\n"));
        }

        BigDecimal totalPedido = calcularTotalPedido(productosSeleccionados);

        Pedido pedido = new Pedido(
                null,
                cliente,
                empleado,
                formaEntrega,
                fechaEntrega,
                "Sin empezar",
                "",
                totalPedido
        );

        pedidoDAO.save(pedido); // Persistimos para obtener ID

        List<PedidoProducto> pedidoProductos = new ArrayList<>();
        for (Map.Entry<Producto, Integer> entry : productosSeleccionados.entrySet()) {
            Producto producto = entry.getKey();
            int cantidad = entry.getValue();

            PedidoProducto pedidoProducto = new PedidoProducto(pedido, producto, cantidad);
            PedidoProductoId id = new PedidoProductoId(pedido.getNumeroPedido(), producto.getId());
            pedidoProducto.setId(id);
            pedidoProductos.add(pedidoProducto);
        }

        pedido.setPedidoProductos(pedidoProductos);

        pedidoDAO.save(pedido); // Persistimos productos vía cascade

        return new PedidoConFaltantes(pedido, mensajeFaltantes);
    }

    private BigDecimal calcularTotalPedido(Map<Producto, Integer> productos) {
        return productos.entrySet().stream()
                .map(entry -> entry.getKey().getPrecio().multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ✅ Clase auxiliar que incluye el pedido y los faltantes

    @Getter
    public static class PedidoConFaltantes {
        private final Pedido pedido;
        private final String mensajeFaltantes;

        public PedidoConFaltantes(Pedido pedido, String mensajeFaltantes) {
            this.pedido = pedido;
            this.mensajeFaltantes = mensajeFaltantes;
        }
    }

}
