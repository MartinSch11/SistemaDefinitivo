package persistence.dao;

import jakarta.persistence.*;
import model.CatalogoInsumo;
import model.InsumoFaltante;

import java.util.List;

public class InsumoFaltanteDAO {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("pasteleriaPU");

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void save(InsumoFaltante faltante) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(faltante);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("No se pudo guardar el faltante", e);
        } finally {
            em.close();
        }
    }

    public void update(InsumoFaltante faltante) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(faltante);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error actualizando faltante", e);
        } finally {
            em.close();
        }
    }

    public List<InsumoFaltante> findPendientesPorInsumo(CatalogoInsumo catalogoInsumo) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT i FROM InsumoFaltante i WHERE i.catalogoInsumo = :catalogoInsumo AND i.resuelto = false",
                    InsumoFaltante.class).setParameter("catalogoInsumo", catalogoInsumo).getResultList();
        } finally {
            em.close();
        }
    }

    public List<InsumoFaltante> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT i FROM InsumoFaltante i", InsumoFaltante.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<InsumoFaltante> findAllPendientes() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT i FROM InsumoFaltante i WHERE i.resuelto = false", InsumoFaltante.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}