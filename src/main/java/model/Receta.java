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

    // Constructor vacío
    public Receta() {
        this.nombreReceta = new SimpleStringProperty();
        this.insumosReceta = new ArrayList<>();
    }

    // Constructor con nombre
    public Receta(String nombreRecetaTexto) {
        this();
        setNombreReceta(nombreRecetaTexto);
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

    public StringProperty idProperty() {
        return new SimpleStringProperty(id != null ? String.valueOf(id) : "");
    }

    public void addInsumo(InsumoReceta insumoReceta) {
        if (insumoReceta == null || insumosReceta.contains(insumoReceta)) {
            return; // Evita agregar nulos o duplicados
        }
        insumosReceta.add(insumoReceta);
        insumoReceta.setReceta(this);
    }

    public void removeInsumo(InsumoReceta insumoReceta) {
        if (insumoReceta == null || !insumosReceta.contains(insumoReceta)) {
            return; // Evita eliminar nulos o elementos inexistentes
        }
        insumosReceta.remove(insumoReceta);
        insumoReceta.setReceta(null); // Rompe la asociación bidireccional
    }

    public List<Insumo> getInsumos() {
        return insumosReceta.stream()
                .map(InsumoReceta::getInsumo)
                .toList(); // Devuelve una lista de insumos asociados a la receta
    }

    public double getCantidadInsumo(Insumo insumo) {
        return insumosReceta.stream()
                .filter(insumoReceta -> insumoReceta.getInsumo().equals(insumo))
                .findFirst()
                .map(ir -> (double) ir.getCantidadUtilizada())
                .orElse(0.0);
    }

    @Override
    public String toString() {
        return nombreReceta.get(); // Retorna el valor de la propiedad
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getNombreRecetaTexto() {
        return nombreRecetaTexto;
    }
    public void setNombreRecetaTexto(String nombreRecetaTexto) {
        this.nombreRecetaTexto = nombreRecetaTexto;
    }
    public List<InsumoReceta> getInsumosReceta() {
        return insumosReceta;
    }
    public void setInsumosReceta(List<InsumoReceta> insumosReceta) {
        this.insumosReceta = insumosReceta;
    }
}