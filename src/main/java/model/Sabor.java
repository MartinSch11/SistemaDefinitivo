package model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sabores")
public class Sabor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sabor")
    private int id_sabor;

    @Column(name = "sabor")
    private String sabor;

    @Override
    public String toString() {
        return this.sabor;  // Asumiendo que 'sabor' es el nombre del sabor
    }

    public int getId_sabor() {
        return id_sabor;
    }
    public void setId_sabor(int id_sabor) {
        this.id_sabor = id_sabor;
    }
    public String getSabor() {
        return sabor;
    }
    public void setSabor(String sabor) {
        this.sabor = sabor;
    }
}
