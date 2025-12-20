package model;

import javafx.beans.property.*;
import java.time.format.DateTimeFormatter;

public class InsumoViewModel {
    
    // Ahora envolvemos un LOTE (que es lo que tenés en stock)
    private final Lote lote;

    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty fechaCompra = new SimpleStringProperty();
    private final StringProperty fechaCaducidad = new SimpleStringProperty();
    private final StringProperty cantidad = new SimpleStringProperty();
    private final StringProperty proveedor = new SimpleStringProperty();

    public InsumoViewModel(Lote lote) {
        this.lote = lote;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        
        // 1. Obtener nombre del Ingrediente (Navegamos la relación)
        // Usamos el helper getNombre() que pusimos en la clase Lote nueva
        nombre.set(lote.getNombre()); 

        // 2. Fechas
        if (lote.getFechaCompra() != null)
            fechaCompra.set(lote.getFechaCompra().format(formatter));
        if (lote.getFechaCaducidad() != null)
            fechaCaducidad.set(lote.getFechaCaducidad().format(formatter));

        // 3. Cantidad y Medida
        cantidad.set(lote.getCantidadActual() + " " + lote.getMedida());

        // 4. Proveedor
        Proveedor p = lote.getProveedor();
        proveedor.set(p != null ? p.getNombre() : "Sin proveedor");
    }

    public Lote getLote() { // Renombramos getInsumo a getLote para ser claros
        return lote;
    }

    // Getters de propiedades para la tabla JavaFX
    public StringProperty nombreProperty() { return nombre; }
    public StringProperty fechaCompraProperty() { return fechaCompra; }
    public StringProperty fechaCaducidadProperty() { return fechaCaducidad; }
    public StringProperty cantidadProperty() { return cantidad; }
    public StringProperty proveedorProperty() { return proveedor; }

    public void setCantidad(String cantidad) {
        this.cantidad.set(cantidad);
    }
}