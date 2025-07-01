package utilities;

import model.*;
import org.junit.jupiter.api.Test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RecetaProcessorTest {

    private static final DecimalFormat formatoDecimal = new DecimalFormat("#.##");

    static {
        formatoDecimal.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(new Locale("es", "ES")));
    }

    @Test
    void testValidarInsumosSuficientes_conInsumosCompartidos_OK() {
        System.out.println("\n--- Test: Insumos suficientes con insumos compartidos ---");

        Insumo harina = new Insumo();
        harina.setId(1);
        harina.setNombre("Harina");
        harina.setMedida("KG");
        harina.setCantidad(1.5); // 1.5 KG

        Producto p1 = crearProducto("GR", 500, harina);
        Producto p2 = crearProducto("GR", 750, harina);

        Map<Producto, Integer> productos = new HashMap<>();
        productos.put(p1, 1);
        productos.put(p2, 1);

        RecetaProcessor processor = new RecetaProcessor();
        boolean resultado = processor.validarInsumosSuficientes(productos);

        System.out.println("Resultado esperado: TRUE");
        System.out.println("Resultado obtenido: " + resultado);
        assertTrue(resultado);
    }

    @Test
    void testValidarInsumosSuficientes_conInsumosCompartidos_Falla() {
        System.out.println("\n--- Test: Falla por insumos insuficientes ---");

        Insumo leche = new Insumo();
        leche.setId(2);
        leche.setNombre("Leche");
        leche.setMedida("L");
        leche.setCantidad(1.0); // 1L

        Producto p1 = crearProducto("ML", 600, leche); // 0.6 L
        Producto p2 = crearProducto("ML", 500, leche); // 0.5 L

        Map<Producto, Integer> productos = new HashMap<>();
        productos.put(p1, 1);
        productos.put(p2, 1);

        RecetaProcessor processor = new RecetaProcessor();
        boolean resultado = processor.validarInsumosSuficientes(productos);

        System.out.println("Resultado esperado: FALSE");
        System.out.println("Resultado obtenido: " + resultado);
        assertFalse(resultado);
    }

    private Producto crearProducto(String unidad, int cantidad, Insumo insumo) {
        Producto producto = new Producto();
        Receta receta = new Receta();
        InsumoReceta ir = new InsumoReceta();

        ir.setInsumo(insumo);
        ir.setUnidad(unidad);
        ir.setCantidadUtilizada(cantidad);

        receta.setInsumosReceta(List.of(ir));
        producto.setReceta(receta);
        return producto;
    }
}
