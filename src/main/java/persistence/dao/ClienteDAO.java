package persistence.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.Cliente;

import java.util.List;

public class ClienteDAO {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("pasteleriaPU");

    public void save(Cliente cliente) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(cliente);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void update(Cliente cliente) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(cliente);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(String dni) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Cliente cliente = em.find(Cliente.class, dni); // Buscar por el dni
            if (cliente != null) {
                em.remove(cliente);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Cliente findByDni(String dni) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Cliente> resultados = em.createQuery("SELECT c FROM Cliente c WHERE c.dni = :dni", Cliente.class)
                    .setParameter("dni", dni)
                    .getResultList(); // Devuelve una lista en lugar de un único resultado

            return resultados.isEmpty() ? null : resultados.get(0); // Retorna null si no hay resultados
        } catch (Exception e) {
            System.err.println("Error al buscar el cliente con DNI " + dni + ": " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }


    public Cliente findById(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Cliente.class, id);
        } finally {
            em.close();
        }
    }

    public List<Cliente> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Cliente c", Cliente.class).getResultList();
        } finally {
            em.close();
        }
    }
}