package model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Table(name = "trabajadores")
public class Trabajador {

    @Id
    @Column(name = "dni", nullable = false, length = 15)
    private String dni;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "sueldo")
    private BigDecimal sueldo;

    @Column(name = "fechaContratacion")
    private LocalDate fechaContratacion;

    @Column(name = "ultimaActividad")
    private Timestamp ultimaActividad;

    @Column(name = "sexo")
    private String sexo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", referencedColumnName = "id_rol")
    private Rol rol;

    @OneToOne(mappedBy = "trabajador", cascade = CascadeType.ALL)
    private Credencial credencial;

    // Constructor con todos los campos
    public Trabajador(String dni, String nombre, String sexo, String direccion, String telefono,
                      BigDecimal sueldo, LocalDate fechaContratacion,
                      Timestamp ultimaActividad, Rol rol) {
        this.dni = dni;
        this.nombre = nombre;
        this.sexo = sexo;
        this.direccion = direccion;
        this.telefono = telefono;
        this.sueldo = sueldo;
        this.fechaContratacion = fechaContratacion;
        this.ultimaActividad = ultimaActividad;
        this.rol = rol;
    }

    public Trabajador() {
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public BigDecimal getSueldo() {
        return sueldo;
    }

    public void setSueldo(BigDecimal sueldo) {
        this.sueldo = sueldo;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public Timestamp getUltimaActividad() {
        return ultimaActividad;
    }

    public void setUltimaActividad(Timestamp ultimaActividad) {
        this.ultimaActividad = ultimaActividad;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Credencial getCredencial() {
        return credencial;
    }

    public void setCredencial(Credencial credencial) {
        this.credencial = credencial;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
