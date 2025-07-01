package service;

import model.Evento;
import model.Notificacion;
import model.Pedido;
import persistence.dao.EventoDAO;
import persistence.dao.PedidoDAO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NotificacionService {
    /**
     * Busca eventos próximos y genera notificaciones.
     * También agrega notificación de pedidos pendientes para hoy.
     * @param diasAdelante cantidad de días a futuro para buscar eventos
     * @return lista de notificaciones
     */
    public List<Notificacion> obtenerNotificacionesEventosProximos(int diasAdelante) {
        List<Notificacion> notificaciones = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        // --- Notificación de pedidos pendientes para hoy ---
        PedidoDAO pedidoDAO = new PedidoDAO();
        List<Pedido> pedidos = pedidoDAO.findAll();
        long pedidosPendientesHoy = pedidos.stream()
                .filter(p -> hoy.equals(p.getFechaEntrega()) && !"Hecho".equalsIgnoreCase(p.getEstadoPedido()))
                .count();
        if (pedidosPendientesHoy > 0) {
            String mensajePedidos = pedidosPendientesHoy == 1
                    ? "Hay 1 pedido pendiente para hoy"
                    : String.format("Hay %d pedidos pendientes para hoy", pedidosPendientesHoy);
            notificaciones.add(new Notificacion(mensajePedidos, "Pedidos", hoy));
        }
        // --- Notificaciones de eventos próximos ---
        EventoDAO eventoDAO = new EventoDAO();
        LocalDate hasta = hoy.plusDays(diasAdelante);
        List<Evento> eventos = eventoDAO.findAll();
        eventoDAO.close();
        for (Evento evento : eventos) {
            LocalDate fecha = evento.getFecha_evento();
            if (fecha != null && !fecha.isBefore(hoy) && !fecha.isAfter(hasta)) {
                String mensaje = String.format("En %d días tienes un evento: '%s'",
                        fecha.toEpochDay() - hoy.toEpochDay(), evento.getNombre_evento());
                notificaciones.add(new Notificacion(mensaje, evento.getNombre_evento(), fecha));
            }
        }
        return notificaciones;
    }
}
