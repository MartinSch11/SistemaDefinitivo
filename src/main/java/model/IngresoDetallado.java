package model;

import java.time.LocalDate;

public class IngresoDetallado {
    private LocalDate fecha;
    private int numeroPedido;
    private String cliente;
    private String producto;
    private int cantidad;
    private double precioUnitario;
    private double totalPedido;

    public IngresoDetallado(LocalDate fecha, int numeroPedido, String cliente, String producto, int cantidad, double precioUnitario, double totalPedido) {
        this.fecha = fecha;
        this.numeroPedido = numeroPedido;
        this.cliente = cliente;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.totalPedido = totalPedido;
    }

    public LocalDate getFecha() { return fecha; }
    public int getNumeroPedido() { return numeroPedido; }
    public String getCliente() { return cliente; }
    public String getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public double getTotalPedido() { return totalPedido; }
}
