package persistence.dao;

import jakarta.persistence.*;
import model.Medida;

import java.util.List;

public class MedidaDAO {
    private EntityManagerFactory emf;
    private EntityManager em;

    public MedidaDAO() {
        emf = Persistence.createEntityManagerFactory("pasteleriaPU");  // Asegúrate de que coincide con tu persistence.xml
        em = emf.createEntityManager();
    }

    public void save(Medida medida) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(medida);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public Medida findById(int id) {
        return em.find(Medida.class, id);
    }

    public List<Medida> findAll() {
        return em.createQuery("SELECT m FROM Medida m", Medida.class).getResultList();
    }

    public void update(Medida medida) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(medida);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public void delete(Medida medida) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(em.contains(medida) ? medida : em.merge(medida));
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public void close() {
        em.close();
        emf.close();
    }
}
