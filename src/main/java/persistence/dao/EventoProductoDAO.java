package persistence.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import model.EventoProducto;
import utilities.JpaUtil;

import java.util.List;

public class EventoProductoDAO {

    // -------- CRUD básico --------

    public void save(EventoProducto ep) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(ep);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public EventoProducto findById(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(EventoProducto.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Carga todos los ítems de un evento, incluyendo el Producto (para mostrar
     * nombre/precio)
     */
    public List<EventoProducto> findAllByEventoId(Long eventoId) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<EventoProducto> q = em.createQuery(
                    "SELECT ep FROM EventoProducto ep " +
                            "JOIN FETCH ep.producto p " +
                            "WHERE ep.evento.id = :eventoId " +
                            "ORDER BY p.nombre ASC",
                    EventoProducto.class);
            q.setParameter("eventoId", eventoId);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Busca un ítem por (evento, producto) – útil si todavía no tenés el id del
     * ítem.
     */
    public EventoProducto findByEventoAndProducto(Long eventoId, Long productoId) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<EventoProducto> q = em.createQuery(
                    "SELECT ep FROM EventoProducto ep " +
                            "WHERE ep.evento.id = :eventoId AND ep.producto.id = :productoId",
                    EventoProducto.class);
            q.setParameter("eventoId", eventoId);
            q.setParameter("productoId", productoId);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        } finally {
            em.close();
        }
    }

    public EventoProducto update(EventoProducto detachedEp) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            EventoProducto merged = em.merge(detachedEp);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(EventoProducto ep) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.remove(em.contains(ep) ? ep : em.merge(ep));
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteById(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            EventoProducto ep = em.find(EventoProducto.class, id);
            if (ep != null)
                em.remove(ep);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // -------- Actualizaciones específicas (performantes y seguras) --------

    /**
     * Marca/desmarca el ítem como hecho sin cargar todo el evento.
     */
    public void updateHecho(Long id, boolean hecho) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("UPDATE EventoProducto ep SET ep.hecho = :hecho WHERE ep.id = :id")
                    .setParameter("hecho", hecho)
                    .setParameter("id", id)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Cambia la cantidad del ítem (por si permitís editar cantidades desde el UI).
     */
    public void updateCantidad(Long id, int cantidad) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("UPDATE EventoProducto ep SET ep.cantidad = :cant WHERE ep.id = :id")
                    .setParameter("cant", cantidad)
                    .setParameter("id", id)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
