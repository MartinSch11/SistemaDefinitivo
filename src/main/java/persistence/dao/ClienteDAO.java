package persistence.dao;

import jakarta.persistence.EntityManager;
import model.Cliente;
import java.util.List;
import utilities.JpaUtil;

public class ClienteDAO {
    public void save(Cliente cliente) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(cliente);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void update(Cliente cliente) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(cliente);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(String dni) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Long count = em.createQuery("SELECT COUNT(p) FROM Pedido p WHERE p.cliente.dni = :dni", Long.class)
                    .setParameter("dni", dni)
                    .getSingleResult();
            if (count > 0) {
                throw new RuntimeException("No se puede eliminar el cliente porque tiene pedidos asignados.");
            }
            Cliente cliente = em.find(Cliente.class, dni);
            if (cliente != null) {
                em.remove(cliente);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Cliente findByDni(String dni) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Cliente c WHERE c.dni = :dni", Cliente.class)
                    .setParameter("dni", dni)
                    .setMaxResults(1)
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElse(null);
        } finally {
            em.close();
        }
    }

    public List<Cliente> findAll() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Cliente c", Cliente.class).getResultList();
        } finally {
            em.close();
        }
    }
}