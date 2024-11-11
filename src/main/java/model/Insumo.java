package model;

import jakarta.persistence.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private int id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "cantidad")
    private int cantidad;

    @Column(name = "lote")
    private String lote;

    @Column(name = "fecha_caducidad")
    private LocalDate fechaCaducidad;

    @ManyToOne
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;

    // Propiedad SimpleStringProperty para la propiedad 'nombre' en JavaFX
    @Transient
    private SimpleStringProperty nombreProperty;

    // Constructor adicional con los parámetros utilizados en el controlador
    public Insumo(String nombre, int cantidad, String lote, LocalDate fechaCaducidad, Proveedor proveedor) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.lote = lote;
        this.fechaCaducidad = fechaCaducidad;
        this.proveedor = proveedor;
        this.nombreProperty = new SimpleStringProperty(nombre);  // Inicializamos la propiedad SimpleStringProperty
    }

    // Método para reducir la cantidad disponible
    public void reducirCantidad(double cantidadUtilizada) {
        if (this.cantidad >= cantidadUtilizada) {
            this.cantidad -= cantidadUtilizada;
        } else {
            throw new IllegalArgumentException("No hay suficiente cantidad disponible para el insumo: " + nombre);
        }
    }

    // Getter para nombreProperty
    public StringProperty nombreProperty() {
        if (nombreProperty == null) {
            nombreProperty = new SimpleStringProperty(nombre);  // Si no está inicializada, la inicializamos
        }
        return nombreProperty;
    }

    // Setter para nombreProperty
    public void setNombre(String nombre) {
        this.nombre = nombre;
        if (nombreProperty != null) {
            this.nombreProperty.set(nombre);  // Actualizar el valor de nombreProperty
        }
    }

    @Override
    public String toString() {
        return nombre;  // Devolver solo el nombre como texto en el ComboBox y en otros lugares
    }

    public String getNombre() {
        return nombre;  // Asumiendo que tienes un campo 'nombre' en la clase Insumo
    }


}
