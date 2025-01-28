package model;

import jakarta.persistence.*;

@Entity
@Table(name = "permisos")
public class Permiso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_permiso;

    private String nombre;

    // Getters y setters
}
