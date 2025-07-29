package persistence.dao;

import model.Categoria;
import jakarta.persistence.EntityManager;
import java.util.List;
import utilities.JpaUtil;

public class CategoriaDAO {
    public List<Categoria> findAll() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        List<Categoria> categorias = null;
        try {
            categorias = em.createQuery("FROM Categoria", Categoria.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return categorias;
    }

    public Categoria findByName(String nombre) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        Categoria categoria = null;
        try {
            categoria = em.createQuery("SELECT c FROM Categoria c WHERE c.nombre = :nombre", Categoria.class)
                    .setParameter("nombre", nombre)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return categoria;
    }
}
