package service;

import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
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

    // ✅ Dos servicios
    private final ScheduledService<Void> notificacionesService;
    private final ScheduledService<Void> estadosService;

    // 🔔 sonido y conteo previo
    private final NotificacionEntityDAO notiDAO = new NotificacionEntityDAO();
    
    // CAMBIO: Usamos MediaPlayer en lugar de AudioClip
    private MediaPlayer notificationPlayer; 
    
    private volatile int lastUnreadCount = -1;

    private NotificacionScheduler() {
        // === inicializar sonido con MediaPlayer ===
        try {
            String url = Objects.requireNonNull(
                    getClass().getResource("/com/example/sound/notify.mp3"),
                    "No se encontró /com/example/sound/notify.mp3 en resources"
            ).toExternalForm();
            
            // CAMBIO: Lógica de Media y MediaPlayer
            Media sound = new Media(url);
            notificationPlayer = new MediaPlayer(sound);
            notificationPlayer.setVolume(0.6); 
            
        } catch (Exception ex) {
            System.err.println("No se pudo cargar el sonido de notificación: " + ex.getMessage());
            notificationPlayer = null;
        }

        // ---- Servicio de NOTIFICACIONES ----
        notificacionesService = new ScheduledService<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        // Generar/persistir notificaciones
                        NotificacionConfig cfg = new NotificacionConfigDAO().findOrDefault();
                        new NotificacionService().obtenerTodasLasNotificaciones(
                                cfg.getDiasAnticipacion(),
                                cfg.getDiasAnticipacionCaducidad(),
                                cfg.getDiasAnticipacionPedidos()
                        );

                        // Comprobar no leídas
                        int unread = 0;
                        try {
                            unread = notiDAO.findNoLeidas().size();
                        } catch (Exception ignored) { }

                        final int count = unread;
                        Platform.runLater(() ->
                                NotificacionCenter.getInstance().setUnreadCount(count)
                        );

                        if (lastUnreadCount < 0) {
                            lastUnreadCount = unread;
                        } else if (unread > lastUnreadCount) {
                            // CAMBIO: Reproducir con MediaPlayer
                            if (notificationPlayer != null) {
                                Platform.runLater(() -> {
                                    // MediaPlayer necesita detenerse y rebobinarse antes de volver a sonar
                                    notificationPlayer.stop(); 
                                    notificationPlayer.play();
                                });
                            }
                            lastUnreadCount = unread;
                        } else {
                            lastUnreadCount = unread;
                        }

                        return null;
                    }
                };
            }
        };
        notificacionesService.setPeriod(Duration.seconds(5));
        notificacionesService.setDelay(Duration.seconds(1));
        notificacionesService.setRestartOnFailure(true);

        // ---- Servicio de ESTADOS ----
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
        estadosService.setPeriod(Duration.seconds(60));
        estadosService.setDelay(Duration.seconds(5));
        estadosService.setRestartOnFailure(true);
    }

    public static NotificacionScheduler getInstance() {
        return INSTANCE;
    }

    public void start() {
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
            case SCHEDULED, RUNNING -> { }
        }
        switch (estadosService.getState()) {
            case READY, SUCCEEDED, CANCELLED, FAILED -> estadosService.restart();
            case SCHEDULED, RUNNING -> { }
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

    // CAMBIO: Ajustar volumen en MediaPlayer
    public void setNotificationVolume(double volume0to1) {
        if (notificationPlayer != null) {
            notificationPlayer.setVolume(Math.max(0, Math.min(1, volume0to1)));
        }
    }
}