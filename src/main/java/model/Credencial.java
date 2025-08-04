package model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "credenciales")
public class Credencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dni", unique = true, nullable = false)
    private String dni;

    @Column(name = "contraseña", nullable = false)
    private String contraseña;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_trabajador", referencedColumnName = "id", unique = true)
    private Trabajador trabajador;

    public Credencial() {}

    public Credencial(String dni, String contraseña, Trabajador trabajador) {
        this.dni = dni;
        this.contraseña = contraseña;
        this.trabajador = trabajador;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
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


