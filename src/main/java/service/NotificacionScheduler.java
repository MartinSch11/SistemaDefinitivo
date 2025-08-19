package service;

import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;
import model.Evento;
import model.NotificacionConfig;
import persistence.dao.EventoDAO;
import persistence.dao.NotificacionConfigDAO;
import persistence.dao.NotificacionEntityDAO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class NotificacionScheduler {

    private static final NotificacionScheduler INSTANCE = new NotificacionScheduler();

    // ✅ Dos servicios: uno rápido para notificaciones, otro más espaciado para estados
    private final ScheduledService<Void> notificacionesService;
    private final ScheduledService<Void> estadosService;

    // 🔔 sonido y conteo previo
    private final NotificacionEntityDAO notiDAO = new NotificacionEntityDAO();
    private final AudioClip notificationClip;
    private volatile int lastUnreadCount = -1;

    private NotificacionScheduler() {
        // === inicializar clip (poné tu ruta real del recurso) ===
        AudioClip tmpClip = null;
        try {
            String url = Objects.requireNonNull(
                    getClass().getResource("/com/example/sound/notify.mp3"),
                    "No se encontró /com/example/sound/notify.mp3 en resources"
            ).toExternalForm();
            tmpClip = new AudioClip(url);
            tmpClip.setVolume(0.6); // volumen inicial (0.0 a 1.0)
        } catch (Exception ex) {
            System.err.println("No se pudo cargar el sonido de notificación: " + ex.getMessage());
        }
        notificationClip = tmpClip;

        // ---- Servicio de NOTIFICACIONES (prioritario) ----
        notificacionesService = new ScheduledService<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        // Generar/persistir notificaciones (idealmente idempotente)
                        NotificacionConfig cfg = new NotificacionConfigDAO().findOrDefault();
                        new NotificacionService().obtenerTodasLasNotificaciones(
                                cfg.getDiasAnticipacion(),
                                cfg.getDiasAnticipacionCaducidad(),
                                cfg.getDiasAnticipacionPedidos()
                        );

                        // Comprobar no leídas y sonar si aumentó
                        int unread = 0;
                        try {
                            unread = notiDAO.findNoLeidas().size();
                        } catch (Exception ignored) { }

                        // 👉 emitir el valor actual al bus (para refrescar el badge en caliente)
                        final int count = unread;
                        Platform.runLater(() ->
                                NotificacionCenter.getInstance().setUnreadCount(count)
                        );

                        if (lastUnreadCount < 0) {
                            // primera corrida: baseline, NO sonar
                            lastUnreadCount = unread;
                        } else if (unread > lastUnreadCount) {
                            // llegaron nuevas no leídas -> sonar
                            if (notificationClip != null) {
                                Platform.runLater(notificationClip::play);
                            }
                            lastUnreadCount = unread;
                        } else {
                            lastUnreadCount = unread; // mantener baseline al día
                        }

                        return null;
                    }
                };
            }
        };
        // ⏱️ Corre muy seguido (ajustable)
        notificacionesService.setPeriod(Duration.seconds(5)); // p.ej., cada 5s
        notificacionesService.setDelay(Duration.seconds(1));
        notificacionesService.setRestartOnFailure(true);

        // ---- Servicio de ESTADOS (menos prioritario) ----
        estadosService = new ScheduledService<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        actualizarEstadosEventosSegunFechaYHora();
                        return null;
                    }
                };
            }
        };
        // ⏱️ Corre con menor frecuencia (ajustable)
        estadosService.setPeriod(Duration.seconds(60)); // p.ej., cada 60s
        estadosService.setDelay(Duration.seconds(5));
        estadosService.setRestartOnFailure(true);
    }

    public static NotificacionScheduler getInstance() {
        return INSTANCE;
    }

    public void start() {
        // baseline del contador para evitar sonido inicial y sincronizar el bus
        int initial = 0;
        try {
            initial = notiDAO.findNoLeidas().size();
        } catch (Exception ignored) { }
        lastUnreadCount = initial;
        final int count = initial;
        Platform.runLater(() ->
                NotificacionCenter.getInstance().setUnreadCount(count)
        );

        switch (notificacionesService.getState()) {
            case READY, SUCCEEDED, CANCELLED, FAILED -> notificacionesService.restart();
            case SCHEDULED, RUNNING -> { /* ya corriendo */ }
        }
        switch (estadosService.getState()) {
            case READY, SUCCEEDED, CANCELLED, FAILED -> estadosService.restart();
            case SCHEDULED, RUNNING -> { /* ya corriendo */ }
        }
    }

    public void stop() {
        notificacionesService.cancel();
        estadosService.cancel();
    }

    private void actualizarEstadosEventosSegunFechaYHora() {
        EventoDAO eventoDAO = new EventoDAO();
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        for (Evento evento : eventoDAO.findAll()) {
            String estado = evento.getEstado();
            if ("Realizado".equalsIgnoreCase(estado)) continue;

            LocalDate fecha = evento.getFecha_evento();
            LocalTime hora = null;
            try {
                // ⚠️ Ajustá el getter si tu entidad usa otro nombre
                hora = evento.getHorario_evento();
            } catch (Exception ignored) { }

            boolean marcar = false;
            if (fecha != null) {
                if (fecha.isBefore(hoy)) {
                    marcar = true;
                } else if (fecha.isEqual(hoy) && hora != null && !hora.isAfter(ahora)) {
                    marcar = true;
                }
            }
            if (marcar) {
                evento.setEstado("Realizado");
                eventoDAO.update(evento);
            }
        }
    }

    // (Opcional) cambiar volumen en runtime
    public void setNotificationVolume(double volume0to1) {
        if (notificationClip != null) {
            notificationClip.setVolume(Math.max(0, Math.min(1, volume0to1)));
        }
    }
}
