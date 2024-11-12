package persistence.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.Rol;

import java.util.List;

public class RolesDAO {
    private EntityManagerFactory emf;

    public RolesDAO() {
        emf = Persistence.createEntityManagerFactory("pasteleriaPU");
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public List<Rol> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("FROM Rol", Rol.class).getResultList();
        } finally {
            em.close();
        }
    }
}
