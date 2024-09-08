package persistence.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.Producto;

import java.util.List;

public class ProductoDAO {
    private EntityManagerFactory emf;
    private EntityManager em;

    public ProductoDAO() {
        emf = Persistence.createEntityManagerFactory("pasteleriaPU"); // Asegúrate de que el nombre coincida con el de tu persistence.xml
        em = emf.createEntityManager();
    }

    public void save(Producto producto) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(producto);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public Producto findById(int id) {
        return em.find(Producto.class, id);
    }

    public List<Producto> findAll() {
        return em.createQuery("SELECT p FROM Producto p", Producto.class).getResultList();
    }

    public void update(Producto producto) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(producto);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public void delete(Producto producto) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(em.contains(producto) ? producto : em.merge(producto));
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
