package persistence.dao;

import jakarta.persistence.*;
import model.Producto;
import model.Receta;

import java.util.List;

public class RecetaDAO {
    private EntityManagerFactory emf;

    public RecetaDAO() {
        emf = Persistence.createEntityManagerFactory("pasteleriaPU");
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void save(Receta receta) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(receta);
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

    public Receta findByProducto(Producto producto) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT r FROM Receta r WHERE r.producto = :producto", Receta.class)
                    .setParameter("producto", producto)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // No hay receta asociada al producto
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    public Receta findById(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Receta.class, id);
        } finally {
            em.close();
        }
    }

    public List<Receta> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("FROM Receta", Receta.class).getResultList();
        } finally {
            em.close();
        }
    }

    public void update(Receta receta) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(receta);
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

    public void delete(Receta receta) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(em.contains(receta) ? receta : em.merge(receta));
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

    public Receta findRecetaWithInsumos(int recetaId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Receta r LEFT JOIN FETCH r.insumosReceta WHERE r.id = :id", Receta.class)
                    .setParameter("id", recetaId)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
