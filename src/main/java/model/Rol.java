package model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
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

    // Constructor vacío explícito para compatibilidad con Hibernate
    public Rol() {}

    // Constructor con todos los campos
    public Rol(int idRol, String nombre) {
        this.idRol = idRol;
        this.nombre = nombre;
    }

    public int getIdRol() {
        return idRol;
    }
    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public List<Trabajador> getTrabajadores() {
        return trabajadores;
    }
    public void setTrabajadores(List<Trabajador> trabajadores) {
        this.trabajadores = trabajadores;
    }
    public List<RolPermiso> getRolPermisos() {
        return rolPermisos;
    }
    public void setRolPermisos(List<RolPermiso> rolPermisos) {
        this.rolPermisos = rolPermisos;
    }

    @Override
    public String toString() {
        return nombre;
    }
}

