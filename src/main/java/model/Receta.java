package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Receta {
    private int idReceta;
    private Producto producto;
    private String nombreReceta;
    private List<InsumoReceta> insumosReceta;

    // Método para obtener la lista de insumos de la receta
    public List<Insumo> getInsumos() {
        // Devuelve la lista de insumos a partir de la lista de InsumoReceta
        return insumosReceta.stream()
                .map(InsumoReceta::getInsumo)
                .toList();
    }

    // Método para obtener la cantidad utilizada de un insumo específico en la receta
    public double getCantidadInsumo(Insumo insumo) {
        for (InsumoReceta insumoReceta : insumosReceta) {
            if (insumoReceta.getInsumo().equals(insumo)) {
                return insumoReceta.getCantidadUtilizada();
            }
        }
        return 0.0; // Si el insumo no está en la receta, devuelve 0
    }
}
