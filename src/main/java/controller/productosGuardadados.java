package controller;

import model.Producto;

import java.util.HashMap;
import java.util.Map;

public class productosGuardadados {
    private static Map<Producto, Integer> productos = new HashMap<>();

    public static void agregarProducto(Producto producto, int cantidad) {
        productos.put(producto, cantidad);
    }

    public static Map<Producto, Integer> getProductos() {
        return new HashMap<>(productos);
    }

    public static void limpiarProductos() {
        productos.clear();
    }
}

