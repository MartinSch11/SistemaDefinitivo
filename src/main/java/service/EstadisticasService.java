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
import model.Pedido;
import model.PedidoProducto;
import model.IngresoDetallado;
import model.EgresoDetallado;
import model.MovimientoStock;
import model.Evento;

public class EstadisticasService {
    private PedidoDAO pedidoDAO;
    private HistorialCompraDAO historialCompraDAO = new HistorialCompraDAO();

    public EstadisticasService() {
        this.pedidoDAO = new PedidoDAO();
    }

    // Obtener ingresos desde los pedidos
    public Map<String, Double> obtenerIngresosPorFecha(LocalDate fechaDesde, LocalDate fechaHasta) {
        List<Pedido> pedidos = pedidoDAO.findAll().stream()
                .filter(p -> !p.getFechaEntrega().isBefore(fechaDesde)
                        && !p.getFechaEntrega().isAfter(fechaHasta))
                .filter(p -> "Entregado".equalsIgnoreCase(p.getEstadoPedido())) // Solo pedidos entregados
                .collect(Collectors.toList());

        Map<String, Double> ingresosPorFecha = pedidos.stream().collect(Collectors.groupingBy(
                p -> p.getFechaEntregado().toString(), // Agrupar por fecha de entrega
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
        return ingresosPorFecha;
    }

    // Obtener egresos desde los insumos comprados (V2.0: Usamos MovimientoStock, es más preciso)
    public Map<String, Double> obtenerEgresosPorFecha(LocalDate fechaDesde, LocalDate fechaHasta) {
        // CORRECCIÓN: En vez de mirar lotes (que pueden haberse consumido), miramos el historial de compras
        List<MovimientoStock> compras = historialCompraDAO.findAll().stream()
                .filter(m -> m.getTipo() == MovimientoStock.TipoMovimiento.COMPRA)
                .filter(m -> {
                    LocalDate fechaMov = m.getFechaMovimiento().toLocalDate();
                    return !fechaMov.isBefore(fechaDesde) && !fechaMov.isAfter(fechaHasta);
                })
                .collect(Collectors.toList());

        return compras.stream().collect(Collectors.groupingBy(
                m -> m.getFechaMovimiento().toLocalDate().toString(),
                Collectors.summingDouble(MovimientoStock::getCostoTotal)));
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
            // Productos individuales
            for (PedidoProducto pp : pedido.getPedidoProductos()) {
                String nombre = pp.getProducto() != null ? pp.getProducto().getNombre() : "";
                productosVendidos.put(nombre, productosVendidos.getOrDefault(nombre, 0) + pp.getCantidad());
            }
            // Productos de combos
            if (pedido.getPedidoCombos() != null) {
                for (model.PedidoCombo pc : pedido.getPedidoCombos()) {
                    int cantidadCombo = pc.getCantidad();
                    model.Combo combo = pc.getCombo();
                    if (combo != null && combo.getProductos() != null) {
                        for (model.ComboProducto cp : combo.getProductos()) {
                            String nombreProd = cp.getProducto() != null ? cp.getProducto().getNombre() : "";
                            int cantidadEnCombo = cp.getCantidad() != null ? cp.getCantidad() : 1;
                            productosVendidos.put(nombreProd, productosVendidos.getOrDefault(nombreProd, 0) + cantidadCombo * cantidadEnCombo);
                        }
                    }
                }
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
            // Productos individuales
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
            // Combos vendidos (mostrar como combo, no desglosar)
            if (pedido.getPedidoCombos() != null) {
                for (model.PedidoCombo pc : pedido.getPedidoCombos()) {
                    String nombreCombo = pc.getCombo() != null ? pc.getCombo().getNombre() : "";
                    double precioUnitarioCombo = pc.getCombo() != null && pc.getCombo().getPrecio() != null ? pc.getCombo().getPrecio().doubleValue() : 0.0;
                    detalles.add(new IngresoDetallado(
                            pedido.getFechaEntregado(),
                            pedido.getNumeroPedido() != null ? pedido.getNumeroPedido().intValue() : 0,
                            cliente,
                            nombreCombo,
                            pc.getCantidad(),
                            precioUnitarioCombo,
                            pedido.getTotalPedido() != null ? pedido.getTotalPedido().doubleValue() : 0.0
                    ));
                }
            }
        }
        return detalles;
    }

    // Obtener egresos detallados para exportar desde historial_compra (V2.0)
    public List<EgresoDetallado> obtenerEgresosDetallados(LocalDate fechaDesde, LocalDate fechaHasta) {
        List<MovimientoStock> compras = historialCompraDAO.findAll().stream()
                .filter(c -> c.getTipo() == MovimientoStock.TipoMovimiento.COMPRA)
                .filter(c -> {
                    LocalDate fecha = c.getFechaMovimiento().toLocalDate();
                    return !fecha.isBefore(fechaDesde) && !fecha.isAfter(fechaHasta);
                })
                .collect(Collectors.toList());
                
        List<EgresoDetallado> detalles = new java.util.ArrayList<>();
        for (MovimientoStock compra : compras) {
            detalles.add(new EgresoDetallado(
                    compra.getFechaMovimiento().toLocalDate(), // Convertir LocalDateTime a LocalDate
                    compra.getNombreIngrediente(), // getNombreIngrediente()
                    compra.getCantidad(),
                    compra.getMedida(),
                    compra.getDetalle(), // Proveedor está en detalle
                    compra.getCostoTotal() // getCostoTotal()
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
        return eventos;
    }
}