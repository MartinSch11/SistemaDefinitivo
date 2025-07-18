package model;

import jakarta.persistence.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@Entity
@Table(name = "insumos")
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_insumo")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_insumo")
    private CatalogoInsumo catalogoInsumo;

    @Column(name = "cantidad")
    private double cantidad;

    @Column(name = "fecha_caducidad")
    private LocalDate fechaCaducidad;

    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @Column(name = "medida")
    private String medida;

    @Column(name = "precio")
    private double precio;

    @ManyToOne
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;

    @Transient
    private SimpleStringProperty nombreProperty;

    // Constructor principal usando CatalogoInsumo
    public Insumo(CatalogoInsumo catalogoInsumo, int cantidad, double precio, String medida, LocalDate fechaCompra,
                  LocalDate fechaCaducidad, Proveedor proveedor) {
        this.catalogoInsumo = catalogoInsumo;
        this.cantidad = cantidad;
        this.precio = precio;
        this.medida = medida;
        this.fechaCompra = fechaCompra;
        this.fechaCaducidad = fechaCaducidad;
        this.proveedor = proveedor;
    }

    public Insumo(CatalogoInsumo catalogoInsumo, int cantidad, double precio, String medida, LocalDate fechaCompra,
                  LocalDate fechaCaducidad) {
        this.catalogoInsumo = catalogoInsumo;
        this.cantidad = cantidad;
        this.precio = precio;
        this.medida = medida;
        this.fechaCompra = fechaCompra;
        this.fechaCaducidad = fechaCaducidad;
    }

    // Constructor vacío para compatibilidad con JPA y controladores
    public Insumo() {}

    // Getter para nombreProperty (opcional, si la UI lo requiere)
    public StringProperty nombreProperty() {
        return new SimpleStringProperty(getNombre());
    }

    // Setter para nombreProperty (opcional, si la UI lo requiere)
    public void setNombre(String nombre) {
        if (this.catalogoInsumo != null) {
            this.catalogoInsumo.setNombre(nombre);
        }
    }

    public String getNombre() {
        return catalogoInsumo != null ? catalogoInsumo.getNombre() : null;
    }

    @Override
    public String toString() {
        return getNombre();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Insumo insumo = (Insumo) o;
        return getNombre() != null && getNombre().equals(insumo.getNombre());
    }

    @Override
    public int hashCode() {
        return getNombre() != null ? getNombre().hashCode() : 0;
    }

    public double getCantidad() {
        return this.cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public String getNombreProveedor() {
        return proveedor != null ? proveedor.getNombre() : "Sin proveedor";
    }

    public void reducirCantidad(double cantidadUtilizada, String unidadUtilizada) {
        double cantidadConvertida = convertirUnidad(cantidadUtilizada, unidadUtilizada, this.medida);

        if (this.cantidad >= cantidadConvertida) {
            this.cantidad -= cantidadConvertida;

            // Redondear a 2 decimales para evitar infinitos flotantes
            this.cantidad = BigDecimal.valueOf(this.cantidad)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        } else {
            throw new IllegalArgumentException("No hay suficiente cantidad disponible para el insumo: " + getNombre());
        }
    }

    public double getCantidadDisponible() {
        return this.cantidad; // Devuelve la cantidad actual como double
    }

    public double convertirUnidad(double cantidad, String unidadDesde, String unidadHacia) {
        if (unidadDesde == null || unidadHacia == null) {
            throw new IllegalArgumentException("Las unidades de medida no pueden ser nulas");
        }

        String de = unidadDesde.trim().toUpperCase();
        String a = unidadHacia.trim().toUpperCase();

        if (de.equals(a))
            return cantidad; // misma unidad, no conversion

        // Agrupamos tipos de unidades
        boolean esPesoDesde = de.equals("KG") || de.equals("GR");
        boolean esPesoHacia = a.equals("KG") || a.equals("GR");
        boolean esVolumenDesde = de.equals("L") || de.equals("ML");
        boolean esVolumenHacia = a.equals("L") || a.equals("ML");
        boolean esUnidadDesde = de.equals("UNIDAD") || de.equals("UNIDADES");
        boolean esUnidadHacia = a.equals("UNIDAD") || a.equals("UNIDADES");

        // Solo permitimos conversiones dentro del mismo tipo
        if (esPesoDesde && esPesoHacia) {
            if (de.equals("KG") && a.equals("GR"))
                return cantidad * 1000;
            if (de.equals("GR") && a.equals("KG"))
                return cantidad / 1000;
        } else if (esVolumenDesde && esVolumenHacia) {
            if (de.equals("L") && a.equals("ML"))
                return cantidad * 1000;
            if (de.equals("ML") && a.equals("L"))
                return cantidad / 1000;
        } else if (esUnidadDesde && esUnidadHacia) {
            return cantidad; // UNIDAD <-> UNIDADES
        }

        // Si llegamos aquí, la conversión es inválida
        throw new IllegalArgumentException("No se puede convertir de " + unidadDesde + " a " + unidadHacia
                + ". Solo se permiten conversiones entre unidades compatibles (peso, volumen o unidades). Si necesitas convertir leche entre L y GR, implementa una excepción específica para ese insumo.");
    }

    /**
     * Aumenta la cantidad de insumo en la unidad indicada, sin superar la capacidad máxima (si se define una lógica de capacidad máxima).
     * Si no hay límite, simplemente suma.
     */
    public void aumentarCantidad(double cantidadAAgregar, String unidadAAgregar) {
        double cantidadConvertida = convertirUnidad(cantidadAAgregar, unidadAAgregar, this.medida);
        this.cantidad += cantidadConvertida;
        // Redondear a 2 decimales para evitar infinitos flotantes
        this.cantidad = BigDecimal.valueOf(this.cantidad)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Devuelve la capacidad original del lote. Si no hay campo explícito, se asume que la capacidad máxima es la cantidad inicial al crear el lote.
     * Por defecto, retorna la cantidad actual (sin límite). Si tienes un campo de capacidad máxima, reemplaza esta lógica.
     */
    public double getCapacidadOriginal() {
        // Si tienes un campo de capacidad máxima, retorna ese valor aquí.
        // Por ahora, asumimos que no hay límite y devolvemos Double.MAX_VALUE.
        return Double.MAX_VALUE;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public CatalogoInsumo getCatalogoInsumo() {
        return catalogoInsumo;
    }
    public void setCatalogoInsumo(CatalogoInsumo catalogoInsumo) {
        this.catalogoInsumo = catalogoInsumo;
    }
    public LocalDate getFechaCaducidad() {
        return fechaCaducidad;
    }
    public void setFechaCaducidad(LocalDate fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }
    public LocalDate getFechaCompra() {
        return fechaCompra;
    }
    public void setFechaCompra(LocalDate fechaCompra) {
        this.fechaCompra = fechaCompra;
    }
    public String getMedida() {
        return medida;
    }
    public void setMedida(String medida) {
        this.medida = medida;
    }
    public double getPrecio() {
        return precio;
    }
    public void setPrecio(double precio) {
        this.precio = precio;
    }
    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }
    public SimpleStringProperty getNombreProperty() {
        return nombreProperty;
    }
    public void setNombreProperty(SimpleStringProperty nombreProperty) {
        this.nombreProperty = nombreProperty;
    }
}