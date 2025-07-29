package persistence.dao;

import jakarta.persistence.*;
import model.Producto;
import java.util.List;
import java.util.logging.Logger;
import utilities.JpaUtil;

public class ProductoDAO {
    private static final Logger LOGGER = Logger.getLogger(ProductoDAO.class.getName());

    public void save(Producto producto) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(producto);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive())
                transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public Producto findById(int id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Producto p WHERE p.id = :id", Producto.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            LOGGER.severe("Error al buscar producto por ID: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public List<Producto> findAll() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em
                .createQuery("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.sabores", Producto.class)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public void update(Producto producto) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(producto);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive())
                transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void delete(Producto producto) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(em.contains(producto) ? producto : em.merge(producto));
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive())
                transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
