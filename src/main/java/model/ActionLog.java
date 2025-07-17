package model;

public class ActionLog {
    private String fecha;
    private String usuario;
    private String rol;
    private String accion;

    public ActionLog(String fecha, String usuario, String rol, String accion) {
        this.fecha = fecha;
        this.usuario = usuario;
        this.rol = rol;
        this.accion = accion;
    }

    // Getters y setters
    public String getFecha() {
        return fecha;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getRol() {
        return rol;
    }

    public String getAccion() {
        return accion;
    }
}
