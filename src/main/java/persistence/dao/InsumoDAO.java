package persistence.dao;

import jakarta.persistence.*;
import model.Insumo;

import java.util.List;

public class InsumoDAO {
    private EntityManagerFactory emf;

    public InsumoDAO() {
        emf = Persistence.createEntityManagerFactory("pasteleriaPU"); // Cambia por el nombre correcto de tu persistence.xml
    }

    public void save(Insumo insumo) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(insumo);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public Insumo findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Insumo.class, id);
        } finally {
            em.close();
        }
    }

    public List<Insumo> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT i FROM Insumo i", Insumo.class).getResultList();
        } finally {
            em.close();
        }
    }

    public void update(Insumo insumo) {
        EntityManager em = emf.createEntityManager();
        if (insumo.getId() == null) {
            System.out.println("El insumo no tiene ID, no se puede actualizar.");
        } else {
            System.out.println("Actualizando insumo con ID: " + insumo.getId());
        }
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(insumo);
            transaction.commit();
            System.out.println("Insumo actualizado exitosamente.");
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public List<Insumo> findDisponiblesPorNombreOrdenado(String nombre) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("""
                            SELECT i FROM Insumo i 
                            WHERE LOWER(i.nombre) = LOWER(:nombre) 
                              AND i.cantidad > 0
                            ORDER BY i.fechaCaducidad ASC
                        """, Insumo.class)
                    .setParameter("nombre", nombre)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void delete(Insumo insumo) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(em.contains(insumo) ? insumo : em.merge(insumo));
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public Insumo findByCatalogoInsumoId(Long catalogoInsumoId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT i FROM Insumo i WHERE i.catalogoInsumo.id = :catalogoId ORDER BY i.id ASC", Insumo.class)
                    .setParameter("catalogoId", catalogoInsumoId)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public void close() {
        emf.close();
    }
}