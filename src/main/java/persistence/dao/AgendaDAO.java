package persistence.dao;

import jakarta.persistence.*;
import model.Agenda;
import java.time.LocalDate;
import java.util.List;
import utilities.JpaUtil;

public class AgendaDAO {
    public void save(Agenda agenda) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(agenda);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void update(Agenda agenda) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(agenda);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void delete(Agenda agenda) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(em.contains(agenda) ? agenda : em.merge(agenda));
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public List<Agenda> findByFecha(LocalDate fecha) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT e FROM Agenda e WHERE e.fecha_pendiente = :fecha", Agenda.class)
                    .setParameter("fecha", fecha)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Agenda> findByWeek(LocalDate startOfWeek, LocalDate endOfWeek) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT e FROM Agenda e WHERE e.fecha_pendiente BETWEEN :startOfWeek AND :endOfWeek", Agenda.class)
                    .setParameter("startOfWeek", startOfWeek)
                    .setParameter("endOfWeek", endOfWeek)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Agenda findById(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Agenda.class, id);
        } finally {
            em.close();
        }
    }

    public List<Agenda> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT e FROM Agenda e WHERE e.fecha_pendiente BETWEEN :fechaInicio AND :fechaFin", Agenda.class)
                    .setParameter("fechaInicio", fechaInicio)
                    .setParameter("fechaFin", fechaFin)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Agenda findByCampos(String empleadoTarea, LocalDate fechaPendiente, int horaPendiente, int minutoPendiente, String pendiente) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                "SELECT a FROM Agenda a WHERE a.pendiente = :pendiente AND a.fecha_pendiente = :fecha AND a.hora = :hora AND a.idEmpleado = (SELECT e.id FROM Trabajador e WHERE e.nombre = :empleado)",
                Agenda.class)
                .setParameter("pendiente", pendiente)
                .setParameter("fecha", fechaPendiente)
                .setParameter("hora", java.sql.Time.valueOf(String.format("%02d:%02d:00", horaPendiente, minutoPendiente)))
                .setParameter("empleado", empleadoTarea)
                .setMaxResults(1)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}

