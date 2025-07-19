package persistence.dao;

import jakarta.persistence.*;
import model.Trabajador;
import java.util.List;

public class TrabajadorDAO {
    private EntityManagerFactory emf;
    private EntityManager em;

    public TrabajadorDAO() {
        emf = Persistence.createEntityManagerFactory("pasteleriaPU"); // Asegúrate de que el nombre coincida con el de tu persistence.xml
        em = emf.createEntityManager();
    }

    public void save(Trabajador trabajador) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(trabajador);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public void update(Trabajador trabajador) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(trabajador);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }

    public void delete(Trabajador trabajador) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            // Verificar si el trabajador tiene pedidos asociados
            Long count = em.createQuery("SELECT COUNT(p) FROM Pedido p WHERE p.empleadoAsignado = :trabajador", Long.class)
                    .setParameter("trabajador", trabajador)
                    .getSingleResult();

            if (count > 0) {
                throw new RuntimeException("No se puede eliminar el trabajador porque tiene pedidos asignados.");
            }

            em.remove(em.contains(trabajador) ? trabajador : em.merge(trabajador));
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        }
    }


    public void close() {
        em.close();
        emf.close();
    }

    public Trabajador findByDNI(String dniEmpleado) {
        try {
            return em.createQuery("SELECT e FROM Trabajador e WHERE e.dni = :dniEmpleado", Trabajador.class)
                    .setParameter("dniEmpleado", dniEmpleado)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    //lista nombres
    public List<String> findAllNombres() {
        return em.createQuery("SELECT t.nombre FROM Trabajador t", String.class).getResultList();
    }

    //busca nombres
    public Trabajador findByNombre(String nombre) {
        try {
            List<Trabajador> resultados = em.createQuery(
                            "SELECT t FROM Trabajador t WHERE t.nombre = :nombre", Trabajador.class)
                    .setParameter("nombre", nombre)
                    .getResultList();

            return resultados.isEmpty() ? null : resultados.get(0); // Retorna el primero o null si no hay resultados
        } catch (Exception e) {
            return null;
        }
    }

    public List<Trabajador> findAll() {
        return em.createQuery("SELECT t FROM Trabajador t", Trabajador.class).getResultList();
    }

    public String findNombreById(Integer id) {
        try {
            return em.createQuery("SELECT t.nombre FROM Trabajador t WHERE t.id = :id", String.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            return "";
        }
    }
}
