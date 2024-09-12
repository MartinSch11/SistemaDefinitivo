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

    public Sabor findByName(String nombre) {
        EntityManager em = entityManagerFactory.createEntityManager(); // Crear entityManager
        Sabor sabor = null;
        try {
            sabor = em.createQuery("SELECT s FROM Sabor s WHERE s.sabor = :nombre", Sabor.class)
                    .setParameter("nombre", nombre)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace(); // Manejo básico de excepciones
        } finally {
            em.close(); // Asegurarse de cerrar el entityManager
        }
        return sabor;
    }
}
