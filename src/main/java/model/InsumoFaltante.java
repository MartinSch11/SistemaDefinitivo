package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "insumos_faltantes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsumoFaltante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_insumo_faltante")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_insumo")
    private CatalogoInsumo catalogoInsumo;

    @Column(name ="cantidad_faltante")
    private double cantidadFaltante;

    @Column(name = "unidad")
    private String unidad;

    @Column(name = "resuelto")
    private boolean resuelto = false;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public CatalogoInsumo getCatalogoInsumo() {
        return catalogoInsumo;
    }
    public void setCatalogoInsumo(CatalogoInsumo catalogoInsumo) {
        this.catalogoInsumo = catalogoInsumo;
    }
    public double getCantidadFaltante() {
        return cantidadFaltante;
    }
    public void setCantidadFaltante(double cantidadFaltante) {
        this.cantidadFaltante = cantidadFaltante;
    }
    public String getUnidad() {
        return unidad;
    }
    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }
    public boolean isResuelto() {
        return resuelto;
    }
    public void setResuelto(boolean resuelto) {
        this.resuelto = resuelto;
    }
}