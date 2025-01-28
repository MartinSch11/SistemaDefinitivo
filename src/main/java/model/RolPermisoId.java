package model;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class RolPermisoId implements Serializable {

    private int idRol;
    private int idPermiso;

    // Constructor vacío necesario para JPA
    public RolPermisoId() {}

    public RolPermisoId(int idRol, int idPermiso) {
        this.idRol = idRol;
        this.idPermiso = idPermiso;
    }

    // Equals y hashCode necesarios para la clave compuesta
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RolPermisoId that = (RolPermisoId) o;
        return idRol == that.idRol && idPermiso == that.idPermiso;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRol, idPermiso);
    }
}
