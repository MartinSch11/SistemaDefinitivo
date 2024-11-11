package model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @Column(name = "dni_cliente")
    private String dniCliente;

    @Column(name = "nombre_cliente", nullable = false)
    private String nombreCliente;

    @Column(name = "contacto_cliente", nullable = false)
    private String contactoCliente;

    @Column(name = "detalle_pedido", nullable = false)
    private String detallePedido;

    @Column(name = "empleado_asignado", nullable = false)
    private String empleadoAsignado;

    @Column(name = "formaEntrega", nullable = false)
    private String formaEntrega;

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDate fechaEntrega;

    @Column(name = "estado_pedido", nullable = false)
    private String estadoPedido;

    @ManyToMany
    @JoinTable(
            name = "pedido_producto",
            joinColumns = @JoinColumn(name = "pedido_id"),
            inverseJoinColumns = @JoinColumn(name = "producto_id")
    )
    private List<Producto> productos;

    public Pedido(String nombreCliente, String contactoCliente, String dniCliente, String detallePedido, String empleadoAsignado, String formaEntrega, LocalDate fechaEntrega, String estadoPedido) {
        this.nombreCliente = nombreCliente;
        this.contactoCliente = contactoCliente;
        this.dniCliente = dniCliente;
        this.detallePedido = detallePedido;
        this.empleadoAsignado = empleadoAsignado;
        this.formaEntrega = formaEntrega;
        this.fechaEntrega = fechaEntrega;
        this.estadoPedido = estadoPedido;
        this.productos = productos;
    }

}
