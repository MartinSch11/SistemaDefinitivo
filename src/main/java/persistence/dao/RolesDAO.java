package persistence.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.Rol;
import model.Permiso;
import model.RolPermiso;

import java.util.List;
import java.util.stream.Collectors;

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

    // Devuelve una lista de strings tipo "Recurso-Accion" para el rol
    public List<String> obtenerPermisosPorRol(int idRol) {
        EntityManager em = getEntityManager();
        try {
            List<Permiso> permisos = em.createQuery(
                            "SELECT rp.permiso FROM RolPermiso rp WHERE rp.rol.idRol = :idRol", Permiso.class)
                    .setParameter("idRol", idRol)
                    .getResultList();
            return permisos.stream()
                    .map(p -> p.getRecurso() + "-" + p.getAccion())
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    public Integer obtenerIdRolPorNombre(String nombreRol) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT r.idRol FROM Rol r WHERE r.nombre = :nombreRol", Integer.class)
                    .setParameter("nombreRol", nombreRol)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Retorna null si no se encuentra el rol
        } finally {
            em.close();
        }
    }

    // Actualiza el permiso (agrega o elimina) para el rol y el permiso (por recurso y acción)
    public void actualizarPermiso(int idRol, String recurso, String accion, boolean concedido) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            // Buscar el id_permiso correspondiente
            Integer idPermiso = (Integer) em.createQuery(
                            "SELECT p.idPermiso FROM Permiso p WHERE p.recurso = :recurso AND p.accion = :accion")
                    .setParameter("recurso", recurso)
                    .setParameter("accion", accion)
                    .getSingleResult();
            if (concedido) {
                // Agregar si no existe
                Long count = em.createQuery(
                                "SELECT COUNT(rp) FROM RolPermiso rp WHERE rp.rol.idRol = :idRol AND rp.permiso.idPermiso = :idPermiso", Long.class)
                        .setParameter("idRol", idRol)
                        .setParameter("idPermiso", idPermiso)
                        .getSingleResult();
                if (count == 0) {
                    RolPermiso rp = new RolPermiso();
                    rp.setRol(em.find(Rol.class, idRol));
                    rp.setPermiso(em.find(Permiso.class, idPermiso));
                    em.persist(rp);
                }
            } else {
                // Eliminar si existe
                em.createQuery("DELETE FROM RolPermiso rp WHERE rp.rol.idRol = :idRol AND rp.permiso.idPermiso = :idPermiso")
                        .setParameter("idRol", idRol)
                        .setParameter("idPermiso", idPermiso)
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
            return em.createQuery("SELECT r.nombre FROM Rol r WHERE r.idRol = :idRol", String.class)
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