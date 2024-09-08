package model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "sabores")
public class Sabor {
    @Id
    private int id_sabor;
    private String sabor;

    // Getters y Setters generados por Lombok
}
