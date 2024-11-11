package persistence.dao;

import jakarta.persistence.*;
import model.Producto;

import java.util.List;
import java.util.logging.Logger;

public class ProductoDAO {
    private static final Logger LOGGER = Logger.getLogger(ProductoDAO.class.getName());
    private static EntityManagerFactory emf;
    private EntityManager em;

    static {
        emf = Persistence.createEntityManagerFactory("pasteleriaPU"); // Asegúrate de que el nombre coincida con el de tu persistence.xml
    }

    public ProductoDAO() {
        em = emf.createEntityManager();
    }

    public void save(Producto producto) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            // Si el producto tiene una receta, persiste la receta también (esto sucede gracias a CascadeType.PERSIST)
            em.persist(producto);

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public Producto findById(int id) {
        try {
            return em.createQuery("SELECT p FROM Producto p WHERE p.id = :id", Producto.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Devuelve null si no se encuentra el producto
        } catch (Exception e) {
            LOGGER.severe("Error al buscar producto por ID: " + e.getMessage());
            return null;
        }
    }

    public List<Producto> findAll() {
        String hql = "SELECT p FROM Producto p LEFT JOIN FETCH p.receta";
        return em.createQuery(hql, Producto.class).getResultList(); // Cambié 'entityManager' por 'em'
    }

    public void update(Producto producto) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            // Si el producto tiene una receta, se actualizará automáticamente (gracias a CascadeType.MERGE)
            em.merge(producto);

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public void delete(Producto producto) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            em.remove(em.contains(producto) ? producto : em.merge(producto));

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public void close() {
        em.close();
    }
}
