package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import model.Rol;
import persistence.dao.RolesDAO;
import utilities.ActionLogger;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ControlRoleController implements Initializable {

    @FXML private ComboBox<Rol> cmbRoles;
    @FXML private ToggleButton permisoEstadistica;
    @FXML private ToggleButton permisoEventos;
    @FXML private ToggleButton permisoPedidos;
    @FXML private ToggleButton permisoRecetas;
    @FXML private ToggleButton permisoProductos;
    @FXML private ToggleButton permisoProveedores;
    @FXML private ToggleButton permisoStock;
    @FXML private ToggleButton permisoAgenda;

    private RolesDAO rolesDAO = new RolesDAO();
    private Map<ToggleButton, String> permisosMap = new HashMap<>(); // Asocia botones con permisos

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ActionLogger.log("El usuario accedió a la pantalla de gestión de roles.");
        configurarBotones(); // Configura todos los ToggleButton
        cargarRoles(); // Carga los roles desde la base de datos

        // Escuchar cambios en la selección del ComboBox
        cmbRoles.setOnAction(event -> {
            Rol rolSeleccionado = cmbRoles.getValue();
            if (rolSeleccionado != null) {
                ActionLogger.log("El usuario seleccionó el rol: " + rolSeleccionado.getNombre());
            }
            cargarPermisos();
        });

        // Inicializar el estado de los ToggleButtons según la selección inicial
        cargarPermisos();
    }

    private void configurarBotones() {
        permisosMap.put(permisoEstadistica, "Estadística");
        permisosMap.put(permisoEventos, "Eventos");
        permisosMap.put(permisoPedidos, "Pedidos");
        permisosMap.put(permisoRecetas, "Recetas");
        permisosMap.put(permisoProductos, "Productos");
        permisosMap.put(permisoProveedores, "Proveedores");
        permisosMap.put(permisoStock, "Stock");
        permisosMap.put(permisoAgenda, "Agenda");

        permisosMap.forEach((boton, permiso) -> {
            boton.setSelected(false); // Desactivado por defecto
            boton.setText("Sin permiso"); // Texto inicial

            boton.selectedProperty().addListener((observable, oldValue, newValue) -> {
                Rol rolSeleccionado = cmbRoles.getValue();
                if (rolSeleccionado != null && !rolSeleccionado.getNombre().equals("Administrador")) {
                    boton.setText(newValue ? "Con permiso" : "Sin permiso");
                    actualizarPermisoEnBaseDeDatos(rolSeleccionado, permiso, newValue);
                    ActionLogger.log("El usuario " + (newValue ? "asignó" : "revocó") +
                            " el permiso de " + permiso + " al rol: " + rolSeleccionado.getNombre());
                }
            });
        });
    }

    private void actualizarPermisoEnBaseDeDatos(Rol rol, String permiso, boolean concedido) {
        if (rol != null) {
            rolesDAO.actualizarPermiso(rol.getIdRol(), permiso, concedido);
        }
    }

    private void cargarRoles() {
        List<Rol> roles = rolesDAO.findAll(); // Carga los roles desde la base de datos
        cmbRoles.getItems().clear();
        cmbRoles.getItems().addAll(roles);
        ActionLogger.log("Roles cargados desde la base de datos: " + roles.size() + " roles disponibles.");
    }

    private void cargarPermisos() {
        Rol rolSeleccionado = cmbRoles.getValue();
        if (rolSeleccionado != null) {
            if (rolSeleccionado.getNombre().equals("Administrador")) {
                // Administrador: todos los permisos activados y deshabilitados
                permisosMap.forEach((boton, permiso) -> {
                    boton.setDisable(true);
                    boton.setSelected(true);
                    boton.setText("Con permiso");
                });
                ActionLogger.log("Se activaron todos los permisos para el rol Administrador.");
            } else {
                // Otros roles: habilitar botones y cargar permisos desde la base de datos
                deshabilitarPermisos(false);
                List<String> permisosRol = rolesDAO.obtenerPermisosPorRol(rolSeleccionado.getIdRol());
                actualizarPermisos(permisosRol);
                ActionLogger.log("Se cargaron permisos para el rol: " + rolSeleccionado.getNombre());
            }
        } else {
            // Si no hay rol seleccionado, deshabilitar los botones y resetear el estado
            deshabilitarPermisos(true);
            ActionLogger.log("No se seleccionó ningún rol. Los permisos han sido deshabilitados.");
        }
    }

    private void deshabilitarPermisos(boolean deshabilitar) {
        permisosMap.keySet().forEach(boton -> {
            boton.setDisable(deshabilitar); // Deshabilitar o habilitar el botón
            if (deshabilitar) {
                boton.setSelected(false); // Desmarcar los botones cuando se deshabilitan
                boton.setText("Sin permiso"); // Reiniciar el texto
            }
        });
    }

    private void actualizarPermisos(List<String> permisosRol) {
        permisosMap.forEach((boton, permiso) -> {
            boolean tienePermiso = permisosRol.contains(permiso);
            boton.setSelected(tienePermiso);
            ActionLogger.log("El permiso de " + permiso + " está " + (tienePermiso ? "activado" : "desactivado") +
                    " para el rol seleccionado.");
        });
    }
}
