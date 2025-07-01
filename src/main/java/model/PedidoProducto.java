package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    @JoinColumn(name = "id_producto", insertable = false, updatable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_pedido", insertable = false, updatable = false)
    private Pedido pedido;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;  // Nueva columna para almacenar la cantidad de productos en el pedido

    // Constructor sin ID para facilitar la creación de objetos
    public PedidoProducto(Pedido pedido, Producto producto, int cantidad) {
        this.pedido = pedido;
        this.producto = producto;
        this.cantidad = cantidad;

        if (pedido.getNumeroPedido() != null && producto.getId() != null) {
            this.id = new PedidoProductoId(pedido.getNumeroPedido(), producto.getId());
        } else {
            // Lo dejás en null y lo seteás después, manualmente (en el controller)
            this.id = null;
        }
    }

}
