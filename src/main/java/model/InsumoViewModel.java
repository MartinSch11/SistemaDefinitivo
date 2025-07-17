package model;

import javafx.beans.property.*;

import java.time.format.DateTimeFormatter;

public class InsumoViewModel {
    private final Insumo insumo;

    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty fechaCompra = new SimpleStringProperty();
    private final StringProperty fechaCaducidad = new SimpleStringProperty();
    private final StringProperty cantidad = new SimpleStringProperty();
    private final StringProperty proveedor = new SimpleStringProperty();

    public InsumoViewModel(Insumo insumo) {
        this.insumo = insumo;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        nombre.set(insumo.getNombre());
        if (insumo.getFechaCompra() != null)
            fechaCompra.set(insumo.getFechaCompra().format(formatter));
        if (insumo.getFechaCaducidad() != null)
            fechaCaducidad.set(insumo.getFechaCaducidad().format(formatter));

        cantidad.set(insumo.getCantidad() + " " + insumo.getMedida());

        Proveedor p = insumo.getProveedor();
        proveedor.set(p != null ? p.getNombre() : "Sin proveedor");
    }

    public Insumo getInsumo() {
        return insumo;
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public StringProperty fechaCompraProperty() {
        return fechaCompra;
    }

    public StringProperty fechaCaducidadProperty() {
        return fechaCaducidad;
    }

    public StringProperty cantidadProperty() {
        return cantidad;
    }

    public StringProperty proveedorProperty() {
        return proveedor;
    }

    public void setCantidad(String cantidad) {
        this.cantidad.set(cantidad);
    }
}
