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

            // Calcular el total antes de persistir el pedido
            pedido.setTotalPedido(pedido.calcularTotalPedido());

            // Persistir el pedido
            em.persist(pedido);
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

    public void update(Pedido pedido) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            // Recalcular total antes de actualizar
            pedido.setTotalPedido(pedido.calcularTotalPedido());

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
            return em.createQuery("SELECT p FROM Pedido p", Pedido.class).getResultList();
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
