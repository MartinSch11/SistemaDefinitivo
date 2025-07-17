package model;

import java.time.LocalDate;

public class Transaccion {
    private LocalDate fecha;
    private String tipo; // "Ingreso" o "Egreso"
    private double monto;

    public Transaccion(LocalDate fecha, String tipo, double monto) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.monto = monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public double getMonto() {
        return monto;
    }
}