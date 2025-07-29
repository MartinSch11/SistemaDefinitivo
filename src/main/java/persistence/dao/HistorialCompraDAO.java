package persistence.dao;

import model.HistorialCompra;
import java.util.List;
import jakarta.persistence.*;
import utilities.JpaUtil;

public class HistorialCompraDAO {
    public void save(HistorialCompra compra) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(compra);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public List<HistorialCompra> findAll() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT h FROM HistorialCompra h", HistorialCompra.class).getResultList();
        } finally {
            em.close();
        }
    }
}
