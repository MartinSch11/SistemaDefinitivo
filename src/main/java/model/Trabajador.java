package model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "Trabajadores")
public class Trabajador {
    @Id
    private String dni;

    private int edad;
    private String direccion;
    private String telefono;
    private LocalDate fechaContratacion;
    private Timestamp ultimaActividad;
    private int idRol;

    // Constructor por defecto
    public Trabajador() {}

    // Constructor con todos los campos
    public Trabajador(String dni, int edad, String direccion, String telefono, LocalDate fechaContratacion, Timestamp ultimaActividad, int idRol) {
        this.dni = dni;
        this.edad = edad;
        this.direccion = direccion;
        this.telefono = telefono;
        this.fechaContratacion = fechaContratacion;
        this.ultimaActividad = ultimaActividad;
        this.idRol = idRol;
    }
}
