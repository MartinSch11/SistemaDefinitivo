package persistence.dao;

import jakarta.persistence.*;
import model.Insumo;
import java.util.List;
import utilities.JpaUtil;

public class InsumoDAO {
    public void save(Insumo insumo) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
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
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Insumo.class, id);
        } finally {
            em.close();
        }
    }

    public List<Insumo> findAll() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT i FROM Insumo i", Insumo.class).getResultList();
        } finally {
            em.close();
        }
    }

    public void update(Insumo insumo) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            if (insumo.getId() == null) {
                System.out.println("El insumo no tiene ID, no se puede actualizar.");
            } else {
                System.out.println("Actualizando insumo con ID: " + insumo.getId());
            }
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
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
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

    public Insumo findByCatalogoInsumoId(Long catalogoInsumoId) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT i FROM Insumo i WHERE i.catalogoInsumo.id = :catalogoInsumoId ORDER BY i.id ASC", Insumo.class)
                    .setParameter("catalogoInsumoId", catalogoInsumoId)
                    .setMaxResults(1)
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElse(null);
        } finally {
            em.close();
        }
    }
}