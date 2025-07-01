package utilities;

import model.*;
import persistence.dao.InsumoDAO;
import persistence.dao.InsumoFaltanteDAO;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

public class RecetaProcessor {

    private final InsumoDAO insumoDAO = new InsumoDAO();
    private final InsumoFaltanteDAO faltanteDAO = new InsumoFaltanteDAO();

    /**
     * Procesa las recetas de productos, descuenta stock y guarda insumos faltantes si no alcanza.
     * @param productos productos seleccionados con su cantidad
     * @return lista de insumos faltantes
     */
    public List<InsumoFaltante> procesarRecetas(Map<Producto, Integer> productos) {
        List<InsumoFaltante> faltantes = new ArrayList<>();

        for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
            Producto producto = entry.getKey();
            int cantidadProducto = entry.getValue();
            Receta receta = producto.getReceta();

            if (receta == null) continue;

            for (InsumoReceta insumoReceta : receta.getInsumosReceta()) {
                String nombreInsumo = insumoReceta.getInsumo().getNombre();
                String unidadRequerida = insumoReceta.getUnidad();
                double totalNecesario = insumoReceta.getCantidadUtilizada() * cantidadProducto;

                // 🔍 Buscar todos los lotes de este insumo por nombre (sin importar ID)
                List<Insumo> lotes = insumoDAO.findAll().stream()
                        .filter(i -> i.getNombre().equalsIgnoreCase(nombreInsumo))
                        .sorted(Comparator.comparing(Insumo::getFechaCaducidad)) // FIFO por vencimiento
                        .collect(Collectors.toList());

                double restante = totalNecesario;

                for (Insumo lote : lotes) {
                    if (restante <= 0.0001) break;

                    double disponible = lote.convertirUnidad(lote.getCantidad(), lote.getMedida(), unidadRequerida);
                    double usado = Math.min(disponible, restante);

                    if (usado > 0) {
                        lote.reducirCantidad(usado, unidadRequerida);
                        insumoDAO.update(lote);

                        restante -= usado;
                    }
                }

                if (restante > 0.0001) {
                    // ⚠️ Faltante
                    InsumoFaltante faltante = new InsumoFaltante();
                    faltante.setInsumo(insumoReceta.getInsumo());
                    faltante.setProducto(producto);
                    faltante.setCantidadFaltante(restante);
                    faltante.setUnidad(unidadRequerida);
                    faltante.setResuelto(false);
                    faltante.setPedido(null); // No lo asociamos a un pedido

                    faltanteDAO.save(faltante);
                    faltantes.add(faltante);
                }
            }
        }

        return faltantes;
    }

    /**
     * Simula el stock para validar si alcanza para todos los productos.
     * @param productos mapa de productos y sus cantidades
     * @return true si alcanza el stock, false si falta alguno
     */
    public boolean validarInsumosSuficientes(Map<Producto, Integer> productos) {
        Map<String, Double> stockPorNombre = new HashMap<>();

        for (Insumo insumo : insumoDAO.findAll()) {
            String nombre = insumo.getNombre();
            double acumulado = stockPorNombre.getOrDefault(nombre, 0.0);
            double enGramos = insumo.convertirUnidad(insumo.getCantidad(), insumo.getMedida(), "GR");
            stockPorNombre.put(nombre, acumulado + enGramos);
        }

        for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
            Producto producto = entry.getKey();
            int cantidad = entry.getValue();

            if (producto.getReceta() == null) continue;

            for (InsumoReceta insumoReceta : producto.getReceta().getInsumosReceta()) {
                String nombre = insumoReceta.getInsumo().getNombre();
                String unidad = insumoReceta.getUnidad();
                double requerido = insumoReceta.getCantidadUtilizada() * cantidad;
                double disponible = stockPorNombre.getOrDefault(nombre, 0.0);
                double requeridoGr = insumoReceta.getInsumo().convertirUnidad(requerido, unidad, "GR");

                if (disponible < requeridoGr) {
                    return false;
                }

                stockPorNombre.put(nombre, disponible - requeridoGr);
            }
        }

        return true;
    }
}
