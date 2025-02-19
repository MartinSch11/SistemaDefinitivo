package model;

import persistence.dao.InsumoDAO;
import persistence.dao.PedidoDAO;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EstadisticasService {
    private PedidoDAO pedidoDAO;
    private InsumoDAO insumoDAO;

    public EstadisticasService() {
        this.pedidoDAO = new PedidoDAO();
        this.insumoDAO = new InsumoDAO();
    }

    // Obtener ingresos desde los pedidos
    public Map<String, Double> obtenerIngresosPorFecha(LocalDate fechaDesde, LocalDate fechaHasta) {
        List<Pedido> pedidos = pedidoDAO.findAll().stream()
                .filter(p -> !p.getFechaEntrega().isBefore(fechaDesde) && !p.getFechaEntrega().isAfter(fechaHasta))
                .collect(Collectors.toList());

        return pedidos.stream().collect(Collectors.groupingBy(
                p -> p.getFechaEntrega().toString(), // Agrupar por fecha de entrega
                Collectors.summingDouble(this::calcularTotalPedido) // Calcular total del pedido
        ));
    }

    // Obtener egresos desde los insumos comprados
    public Map<String, Double> obtenerEgresosPorFecha(LocalDate fechaDesde, LocalDate fechaHasta) {
        List<Insumo> insumos = insumoDAO.findAll().stream()
                .filter(i -> i.getFechaCompra() != null) // Evitar NullPointerException
                .filter(i -> !i.getFechaCompra().isBefore(fechaDesde) && !i.getFechaCompra().isAfter(fechaHasta))
                .collect(Collectors.toList());

        return insumos.stream().collect(Collectors.groupingBy(
                i -> i.getFechaCompra().toString(),
                Collectors.summingDouble(i -> i.getPrecio() * i.getCantidad()) // Multiplicación correcta
        ));
    }

    // Método auxiliar para calcular el total de un pedido
    private double calcularTotalPedido(Pedido pedido) {
        // Agrupar productos y contar cuántas veces aparecen en pedido_producto
        Map<Producto, Long> contadorProductos = pedido.getPedidoProductos().stream()
                .collect(Collectors.groupingBy(
                        PedidoProducto::getProducto,
                        Collectors.counting() // Cuenta las repeticiones del producto
                ));

        // Multiplicar precio por cantidad (convertir BigDecimal a double)
        return contadorProductos.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrecio().doubleValue() * entry.getValue().doubleValue())
                .sum();
    }


    public Map<String, Integer> generarDatosPruebaProductosVendidos() {
        Map<String, Integer> productos = new HashMap<>();
        productos.put("Torta de Chocolate", 120);
        productos.put("Cheesecake", 90);
        productos.put("Macarons", 150);
        productos.put("Brownies", 80);
        return productos;
    }
}
