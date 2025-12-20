package persistence.dao;

import jakarta.persistence.*;
import model.Producto;
import model.Receta;
import java.util.List;
import utilities.JpaUtil;

public class RecetaDAO {
    public void save(Receta receta) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(receta);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive())
                transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public Receta findByProducto(Producto producto) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            // Nota: Si Producto tiene la relación mapeada, esto está bien.
            return em.createQuery("SELECT r FROM Receta r WHERE r.producto = :producto", Receta.class)
                    .setParameter("producto", producto)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    public Receta findById(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Receta.class, id);
        } finally {
            em.close();
        }
    }

    public List<Receta> findAll() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("FROM Receta", Receta.class).getResultList();
        } finally {
            em.close();
        }
    }

    public void update(Receta receta) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(receta);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive())
                transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void delete(Receta receta) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
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

    public void eliminarInsumoDeReceta(int idDetalle) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Query query = em.createQuery("DELETE FROM RecetaDetalle rd WHERE rd.id = :id");
            query.setParameter("id", idDetalle);
            query.executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Receta findRecetaWithInsumos(int recetaId) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT r FROM Receta r LEFT JOIN FETCH r.ingredientes WHERE r.id = :id", Receta.class)
                    .setParameter("id", recetaId)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }
}
