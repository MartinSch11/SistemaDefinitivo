package model;

import jakarta.persistence.*;

@Entity
@Table(name = "permisos")
public class Permiso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private int idPermiso;

    @Column(name = "recurso")
    private String recurso;

    @Column(name = "accion")
    private String accion;

    // Getters y setters
    public int getIdPermiso() {
        return idPermiso;
    }
    public void setIdPermiso(int idPermiso) {
        this.idPermiso = idPermiso;
    }
    public String getRecurso() {
        return recurso;
    }
    public void setRecurso(String recurso) {
        this.recurso = recurso;
    }
    public String getAccion() {
        return accion;
    }
    public void setAccion(String accion) {
        this.accion = accion;
    }

    @Override
    public String toString() {
        return recurso + "-" + accion;
    }
}
