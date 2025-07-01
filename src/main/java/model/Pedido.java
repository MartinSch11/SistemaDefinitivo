package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long numeroPedido;

    // Relación con Cliente (FK por DNI)
    @ManyToOne
    @JoinColumn(name = "dni_cliente", referencedColumnName = "dni")
    private Cliente cliente;

    // Relación con Trabajador (FK por DNI)
    @ManyToOne
    @JoinColumn(name = "dni_empleado", referencedColumnName = "dni")
    private Trabajador empleadoAsignado;

    @Column(name = "forma_entrega", nullable = false)
    private String formaEntrega;

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDate fechaEntrega;

    @Column(name = "estado_pedido", nullable = false)
    private String estadoPedido;

    @Column(name = "total_pedido", nullable = false)
    private BigDecimal totalPedido;

    @Column(name = "fecha_entregado")
    private LocalDate fechaEntregado;

    @OneToMany(mappedBy = "pedido", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoProducto> pedidoProductos = new ArrayList<>();

    // Constructor sin cliente ni empleado para mayor flexibilidad
    public Pedido(Long numeroPedido, Cliente cliente, Trabajador empleadoAsignado,
                  String formaEntrega, LocalDate fechaEntrega, String estadoPedido,
                  String descripcion, BigDecimal totalPedido) {
        this.numeroPedido = numeroPedido;
        this.cliente = cliente;
        this.empleadoAsignado = empleadoAsignado;
        this.formaEntrega = formaEntrega;
        this.fechaEntrega = fechaEntrega;
        this.estadoPedido = estadoPedido;
        this.totalPedido = totalPedido;
    }

    public String generarDetalle() {
        if (pedidoProductos == null || pedidoProductos.isEmpty()) {
            return "No hay productos en este pedido.";
        }

        StringBuilder detalle = new StringBuilder();
        // Mostrar cada producto con su cantidad real, sin mostrar la receta
        for (PedidoProducto pp : pedidoProductos) {
            detalle.append(pp.getCantidad())
                    .append(" ")
                    .append(pp.getProducto().getNombre())
                    .append("\n");
        }
        return detalle.toString();
    }

    // Método para obtener los productos del pedido
    public List<Producto> getProductos() {
        List<Producto> productos = new ArrayList<>();
        for (PedidoProducto pedidoProducto : pedidoProductos) {
            productos.add(pedidoProducto.getProducto());
        }
        return productos;
    }

    // Método para calcular el total del pedido si es necesario
    public BigDecimal calcularTotalPedido() {
        return pedidoProductos.stream()
                .map(p -> BigDecimal.valueOf(p.getCantidad()).multiply(p.getProducto().getPrecio())) // Convertimos int
                // a BigDecimal
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Sumar todos los valores
    }
}
