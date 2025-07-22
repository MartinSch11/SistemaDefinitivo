package model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "combo_productos")
public class ComboProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_combo_producto")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_combo")
    private Combo combo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(name = "cantidad")
    private Integer cantidad;

    public ComboProducto() {}

    public Producto getProducto() {
        return producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
    public void setCombo(Combo combo) {
        this.combo = combo;
    }
}
