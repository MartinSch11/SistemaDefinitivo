package persistence.dao;

import jakarta.persistence.*;
import model.Evento;

import java.time.LocalDate;
import java.util.List;

public class EventoDAO {
    private EntityManagerFactory emf;
    private EntityManager em;

    public EventoDAO() {
        emf = Persistence.createEntityManagerFactory("pasteleriaPU"); // Asegúrate de que el nombre coincida con el de tu persistence.xml
        em = emf.createEntityManager();
    }

    public void save(Evento evento) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(evento);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public Evento findById(Long id) {
        return em.find(Evento.class, id);
    }

    public List<Evento> findAll() {
        return em.createQuery("SELECT e FROM Evento e", Evento.class).getResultList();
    }

    public void update(Evento evento) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(evento);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public void delete(Evento evento) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(em.contains(evento) ? evento : em.merge(evento));
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

    public Evento findByFecha(LocalDate fecha) {
        try {
            return em.createQuery("SELECT e FROM Evento e WHERE e.fecha_evento = :fecha", Evento.class)
                    .setParameter("fecha", fecha)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Evento findUltimoEvento() {
        try {
            return em.createQuery("SELECT e FROM Evento e ORDER BY e.fecha_evento DESC", Evento.class)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
