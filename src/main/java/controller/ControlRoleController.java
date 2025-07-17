package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import model.Rol;
import persistence.dao.RolesDAO;
import utilities.ActionLogger;
import java.net.URL;
import java.util.*;

public class ControlRoleController implements Initializable {

    @FXML private ComboBox<Rol> cmbRoles;
    // ToggleButtons para cada recurso y acción
    @FXML private ToggleButton estadisticaVer, estadisticaCrear;
    @FXML private ToggleButton eventosVer, eventosModificar, eventosEliminar, eventosTodos;
    @FXML private ToggleButton pedidosVer, pedidosModificar, pedidosEliminar, pedidosTodos;
    @FXML private ToggleButton recetasVer, recetasModificar, recetasEliminar, recetasTodos;
    @FXML private ToggleButton productosVer, productosModificar, productosEliminar, productosTodos;
    @FXML private ToggleButton proveedoresVer, proveedoresModificar, proveedoresEliminar, proveedoresTodos;
    @FXML private ToggleButton stockVer, stockModificar, stockEliminar, stockTodos;
    @FXML private ToggleButton agendaVer, agendaModificar, agendaEliminar, agendaTodos;
    @FXML private ToggleButton insumosVer, insumosCrear, insumosModificar, insumosEliminar, insumosTodos;
    @FXML private ToggleButton clientesVer, clientesCrear, clientesModificar, clientesEliminar, clientesTodos;
    @FXML private ToggleButton saboresVer, saboresCrear, saboresModificar, saboresEliminar, saboresTodos;
    @FXML private ToggleButton configEmpleadosVer, configEmpleadosCrear, configEmpleadosModificar, configEmpleadosEliminar, configEmpleadosTodos;

    @FXML private ToggleButton eventosCrear, pedidosCrear, recetasCrear, productosCrear, proveedoresCrear, stockCrear, agendaCrear;
    @FXML private ToggleButton settingsVer;
    @FXML private ToggleButton notificacionesVer;

    private RolesDAO rolesDAO = new RolesDAO();
    // Mapa: (recurso, accion) -> ToggleButton
    private final Map<String, ToggleButton> toggleMap = new HashMap<>();
    // Lista de recursos y acciones
    private final String[] recursos = {"Estadísticas", "Eventos", "Pedidos", "Recetas", "Productos", "Insumos", "Clientes", "Sabores", "Proveedores", "Stock", "Agenda", "Config. Empleados", "Settings", "Notificaciones"};
    private final String[] acciones = {"ver", "crear", "modificar", "eliminar", "todos"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ActionLogger.log("El usuario accedió a la pantalla de gestión de roles.");
        mapearToggles();
        cargarRoles();
        cmbRoles.setOnAction(event -> cargarPermisos());
        cargarPermisos();
    }

    private void mapearToggles() {
        // Mapea cada ToggleButton a su clave "Recurso-Accion"
        toggleMap.put("Estadísticas-ver", estadisticaVer);
        toggleMap.put("Estadísticas-crear", estadisticaCrear);
        toggleMap.put("Eventos-ver", eventosVer);
        toggleMap.put("Eventos-crear", eventosCrear);
        toggleMap.put("Eventos-modificar", eventosModificar);
        toggleMap.put("Eventos-eliminar", eventosEliminar);
        toggleMap.put("Eventos-todos", eventosTodos);
        toggleMap.put("Pedidos-ver", pedidosVer);
        toggleMap.put("Pedidos-crear", pedidosCrear);
        toggleMap.put("Pedidos-modificar", pedidosModificar);
        toggleMap.put("Pedidos-eliminar", pedidosEliminar);
        toggleMap.put("Pedidos-todos", pedidosTodos);
        toggleMap.put("Recetas-ver", recetasVer);
        toggleMap.put("Recetas-crear", recetasCrear);
        toggleMap.put("Recetas-modificar", recetasModificar);
        toggleMap.put("Recetas-eliminar", recetasEliminar);
        toggleMap.put("Recetas-todos", recetasTodos);
        toggleMap.put("Productos-ver", productosVer);
        toggleMap.put("Productos-crear", productosCrear);
        toggleMap.put("Productos-modificar", productosModificar);
        toggleMap.put("Productos-eliminar", productosEliminar);
        toggleMap.put("Productos-todos", productosTodos);
        toggleMap.put("Proveedores-ver", proveedoresVer);
        toggleMap.put("Proveedores-crear", proveedoresCrear);
        toggleMap.put("Proveedores-modificar", proveedoresModificar);
        toggleMap.put("Proveedores-eliminar", proveedoresEliminar);
        toggleMap.put("Proveedores-todos", proveedoresTodos);
        toggleMap.put("Stock-ver", stockVer);
        toggleMap.put("Stock-crear", stockCrear);
        toggleMap.put("Stock-modificar", stockModificar);
        toggleMap.put("Stock-eliminar", stockEliminar);
        toggleMap.put("Stock-todos", stockTodos);
        toggleMap.put("Agenda-ver", agendaVer);
        toggleMap.put("Agenda-crear", agendaCrear);
        toggleMap.put("Agenda-modificar", agendaModificar);
        toggleMap.put("Agenda-eliminar", agendaEliminar);
        toggleMap.put("Agenda-todos", agendaTodos);
        toggleMap.put("Settings-ver", settingsVer);
        toggleMap.put("Insumos-ver", insumosVer);
        toggleMap.put("Insumos-crear", insumosCrear);
        toggleMap.put("Insumos-modificar", insumosModificar);
        toggleMap.put("Insumos-eliminar", insumosEliminar);
        toggleMap.put("Insumos-todos", insumosTodos);
        toggleMap.put("Clientes-ver", clientesVer);
        toggleMap.put("Clientes-crear", clientesCrear);
        toggleMap.put("Clientes-modificar", clientesModificar);
        toggleMap.put("Clientes-eliminar", clientesEliminar);
        toggleMap.put("Clientes-todos", clientesTodos);
        toggleMap.put("Sabores-ver", saboresVer);
        toggleMap.put("Sabores-crear", saboresCrear);
        toggleMap.put("Sabores-modificar", saboresModificar);
        toggleMap.put("Sabores-eliminar", saboresEliminar);
        toggleMap.put("Sabores-todos", saboresTodos);
        toggleMap.put("Config. Empleados-ver", configEmpleadosVer);
        toggleMap.put("Config. Empleados-crear", configEmpleadosCrear);
        toggleMap.put("Config. Empleados-modificar", configEmpleadosModificar);
        toggleMap.put("Config. Empleados-eliminar", configEmpleadosEliminar);
        toggleMap.put("Config. Empleados-todos", configEmpleadosTodos);
        toggleMap.put("Notificaciones-ver", notificacionesVer);


        // Listener para cada toggle
        for (String recurso : recursos) {
            for (String accion : acciones) {
                ToggleButton toggle = toggleMap.get(recurso + "-" + accion);
                if (toggle != null) {
                    toggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        Rol rol = cmbRoles.getValue();
                        if (rol != null && !rol.getNombre().equals("Administrador")) {
                            // Si se activa "todos", activa los otros
                            if (accion.equals("todos") && newVal) {
                                for (String a : acciones) {
                                    if (!a.equals("todos")) {
                                        ToggleButton t = toggleMap.get(recurso + "-" + a);
                                        if (t != null && !t.isSelected()) t.setSelected(true);
                                    }
                                }
                            }
                            // Si se desactiva "todos", no cambia los otros
                            if (accion.equals("todos") && !newVal) {
                                // No hacer nada especial
                            }
                            // Si se activa/desactiva otro, y todos están activos, activa "todos"
                            if (!accion.equals("todos")) {
                                if (newVal) {
                                    boolean all = true;
                                    for (String a : acciones) {
                                        if (!a.equals("todos")) {
                                            ToggleButton t = toggleMap.get(recurso + "-" + a);
                                            if (t != null && !t.isSelected()) {
                                                all = false; break;
                                            }
                                        }
                                    }
                                    if (all) {
                                        ToggleButton tTodos = toggleMap.get(recurso + "-todos");
                                        if (tTodos != null && !tTodos.isSelected()) tTodos.setSelected(true);
                                    }
                                } else {
                                    // Si se desactiva uno, desactiva "todos"
                                    ToggleButton tTodos = toggleMap.get(recurso + "-todos");
                                    if (tTodos != null && tTodos.isSelected()) tTodos.setSelected(false);
                                }
                            }
                            // Guardar en base de datos
                            rolesDAO.actualizarPermiso(rol.getIdRol(), recurso, accion, newVal);
                            ActionLogger.log("El usuario " + (newVal ? "asignó" : "revocó") + " el permiso " + recurso + "-" + accion + " al rol: " + rol.getNombre());
                        }
                    });
                }
            }
        }
    }

    private void cargarRoles() {
        List<Rol> roles = rolesDAO.findAll();
        cmbRoles.getItems().clear();
        cmbRoles.getItems().addAll(roles);
        ActionLogger.log("Roles cargados desde la base de datos: " + roles.size() + " roles disponibles.");
    }

    private void cargarPermisos() {
        Rol rolSeleccionado = cmbRoles.getValue();
        if (rolSeleccionado != null) {
            if (rolSeleccionado.getNombre().equals("Administrador")) {
                // Todos los permisos activados y deshabilitados
                toggleMap.values().forEach(t -> {
                    t.setDisable(true);
                    t.setSelected(true);
                });
                // Habilitar btnGenerarReportes para Administrador
                try {
                    javafx.scene.control.Button btnGenerarReportes = (javafx.scene.control.Button) cmbRoles.getScene().lookup("#btnGenerarReportes");
                    if (btnGenerarReportes != null) btnGenerarReportes.setDisable(false);
                } catch (Exception ignored) {}
                ActionLogger.log("Se activaron todos los permisos para el rol Administrador.");
            } else {
                // Habilitar toggles y cargar permisos
                toggleMap.values().forEach(t -> {
                    t.setDisable(false);
                    t.setSelected(false);
                });
                List<String> permisosRol = rolesDAO.obtenerPermisosPorRol(rolSeleccionado.getIdRol());
                for (String permiso : permisosRol) {
                    ToggleButton t = toggleMap.get(permiso);
                    if (t != null) t.setSelected(true);
                }
                // Habilitar/deshabilitar btnGenerarReportes según permiso Estadística-crear
                try {
                    javafx.scene.control.Button btnGenerarReportes = (javafx.scene.control.Button) cmbRoles.getScene().lookup("#btnGenerarReportes");
                    boolean puedeCrearEstadistica = permisosRol.contains("Estadísticas-crear");
                    if (btnGenerarReportes != null) btnGenerarReportes.setDisable(!puedeCrearEstadistica);
                } catch (Exception ignored) {}
                ActionLogger.log("Se cargaron permisos para el rol: " + rolSeleccionado.getNombre());
            }
        } else {
            // Deshabilitar todos
            toggleMap.values().forEach(t -> {
                t.setDisable(true);
                t.setSelected(false);
            });
            // Deshabilitar btnGenerarReportes si no hay rol
            try {
                javafx.scene.control.Button btnGenerarReportes = (javafx.scene.control.Button) cmbRoles.getScene().lookup("#btnGenerarReportes");
                if (btnGenerarReportes != null) btnGenerarReportes.setDisable(true);
            } catch (Exception ignored) {}
            ActionLogger.log("No se seleccionó ningún rol. Los permisos han sido deshabilitados.");
        }
    }
}
