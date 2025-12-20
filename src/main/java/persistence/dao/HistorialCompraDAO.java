package persistence.dao;

import model.MovimientoStock;
import java.util.List;
import jakarta.persistence.*;
import utilities.JpaUtil;

public class HistorialCompraDAO {
    public void save(MovimientoStock compra) {
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

    public List<MovimientoStock> findAll() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT h FROM HistorialCompra h", MovimientoStock.class).getResultList();
        } finally {
            em.close();
        }
    }
}
