package model;

import lombok.Data;

@Data
public class Producto {
    private int id;
    private String nombre;
    private float precio;
    private String descripcion;
}
