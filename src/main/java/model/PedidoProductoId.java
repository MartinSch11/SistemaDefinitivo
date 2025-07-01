package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@Embeddable
public class PedidoProductoId implements Serializable {

    // Getters y setters
    @Column(name = "id_producto")
    private Long productoId;

    @Column(name = "id_pedido")
    private Long pedidoId;

    public PedidoProductoId(Long pedidoId, Long productoId) {
        this.pedidoId = pedidoId;
        this.productoId = productoId;
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
