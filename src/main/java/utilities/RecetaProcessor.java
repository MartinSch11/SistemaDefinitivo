package utilities;

import model.*;
import persistence.dao.InsumoDAO;
import java.util.Map;

public class RecetaProcessor {

    InsumoDAO insumoDAO = new InsumoDAO();

    /**
     * Procesa las recetas de los productos seleccionados y calcula los insumos requeridos.
     *
     * @param productos Mapa de productos y sus cantidades.
     */
    public void procesarRecetas(Map<Producto, Integer> productos) {
        // Primero validamos si hay suficientes insumos
        if (!validarInsumosSuficientes(productos)) {
            System.out.println("No hay suficientes insumos para procesar el pedido.");
            return; // Si no hay insumos suficientes, no procesamos más
        }

        // Si la validación pasa, procesamos las recetas
        for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
            Producto producto = entry.getKey();
            int cantidadProducto = entry.getValue();
            Receta receta = producto.getReceta();

            if (receta != null) {
                for (InsumoReceta insumoReceta : receta.getInsumosReceta()) {
                    Insumo insumo = insumoReceta.getInsumo();
                    double cantidadRequerida = insumoReceta.getCantidadUtilizada() * cantidadProducto;

                    // Convertir la cantidad requerida de la receta a la unidad del insumo en el stock
                    double cantidadRequeridaConvertida = insumo.convertirUnidad(cantidadRequerida, insumoReceta.getUnidad(), insumo.getMedida());

                    // Validar si hay suficiente insumo disponible
                    if (insumo.getCantidad() < cantidadRequeridaConvertida) {
                        System.out.println("Insumo insuficiente: " + insumo.getNombre() +
                                ". Requerido: " + cantidadRequeridaConvertida +
                                ", disponible: " + insumo.getCantidad());
                    } else {
                        // Registrar el uso del insumo
                        insumo.reducirCantidad(cantidadRequeridaConvertida, insumoReceta.getUnidad());
                        insumoDAO.update(insumo); // Actualizar el insumo en la base de datos
                        System.out.println("Usado " + cantidadRequeridaConvertida + " de " + insumo.getNombre() +
                                ". Restante: " + insumo.getCantidad());
                    }
                }
            }
        }
    }


    private boolean validarInsumosSuficientes(Map<Producto, Integer> productos) {
        // Validación de insumos antes de procesar recetas
        for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
            Producto producto = entry.getKey();
            int cantidadProducto = entry.getValue();
            Receta receta = producto.getReceta();

            if (receta != null) {
                for (InsumoReceta insumoReceta : receta.getInsumosReceta()) {
                    Insumo insumo = insumoReceta.getInsumo();
                    double cantidadRequerida = insumoReceta.getCantidadUtilizada() * cantidadProducto;

                    if (insumo.getCantidad() < cantidadRequerida) {
                        System.out.println("Insumo insuficiente: " + insumo.getNombre() +
                                ". Requerido: " + cantidadRequerida +
                                ", disponible: " + insumo.getCantidad());
                        return false; // Si hay algún insumo insuficiente, la validación falla
                    }
                }
            }
        }
        return true; // Todos los insumos son suficientes
    }



}
