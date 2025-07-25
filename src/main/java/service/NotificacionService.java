package service;

import model.Evento;
import model.Notificacion;
import model.Pedido;
import persistence.dao.EventoDAO;
import persistence.dao.PedidoDAO;
import persistence.dao.InsumoDAO;
import model.Insumo;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NotificacionService {
    /**
     * Busca eventos próximos y genera notificaciones.
     * También agrega notificación de pedidos pendientes para hoy y de insumos
     * próximos a vencer.
     * 
     * @param diasAnticipoEventos   cantidad de días a futuro para buscar eventos
     * @param diasAnticipoCaducidad cantidad de días a futuro para notificar insumos
     *                              por caducar
     * @return lista de notificaciones
     */
    public List<Notificacion> obtenerNotificacionesEventosYCaducidad(int diasAnticipoEventos,
            int diasAnticipoCaducidad) {
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
                    String nombre = insumo.getCatalogoInsumo() != null ? insumo.getCatalogoInsumo().getNombre()
                            : "Insumo";
                    String diasTexto = diasRestantes == 1 ? "día" : "días";
                    String mensaje = diasRestantes == 0
                            ? String.format("El insumo '%s' vence hoy", nombre)
                            : String.format("El insumo '%s' vence en %d %s", nombre, diasRestantes, diasTexto);
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
                if (diasRestantes == 0) {
                    // Si el evento es hoy, mostrar solo si la hora actual es menor al horario del
                    // evento
                    if (evento.getHorario_evento() != null
                            && java.time.LocalTime.now().isBefore(evento.getHorario_evento())) {
                        String mensaje = String.format("Hoy tienes un evento: '%s' a las %s",
                                evento.getNombre_evento(), evento.getHorario_evento().toString());
                        notificaciones.add(new Notificacion(mensaje, evento.getNombre_evento(), fecha));
                    }
                    continue;
                }
                String diasTexto = diasRestantes == 1 ? "día" : "días";
                String mensaje = String.format("En %d %s tienes un evento: '%s'",
                        diasRestantes, diasTexto, evento.getNombre_evento());
                notificaciones.add(new Notificacion(mensaje, evento.getNombre_evento(), fecha));
            }
        }
        return notificaciones;
    }
}
