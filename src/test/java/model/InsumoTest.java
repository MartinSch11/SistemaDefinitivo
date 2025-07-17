package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InsumoTest {

    @Test
    void testConvertirUnidad_KG_to_GR() {
        Insumo insumo = new Insumo();
        double resultado = insumo.convertirUnidad(1.5, "KG", "GR");
        assertEquals(1500, resultado, 0.01);
    }

    @Test
    void testConvertirUnidad_GR_to_KG() {
        Insumo insumo = new Insumo();
        double resultado = insumo.convertirUnidad(1000, "GR", "KG");
        assertEquals(1.0, resultado, 0.01);
    }

    @Test
    void testConvertirUnidad_L_to_ML() {
        Insumo insumo = new Insumo();
        double resultado = insumo.convertirUnidad(2.0, "L", "ML");
        assertEquals(2000, resultado, 0.01);
    }

    @Test
    void testConvertirUnidad_ML_to_L() {
        Insumo insumo = new Insumo();
        double resultado = insumo.convertirUnidad(1500, "ML", "L");
        assertEquals(1.5, resultado, 0.01);
    }

    @Test
    void testConvertirUnidad_MismaUnidad() {
        Insumo insumo = new Insumo();
        double resultado = insumo.convertirUnidad(123.45, "GR", "GR");
        assertEquals(123.45, resultado, 0.01);
    }

    @Test
    void testConvertirUnidad_Invalida() {
        Insumo insumo = new Insumo();
        assertThrows(IllegalArgumentException.class, () -> {
            insumo.convertirUnidad(1, "KG", "L");
        });
    }
}
