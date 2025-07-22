package persistence.dao;

import jakarta.persistence.*;
import model.Combo;
import java.util.List;
import java.util.logging.Logger;

public class ComboDAO {
    private static final Logger LOGGER = Logger.getLogger(ComboDAO.class.getName());
    private static EntityManagerFactory emf;
    private EntityManager em;

    static {
        emf = Persistence.createEntityManagerFactory("pasteleriaPU");
    }

    public ComboDAO() {
        em = emf.createEntityManager();
    }

    public void save(Combo combo) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(combo);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public Combo findById(Long id) {
        try {
            return em.createQuery("SELECT c FROM Combo c LEFT JOIN FETCH c.productos WHERE c.id = :id", Combo.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            LOGGER.severe("Error al buscar combo por ID: " + e.getMessage());
            return null;
        }
    }

    public List<Combo> findAll() {
        String hql = "SELECT DISTINCT c FROM Combo c LEFT JOIN FETCH c.productos";
        return em.createQuery(hql, Combo.class).getResultList();
    }

    public void update(Combo combo) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(combo);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public void delete(Long id) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Combo combo = em.find(Combo.class, id);
            if (combo != null) {
                em.remove(combo);
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }
}
