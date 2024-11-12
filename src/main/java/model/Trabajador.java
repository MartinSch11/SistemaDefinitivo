package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", referencedColumnName = "id_rol")
    private Rol rol;

    @OneToOne(mappedBy = "trabajador", cascade = CascadeType.ALL)
    private Credencial credencial;

    // Constructor con todos los campos
    public Trabajador(String dni, String nombre, String direccion, String telefono,
                      BigDecimal sueldo, LocalDate fechaContratacion,
                      Timestamp ultimaActividad, Rol rol) {
        this.dni = dni;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.sueldo = sueldo;
        this.fechaContratacion = fechaContratacion;
        this.ultimaActividad = ultimaActividad;
        this.rol = rol;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
