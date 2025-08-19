package service;

import model.Evento;
import model.Notificacion;
import model.NotificacionEntity;
import model.Pedido;
import model.Insumo;
import model.Agenda;
import persistence.dao.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class NotificacionService {

    private final NotificacionEntityDAO entityDAO = new NotificacionEntityDAO();

    /** Nuevo: con anticipación para PEDIDOS */
    public List<Notificacion> obtenerTodasLasNotificaciones(
            int diasAnticipoEventos,
            int diasAnticipoCaducidad,
            int diasAnticipoPedidos
    ) {
        List<Notificacion> notificaciones = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        // --- PEDIDOS (hoy y próximos dentro de la ventana) ---
        PedidoDAO pedidoDAO = new PedidoDAO();
        List<Pedido> pedidos = pedidoDAO.findAll();

        LocalDate limitePedidos = hoy.plusDays(diasAnticipoPedidos);

        Map<LocalDate, Long> pendientesPorDia = pedidos.stream()
                .filter(p -> {
                    LocalDate f = p.getFechaEntrega();
                    if (f == null) return false;
                    if (f.isBefore(hoy) || f.isAfter(limitePedidos)) return false;
                    String estado = p.getEstadoPedido();
                    return !(estado != null && (estado.equalsIgnoreCase("Hecho") || estado.equalsIgnoreCase("Entregado")));
                })
                .collect(Collectors.groupingBy(Pedido::getFechaEntrega, Collectors.counting()));

        for (Map.Entry<LocalDate, Long> e : pendientesPorDia.entrySet()) {
            LocalDate fecha = e.getKey();
            long cant = e.getValue();
            long diasRestantes = ChronoUnit.DAYS.between(hoy, fecha);
            String cuando = humanizaDias(diasRestantes);
            String textoPedidos = cant == 1 ? "1 pedido" : (cant + " pedidos");
            String mensaje = String.format("%s hay %s pendiente%s de entrega",
                    cuando,
                    textoPedidos,
                    cant == 1 ? "" : "s");

            guardarNotificacionSiNoExiste(mensaje, "Pedidos", hoy);
            notificaciones.add(new Notificacion(mensaje, "Pedidos", hoy));
        }

        // --- INSUMOS por caducar ---
        InsumoDAO insumoDAO = new InsumoDAO();
        List<Insumo> insumos = insumoDAO.findAll();
        LocalDate limiteCaducidad = hoy.plusDays(diasAnticipoCaducidad);

        for (Insumo insumo : insumos) {
            if (insumo.getFechaCaducidad() != null && insumo.getCantidad() > 0) {
                LocalDate caducidad = insumo.getFechaCaducidad();
                if (!caducidad.isBefore(hoy) && !caducidad.isAfter(limiteCaducidad)) {
                    long diasRestantes = ChronoUnit.DAYS.between(hoy, caducidad);
                    String nombre = insumo.getCatalogoInsumo() != null
                            ? insumo.getCatalogoInsumo().getNombre()
                            : "Insumo";
                    String mensaje = String.format(
                            "El insumo '%s' vence %s (%s)",
                            nombre,
                            humanizaDias(diasRestantes).toLowerCase(),
                            caducidad
                    );

                    guardarNotificacionSiNoExiste(mensaje, "Insumos", hoy);
                    notificaciones.add(new Notificacion(mensaje, nombre, hoy));
                }
            }
        }

        // --- EVENTOS próximos (con productos sin terminar) ---
        EventoDAO eventoDAO = new EventoDAO();
        LocalDate hastaEventos = hoy.plusDays(diasAnticipoEventos);
        List<Evento> eventos = eventoDAO.findAll();

        for (Evento evento : eventos) {
            LocalDate fecha = evento.getFecha_evento();
            if (fecha == null || fecha.isBefore(hoy) || fecha.isAfter(hastaEventos)) continue;

            Evento eventoConItems = eventoDAO.findByIdWithItems(evento.getId());
            if (eventoConItems == null) continue;

            long sinTerminar = eventoConItems.getItems() == null ? 0
                    : eventoConItems.getItems().stream().filter(it -> !it.isHecho()).count();

            long diasRestantes = ChronoUnit.DAYS.between(hoy, fecha);
            String cuando = humanizaDias(diasRestantes);

            String msg = (sinTerminar > 0)
                    ? String.format("%s hay evento: '%s' y hay %d %s por terminar",
                                    cuando,
                                    eventoConItems.getNombre_evento(),
                                    sinTerminar,
                                    plural(sinTerminar, "producto", "productos"))
                    : String.format("%s hay evento: '%s'",
                                    cuando,
                                    eventoConItems.getNombre_evento());

            guardarNotificacionSiNoExiste(msg, "Eventos", hoy);
            notificaciones.add(new Notificacion(msg, eventoConItems.getNombre_evento(), hoy));
        }

        // --- AGENDA: tareas pendientes esta semana ---
        AgendaDAO agendaDAO = new AgendaDAO();
        LocalDate inicioSemana = hoy.with(DayOfWeek.MONDAY);
        LocalDate finSemana = hoy.with(DayOfWeek.SUNDAY);
        List<Agenda> tareasSemana = agendaDAO.findByWeek(inicioSemana, finSemana);

        for (Agenda tarea : tareasSemana) {
            if (tarea.getEstado() == null || tarea.getEstado().equalsIgnoreCase("Pendiente")) {
                LocalDate fechaTarea = tarea.getFecha();
                if (fechaTarea == null) continue;
                long diasRestantes = ChronoUnit.DAYS.between(hoy, fechaTarea);
                if (diasRestantes < 0) continue;

                String cuando = humanizaDias(diasRestantes);
                String mensaje = String.format("%s tienes una tarea pendiente: '%s'",
                        cuando, tarea.getDescripcion());

                guardarNotificacionSiNoExiste(mensaje, "Agenda", hoy);
                notificaciones.add(new Notificacion(mensaje, "Agenda", hoy));
            }
        }

        return notificaciones;
    }

    /** Compatibilidad hacia atrás: delega a la versión con 3 parámetros (pedidos=1 día por defecto). */
    public List<Notificacion> obtenerTodasLasNotificaciones(int diasAnticipoEventos, int diasAnticipoCaducidad) {
        return obtenerTodasLasNotificaciones(diasAnticipoEventos, diasAnticipoCaducidad, 1);
    }

    // =================== Helpers ===================

    private void guardarNotificacionSiNoExiste(String mensaje, String tipo, LocalDate fecha) {
        boolean existe = entityDAO.existePorContenidoYFecha(mensaje, fecha);
        if (!existe) {
            NotificacionEntity entity = new NotificacionEntity(mensaje, tipo, fecha, false);
            entityDAO.save(entity);
        }
    }

    private static String plural(long n, String uno, String muchos) {
        return n == 1 ? uno : muchos;
    }

    private static String humanizaDias(long dias) {
        if (dias == 0) return "Hoy";
        if (dias == 1) return "Mañana";
        return "En " + dias + " " + plural(dias, "día", "días");
    }
}
