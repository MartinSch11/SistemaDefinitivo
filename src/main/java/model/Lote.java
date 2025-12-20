package model;

import jakarta.persistence.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "lote")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lote")
    private Integer id;

    // Relación con la definición abstracta
    @ManyToOne
    @JoinColumn(name = "id_ingrediente", nullable = false)
    private Ingrediente ingrediente;

    @Column(name = "cantidad_actual", nullable = false)
    private double cantidadActual;

    @Column(name = "cantidad_inicial") // Útil para saber cuánto se gastó del paquete
    private double cantidadInicial;

    @Column(name = "fecha_caducidad")
    private LocalDate fechaCaducidad;

    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @Column(name = "medida")
    private String medida; // KG, L, UNIDAD

    @Column(name = "costo_unitario") // Precio al que lo pagaste
    private double costoUnitario;

    @ManyToOne
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;

    // Constructor para cuando comprás mercadería nueva
    public Lote(Ingrediente ingrediente, double cantidad, double costo, String medida,
            LocalDate fechaCompra, LocalDate fechaCaducidad, Proveedor proveedor) {
        this.ingrediente = ingrediente;
        this.cantidadActual = cantidad;
        this.cantidadInicial = cantidad; // Al inicio son iguales
        this.costoUnitario = costo;
        this.medida = medida;
        this.fechaCompra = fechaCompra;
        this.fechaCaducidad = fechaCaducidad;
        this.proveedor = proveedor;
    }

    // --- Lógica de Negocio ---

    // Método para descontar stock (usado por el algoritmo FIFO)
    public void descontar(double cantidadAUsar) {
        if (cantidadAUsar > this.cantidadActual) {
            throw new IllegalArgumentException("No hay suficiente stock en este lote.");
        }
        // Resta y redondeo seguro para evitar 0.00000001
        BigDecimal actual = BigDecimal.valueOf(this.cantidadActual);
        BigDecimal resta = BigDecimal.valueOf(cantidadAUsar);
        this.cantidadActual = actual.subtract(resta).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }

    // Propiedades para JavaFX (Tablas)
    @Transient
    public StringProperty nombreProperty() {
        return new SimpleStringProperty(ingrediente != null ? ingrediente.getNombre() : "Desconocido");
    }

    public String getNombre() {
        return ingrediente != null ? ingrediente.getNombre() : "Desconocido";
    }

    // ==========================================================================
    // LÓGICA DE NEGOCIO RECUPERADA (Pegar dentro de Lote.java)
    // ==========================================================================

    /**
     * Aumenta el stock actual manejando conversiones y redondeo.
     */
    public void aumentarCantidad(double cantidadAAgregar, String unidadAAgregar) {
        // 1. Convertimos lo que entra a la medida que maneja este Lote
        double cantidadConvertida = convertirUnidad(cantidadAAgregar, unidadAAgregar, this.medida);

        // 2. Sumamos al campo nuevo (cantidadActual)
        this.cantidadActual += cantidadConvertida;

        // 3. Redondear a 2 decimales para evitar "0.3000000004"
        this.cantidadActual = BigDecimal.valueOf(this.cantidadActual)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Helper interno para convertir unidades (KG<->GR, L<->ML).
     */
    public double convertirUnidad(double cantidad, String unidadDesde, String unidadHacia) {
        if (unidadDesde.equalsIgnoreCase(unidadHacia))
            return cantidad;

        String de = unidadDesde.toUpperCase();
        String a = unidadHacia.toUpperCase();

        // Peso
        if (de.equals("KG") && a.equals("GR"))
            return cantidad * 1000.0;
        if (de.equals("GR") && a.equals("KG"))
            return cantidad / 1000.0;

        // Volumen
        if (de.equals("L") && a.equals("ML"))
            return cantidad * 1000.0;
        if (de.equals("ML") && a.equals("L"))
            return cantidad / 1000.0;

        // Unidades (Asumimos compatibilidad directa 1 a 1 si son variantes de nombre)
        if (de.startsWith("UNIDAD") && a.startsWith("UNIDAD"))
            return cantidad;

        // Si no son compatibles (ej: Litros a Kilos sin densidad), devolvemos lo mismo
        // o podrías lanzar excepción, pero para que no explote la UI devolvemos igual.
        return cantidad;
    }

    // Método extra para saber la capacidad original (usado en devoluciones)
    public double getCapacidadOriginal() {
        return this.cantidadInicial > 0 ? this.cantidadInicial : Double.MAX_VALUE;
    }
}