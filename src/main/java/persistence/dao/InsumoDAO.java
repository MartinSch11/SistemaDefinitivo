package persistence.dao;

import jakarta.persistence.*;
import model.Lote;
import java.util.List;
import utilities.JpaUtil;

public class InsumoDAO {

    public void save(Lote insumo) {
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

    public Lote findById(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Lote.class, id);
        } finally {
            em.close();
        }
    }

    public List<Lote> findAll() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            // "l" es el alias de Lote (mejor que "i" de insumo para no confundir)
            return em.createQuery("SELECT l FROM Lote l", Lote.class).getResultList();
        } finally {
            em.close();
        }
    }

    public void update(Lote insumo) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            // Validación simple
            if (insumo.getId() == null) {
                System.out.println("El lote no tiene ID, no se puede actualizar.");
                return;
            }
            
            transaction.begin();
            em.merge(insumo);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public List<Lote> findDisponiblesPorNombreOrdenado(String nombre) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            // CORRECCIÓN 1: Navegamos a 'l.ingrediente.nombre'
            // CORRECCIÓN 2: Usamos 'cantidadActual'
            return em.createQuery("""
                            SELECT l FROM Lote l 
                            WHERE LOWER(l.ingrediente.nombre) = LOWER(:nombre) 
                              AND l.cantidadActual > 0
                            ORDER BY l.fechaCaducidad ASC
                        """, Lote.class)
                    .setParameter("nombre", nombre)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Buscamos un Lote asociado a un ID de Ingrediente (antes catalogoInsumo)
    public Lote findByCatalogoInsumoId(Long idIngrediente) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            // CORRECCIÓN: Filtramos por 'l.ingrediente.id'
            return em.createQuery("SELECT l FROM Lote l WHERE l.ingrediente.id = :id ORDER BY l.id ASC", Lote.class)
                    .setParameter("id", idIngrediente)
                    .setMaxResults(1)
                    .getResultStream() // getResultStream() es más moderno que getResultList().stream()
                    .findFirst()
                    .orElse(null);
        } finally {
            em.close();
        }
    }
    
    // Agrego este método que usabas en RecetasController
    public void delete(Lote lote) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            // Hay que hacer merge antes de remove si el objeto está desconectado (detached)
            Lote toDelete = em.merge(lote);
            em.remove(toDelete);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}