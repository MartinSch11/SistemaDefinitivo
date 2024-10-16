package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsumoReceta {
    private Insumo insumo;
    private double cantidadUtilizada;

    // Constructor, getters y setters
}