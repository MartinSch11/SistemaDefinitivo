package persistence.dao;

import jakarta.persistence.EntityManager;
import model.NotificacionConfig;
import utilities.JpaUtil;

public class NotificacionConfigDAO {

    public void save(NotificacionConfig config) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(config);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void update(NotificacionConfig config) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(config);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public NotificacionConfig findById(int id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(NotificacionConfig.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Devuelve la configuración global (id=1) o valores por defecto si no existe.
     */
    public NotificacionConfig findOrDefault() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            // Trae la primera config que exista
            var list = em.createQuery("SELECT c FROM NotificacionConfig c ORDER BY c.id ASC", NotificacionConfig.class)
                    .setMaxResults(1)
                    .getResultList();
            if (!list.isEmpty()) {
                return list.get(0);
            }
            // Si no hay registro, devolvés defaults (compatibles con tu UI actual)
            return new NotificacionConfig(30, 3, 0); // eventos, caducidad, pedidos
        } finally {
            em.close();
        }
    }

    /**
     * Guarda o actualiza la configuración global (id=1).
     */
    public void saveOrUpdateGlobal(NotificacionConfig config) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            if (config.getId() == null) {
                em.persist(config);
            } else {
                em.merge(config);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}