package model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "credenciales")
public class Credencial {

    @Id
    @Column(name = "dni")
    private String dni;

    @Column(name = "contraseña", nullable = false)
    private String contraseña;

    // Definiendo la relación con Trabajador como One-to-One
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dni", referencedColumnName = "dni")
    private Trabajador trabajador;

    // Constructor con todos los campos
    public Credencial(String dni, String contraseña, Trabajador trabajador) {
        this.dni = dni;
        this.contraseña = contraseña;
        this.trabajador = trabajador;
    }

    public Credencial() {}

    public String getDni() {
        return dni;
    }
    public void setDni(String dni) {
        this.dni = dni;
    }
    public String getContraseña() {
        return contraseña;
    }
    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }
    public Trabajador getTrabajador() {
        return trabajador;
    }
    public void setTrabajador(Trabajador trabajador) {
        this.trabajador = trabajador;
    }
}


