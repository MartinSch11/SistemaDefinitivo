package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import model.SessionContext;
import utilities.*;

import java.util.ArrayList;
import java.util.List;

public class MainMenuController {

    @FXML private MenuButton adminLogin;
    @FXML private MenuItem btnCambiarUsuario;
    @FXML private MenuItem btnSettings;
    @FXML private MenuItem btnAyuda;
    @FXML private Button btnProductos;
    @FXML private Button btnEventos;
    @FXML private Button btnPedidos;
    @FXML private Button btnProveedores;
    @FXML private Button btnEstadistica;
    @FXML private Button btnStock;
    @FXML private Button btnRecetas;
    @FXML private Button btnAgenda;
    @FXML private Label lblRol;

    public void initialize() {
        // Asegurarse de que el nombre de usuario y el rol estén establecidos
        SessionContext session = SessionContext.getInstance();

        // Verificar que los valores no sean null
        String userName = session.getUserName() != null ? session.getUserName() : "Desconocido";
        String roleName = session.getRoleName() != null ? session.getRoleName() : "Sin rol";

        // Registrar un único log de inicio de sesión con valores válidos
        ActionLogger.log("El usuario ha iniciado sesión.");

        // Actualizar la interfaz con los valores validados
        setUserNameAndRole(userName, roleName);

        // Configurar permisos
        configurarPermisos(session.getPermisos());
    }


    public void setUserNameAndRole(String userName, String roleName) {
        adminLogin.setText(userName); // Muestra el nombre del trabajador en el MenuButton
        lblRol.setText(roleName);     // Muestra el rol en el Label
    }

    public void configurarPermisos(List<String> permisos) {
        if (permisos == null) {
            permisos = new ArrayList<>(); // Initialize as empty if null
        }
        btnEstadistica.setDisable(!permisos.contains("Estadística"));
        btnEventos.setDisable(!permisos.contains("Eventos"));
        btnPedidos.setDisable(!permisos.contains("Pedidos"));
        btnRecetas.setDisable(!permisos.contains("Recetas"));
        btnProductos.setDisable(!permisos.contains("Productos"));
        btnProveedores.setDisable(!permisos.contains("Proveedores"));
        btnStock.setDisable(!permisos.contains("Stock"));
        btnAgenda.setDisable(!permisos.contains("Agenda"));
    }

    @FXML
    void handleAyuda(ActionEvent event) {
        ActionLogger.log("El usuario accedió a la sección de Ayuda");
        SceneLoader.loadScene(new MenuItemSceneStrategy(adminLogin), Paths.AYUDA, "/css/components.css", true);
    }

    @FXML
    void changeUser(ActionEvent event) {
        ActionLogger.log("El usuario cerró sesión");
        SceneLoader.loadScene(new MenuItemSceneStrategy(adminLogin), Paths.LOGIN, "/css/components.css", false);
    }

    @FXML
    void handleSettings(ActionEvent event) {
        ActionLogger.log("El usuario accedió a la sección de Configuración");
        SceneLoader.loadScene(new MenuItemSceneStrategy(adminLogin), Paths.SETTINGS, "/css/components.css", true);
    }

    @FXML
    void handleEventos(ActionEvent event) {
        ActionLogger.log("Accedió a la sección de Eventos");
        SceneLoader.loadScene(new NodeSceneStrategy(btnEventos), Paths.EVENTOS, "/css/eventos.css", true);
    }

    @FXML
    void handleProductos(ActionEvent event) {
        ActionLogger.log("Accedió a la sección de Productos");
        SceneLoader.loadScene(new NodeSceneStrategy(btnProductos), Paths.PRODUCTOS, "/css/productos.css", true);
    }

    @FXML
    void handlePedidos(ActionEvent event) {
        ActionLogger.log("Accedió a la sección de Pedidos");
        SceneLoader.loadScene(new NodeSceneStrategy(btnPedidos), Paths.PEDIDOS, "/css/components.css", true);
    }

    @FXML
    void handleProveedores(ActionEvent event)  {
        ActionLogger.log("Accedió a la sección de Proveedores");
        SceneLoader.loadScene(new NodeSceneStrategy(btnProveedores), Paths.PROVEEDORES, "/css/components.css", true);
    }

    @FXML
    void handleEstadistica(ActionEvent event) {
        ActionLogger.log("Accedió a la sección de Estadísticas");
        SceneLoader.loadScene(new NodeSceneStrategy(btnEstadistica), Paths.ESTADISTICA, "/css/components.css", true);
    }

    @FXML
    void handleStock(ActionEvent event) {
        ActionLogger.log("Accedió a la sección de Stock");
        SceneLoader.loadScene(new NodeSceneStrategy(btnStock), Paths.STOCK, "/css/components.css", true);
    }

    @FXML
    void handleRecetas(ActionEvent event){
        ActionLogger.log("Accedió a la sección de Recetas");
        SceneLoader.loadScene(new NodeSceneStrategy(btnRecetas), Paths.RECETAS, "/css/components.css", true);
    }

    @FXML
    void viewAgenda(ActionEvent event) {
        ActionLogger.log("Accedió a la sección de Agenda");
        SceneLoader.loadScene(new NodeSceneStrategy(btnAgenda), Paths.AGENDA, "/css/components.css", true);
    }

}
