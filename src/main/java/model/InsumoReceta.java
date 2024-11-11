package model;

import jakarta.persistence.*;
import lombok.Data;
import model.Receta;

@Data
@Entity
@Table(name = "receta_insumo")
public class InsumoReceta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receta_insumo")
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_receta")
    private Receta receta;

    @ManyToOne
    @JoinColumn(name = "id_insumo", nullable = false)
    private Insumo insumo;

    @Column(name = "cantidad_utilizada")
    private int cantidadUtilizada;

    @Column(name = "unidad")
    private String unidad; // Atributo para la unidad

    public InsumoReceta() {}

    public InsumoReceta(Receta receta, Insumo insumo, int cantidadUtilizada, String unidad) {
        this.receta = receta;
        this.insumo = insumo;
        this.cantidadUtilizada = cantidadUtilizada;
        this.unidad = unidad;  // Asignamos la unidad
    }

    public String getUnidad() {
        return unidad;
    }

    @Override
    public String toString() {
        return "InsumoReceta{id=" + id + ", insumo=" + insumo.getNombre() + ", cantidadUtilizada=" + cantidadUtilizada + ", unidad=" + unidad + "}";
    }
}