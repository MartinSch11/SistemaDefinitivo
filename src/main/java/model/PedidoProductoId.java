package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PedidoProductoId implements Serializable {

    @Column(name = "id_producto")
    private Long productoId;

    @Column(name = "id_pedido")
    private Long pedidoId;

    // Getters y setters
    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    // Implementación segura de equals y hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PedidoProductoId that = (PedidoProductoId) o;
        return Objects.equals(productoId, that.productoId) &&
                Objects.equals(pedidoId, that.pedidoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productoId, pedidoId);
    }

    // Método toString para facilitar la depuración
    @Override
    public String toString() {
        return "PedidoProductoId{" +
                "productoId=" + productoId +
                ", pedidoId=" + pedidoId +
                '}';
    }
}
