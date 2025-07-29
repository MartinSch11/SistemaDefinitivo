package persistence.dao;

import jakarta.persistence.*;
import model.Pedido;
import model.PedidoProducto;
import java.util.List;
import utilities.JpaUtil;

public class PedidoDAO {
    public void save(Pedido pedido) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            if (pedido.getNumeroPedido() == null) {
                em.persist(pedido);
            } else {
                pedido = em.merge(pedido);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
            throw new RuntimeException("No se pudo guardar el pedido: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void update(Pedido pedido) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Pedido managedPedido = em.find(Pedido.class, pedido.getNumeroPedido());
            if (managedPedido != null) {
                managedPedido.setCliente(pedido.getCliente());
                managedPedido.setEmpleadoAsignado(pedido.getEmpleadoAsignado());
                managedPedido.setFormaEntrega(pedido.getFormaEntrega());
                managedPedido.setFechaEntrega(pedido.getFechaEntrega());
                managedPedido.setFechaEntregado(pedido.getFechaEntregado());
                managedPedido.setTotalPedido(pedido.getTotalPedido());
                managedPedido.setEstadoPedido(pedido.getEstadoPedido());
                managedPedido.getPedidoProductos().clear();
                for (PedidoProducto pp : pedido.getPedidoProductos()) {
                    pp.setPedido(managedPedido);
                    managedPedido.getPedidoProductos().add(pp);
                }
            }
            em.merge(managedPedido);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public Pedido findByNumeroPedido(Long numeroPedido) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Pedido.class, numeroPedido);
        } finally {
            em.close();
        }
    }

    public List<Pedido> findAll() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.pedidoProductos pp LEFT JOIN FETCH pp.producto",
                    Pedido.class).getResultList();
        } finally {
            em.close();
        }
    }

    public void delete(Pedido pedido) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.remove(em.contains(pedido) ? pedido : em.merge(pedido));
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public List<PedidoProducto> obtenerPedidoProductosPorPedido(Long idPedido) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT pp FROM PedidoProducto pp WHERE pp.pedido.numeroPedido = :idPedido",
                            PedidoProducto.class).setParameter("idPedido", idPedido)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void deletePedidoProducto(PedidoProducto pedidoProducto) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            PedidoProducto managed = em.find(PedidoProducto.class, pedidoProducto.getId());
            if (managed != null) {
                em.remove(managed);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<Pedido> findByEstado(String estado) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT p FROM Pedido p " +
                                    "LEFT JOIN FETCH p.pedidoProductos pp " +
                                    "LEFT JOIN FETCH pp.producto " +
                                    "LEFT JOIN FETCH p.empleadoAsignado ea " +
                                    "LEFT JOIN FETCH ea.rol " +
                                    "WHERE p.estadoPedido = :estado",
                            Pedido.class)
                    .setParameter("estado", estado)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
