package persistence.dao;

import jakarta.persistence.*;
import model.Combo;
import java.util.List;
import java.util.logging.Logger;
import utilities.JpaUtil;

public class ComboDAO {
    private static final Logger LOGGER = Logger.getLogger(ComboDAO.class.getName());

    public void save(Combo combo) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(combo);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public Combo findById(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Combo c LEFT JOIN FETCH c.productos WHERE c.id = :id", Combo.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            LOGGER.severe("Error al buscar combo por ID: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public List<Combo> findAll() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            String hql = "SELECT DISTINCT c FROM Combo c LEFT JOIN FETCH c.productos";
            return em.createQuery(hql, Combo.class).getResultList();
        } finally {
            em.close();
        }
    }

    public void update(Combo combo) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(combo);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Combo combo = em.find(Combo.class, id);
            if (combo != null) {
                em.remove(combo);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
