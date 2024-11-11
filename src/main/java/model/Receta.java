package model;

import jakarta.persistence.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "recetas")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receta")
    private Integer id;

    @Column(name = "nombre_receta")
    private String nombreRecetaTexto;

    @Transient
    private StringProperty nombreReceta = new SimpleStringProperty();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InsumoReceta> insumosReceta = new ArrayList<>();

    @PostLoad
    public void initializeNombreReceta() {
        if (nombreRecetaTexto != null) {
            nombreReceta.set(nombreRecetaTexto);
        }
    }

    public Receta() {
        // Constructor vacío necesario para JPA
    }

    // Constructor que acepta el nombre
    public Receta(String nombreRecetaTexto) {
        this.nombreRecetaTexto = nombreRecetaTexto;
        this.nombreReceta.set(nombreRecetaTexto);
        this.insumosReceta = new ArrayList<>();  // Asegurar que la lista no sea nula
    }

    public String getNombreReceta() {
        return nombreReceta.get();
    }

    public void setNombreReceta(String nombre) {
        this.nombreReceta.set(nombre);
        this.nombreRecetaTexto = nombre;
    }

    public StringProperty nombreRecetaProperty() {
        return nombreReceta;
    }

    // Método agregado para id como StringProperty
    public StringProperty idProperty() {
        return new SimpleStringProperty(String.valueOf(id));
    }

    public void addInsumo(InsumoReceta insumoReceta) {
        insumosReceta.add(insumoReceta);
        insumoReceta.setReceta(this); // Asignación en ambos sentidos
    }

    public void removeInsumo(InsumoReceta insumoReceta) {
        insumosReceta.remove(insumoReceta);
        insumoReceta.setReceta(null); // Eliminar la referencia a la receta
    }

    public List<Insumo> getInsumos() {
        List<Insumo> insumos = new ArrayList<>();
        for (InsumoReceta insumoReceta : insumosReceta) {
            insumos.add(insumoReceta.getInsumo());
        }
        return insumos;
    }

    public int getCantidadInsumo(Insumo insumo) {
        for (InsumoReceta insumoReceta : insumosReceta) {
            if (insumoReceta.getInsumo().equals(insumo)) {
                return insumoReceta.getCantidadUtilizada();
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return nombreReceta.get();  // Acceder al valor de la propiedad StringProperty
    }
}
