package model;

import java.time.LocalDate;

public class Notificacion {
    private final String mensaje;
    private final String nombreEvento;
    private final LocalDate fechaEvento;

    public Notificacion(String mensaje, String nombreEvento, LocalDate fechaEvento) {
        this.mensaje = mensaje;
        this.nombreEvento = nombreEvento;
        this.fechaEvento = fechaEvento;
    }
    public String getMensaje() { return mensaje; }
    public String getNombreEvento() { return nombreEvento; }
    public LocalDate getFechaEvento() { return fechaEvento; }
}
