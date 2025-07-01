package persistence.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.NotificacionConfig;

public class NotificacionConfigDAO {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("pasteleriaPU");

    public void save(NotificacionConfig config) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(config);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void update(NotificacionConfig config) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(config);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public NotificacionConfig findById(int id) {
        EntityManager em = emf.createEntityManager();
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
        NotificacionConfig config = findById(1);
        if (config != null) {
            return config;
        }
        return new NotificacionConfig(30, 3);
    }

    /**
     * Guarda o actualiza la configuración global (id=1).
     */
    public void saveOrUpdateGlobal(NotificacionConfig config) {
        EntityManager em = emf.createEntityManager();
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