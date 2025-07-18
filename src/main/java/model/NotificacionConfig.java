package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "config_notificaciones")
public class NotificacionConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "minutos", nullable = false)
    private int minutos;

    @Column(name = "dias_anticipacion", nullable = false)
    private int diasAnticipacion;

    @Column(name = "notificaciones_activas", nullable = false)
    private boolean notificacionesActivas = true;

    @Column(name = "duracion_segundos", nullable = false)
    private int duracionSegundos = 5;

    @Column(name = "dias_anticipacion_caducidad", nullable = false)
    private int diasAnticipacionCaducidad = 3;

    public NotificacionConfig() {
        // Constructor vacío requerido por JPA
    }

    public NotificacionConfig(int minutos, int diasAnticipacion) {
        this.minutos = minutos;
        this.diasAnticipacion = diasAnticipacion;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public int getMinutos() { return minutos; }
    public void setMinutos(int minutos) { this.minutos = minutos; }

    public int getDiasAnticipacion() { return diasAnticipacion; }
    public void setDiasAnticipacion(int diasAnticipacion) { this.diasAnticipacion = diasAnticipacion; }

    public boolean isNotificacionesActivas() { return notificacionesActivas; }
    public void setNotificacionesActivas(boolean notificacionesActivas) { this.notificacionesActivas = notificacionesActivas; }

    public int getDuracionSegundos() { return duracionSegundos; }
    public void setDuracionSegundos(int duracionSegundos) { this.duracionSegundos = duracionSegundos; }

    public int getDiasAnticipacionCaducidad() { return diasAnticipacionCaducidad; }
    public void setDiasAnticipacionCaducidad(int diasAnticipacionCaducidad) { this.diasAnticipacionCaducidad = diasAnticipacionCaducidad; }
}