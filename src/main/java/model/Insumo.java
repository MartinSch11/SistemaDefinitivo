package model;

import jakarta.persistence.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "insumos")
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_insumo")
    private Integer id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "cantidad")
    private double cantidad;

    @Column(name = "fecha_caducidad")
    private LocalDate fechaCaducidad;

    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @Column(name = "medida")
    private String medida;

    @Column(name = "precio")
    private double precio;

    @ManyToOne
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;

    @Transient
    private SimpleStringProperty nombreProperty;

    public Insumo(String nombre, int cantidad, double precio, String medida, LocalDate fechaCompra, LocalDate fechaCaducidad, Proveedor proveedor) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precio = precio;
        this.medida = medida;
        this.fechaCompra = fechaCompra;
        this.fechaCaducidad = fechaCaducidad;
        this.proveedor = proveedor;
        this.nombreProperty = new SimpleStringProperty(nombre);
    }

    public Insumo(String nombre, int cantidad, double precio, String medida, LocalDate fechaCompra, LocalDate fechaCaducidad) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precio = precio;
        this.medida = medida;
        this.fechaCompra = fechaCompra;
        this.fechaCaducidad = fechaCaducidad;
        this.nombreProperty = new SimpleStringProperty(nombre);
    }

    // Getter para nombreProperty
    public StringProperty nombreProperty() {
        if (nombreProperty == null) {
            nombreProperty = new SimpleStringProperty(nombre);
        }
        return nombreProperty;
    }

    // Setter para nombreProperty
    public void setNombre(String nombre) {
        this.nombre = nombre;
        if (nombreProperty != null) {
            this.nombreProperty.set(nombre);
        }
    }

    public double getCantidad() {
        return this.cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    @Override
    public String toString() {
        return nombre;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public String getNombreProveedor() {
        return proveedor != null ? proveedor.getNombre() : "Sin proveedor";
    }

    public String getNombre() {
        return nombre;
    }

    public void reducirCantidad(double cantidadUtilizada, String unidadUtilizada) {
        double cantidadConvertida = convertirUnidad(cantidadUtilizada, unidadUtilizada, this.medida);

        if (this.cantidad >= cantidadConvertida) {
            this.cantidad -= cantidadConvertida;

            // Redondear a 2 decimales para evitar infinitos flotantes
            this.cantidad = BigDecimal.valueOf(this.cantidad)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        } else {
            throw new IllegalArgumentException("No hay suficiente cantidad disponible para el insumo: " + nombre);
        }
    }


    public double getCantidadDisponible() {
        return this.cantidad; // Devuelve la cantidad actual como double
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Insumo insumo = (Insumo) o;
        return nombre != null && nombre.equals(insumo.nombre);
    }

    @Override
    public int hashCode() {
        return nombre != null ? nombre.hashCode() : 0;
    }

    public double convertirUnidad(double cantidad, String unidadDesde, String unidadHacia) {
        if (unidadDesde == null || unidadHacia == null) {
            throw new IllegalArgumentException("Las unidades de medida no pueden ser nulas");
        }

        String de = unidadDesde.toUpperCase();
        String a = unidadHacia.toUpperCase();

        if (de.equals(a)) return cantidad; // misma unidad, no conversion

        switch (de) {
            case "KG": if (a.equals("GR")) return cantidad * 1000; break;
            case "GR": if (a.equals("KG")) return cantidad / 1000; break;
            case "L":  if (a.equals("ML")) return cantidad * 1000; break;
            case "ML": if (a.equals("L"))  return cantidad / 1000; break;
        }

        throw new IllegalArgumentException("No se puede convertir de " + unidadDesde + " a " + unidadHacia);
    }

}