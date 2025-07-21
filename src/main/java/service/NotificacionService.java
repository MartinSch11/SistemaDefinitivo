package service;

import model.Evento;
import model.Notificacion;
import model.Pedido;
import persistence.dao.EventoDAO;
import persistence.dao.PedidoDAO;
import persistence.dao.InsumoDAO;
import persistence.dao.AgendaDAO;
import model.Insumo;
import model.Agenda;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public class NotificacionService {
    /**
     * Busca eventos próximos y genera notificaciones.
     * También agrega notificación de pedidos pendientes para hoy y de insumos próximos a vencer.
     * @param diasAnticipoEventos cantidad de días a futuro para buscar eventos
     * @param diasAnticipoCaducidad cantidad de días a futuro para notificar insumos por caducar
     * @return lista de notificaciones
     */
    public List<Notificacion> obtenerNotificacionesEventosYCaducidad(int diasAnticipoEventos, int diasAnticipoCaducidad) {
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
        // --- Notificaciones de insumos próximos a vencer ---
        InsumoDAO insumoDAO = new InsumoDAO();
        List<Insumo> insumos = insumoDAO.findAll();
        LocalDate limiteCaducidad = hoy.plusDays(diasAnticipoCaducidad);
        for (Insumo insumo : insumos) {
            if (insumo.getFechaCaducidad() != null && insumo.getCantidad() > 0) {
                LocalDate caducidad = insumo.getFechaCaducidad();
                if (!caducidad.isBefore(hoy) && !caducidad.isAfter(limiteCaducidad)) {
                    long diasRestantes = caducidad.toEpochDay() - hoy.toEpochDay();
                    String nombre = insumo.getCatalogoInsumo() != null ? insumo.getCatalogoInsumo().getNombre() : "Insumo";
                    String diasTexto = diasRestantes == 1 ? "día" : "días";
                    String mensaje = diasRestantes == 0
                            ? String.format("El insumo '%s' vence hoy", nombre)
                            : String.format("El insumo '%s' vence en %d %s (%s)", nombre, diasRestantes, diasTexto, caducidad);
                    notificaciones.add(new Notificacion(mensaje, nombre, caducidad));
                }
            }
        }
        // --- Notificaciones de eventos próximos ---
        EventoDAO eventoDAO = new EventoDAO();
        LocalDate hasta = hoy.plusDays(diasAnticipoEventos);
        List<Evento> eventos = eventoDAO.findAll();
        eventoDAO.close();
        for (Evento evento : eventos) {
            LocalDate fecha = evento.getFecha_evento();
            if (fecha != null && !fecha.isBefore(hoy) && !fecha.isAfter(hasta)) {
                long diasRestantes = fecha.toEpochDay() - hoy.toEpochDay();
                String diasTexto = diasRestantes == 1 ? "día" : "días";
                String mensaje = String.format("En %d %s tienes un evento: '%s'",
                        diasRestantes, diasTexto, evento.getNombre_evento());
                notificaciones.add(new Notificacion(mensaje, evento.getNombre_evento(), fecha));
            }
        }
        // --- Notificación de tareas pendientes en la agenda para la semana en curso ---
        // Calcular inicio (lunes) y fin (domingo) de la semana actual
        LocalDate inicioSemana = hoy.with(DayOfWeek.MONDAY);
        LocalDate finSemana = hoy.with(DayOfWeek.SUNDAY);
        AgendaDAO agendaDAO = new AgendaDAO();
        List<Agenda> tareasSemana = agendaDAO.findByWeek(inicioSemana, finSemana);
        agendaDAO.close();
        boolean hayPendientes = tareasSemana.stream().anyMatch(a -> a.getEstado() == null || a.getEstado().equalsIgnoreCase("Pendiente"));
        if (hayPendientes) {
            notificaciones.add(new Notificacion("Hay tareas pendientes para esta semana", "Agenda", hoy));
        }
        return notificaciones;
    }
}
