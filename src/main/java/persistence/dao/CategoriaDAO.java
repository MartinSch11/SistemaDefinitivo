package persistence.dao;

import model.Categoria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class CategoriaDAO {
    private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("pasteleriaPU");

    public List<Categoria> findAll() {
        EntityManager em = entityManagerFactory.createEntityManager();
        List<Categoria> categorias = null;
        try {
            categorias = em.createQuery("FROM Categoria", Categoria.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace(); // Manejo básico de excepciones
        } finally {
            em.close(); // Asegurarse de cerrar el entityManager
        }
        return categorias;
    }

    public Categoria findByName(String nombre) {
        EntityManager em = entityManagerFactory.createEntityManager(); // Crear entityManager
        Categoria categoria = null;
        try {
            categoria = em.createQuery("SELECT c FROM Categoria c WHERE c.nombre = :nombre", Categoria.class)
                    .setParameter("nombre", nombre)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace(); // Manejo básico de excepciones
        } finally {
            em.close(); // Asegurarse de cerrar el entityManager
        }
        return categoria;
    }
}
