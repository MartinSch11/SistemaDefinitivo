package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "proveedor")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private int idProveedor;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "ubicacion")
    private String ubicacion;

    @Column(name = "correo")
    private String correo;

    @Column(name = "prefijo")
    private String prefijo;

    @Column(name = "dni")
    private String dni;

    @Column(name = "postfijo")
    private String postfijo;

    // Relación con Insumos
    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Insumo> insumos;

    // Constructor con parámetros
    public Proveedor(String nombre, String telefono, String ubicacion, String correo, String prefijo, String dni, String postfijo) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.ubicacion = ubicacion;
        this.correo = correo;
        this.prefijo = prefijo;
        this.dni = dni;
        this.postfijo = postfijo;
    }

    // Método para concatenar el CUIT
    public String getCuit() {
        return prefijo + "-" + dni + "-" + postfijo;
    }

    @Override
    public String toString() {
        return nombre;
    }


    public String getInsumosString() {
        if (insumos == null || insumos.isEmpty()) {
            return "No tiene insumos";
        }
        // Obtener los nombres de productos de catalogo_insumo asociados a cada insumo
        return insumos.stream()
                .map(insumo -> {
                    if (insumo.getCatalogoInsumo() != null) {
                        return insumo.getCatalogoInsumo().getNombre();
                    } else {
                        return "(Sin producto)";
                    }
                })
                .collect(Collectors.joining(", "));
    }
}
