package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;  // Cambiado a BigDecimal
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    private String nombre;
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    private BigDecimal precio;

    @ManyToMany
    @JoinTable(
            name = "Producto_Sabor",
            joinColumns = @JoinColumn(name = "id_producto"),
            inverseJoinColumns = @JoinColumn(name = "id_sabor")
    )
    private List<Sabor> sabores = new ArrayList<>();

    @Lob
    @Column(name = "imagen")
    private byte[] imagen;

    public Producto(String nombre, String descripcion, Categoria categoria, BigDecimal precio, byte[] imagen) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
        this.imagen = imagen;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
