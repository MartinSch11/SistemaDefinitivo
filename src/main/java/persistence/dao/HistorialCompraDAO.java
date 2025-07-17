package persistence.dao;

import model.HistorialCompra;
import java.util.List;
import jakarta.persistence.*;


public class HistorialCompraDAO {
    private EntityManagerFactory emf;
    private EntityManager em;

    public HistorialCompraDAO() {
        emf = Persistence.createEntityManagerFactory("pasteleriaPU");
        em = emf.createEntityManager();
    }

    public void save(HistorialCompra compra) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(compra);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
        }
    }

    public List<HistorialCompra> findAll() {
        return em.createQuery("SELECT h FROM HistorialCompra h", HistorialCompra.class).getResultList();
    }
}
