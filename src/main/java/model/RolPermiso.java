package model;

import jakarta.persistence.*;

@Entity
@Table(name = "rol_permiso")
public class RolPermiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @ManyToOne
    @JoinColumn(name = "id_permiso", nullable = false)
    private Permiso permiso;

    // Getters y setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Rol getRol() {
        return rol;
    }
    public void setRol(Rol rol) {
        this.rol = rol;
    }
    public Permiso getPermiso() {
        return permiso;
    }
    public void setPermiso(Permiso permiso) {
        this.permiso = permiso;
    }
}
