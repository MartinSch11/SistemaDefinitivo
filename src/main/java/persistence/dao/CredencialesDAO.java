package persistence.dao;

import jakarta.persistence.*;
import model.Credencial;

public class CredencialesDAO {
    private EntityManagerFactory emf;
    private EntityManager em;

    public CredencialesDAO() {
        emf = Persistence.createEntityManagerFactory("pasteleriaPU");
        em = emf.createEntityManager();
    }

    public Integer validateCredentials(String dni, String password) {
        try {
            String query = "SELECT t.rol.id FROM Credencial c " + // Accedemos al id del rol a través de la relación
                    "JOIN c.trabajador t " +
                    "WHERE c.dni = :dni AND c.contraseña = :password";
            Integer rolId = (Integer) em.createQuery(query)
                    .setParameter("dni", dni)
                    .setParameter("password", password)
                    .getSingleResult();
            return rolId;
        } catch (NoResultException e) {
            return null;
        }
    }

    public void save(Credencial credencial) {
        em.getTransaction().begin();
        em.persist(credencial); // Guarda las credenciales
        em.getTransaction().commit();
    }

    public String obtenerNombrePorDNI(String dni) {
        try {
            String query = "SELECT t.nombre FROM Credencial c " +
                    "JOIN c.trabajador t " +
                    "WHERE c.dni = :dni";
            return em.createQuery(query, String.class)
                    .setParameter("dni", dni)
                    .getSingleResult();
        } catch (NoResultException e) {
            return "Usuario"; // Valor por defecto en caso de no encontrar el nombre
        } catch (Exception e) {
            e.printStackTrace();
            return "Error"; // Manejo de excepciones generales
        }
    }


    public Credencial findById(String dni) {
        return em.find(Credencial.class, dni);
    }

    public Credencial findByUsername(String dni) {
        try {
            return em.createQuery("SELECT c FROM Credencial c WHERE c.dni = :dni", Credencial.class)
                    .setParameter("dni", dni)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void update(String dni, String contraseña) {
        // Buscamos la credencial por DNI
        Credencial credencial = findById(dni);
        if (credencial != null) {
            // Si la credencial existe, actualizamos la contraseña
            credencial.setContraseña(contraseña);

            // Comienza la transacción para actualizar
            EntityTransaction transaction = em.getTransaction();
            try {
                transaction.begin();
                em.merge(credencial); // Actualiza la credencial
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                e.printStackTrace();
            }
        }
    }


    public void delete(Credencial credencial) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(em.contains(credencial) ? credencial : em.merge(credencial));
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public String obtenerSexoPorDNI(String dni) {
        try {
            String query = "SELECT t.sexo FROM Credencial c JOIN c.trabajador t WHERE c.dni = :dni";
            return em.createQuery(query, String.class)
                    .setParameter("dni", dni)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Si no se encuentra, retorna null
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() {
        em.close();
        emf.close();
    }
}
