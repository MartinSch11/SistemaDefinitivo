package model;

import java.util.List;

public class SessionContext {
    private static SessionContext instance;
    private String userName;
    private String roleName;
    private List<String> permisos;
    private String sexo;

    private SessionContext() {
        // Inicialización de los valores por defecto
    }

    public static SessionContext getInstance() {
        if (instance == null) {
            instance = new SessionContext();
        }
        return instance;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setPermisos(List<String> permisos) {
        this.permisos = permisos;
    }

    public List<String> getPermisos() {
        return permisos;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getSexo() {
        return sexo;
    }
}