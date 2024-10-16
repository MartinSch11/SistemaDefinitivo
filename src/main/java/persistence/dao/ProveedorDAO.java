package persistence.dao;

import jakarta.persistence.*;
import model.Proveedor;

import java.util.List;

public class ProveedorDAO{
    private EntityManagerFactory emf;
    private EntityManager em;

    public ProveedorDAO() {
        emf = Persistence.createEntityManagerFactory("pasteleriaPU"); // Cambia por el nombre correcto de tu persistence.xml
        em = emf.createEntityManager();
    }

    public void save(Proveedor proveedor) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(proveedor);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public Proveedor findById(Long id) {
        return em.find(Proveedor.class, id);
    }

    public List<Proveedor> findAll() {
        return em.createQuery("SELECT p FROM Proveedor p", Proveedor.class).getResultList();
    }

    public void update(Proveedor proveedor) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(proveedor);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public void delete(Proveedor proveedor) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(em.contains(proveedor) ? proveedor : em.merge(proveedor));
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
