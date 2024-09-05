package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Producto {
    private String nombre;
    private String descripcion;
    private Categoria categoria;
    private float precio;

    private List<Sabor> sabores;  // Lista de sabores

    // Constructor que no incluye sabores
    public Producto(String nombre, String descripcion, Categoria categoria, float precio) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
        this.sabores = new ArrayList<>();  // Inicializar la lista de sabores vacía
    }

    // Otros getters y setters para los atributos
    // ...

    @Override
    public String toString() {
        return nombre;
    }
}
