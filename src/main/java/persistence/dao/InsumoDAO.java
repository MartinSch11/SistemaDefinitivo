package persistence.dao;

import jakarta.persistence.*;
import model.Insumo;

import java.util.List;

public class InsumoDAO {
    private EntityManagerFactory emf;
    private EntityManager em;

    public InsumoDAO() {
        emf = Persistence.createEntityManagerFactory("pasteleriaPU"); // Cambia por el nombre correcto de tu persistence.xml
        em = emf.createEntityManager();
    }

    public void save(Insumo insumo) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(insumo);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public Insumo findById(Long id) {
        return em.find(Insumo.class, id);
    }

    public List<Insumo> findAll() {
        return em.createQuery("SELECT i FROM Insumo i", Insumo.class).getResultList();
    }

    public void update(Insumo insumo) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(insumo);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public void delete(Insumo insumo) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(em.contains(insumo) ? insumo : em.merge(insumo));
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