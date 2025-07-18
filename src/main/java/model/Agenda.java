package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.sql.Time;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@Entity
@Table(name = "agendas")
public class Agenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "pendiente")
    private String pendiente;
    @Column(name = "fecha_pendiente")
    //@Temporal(TemporalType.DATE)
    private LocalDate fecha_pendiente;
    @Column(name = "hora")
    private Time hora;
    @Column(name = "estado")
    private String estado;
    @Column(name = "idEmpleado")
    private Integer idEmpleado;

    public Agenda(String pendiente, LocalDate fecha_pendiente, Time hora, String estado, Integer idEmpleado) {
        this.pendiente = pendiente;
        this.fecha_pendiente = fecha_pendiente;
        this.hora = hora;
        this.estado = estado;
        this.idEmpleado = idEmpleado;
    }

    public Agenda() {}

    public Integer getIdTrabajador() {
        return idEmpleado;
    }

    public LocalDate getFecha() {
        return fecha_pendiente;
    }

    public String getDescripcion() {
        return pendiente;
    }

    public Time getHora() {
        return hora;
    }

    /*@Override
    public String toString() {
        return nombre_evento;
    }*/

}

