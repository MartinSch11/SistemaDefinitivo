package model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "historial_compra")
public class HistorialCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "insumo", nullable = false, length = 100)
    private String insumo;

    @Column(name = "cantidad", nullable = false)
    private double cantidad;

    @Column(name = "medida", nullable = false, length = 20)
    private String medida;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDate fechaCompra;

    @Column(name = "proveedor", nullable = false, length = 100)
    private String proveedor;

    @Column(name = "precio", nullable = false)
    private double precio;

    public HistorialCompra() {
    }

    public HistorialCompra(String insumo, double cantidad, String medida, LocalDate fechaCompra, String proveedor,
                           double precio) {
        this.insumo = insumo;
        this.cantidad = cantidad;
        this.medida = medida;
        this.fechaCompra = fechaCompra;
        this.proveedor = proveedor;
        this.precio = precio;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInsumo() {
        return insumo;
    }

    public void setInsumo(String insumo) {
        this.insumo = insumo;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public String getMedida() {
        return medida;
    }

    public void setMedida(String medida) {
        this.medida = medida;
    }

    public LocalDate getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDate fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }
}
