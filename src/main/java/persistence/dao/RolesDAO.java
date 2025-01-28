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

    public List<String> obtenerPermisosPorRol(int idRol) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            """
                                    SELECT p.nombre 
                                    FROM RolPermiso rp 
                                    JOIN rp.permiso p 
                                    WHERE rp.idRol = :idRol
                                    """, String.class)
                    .setParameter("idRol", idRol)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Integer obtenerIdRolPorNombre(String nombreRol) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT r.id FROM Rol r WHERE r.nombre = :nombreRol", Integer.class)
                    .setParameter("nombreRol", nombreRol)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Retorna null si no se encuentra el rol
        } finally {
            em.close();
        }
    }

    public void actualizarPermiso(int idRol, String permiso, boolean concedido) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            if (concedido) {
                // Agregar permiso si no existe
                em.createNativeQuery(
                                "INSERT INTO rol_permiso (id_rol, id_permiso) " +
                                        "SELECT :idRol, p.id_permiso " +
                                        "FROM permisos p WHERE p.nombre = :permiso " +
                                        "ON DUPLICATE KEY UPDATE id_rol = id_rol"
                        ).setParameter("idRol", idRol)
                        .setParameter("permiso", permiso)
                        .executeUpdate();
            } else {
                // Eliminar permiso si está presente
                em.createNativeQuery(
                                "DELETE FROM rol_permiso " +
                                        "WHERE id_rol = :idRol AND id_permiso = " +
                                        "(SELECT id_permiso FROM permisos WHERE nombre = :permiso)"
                        ).setParameter("idRol", idRol)
                        .setParameter("permiso", permiso)
                        .executeUpdate();
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public String obtenerNombreRolPorId(Integer idRol) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT r.nombre FROM Rol r WHERE r.id = :idRol", String.class)
                    .setParameter("idRol", idRol)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return "Desconocido"; // Valor predeterminado si no encuentra el rol
        } finally {
            em.close();
        }
    }
}