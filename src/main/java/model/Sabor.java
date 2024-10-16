package model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sabores")
public class Sabor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sabor ")
    private int id_sabor;

    @Column(name = "sabor")
    private String sabor;

}
