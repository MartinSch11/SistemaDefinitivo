package model;

import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "catalogo_insumo")
public class CatalogoInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String estado; // LÍQUIDO, SÓLIDO, UNIDAD

    @Column(nullable = false)
    private String proveedor;

    public CatalogoInsumo() {
    }

    public CatalogoInsumo(String nombre, String estado, String proveedor) {
        this.nombre = nombre;
        this.estado = estado;
        this.proveedor = proveedor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }
}