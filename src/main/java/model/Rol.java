package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Table(name = "roles")
public class Rol {
    @Id
    @Column(name = "id_rol")
    private int idRol;

    @Column(name = "nombre")
    private String nombre;

    @OneToMany(mappedBy = "rol", fetch = FetchType.EAGER)
    private List<Trabajador> trabajadores;  // Relación inversa con Trabajadores

    // Relación con RolPermiso (opcional, útil para obtener permisos del rol)
    @OneToMany(mappedBy = "rol", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<RolPermiso> rolPermisos;

    // Constructor con todos los campos
    public Rol(int idRol, String nombre) {
        this.idRol = idRol;
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

