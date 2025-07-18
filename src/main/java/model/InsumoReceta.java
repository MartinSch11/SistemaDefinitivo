package model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "receta_insumo")
public class InsumoReceta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receta_insumo")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_receta", nullable = false)
    private Receta receta;

    @ManyToOne
    @JoinColumn(name = "id_insumo", nullable = false)
    private Insumo insumo;

    @Column(name = "cantidad_utilizada")
    private int cantidadUtilizada;

    @Column(name = "unidad")
    private String unidad;

    public InsumoReceta() {}

    public InsumoReceta(Receta receta, Insumo insumo, int cantidadUtilizada, String unidad) {
        this.receta = receta;
        this.insumo = insumo;
        this.cantidadUtilizada = cantidadUtilizada;
        this.unidad = unidad;  // Asignamos la unidad
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Receta getReceta() {
        return receta;
    }
    public void setReceta(Receta receta) {
        this.receta = receta;
    }
    public Insumo getInsumo() {
        return insumo;
    }
    public void setInsumo(Insumo insumo) {
        this.insumo = insumo;
    }
    public int getCantidadUtilizada() {
        return cantidadUtilizada;
    }
    public void setCantidadUtilizada(int cantidadUtilizada) {
        this.cantidadUtilizada = cantidadUtilizada;
    }
    public String getUnidad() {
        return unidad;
    }
    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    @Override
    public String toString() {
        return "InsumoReceta{id=" + id + ", insumo=" + insumo.getNombre() + ", cantidadUtilizada=" + cantidadUtilizada + ", unidad=" + unidad + "}";
    }

}