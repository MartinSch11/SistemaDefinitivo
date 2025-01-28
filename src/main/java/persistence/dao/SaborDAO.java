package persistence.dao;

import model.Sabor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;

public class SaborDAO {
    private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("pasteleriaPU");

    public List<Sabor> findAll() {
        EntityManager em = entityManagerFactory.createEntityManager();
        List<Sabor> sabores = em.createQuery("FROM Sabor", Sabor.class).getResultList();
        em.close();
        return sabores;
    }

    public Sabor findByName(String nombre) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.createQuery("SELECT s FROM Sabor s WHERE s.nombre = :nombre", Sabor.class)
                    .setParameter("nombre", nombre)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    // Método para save un nuevo sabor en la base de datos
    public void guardarSabor(Sabor sabor) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(sabor);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    // Método para actualizar un sabor existente en la base de datos
    public void actualizarSabor(Sabor sabor) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(sabor);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    // Método para eliminar un sabor de la base de datos
    public void eliminarSabor(Sabor sabor) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            Sabor saborAEliminar = em.find(Sabor.class, sabor.getId_sabor());
            if (saborAEliminar != null) {
                em.remove(saborAEliminar); // Eliminar el sabor
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback(); // Hacer rollback si algo sale mal
            throw e; // Rethrow la excepción
        } finally {
            em.close();
        }
    }
}
