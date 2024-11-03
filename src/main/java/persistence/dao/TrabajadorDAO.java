package persistence.dao;

import jakarta.persistence.*;
import model.Evento;
import model.Trabajador;

import java.time.LocalDate;
import java.util.List;

public class TrabajadorDAO {
    private EntityManagerFactory emf;
    private EntityManager em;

    public TrabajadorDAO() {
        emf = Persistence.createEntityManagerFactory("pasteleriaPU"); // Asegúrate de que el nombre coincida con el de tu persistence.xml
        em = emf.createEntityManager();
    }

    public void save(Trabajador trabajador) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(trabajador);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public void update(Trabajador trabajador) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(trabajador);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public void delete(Trabajador trabajador) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(em.contains(trabajador) ? trabajador : em.merge(trabajador));
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

    public Trabajador findByDNI(String dniEmpleado) {
        try {
            return em.createQuery("SELECT e FROM Trabajador e WHERE e.dni = :dniEmpleado", Trabajador.class)
                    .setParameter("dniEmpleado", dniEmpleado)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    //añadir columna nombre a la base de datos
    public List<String> findAllNombres() {
        return em.createQuery("SELECT t.nombre FROM Trabajador t", String.class).getResultList();
    }

    public List<Trabajador> findAll() {
        return em.createQuery("SELECT t FROM Trabajador t", Trabajador.class).getResultList();
    }
}
