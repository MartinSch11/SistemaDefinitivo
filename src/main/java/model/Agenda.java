package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Time;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
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

    /*@Override
    public String toString() {
        return nombre_evento;
    }*/

}

