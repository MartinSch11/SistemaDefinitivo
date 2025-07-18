package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
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

    public Evento() {}

    @Override
    public String toString() {
        return nombre_evento;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getNombre_evento() {
        return nombre_evento;
    }
    public void setNombre_evento(String nombre_evento) {
        this.nombre_evento = nombre_evento;
    }
    public String getDescripcion_evento() {
        return descripcion_evento;
    }
    public void setDescripcion_evento(String descripcion_evento) {
        this.descripcion_evento = descripcion_evento;
    }
    public String getNombre_cliente() {
        return nombre_cliente;
    }
    public void setNombre_cliente(String nombre_cliente) {
        this.nombre_cliente = nombre_cliente;
    }
    public String getTelefono_cliente() {
        return telefono_cliente;
    }
    public void setTelefono_cliente(String telefono_cliente) {
        this.telefono_cliente = telefono_cliente;
    }
    public String getDireccion_evento() {
        return direccion_evento;
    }
    public void setDireccion_evento(String direccion_evento) {
        this.direccion_evento = direccion_evento;
    }
    public int getCant_personas() {
        return cant_personas;
    }
    public void setCant_personas(int cant_personas) {
        this.cant_personas = cant_personas;
    }
    public BigDecimal getPresupuesto() {
        return presupuesto;
    }
    public void setPresupuesto(BigDecimal presupuesto) {
        this.presupuesto = presupuesto;
    }
    public LocalDate getFecha_evento() {
        return fecha_evento;
    }
    public void setFecha_evento(LocalDate fecha_evento) {
        this.fecha_evento = fecha_evento;
    }
    public String getEstado() {
        return estado;
    }
    public void setEstado(String estado) {
        this.estado = estado;
    }
}
