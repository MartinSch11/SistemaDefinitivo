package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "insumos_faltantes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsumoFaltante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_insumo_faltante")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "insumo_id")
    private Insumo insumo;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @ManyToOne
    @JoinColumn(name ="id_pedido")
    private Pedido pedido;

    @Column(name ="cantidad_faltante")
    private double cantidadFaltante;

    @Column(name = "unidad")
    private String unidad;

    @Column(name = "resuelto")
    private boolean resuelto = false;
}