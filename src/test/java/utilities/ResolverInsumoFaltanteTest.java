package utilities;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.dao.InsumoFaltanteDAO;
import persistence.dao.InsumoDAO;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ResolverInsumoFaltanteTest {

    private Insumo harina;
    private Producto tartaManzana;
    private Pedido pedidoSimulado;
    private InsumoFaltanteDAO faltanteDAO;
    private InsumoDAO insumoDAO;
    private InsumoFaltante faltante;

    @BeforeEach
    void setup() {
        System.out.println("\n🔧 SETUP: Preparando insumo HARINA sin stock (0.0 KG)");

        harina = new Insumo();
        harina.setId(1);
        harina.setNombre("Harina");
        harina.setCantidad(0.0);
        harina.setMedida("KG");
        harina.setFechaCompra(LocalDate.now());
        harina.setFechaCaducidad(LocalDate.now().plusMonths(1));

        tartaManzana = new Producto();
        tartaManzana.setId(100L);
        tartaManzana.setNombre("Tarta de Manzana");

        pedidoSimulado = new Pedido();
        pedidoSimulado.setNumeroPedido(999L);

        faltante = new InsumoFaltante();
        faltante.setId(10L);
        faltante.setInsumo(harina);
        faltante.setProducto(tartaManzana);
        faltante.setPedido(pedidoSimulado);
        faltante.setCantidadFaltante(0.5);
        faltante.setUnidad("KG");
        faltante.setResuelto(false);

        faltanteDAO = new InsumoFaltanteDAO() {
            private final List<InsumoFaltante> db = new ArrayList<>(List.of(faltante));

            @Override
            public List<InsumoFaltante> findPendientesPorInsumo(Insumo i) {
                return db.stream()
                        .filter(f -> f.getInsumo().getId().equals(i.getId()) && !f.isResuelto())
                        .toList();
            }

            @Override
            public void update(InsumoFaltante f) {
                System.out.printf("🛠️ Actualizando faltante #%d: restante=%.2f %s, resuelto=%s\n",
                        f.getId(), f.getCantidadFaltante(), f.getUnidad(), f.isResuelto());
            }
        };

        insumoDAO = new InsumoDAO() {
            @Override
            public void update(Insumo i) {
                System.out.printf("📦 Insumo actualizado: %s ahora tiene %.2f %s\n",
                        i.getNombre(), i.getCantidad(), i.getMedida());
            }
        };
    }

    @Test
    void testResolverFaltanteConStockSuficiente() {
        System.out.println("🧪 TEST: Resolver faltante con stock SUFICIENTE (agregando 1.0 KG)");

        double cantidadAgregada = 1.0;
        List<InsumoFaltante> pendientes = faltanteDAO.findPendientesPorInsumo(harina);

        for (InsumoFaltante faltante : pendientes) {
            if (cantidadAgregada <= 0) break;

            System.out.printf("🔍 Procesando faltante #%d del producto '%s' para pedido #%d...\n",
                    faltante.getId(), faltante.getProducto().getNombre(), faltante.getPedido().getNumeroPedido());

            double requerido = faltante.getCantidadFaltante();
            double usado = Math.min(cantidadAgregada, requerido);

            System.out.printf("   ➕ Necesario: %.2f %s, Disponible: %.2f\n", requerido, faltante.getUnidad(), cantidadAgregada);

            faltante.setCantidadFaltante(requerido - usado);
            if (faltante.getCantidadFaltante() <= 0.0001) {
                faltante.setResuelto(true);
            }

            cantidadAgregada -= usado;
            faltanteDAO.update(faltante);
        }

        harina.setCantidad(cantidadAgregada);
        insumoDAO.update(harina);

        // ✅ Asserts
        assertTrue(faltante.isResuelto());
        assertEquals(0.5, 1.0 - harina.getCantidad(), 0.0001);
        assertEquals(0.5, harina.getCantidad(), 0.0001);

        System.out.println("✅ Resultado: El faltante fue resuelto completamente. Sobraron 0.5 KG de insumo.");
    }

    @Test
    void testResolverFaltanteConStockParcial() {
        System.out.println("🧪 TEST: Resolver faltante con stock PARCIAL (agregando 0.6 KG)");

        faltante.setCantidadFaltante(1.0); // ahora necesita 1 KG
        double cantidadAgregada = 0.6;

        List<InsumoFaltante> pendientes = faltanteDAO.findPendientesPorInsumo(harina);

        for (InsumoFaltante faltante : pendientes) {
            if (cantidadAgregada <= 0) break;

            System.out.printf("🔍 Procesando faltante #%d del producto '%s' para pedido #%d...\n",
                    faltante.getId(), faltante.getProducto().getNombre(), faltante.getPedido().getNumeroPedido());

            double requerido = faltante.getCantidadFaltante();
            double usado = Math.min(cantidadAgregada, requerido);

            System.out.printf("   ➕ Necesario: %.2f %s, Disponible: %.2f\n", requerido, faltante.getUnidad(), cantidadAgregada);

            faltante.setCantidadFaltante(requerido - usado);
            if (faltante.getCantidadFaltante() <= 0.0001) {
                faltante.setResuelto(true);
            }

            cantidadAgregada -= usado;
            faltanteDAO.update(faltante);
        }

        harina.setCantidad(cantidadAgregada);
        insumoDAO.update(harina);

        // ✅ Asserts
        assertFalse(faltante.isResuelto());
        assertEquals(0.4, faltante.getCantidadFaltante(), 0.0001);
        assertEquals(0.0, harina.getCantidad(), 0.0001);

        System.out.println("✅ Resultado: Faltante parcialmente resuelto. Quedan 0.4 KG pendientes.");
    }
}
