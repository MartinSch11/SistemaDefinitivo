package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pedido_producto")
public class PedidoProducto {

    @EmbeddedId
    private PedidoProductoId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto", insertable = false, updatable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_pedido", referencedColumnName = "id_pedido", insertable = false, updatable = false)
    private Pedido pedido;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;  // Nueva columna para almacenar la cantidad de productos en el pedido

    // Constructor sin ID para facilitar la creación de objetos
    public PedidoProducto(Pedido pedido, Producto producto, int cantidad) {
        this.id = new PedidoProductoId(pedido.getNumeroPedido(), producto.getId());
        this.pedido = pedido;
        this.producto = producto;
        this.cantidad = cantidad;
    }
}
