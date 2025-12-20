package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@Entity
@Table(name = "receta")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receta")
    private Integer idReceta;

    @Column(name = "nombre_receta", nullable = false, unique = true)
    private String nombreReceta;

    // Relación con los detalles (ingredientes abstractos)
    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RecetaDetalle> ingredientes = new ArrayList<>();

    // Constructor simple
    public Receta(String nombreReceta) {
        this.nombreReceta = nombreReceta;
    }

    // Método helper para agregar ingredientes fácil
    public void agregarIngrediente(Ingrediente ingrediente, int cantidad, String unidad) {
        RecetaDetalle detalle = new RecetaDetalle(this, ingrediente, cantidad, unidad);
        this.ingredientes.add(detalle);
    }

    // ESTE METODO ES EL QUE DABA ERROR DE MAPEO
    // Devuelve un string bonito con los ingredientes
    public String getResumenIngredientes() {
        if (ingredientes == null || ingredientes.isEmpty()) return "Sin ingredientes";
        
        return ingredientes.stream()
                .map(d -> {
                    // Accedemos a 'ingrediente' y a 'cantidad' (double)
                    String nombre = d.getIngrediente() != null ? d.getIngrediente().getNombre() : "??";
                    return nombre + " (" + d.getCantidad() + " " + d.getUnidad() + ")";
                })
                .collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return nombreReceta;
    }
}