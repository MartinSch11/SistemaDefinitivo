package persistence.dao;

import jakarta.persistence.*;
import model.Pedido;
import model.PedidoProducto;
import model.Producto;

import java.util.List;

public class PedidoDAO {
    private EntityManagerFactory emf;

    public PedidoDAO() {
        emf = Persistence.createEntityManagerFactory("pasteleriaPU");
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void save(Pedido pedido) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            // Gestionar los productos si están asociados al pedido
            if (pedido.getPedidoProductos() != null) {  // Cambiar 'getProductos' por 'getPedidoProductos'
                List<PedidoProducto> pedidoProductos = pedido.getPedidoProductos();  // Ahora trabajamos con PedidoProducto
                for (PedidoProducto pedidoProducto : pedidoProductos) {
                    Producto producto = em.merge(pedidoProducto.getProducto());  // Obtener el producto desde PedidoProducto
                    pedidoProducto.setProducto(producto);  // Actualiza la referencia del producto en PedidoProducto
                }
            }

            em.persist(pedido); // Persiste el pedido
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("No se pudo guardar el pedido: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public Pedido findByNumeroPedido(Long numeroPedido) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Pedido.class, numeroPedido);
        } finally {
            em.close();
        }
    }

    public List<Pedido> findAll() {
        EntityManager em = getEntityManager();
        try {
            List<Pedido> pedidos = em.createQuery(
                            "SELECT p FROM Pedido p " +
                                    "LEFT JOIN FETCH p.pedidoProductos pp " +
                                    "LEFT JOIN FETCH pp.producto", Pedido.class)
                    .getResultList();

            // Depuración
            for (Pedido pedido : pedidos) {
                System.out.println("Pedido: " + pedido.getNumeroPedido());
                for (PedidoProducto pp : pedido.getPedidoProductos()) {
                    System.out.println("  Producto: " + pp.getProducto().getNombre());
                }
            }
            return pedidos;
        } finally {
            em.close();
        }
    }

    public void update(Pedido pedido) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(pedido);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void delete(Pedido pedido) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(em.contains(pedido) ? pedido : em.merge(pedido));
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void close() {
        emf.close();
    }
}
