package persistence.dao;

import jakarta.persistence.TypedQuery;
import model.CatalogoInsumo;
import jakarta.persistence.EntityManager;
import java.util.List;
import utilities.JpaUtil;

public class CatalogoInsumoDAO {
    public void save(CatalogoInsumo catalogoInsumo) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(catalogoInsumo);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public CatalogoInsumo findById(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(CatalogoInsumo.class, id);
        } finally {
            em.close();
        }
    }

    public List<CatalogoInsumo> findAll() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<CatalogoInsumo> query = em.createQuery("SELECT c FROM CatalogoInsumo c", CatalogoInsumo.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public CatalogoInsumo findByNombre(String nombre) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<CatalogoInsumo> query = em.createQuery(
                    "SELECT c FROM CatalogoInsumo c WHERE c.nombre = :nombre", CatalogoInsumo.class);
            query.setParameter("nombre", nombre);
            List<CatalogoInsumo> result = query.getResultList();
            return result.isEmpty() ? null : result.get(0);
        } finally {
            em.close();
        }
    }

    public void update(CatalogoInsumo catalogoInsumo) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(catalogoInsumo);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(CatalogoInsumo catalogoInsumo) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.remove(em.contains(catalogoInsumo) ? catalogoInsumo : em.merge(catalogoInsumo));
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}