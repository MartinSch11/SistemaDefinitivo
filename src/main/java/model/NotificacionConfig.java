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

    public NotificacionConfig() {
        // Constructor vacío requerido por JPA
    }

    public NotificacionConfig(int minutos, int diasAnticipacion) {
        this.minutos = minutos;
        this.diasAnticipacion = diasAnticipacion;
    }

    public int getMinutos() { return minutos; }
    public void setMinutos(int minutos) { this.minutos = minutos; }

    public int getDiasAnticipacion() { return diasAnticipacion; }
    public void setDiasAnticipacion(int diasAnticipacion) { this.diasAnticipacion = diasAnticipacion; }
}