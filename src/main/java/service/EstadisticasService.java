package service;

import persistence.dao.InsumoDAO;
import persistence.dao.PedidoDAO;
import persistence.dao.HistorialCompraDAO;
import persistence.dao.EventoDAO;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.Insumo;
import model.Pedido;
import model.PedidoProducto;
import model.IngresoDetallado;
import model.EgresoDetallado;
import model.HistorialCompra;
import model.Evento;

public class EstadisticasService {
    private PedidoDAO pedidoDAO;
    private InsumoDAO insumoDAO;
    private HistorialCompraDAO historialCompraDAO = new HistorialCompraDAO();

    public EstadisticasService() {
        this.pedidoDAO = new PedidoDAO();
        this.insumoDAO = new InsumoDAO();
    }

    // Obtener ingresos desde los pedidos
    public Map<String, Double> obtenerIngresosPorFecha(LocalDate fechaDesde, LocalDate fechaHasta) {
        List<Pedido> pedidos = pedidoDAO.findAll().stream()
                .filter(p -> !p.getFechaEntrega().isBefore(fechaDesde)
                        && !p.getFechaEntrega().isAfter(fechaHasta))
                .filter(p -> "Entregado".equalsIgnoreCase(p.getEstadoPedido())) // Solo pedidos entregados
                .collect(Collectors.toList());

        Map<String, Double> ingresosPorFecha = pedidos.stream().collect(Collectors.groupingBy(
                p -> p.getFechaEntrega().toString(), // Agrupar por fecha de entrega
                Collectors.summingDouble(p -> p.getTotalPedido().doubleValue()) // Usar total_pedido real
        ));

        // Sumar ingresos de eventos realizados
        EventoDAO eventoDAO = new EventoDAO();
        List<Evento> eventos = eventoDAO.findAll().stream()
                .filter(e -> !e.getFecha_evento().isBefore(fechaDesde)
                        && !e.getFecha_evento().isAfter(fechaHasta))
                .filter(e -> "Realizado".equalsIgnoreCase(e.getEstado()))
                .collect(Collectors.toList());
        for (Evento evento : eventos) {
            String fecha = evento.getFecha_evento().toString();
            double presupuesto = evento.getPresupuesto() != null ? evento.getPresupuesto().doubleValue() : 0.0;
            ingresosPorFecha.put(fecha, ingresosPorFecha.getOrDefault(fecha, 0.0) + presupuesto);
        }
        eventoDAO.close();
        return ingresosPorFecha;
    }

    // Obtener egresos desde los insumos comprados
    public Map<String, Double> obtenerEgresosPorFecha(LocalDate fechaDesde, LocalDate fechaHasta) {
        List<Insumo> insumos = insumoDAO.findAll().stream()
                .filter(i -> i.getFechaCompra() != null) // Evitar NullPointerException
                .filter(i -> !i.getFechaCompra().isBefore(fechaDesde)
                        && !i.getFechaCompra().isAfter(fechaHasta))
                .collect(Collectors.toList());

        return insumos.stream().collect(Collectors.groupingBy(
                i -> i.getFechaCompra().toString(),
                Collectors.summingDouble(Insumo::getPrecio)));
    }

    // Obtener productos vendidos por cantidad in un rango de fechas
    public Map<String, Integer> obtenerProductosVendidosPorCantidad(LocalDate fechaDesde, LocalDate fechaHasta) {
        List<Pedido> pedidos = pedidoDAO.findAll().stream()
                .filter(p -> p.getFechaEntregado() != null)
                .filter(p -> !p.getFechaEntregado().isBefore(fechaDesde)
                        && !p.getFechaEntregado().isAfter(fechaHasta))
                .filter(p -> "Entregado".equalsIgnoreCase(p.getEstadoPedido())) // Solo pedidos entregados
                .collect(Collectors.toList());

        Map<String, Integer> productosVendidos = new HashMap<>();
        for (Pedido pedido : pedidos) {
            for (PedidoProducto pp : pedido.getPedidoProductos()) {
                String nombre = pp.getProducto() != null ? pp.getProducto().getNombre() : "";
                productosVendidos.put(nombre, productosVendidos.getOrDefault(nombre, 0) + pp.getCantidad());
            }
        }
        return productosVendidos;
    }

    // Obtener ingresos detallados para exportar
    public List<IngresoDetallado> obtenerIngresosDetallados(LocalDate fechaDesde, LocalDate fechaHasta) {
        List<Pedido> pedidos = pedidoDAO.findAll().stream()
                .filter(p -> p.getFechaEntregado() != null)
                .filter(p -> !p.getFechaEntregado().isBefore(fechaDesde)
                        && !p.getFechaEntregado().isAfter(fechaHasta))
                .filter(p -> "Entregado".equalsIgnoreCase(p.getEstadoPedido()))
                .collect(Collectors.toList());
        List<IngresoDetallado> detalles = new java.util.ArrayList<>();
        for (Pedido pedido : pedidos) {
            String cliente = pedido.getCliente() != null ? pedido.getCliente().getNombre() + " " + pedido.getCliente().getApellido() : "";
            for (PedidoProducto pp : pedido.getPedidoProductos()) {
                String producto = pp.getProducto() != null ? pp.getProducto().getNombre() : "";
                double precioUnitario = pp.getProducto() != null ? pp.getProducto().getPrecio().doubleValue() : 0.0;
                detalles.add(new IngresoDetallado(
                        pedido.getFechaEntregado(),
                        pedido.getNumeroPedido() != null ? pedido.getNumeroPedido().intValue() : 0,
                        cliente,
                        producto,
                        pp.getCantidad(),
                        precioUnitario,
                        pedido.getTotalPedido() != null ? pedido.getTotalPedido().doubleValue() : 0.0
                ));
            }
        }
        return detalles;
    }

    // Obtener egresos detallados para exportar desde historial_compra
    public List<EgresoDetallado> obtenerEgresosDetallados(LocalDate fechaDesde, LocalDate fechaHasta) {
        List<HistorialCompra> compras = historialCompraDAO.findAll().stream()
                .filter(c -> c.getFechaCompra() != null)
                .filter(c -> !c.getFechaCompra().isBefore(fechaDesde)
                        && !c.getFechaCompra().isAfter(fechaHasta))
                .collect(Collectors.toList());
        List<EgresoDetallado> detalles = new java.util.ArrayList<>();
        for (HistorialCompra compra : compras) {
            detalles.add(new EgresoDetallado(
                    compra.getFechaCompra(),
                    compra.getInsumo(),
                    compra.getCantidad(),
                    compra.getMedida(),
                    compra.getProveedor(),
                    compra.getPrecio()
            ));
        }
        return detalles;
    }

    /**
     * Devuelve los ingresos totales separados por fuente: pedidos y eventos realizados.
     */
    public Map<String, Double> obtenerIngresosSeparadosPorFuente(LocalDate fechaDesde, LocalDate fechaHasta) {
        double totalPedidos = pedidoDAO.findAll().stream()
                .filter(p -> !p.getFechaEntrega().isBefore(fechaDesde)
                        && !p.getFechaEntrega().isAfter(fechaHasta))
                .filter(p -> "Entregado".equalsIgnoreCase(p.getEstadoPedido()))
                .mapToDouble(p -> p.getTotalPedido().doubleValue())
                .sum();

        EventoDAO eventoDAO = new EventoDAO();
        double totalEventos = eventoDAO.findAll().stream()
                .filter(e -> !e.getFecha_evento().isBefore(fechaDesde)
                        && !e.getFecha_evento().isAfter(fechaHasta))
                .filter(e -> "Realizado".equalsIgnoreCase(e.getEstado()))
                .mapToDouble(e -> e.getPresupuesto() != null ? e.getPresupuesto().doubleValue() : 0.0)
                .sum();
        eventoDAO.close();

        Map<String, Double> resultado = new HashMap<>();
        resultado.put("pedidos", totalPedidos);
        resultado.put("eventos", totalEventos);
        System.out.println("[DEPURACIÓN] Ingresos pedidos: " + totalPedidos + ", ingresos eventos: " + totalEventos);
        return resultado;
    }

    /**
     * Devuelve la lista de eventos realizados en el rango de fechas.
     */
    public java.util.List<model.Evento> obtenerEventosRealizados(LocalDate fechaDesde, LocalDate fechaHasta) {
        EventoDAO eventoDAO = new EventoDAO();
        java.util.List<model.Evento> eventos = eventoDAO.findAll().stream()
                .filter(e -> e.getFecha_evento() != null)
                .filter(e -> !e.getFecha_evento().isBefore(fechaDesde) && !e.getFecha_evento().isAfter(fechaHasta))
                .filter(e -> "Realizado".equalsIgnoreCase(e.getEstado()))
                .collect(java.util.stream.Collectors.toList());
        eventoDAO.close();
        return eventos;
    }

}
