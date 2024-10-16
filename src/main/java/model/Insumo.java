package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "insumos")
public class Insumo{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_insumo")
    private int id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "cantidad")
    private double cantidad;

    @ManyToOne
    @JoinColumn(name = "id_medida")  // Relación Many-to-One con la tabla de medidas
    private Medida medida;

    @Column(name = "lote")
    private String lote;

    @Column(name = "fecha_caducidad")
    private LocalDate fechaCaducidad;

    @ManyToOne
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;

    // Constructor adicional con los parámetros utilizados en el controlador
    public Insumo(String nombre, double cantidad, String lote, LocalDate fechaCaducidad, Medida medida, Proveedor proveedor) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.lote = lote;
        this.fechaCaducidad = fechaCaducidad;
        this.medida = medida;
        this.proveedor = proveedor;
    }

    // Método para reducir la cantidad disponible
    public void reducirCantidad(double cantidadUtilizada) {
        if (this.cantidad >= cantidadUtilizada) {
            this.cantidad -= cantidadUtilizada;
        } else {
            throw new IllegalArgumentException("No hay suficiente cantidad disponible para el insumo: " + nombre);
        }
    }

    @Override
    public String toString() {
        return nombre;
    }
}
