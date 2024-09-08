package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "productos")  // Nombre de la tabla en la base de datos
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_producto;

    private String nombre;
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "id_categoria")  // Foreign key de la tabla Categorias
    private Categoria categoria;

    private float precio;

    @ManyToMany
    @JoinTable(
            name = "Producto_Sabor",  // Nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "id_producto"),
            inverseJoinColumns = @JoinColumn(name = "id_sabor")
    )
    private List<Sabor> sabores = new ArrayList<>();  // Relación muchos a muchos

    public Producto(String nombre, String descripcion, Categoria categoria, float precio) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
