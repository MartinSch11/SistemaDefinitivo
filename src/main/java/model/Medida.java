package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "medidas")
public class Medida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medida")
    private int id;

    @Column(name = "nombre_medida")
    private String nombreMedida;

    public Medida(String nombreMedida) {
        this.nombreMedida = nombreMedida;
    }

    @Override
    public String toString() {
        return nombreMedida;
    }
}
