package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import utilities.MenuItemSceneStrategy;
import utilities.Paths;
import utilities.SceneLoader;
import utilities.NodeSceneStrategy;

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

    @FXML
    void handleAyuda(ActionEvent event) {
        SceneLoader.loadScene(new MenuItemSceneStrategy(adminLogin), Paths.AYUDA, "/css/components.css", true);
    }

    @FXML
    void changeUser(ActionEvent event) {
        SceneLoader.loadScene(new MenuItemSceneStrategy(adminLogin), Paths.LOGIN, "/css/login.css", true);
    }

    @FXML
    void handleSettings(ActionEvent event) {
        SceneLoader.loadScene(new MenuItemSceneStrategy(adminLogin), Paths.SETTINGS, "/css/components.css", true);
    }

    @FXML
    void handleEventos(ActionEvent event) {
        SceneLoader.loadScene(new NodeSceneStrategy(btnEventos), Paths.EVENTOS, "/css/eventos.css", true);
    }

    @FXML
    void handleProductos(ActionEvent event) {
        SceneLoader.loadScene(new NodeSceneStrategy(btnProductos), Paths.PRODUCTOS, "/css/productos.css", true);
    }

    @FXML
    void handlePedidos(ActionEvent event) {
        SceneLoader.loadScene(new NodeSceneStrategy(btnPedidos), Paths.PEDIDOS, "/css/components.css", true);
    }

    @FXML
    void handleProveedores(ActionEvent event)  {
        SceneLoader.loadScene(new NodeSceneStrategy(btnProveedores), Paths.PROVEEDORES, "/css/components.css", true);
    }

    @FXML
    void handleEstadistica(ActionEvent event) {
        SceneLoader.loadScene(new NodeSceneStrategy(btnEstadistica), Paths.ESTADISTICA, "/css/components.css", true);
    }

    @FXML
    void handleStock(ActionEvent event) {
        SceneLoader.loadScene(new NodeSceneStrategy(btnStock), Paths.STOCK, "/css/components.css", true);
    }

    @FXML
    void handleRecetas(ActionEvent event){
        SceneLoader.loadScene(new NodeSceneStrategy(btnRecetas), Paths.RECETAS, "/css/components.css", true);
    }
}
