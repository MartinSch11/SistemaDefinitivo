package persistence.dao;

import jakarta.persistence.*;
import model.NotificacionEntity;
import utilities.JpaUtil;

import java.time.LocalDate;
import java.util.List;

public class NotificacionEntityDAO {

    public void save(NotificacionEntity notificacion) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(notificacion);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void update(NotificacionEntity notificacion) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(notificacion);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            NotificacionEntity notificacion = em.find(NotificacionEntity.class, id);
            if (notificacion != null) {
                em.remove(notificacion);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void marcarComoLeida(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            NotificacionEntity n = em.find(NotificacionEntity.class, id);
            if (n != null) {
                n.setLeida(true);
                em.merge(n);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public List<NotificacionEntity> findNoLeidas() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em
                    .createQuery("SELECT n FROM NotificacionEntity n WHERE n.leida = false ORDER BY n.fecha DESC",
                            NotificacionEntity.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<NotificacionEntity> findAll() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT n FROM NotificacionEntity n ORDER BY n.fecha DESC", NotificacionEntity.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public boolean existePorContenidoYFecha(String mensaje, LocalDate fecha) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(n) FROM NotificacionEntity n WHERE n.mensaje = :mensaje AND n.fecha = :fecha",
                    Long.class);
            query.setParameter("mensaje", mensaje);
            query.setParameter("fecha", fecha);
            Long count = query.getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public void marcarTodasComoLeidas() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("UPDATE NotificacionEntity n SET n.leida = true WHERE n.leida = false")
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void eliminarLeidas() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM NotificacionEntity n WHERE n.leida = true")
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

}
