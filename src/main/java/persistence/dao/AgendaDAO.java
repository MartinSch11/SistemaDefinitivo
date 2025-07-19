package persistence.dao;

import jakarta.persistence.*;
import model.Agenda;
import java.time.LocalDate;
import java.util.List;

public class AgendaDAO {
    private EntityManagerFactory emf;
    private EntityManager em;

    public AgendaDAO() {
        emf = Persistence.createEntityManagerFactory("pasteleriaPU"); // Asegúrate de que el nombre coincida con el de tu persistence.xml
        em = emf.createEntityManager();
    }

    public void save(Agenda agenda) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(agenda);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public void update(Agenda agenda) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(agenda);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public void delete(Agenda agenda) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(em.contains(agenda) ? agenda : em.merge(agenda));
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public void close() {
        em.close();
        emf.close();
    }

    public List<Agenda> findByFecha(LocalDate fecha) {
        return em.createQuery("SELECT e FROM Agenda e WHERE e.fecha_pendiente = :fecha", Agenda.class)
                .setParameter("fecha", fecha)
                .getResultList();
    }

    public EntityManager getEntityManager() {
        if (!em.isOpen()) {
            em = emf.createEntityManager();
        }
        return em;
    }

    public List<Agenda> findByWeek(LocalDate startOfWeek, LocalDate endOfWeek) {
        return getEntityManager().createQuery("SELECT e FROM Agenda e WHERE e.fecha_pendiente BETWEEN :startOfWeek AND :endOfWeek", Agenda.class)
                .setParameter("startOfWeek", startOfWeek)
                .setParameter("endOfWeek", endOfWeek)
                .getResultList();
    }

    public Agenda findById(Long id) {
        return em.find(Agenda.class, id);
    }

    public List<Agenda> findAll() {
        return em.createQuery("SELECT e FROM Agenda e", Agenda.class).getResultList();
    }

    public List<Agenda> findByFechaBetween(LocalDate desde, LocalDate hasta) {
        return em.createQuery("SELECT a FROM Agenda a WHERE a.fecha_pendiente BETWEEN :desde AND :hasta", Agenda.class)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();
    }

    public Agenda findByCampos(String empleado, LocalDate fecha, int hora, int minuto, String pendiente) {
        try {
            // Obtener el dni del trabajador por nombre
            String dni = em.createQuery("SELECT t.dni FROM Trabajador t WHERE t.nombre = :empleado", String.class)
                .setParameter("empleado", empleado)
                .getSingleResult();
            return em.createQuery(
                "SELECT a FROM Agenda a WHERE a.pendiente = :pendiente AND a.fecha_pendiente = :fecha AND FUNCTION('HOUR', a.hora) = :hora AND FUNCTION('MINUTE', a.hora) = :minuto AND a.idEmpleado = :dni", Agenda.class)
                .setParameter("pendiente", pendiente)
                .setParameter("fecha", fecha)
                .setParameter("hora", hora)
                .setParameter("minuto", minuto)
                .setParameter("dni", dni)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}

