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

    // Días de anticipación para eventos
    @Column(name = "dias_anticipacion_eventos", nullable = false, columnDefinition = "integer default 30")
    private int diasAnticipacion = 30;

    // Días de anticipación para caducidad de insumos
    @Column(name = "dias_anticipacion_caducidad", nullable = false, columnDefinition = "integer default 3")
    private int diasAnticipacionCaducidad = 3;

    // Días de anticipación para pedidos
    @Column(name = "dias_anticipacion_pedidos", nullable = false, columnDefinition = "integer default 1")
    private int diasAnticipacionPedidos = 1;

    public NotificacionConfig() {
    }

    public NotificacionConfig(int diasAnticipacion, int diasAnticipacionCaducidad, int diasAnticipacionPedidos) {
        this.diasAnticipacion = diasAnticipacion;
        this.diasAnticipacionCaducidad = diasAnticipacionCaducidad;
        this.diasAnticipacionPedidos = diasAnticipacionPedidos;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getDiasAnticipacion() {
        return diasAnticipacion;
    }

    public void setDiasAnticipacion(int diasAnticipacion) {
        this.diasAnticipacion = diasAnticipacion;
    }

    public int getDiasAnticipacionCaducidad() {
        return diasAnticipacionCaducidad;
    }

    public void setDiasAnticipacionCaducidad(int diasAnticipacionCaducidad) {
        this.diasAnticipacionCaducidad = diasAnticipacionCaducidad;
    }

    public int getDiasAnticipacionPedidos() {
        return diasAnticipacionPedidos;
    }

    public void setDiasAnticipacionPedidos(int diasAnticipacionPedidos) {
        this.diasAnticipacionPedidos = diasAnticipacionPedidos;
    }
}
