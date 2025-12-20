package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "receta_detalle")
public class RecetaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receta_detalle")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_receta", nullable = false)
    private Receta receta;

    @ManyToOne
    @JoinColumn(name = "id_ingrediente", nullable = false)
    private Ingrediente ingrediente; 

    // Usamos double para permitir que el usuario cargue "0.5 KG" sin que se transforme en 0
    @Column(name = "cantidad", nullable = false)
    private double cantidad; 

    @Column(name = "unidad")
    private String unidad;

    public RecetaDetalle(Receta receta, Ingrediente ingrediente, double cantidad, String unidad) {
        this.receta = receta;
        this.ingrediente = ingrediente;
        this.cantidad = cantidad;
        this.unidad = unidad;
    }

    @Override
    public String toString() {
        return ingrediente.getNombre() + ": " + cantidad + " " + unidad;
    }
}