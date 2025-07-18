package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor

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

    public int getIdProveedor() {
        return idProveedor;
    }
    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    public String getUbicacion() {
        return ubicacion;
    }
    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }
    public String getCorreo() {
        return correo;
    }
    public void setCorreo(String correo) {
        this.correo = correo;
    }
    public String getPrefijo() {
        return prefijo;
    }
    public void setPrefijo(String prefijo) {
        this.prefijo = prefijo;
    }
    public String getDni() {
        return dni;
    }
    public void setDni(String dni) {
        this.dni = dni;
    }
    public String getPostfijo() {
        return postfijo;
    }
    public void setPostfijo(String postfijo) {
        this.postfijo = postfijo;
    }
    public List<Insumo> getInsumos() {
        return insumos;
    }
    public void setInsumos(List<Insumo> insumos) {
        this.insumos = insumos;
    }

    // Constructor vacío para compatibilidad con JPA y controladores
    public Proveedor(){}
}
