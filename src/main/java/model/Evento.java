package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "nombre_evento")
    private String nombre_evento;
    @Lob
    @Column(name = "descripcion_evento", columnDefinition = "TEXT")
    private String descripcion_evento;
    @Column(name = "nombre_cliente")
    private String nombre_cliente;
    @Column(name = "telefono_cliente")
    private String telefono_cliente;
    @Column(name = "direccion_evento")
    private String direccion_evento;
    @Column(name = "cant_personas")
    private int cant_personas;
    @Column(name = "presupuesto", precision = 10, scale = 2)
    private BigDecimal presupuesto;
    @Column(name = "fecha_evento")
    private LocalDate fecha_evento;
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "Agendado";

    public Evento(String nombre_evento, String descripcion_evento, String nombre_cliente, String telefono_cliente,
                  String direccion_evento, LocalDate fecha_evento, int cant_personas, BigDecimal presupuesto) {
        this.nombre_evento = nombre_evento;
        this.descripcion_evento = descripcion_evento;
        this.nombre_cliente = nombre_cliente;
        this.telefono_cliente = telefono_cliente;
        this.direccion_evento = direccion_evento;
        this.fecha_evento = fecha_evento;
        this.cant_personas = cant_personas;
        this.presupuesto = presupuesto;
        this.estado = "Agendado";
    }

    @Override
    public String toString() {
        return nombre_evento;
    }
}
