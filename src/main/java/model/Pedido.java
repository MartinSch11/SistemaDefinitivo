package model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_cliente", nullable = false)
    private String nombreCliente;

    @Column(name = "contacto_cliente", nullable = false)
    private String contactoCliente;

    @Column(name = "detalle_pedido", nullable = false)
    private String detallePedido;

    @Column(name = "empleado_asignado", nullable = false)
    private String empleadoAsignado;

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDate fechaEntrega;

    @ManyToMany
    @JoinTable(
            name = "pedido_producto",
            joinColumns = @JoinColumn(name = "pedido_id"),
            inverseJoinColumns = @JoinColumn(name = "producto_id")
    )
    private List<Producto> productos;

    public Pedido(String nombreCliente, String contactoCliente, String detallePedido, String empleadoAsignado, LocalDate fechaEntrega, List<Producto> productos) {
        this.nombreCliente = nombreCliente;
        this.contactoCliente = contactoCliente;
        this.detallePedido = detallePedido;
        this.empleadoAsignado = empleadoAsignado;
        this.fechaEntrega = fechaEntrega;
        this.productos = productos;
    }

}
