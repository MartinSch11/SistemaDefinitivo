package model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "movimiento_stock")
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaMovimiento;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipo; // COMPRA, USO_RECETA, AJUSTE, VENCIMIENTO

    @Column(nullable = false)
    private String nombreIngrediente; // Guardamos el nombre string para historial rápido

    @Column(nullable = false)
    private double cantidad;

    @Column(nullable = false)
    private String medida;

    private String detalle; // Ej: "Proveedor X" o "Receta Torta Chocolate"

    private double costoTotal; // Si fue compra

    // Constructor rápido
    public MovimientoStock(TipoMovimiento tipo, String ingrediente, double cantidad, String medida, String detalle, double costo) {
        this.fechaMovimiento = LocalDateTime.now();
        this.tipo = tipo;
        this.nombreIngrediente = ingrediente;
        this.cantidad = cantidad;
        this.medida = medida;
        this.detalle = detalle;
        this.costoTotal = costo;
    }
    
    public enum TipoMovimiento {
        COMPRA,
        PRODUCCION, // Cuando se descuenta por una receta
        AJUSTE,     // Cuando se rompe algo o se pierde
        VENCIMIENTO // Cuando tirás algo podrido
    }
}