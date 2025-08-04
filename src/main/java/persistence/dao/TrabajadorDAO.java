package persistence.dao;

import jakarta.persistence.*;
import model.Trabajador;
import java.util.List;
import utilities.JpaUtil;

public class TrabajadorDAO {
    public void save(Trabajador trabajador) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(trabajador);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive())
                transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void update(Trabajador trabajador) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(trabajador);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive())
                transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void delete(Trabajador trabajador) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Long count = em
                    .createQuery("SELECT COUNT(p) FROM Pedido p WHERE p.empleadoAsignado = :trabajador", Long.class)
                    .setParameter("trabajador", trabajador)
                    .getSingleResult();
            if (count > 0) {
                throw new RuntimeException("No se puede eliminar el trabajador porque tiene pedidos asignados.");
            }
            em.remove(em.contains(trabajador) ? trabajador : em.merge(trabajador));
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive())
                transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public Trabajador findByDNI(String dniEmpleado) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT e FROM Trabajador e WHERE e.dni = :dniEmpleado", Trabajador.class)
                    .setParameter("dniEmpleado", dniEmpleado)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<String> findAllNombres() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT t.nombre FROM Trabajador t", String.class).getResultList();
        } finally {
            em.close();
        }
    }

    // busca nombres
    public Trabajador findByNombre(String nombre) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT t FROM Trabajador t LEFT JOIN FETCH t.rol WHERE t.nombre = :nombre", Trabajador.class)
                    .setParameter("nombre", nombre)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    public List<Trabajador> findAll() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT t FROM Trabajador t", Trabajador.class).getResultList();
        } finally {
            em.close();
        }
    }

    public String findNombreById(Integer id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT t.nombre FROM Trabajador t WHERE t.id = :id", String.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            return "";
        } finally {
            em.close();
        }
    }

    public String findDniByNombre(String nombre) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT t.dni FROM Trabajador t WHERE t.nombre = :nombre", String.class)
                    .setParameter("nombre", nombre)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public String findNombreByDni(String dni) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT t.nombre FROM Trabajador t WHERE t.dni = :dni", String.class)
                    .setParameter("dni", dni)
                    .getSingleResult();
        } catch (NoResultException e) {
            return "";
        } finally {
            em.close();
        }
    }
}
