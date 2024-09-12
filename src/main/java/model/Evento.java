package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Evento {
    private String nombreEvento;
    private String descripcionEvento;
    private String nombreCliente;
    private String telefonoCliente;
    private String direccionEvento;
    private LocalDate fechaEvento;  // Agregamos la fecha del evento

    // Constructor sin fecha por si no la necesitas en algunos casos
    public Evento(String nombreEvento, String descripcionEvento, String nombreCliente, String telefonoCliente, String direccionEvento) {
        this.nombreEvento = nombreEvento;
        this.descripcionEvento = descripcionEvento;
        this.nombreCliente = nombreCliente;
        this.telefonoCliente = telefonoCliente;
        this.direccionEvento = direccionEvento;
    }
}
