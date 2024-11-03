package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@Table(name = "Trabajadores")
public class Trabajador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRol")
    private int idRol;

    @Column(name = "dni", nullable = false)
    private String dni;
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "direccion")
    private String direccion;
    @Column(name = "telefono")
    private String telefono;
    @Column(name = "sueldo")
    private BigDecimal sueldo;
    @Column(name = "fechaContratacion")
    private LocalDate fechaContratacion;
    @Column(name = "ultimaActividad", insertable = false, updatable = false)
    private Timestamp ultimaActividad;


    // Constructor con todos los campos
    public Trabajador(String dni, String nombre, String direccion, String telefono, BigDecimal sueldo, LocalDate fechaContratacion, Timestamp ultimaActividad) {
        this.dni = dni;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.sueldo = sueldo;
        this.fechaContratacion = fechaContratacion;
        this.ultimaActividad = ultimaActividad;

    }

    //public Trabajador(String dniEmpleado, String nombreEmpleado, String direccionEmpleado, String telefonoEmpleado, BigDecimal sueldoEmpleado, LocalDate fechaContratoEmpleado, java.security.Timestamp ultActividadEmpleado) {
    //}
}
