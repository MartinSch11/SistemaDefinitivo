package model;

import java.sql.Timestamp;
import java.time.LocalDate;

import lombok.*;

@Data
public class Trabajador {
    private String dni;
    private int edad;
    private String direccion;
    private String telefono;
    private LocalDate fechaContratacion;
    private Timestamp ultimaActividad;
    private int idRol;
}
