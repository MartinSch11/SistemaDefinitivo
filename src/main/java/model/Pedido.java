package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    @Column(name = "dni_cliente")
    private String dniCliente;

    @Column(name = "nombre_cliente", nullable = false)
    private String nombreCliente;

    @Column(name = "contacto_cliente", nullable = false)
    private String contactoCliente;

    @Column(name = "empleado_asignado", nullable = false)
    private String empleadoAsignado;

    @Column(name = "forma_entrega", nullable = false)
    private String formaEntrega;

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDate fechaEntrega;

    @Column(name = "estado_pedido", nullable = false)
    private String estadoPedido;

    @OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PedidoProducto> pedidoProductos = new ArrayList<>();

    public Pedido(Long numeroPedido, String nombreCliente, String contactoCliente, String dniCliente,
                  String empleadoAsignado, String formaEntrega, LocalDate fechaEntrega, String estadoPedido) {
        this.numeroPedido = numeroPedido;
        this.nombreCliente = nombreCliente;
        this.contactoCliente = contactoCliente;
        this.dniCliente = dniCliente;
        this.empleadoAsignado = empleadoAsignado;
        this.formaEntrega = formaEntrega;
        this.fechaEntrega = fechaEntrega;
        this.estadoPedido = estadoPedido;
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
}
