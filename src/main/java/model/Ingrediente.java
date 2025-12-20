package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor // Lombok genera el constructor vacío
@Table(name = "ingrediente")
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ingrediente") // Nombre más explícito para la PK
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String estado; // LÍQUIDO, SÓLIDO, UNIDAD

    // En un sistema V2.0, el proveedor acá es solo el "sugerido" o "habitual".
    @Column(name = "proveedor_default") 
    private String proveedor;

    public Ingrediente(String nombre, String estado, String proveedor) {
        this.nombre = nombre;
        this.estado = estado;
        this.proveedor = proveedor;
    }
    
    // toString para que los ComboBox muestren el nombre y no el hash del objeto
    @Override
    public String toString() {
        return nombre;
    }
}