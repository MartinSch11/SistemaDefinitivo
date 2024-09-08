package persistence.dao;

import model.Sabor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class SaborDAO {
    private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("pasteleriaPU");

    public List<Sabor> findAll() {
        EntityManager em = entityManagerFactory.createEntityManager();
        List<Sabor> sabores = em.createQuery("FROM Sabor", Sabor.class).getResultList();
        em.close();
        return sabores;
    }
}
