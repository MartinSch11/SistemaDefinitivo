package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Column(name = "descripcion")  // Nueva columna para observaciones
    private String descripcion;

    @Column(name = "total_pedido", nullable = false)
    private BigDecimal totalPedido;

    @OneToMany(mappedBy = "pedido", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<PedidoProducto> pedidoProductos = new ArrayList<>();

    // Constructor sin cliente ni empleado para mayor flexibilidad
    public Pedido(Long numeroPedido, Cliente cliente, Trabajador empleadoAsignado,
                  String formaEntrega, LocalDate fechaEntrega, String estadoPedido,
                  String descripcion, BigDecimal totalPedido) {  // ✅ Ahora usa BigDecimal
        this.numeroPedido = numeroPedido;
        this.cliente = cliente;
        this.empleadoAsignado = empleadoAsignado;
        this.formaEntrega = formaEntrega;
        this.fechaEntrega = fechaEntrega;
        this.estadoPedido = estadoPedido;
        this.descripcion = descripcion;
        this.totalPedido = totalPedido;
    }


    public String generarDetalle() {
        if (pedidoProductos == null || pedidoProductos.isEmpty()) {
            return "No hay productos en este pedido.";
        }

        StringBuilder detalle = new StringBuilder("Detalle del Pedido:\n");

        // Agrupar productos por objeto Producto y contar sus ocurrencias
        Map<Producto, Long> contadorProductos = pedidoProductos.stream()
                .collect(Collectors.groupingBy(
                        PedidoProducto::getProducto,
                        Collectors.counting()
                ));

        // Construir el detalle con manejo seguro de la receta
        contadorProductos.forEach((producto, cantidad) -> {
            String recetaNombre = (producto.getReceta() != null) ? producto.getReceta().getNombreReceta() : "Sin receta";
            detalle.append("Producto: ").append(producto.getNombre())
                    .append(", Cantidad: ").append(cantidad)
                    .append(", Receta: ").append(recetaNombre)
                    .append("\n");
        });

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
                .map(p -> BigDecimal.valueOf(p.getCantidad()).multiply(p.getProducto().getPrecio())) // Convertimos int a BigDecimal
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Sumar todos los valores
    }
}
