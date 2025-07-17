package model;

import java.time.LocalDate;

public class EgresoDetallado {
    private LocalDate fechaCompra;
    private String insumo;
    private double cantidad;
    private String medida;
    private String proveedor;
    private double precio;

    public EgresoDetallado(LocalDate fechaCompra, String insumo, double cantidad, String medida, String proveedor, double precio) {
        this.fechaCompra = fechaCompra;
        this.insumo = insumo;
        this.cantidad = cantidad;
        this.medida = medida;
        this.proveedor = proveedor;
        this.precio = precio;
    }

    public LocalDate getFechaCompra() { return fechaCompra; }
    public String getInsumo() { return insumo; }
    public double getCantidad() { return cantidad; }
    public String getMedida() { return medida; }
    public String getProveedor() { return proveedor; }
    public double getPrecio() { return precio; }
    public double getTotalEgreso() { return precio; }
}
