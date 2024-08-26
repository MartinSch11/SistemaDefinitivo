package model;

import java.sql.Timestamp;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Trabajador {
    private String dni;
    private int edad;
    private String direccion;
    private String telefono;
    private LocalDate fechaContratacion;
    private Timestamp ultimaActividad;
    private int idRol;
}
