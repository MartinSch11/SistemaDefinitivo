package persistence.dao;

import jakarta.persistence.TypedQuery;
import model.Ingrediente; // Usamos el nombre nuevo
import jakarta.persistence.EntityManager;
import java.util.List;
import utilities.JpaUtil;

public class IngredienteDAO {

    public void save(Ingrediente ingrediente) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(ingrediente);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Ingrediente findById(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Ingrediente.class, id);
        } finally {
            em.close();
        }
    }

    public List<Ingrediente> findAll() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            // CORRECCIÓN: "FROM Ingrediente"
            TypedQuery<Ingrediente> query = em.createQuery("SELECT i FROM Ingrediente i", Ingrediente.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Ingrediente findByNombre(String nombre) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            // CORRECCIÓN: "FROM Ingrediente"
            TypedQuery<Ingrediente> query = em.createQuery(
                    "SELECT i FROM Ingrediente i WHERE i.nombre = :nombre", Ingrediente.class);
            query.setParameter("nombre", nombre);
            List<Ingrediente> result = query.getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }

    public void update(Ingrediente ingrediente) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(ingrediente);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(Ingrediente ingrediente) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.remove(em.contains(ingrediente) ? ingrediente : em.merge(ingrediente));
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}