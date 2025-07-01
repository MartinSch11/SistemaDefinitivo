package persistence.dao;

import jakarta.persistence.*;
import model.Insumo;
import model.InsumoFaltante;

import java.util.List;

public class InsumoFaltanteDAO {

    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("pasteleriaPU");

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void save(InsumoFaltante faltante) {
        try (EntityManager em = getEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.persist(faltante);
                tx.commit();
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                throw new RuntimeException("No se pudo guardar el faltante", e);
            }
        }
    }


    public void update(InsumoFaltante faltante) {
        try (EntityManager em = getEntityManager()) {
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
    }

    public List<InsumoFaltante> findPendientesPorInsumo(Insumo insumo) {
        try (EntityManager em = getEntityManager()) {
            try {
                return em.createQuery("SELECT i FROM InsumoFaltante i WHERE i.insumo = :insumo AND i.resuelto = false", InsumoFaltante.class).setParameter("insumo", insumo).getResultList();
            } finally {
                em.close();
            }
        }
    }
}