package persistence.dao;

import jakarta.persistence.*;
import model.CatalogoInsumo;
import model.InsumoFaltante;
import utilities.JpaUtil;
import java.util.List;

public class InsumoFaltanteDAO {

    public void save(InsumoFaltante faltante) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
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
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
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
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT i FROM InsumoFaltante i WHERE i.catalogoInsumo = :catalogoInsumo AND i.resuelto = false",
                    InsumoFaltante.class).setParameter("catalogoInsumo", catalogoInsumo).getResultList();
        } finally {
            em.close();
        }
    }

    public List<InsumoFaltante> findAll() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT i FROM InsumoFaltante i", InsumoFaltante.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<InsumoFaltante> findAllPendientes() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT i FROM InsumoFaltante i WHERE i.resuelto = false", InsumoFaltante.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}