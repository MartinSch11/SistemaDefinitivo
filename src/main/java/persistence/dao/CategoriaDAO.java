package persistence.dao;

import model.Categoria;
import model.Sabor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class CategoriaDAO {
    private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("pasteleriaPU");

    public List<Categoria> findAll() {
        EntityManager em = entityManagerFactory.createEntityManager();
        List<Categoria> categorias = em.createQuery("FROM Categoria", Categoria.class).getResultList();
        em.close();
        return categorias;
    }

    public static class SaborDAO {
        private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("pasteleriaPU");

        public List<Sabor> findAll() {
            EntityManager em = entityManagerFactory.createEntityManager();
            List<Sabor> sabores = em.createQuery("FROM Sabor", Sabor.class).getResultList();
            em.close();
            return sabores;
        }
    }
}
