package model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "notificaciones")
public class NotificacionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String mensaje;

    private String tipo;

    private LocalDate fecha;

    private boolean leida;

    public NotificacionEntity() {
    }

    public NotificacionEntity(String mensaje, String tipo, LocalDate fecha, boolean leida) {
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.fecha = fecha;
        this.leida = leida;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    @Override
    public String toString() {
        return "NotificacionEntity{" +
                "id=" + id +
                ", mensaje='" + mensaje + '\'' +
                ", tipo='" + tipo + '\'' +
                ", fecha=" + fecha +
                ", leida=" + leida +
                '}';
    }
}
