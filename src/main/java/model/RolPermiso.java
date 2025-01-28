package model;

import jakarta.persistence.*;

@Entity
@Table(name = "rol_permiso")
@IdClass(RolPermisoId.class) // Relaciona con la clave compuesta

public class RolPermiso {

    @Id
    @Column(name = "id_rol")
    private int idRol;

    @Id
    @Column(name = "id_permiso")
    private int idPermiso;

    @ManyToOne
    @JoinColumn(name = "id_rol", insertable = false, updatable = false)
    private Rol rol;

    @ManyToOne
    @JoinColumn(name = "id_permiso", insertable = false, updatable = false)
    private Permiso permiso;

    // Getters y setters

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public int getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(int idPermiso) {
        this.idPermiso = idPermiso;
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
