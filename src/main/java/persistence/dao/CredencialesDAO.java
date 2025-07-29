package persistence.dao;

import jakarta.persistence.*;
import model.Credencial;
import utilities.JpaUtil;

public class CredencialesDAO {

    public Integer validateCredentials(String dni, String password) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            String query = "SELECT t.rol.idRol FROM Credencial c JOIN c.trabajador t WHERE c.dni = :dni AND c.contraseña = :password";
            return em.createQuery(query, Integer.class)
                    .setParameter("dni", dni)
                    .setParameter("password", password)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public void save(Credencial credencial) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(credencial);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public String obtenerNombrePorDNI(String dni) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            String query = "SELECT t.nombre FROM Credencial c JOIN c.trabajador t WHERE c.dni = :dni";
            return em.createQuery(query, String.class)
                    .setParameter("dni", dni)
                    .getSingleResult();
        } catch (NoResultException e) {
            return "Usuario";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        } finally {
            em.close();
        }
    }

    public Credencial findById(String dni) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Credencial.class, dni);
        } finally {
            em.close();
        }
    }

    public Credencial findByUsername(String dni) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Credencial c WHERE c.dni = :dni", Credencial.class)
                    .setParameter("dni", dni)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public void update(String dni, String contraseña) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            Credencial credencial = em.find(Credencial.class, dni);
            if (credencial != null) {
                tx.begin();
                credencial.setContraseña(contraseña);
                em.merge(credencial);
                tx.commit();
            }
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void delete(Credencial credencial) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.remove(em.contains(credencial) ? credencial : em.merge(credencial));
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public String obtenerSexoPorDNI(String dni) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            String query = "SELECT t.sexo FROM Credencial c JOIN c.trabajador t WHERE c.dni = :dni";
            return em.createQuery(query, String.class)
                    .setParameter("dni", dni)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }
}
