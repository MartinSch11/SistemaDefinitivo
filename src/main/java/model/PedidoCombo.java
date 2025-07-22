package model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pedido_combo")
public class PedidoCombo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_combo", nullable = false)
    private Combo combo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    public PedidoCombo() {}

    public PedidoCombo(Pedido pedido, Combo combo, int cantidad) {
        this.pedido = pedido;
        this.combo = combo;
        this.cantidad = cantidad;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Combo getCombo() { return combo; }
    public void setCombo(Combo combo) { this.combo = combo; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
