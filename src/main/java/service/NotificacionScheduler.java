package service;

import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import model.Evento;
import model.NotificacionConfig;
import persistence.dao.EventoDAO;
import persistence.dao.NotificacionConfigDAO;
import persistence.dao.NotificacionEntityDAO;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl; // Para el volumen
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;

public class NotificacionScheduler {

    private static final NotificacionScheduler INSTANCE = new NotificacionScheduler();

    private final ScheduledService<Void> notificacionesService;
    private final ScheduledService<Void> estadosService;

    private final NotificacionEntityDAO notiDAO = new NotificacionEntityDAO();

    // VARIABLES PARA AUDIO ESTÁNDAR (javax.sound)
    private Clip notificationClip;
    private float volumenActual = 0.8f; // Valor entre 0.0 y 1.0 (aproximado)

    private volatile int lastUnreadCount = -1;

    private NotificacionScheduler() {
        // === Inicializar sonido con Java Standard Audio (Sin JavaFX Media) ===
        cargarSonido();

        // ---- Servicio de NOTIFICACIONES ----
        notificacionesService = new ScheduledService<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        // Generar notificaciones
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
                            // Reproducir sonido
                            reproducirSonido();
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

    // Método auxiliar para cargar el sonido de forma segura
    private void cargarSonido() {
        try {
            URL url = getClass().getResource("/com/example/sound/notify.wav");
            // Java Standard Audio prefiere .wav, pero algunos JDK modernos leen mp3.
            // Si falla con mp3, te recomiendo convertir el archivo a .wav
            if (url != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                notificationClip = AudioSystem.getClip();
                notificationClip.open(audioIn);
                ajustarVolumen(volumenActual);
            } else {
                System.err.println("No se encontró el archivo de sonido.");
            }
        } catch (Exception ex) {
            System.err.println("Error cargando sonido (intenta usar .wav si falla): " + ex.getMessage());
            notificationClip = null;
        }
    }

    // Método para reproducir
    private void reproducirSonido() {
        if (notificationClip != null) {
            // Reiniciar desde el principio
            notificationClip.setFramePosition(0);
            notificationClip.start();
        }
    }

    // Método para ajustar volumen (conversión logarítmica para dB)
    private void ajustarVolumen(float volumen0to1) {
        if (notificationClip != null && notificationClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) notificationClip.getControl(FloatControl.Type.MASTER_GAIN);
            // Convertir 0-1 a decibeles. Rango aprox -80dB a 6dB
            float range = gainControl.getMaximum() - gainControl.getMinimum();
            float gain = (range * volumen0to1) + gainControl.getMinimum();
            gainControl.setValue(gain);
        }
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

    public void setNotificationVolume(double volume0to1) {
        this.volumenActual = (float) Math.max(0, Math.min(1, volume0to1));
        ajustarVolumen(this.volumenActual);
    }
}