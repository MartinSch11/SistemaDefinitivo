package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import utilities.Paths;
import utilities.SceneLoader;
import utilities.NodeSceneStrategy;

public class MainMenuController {

    @FXML
    private Button adminLogin;
    @FXML
    private Button btnProductos;
    @FXML
    private Button btnEventos;
    @FXML
    private Button btnAyuda;
    @FXML
    private Button btnPedidos;

    @FXML
    void handleAyuda(ActionEvent event) {
        SceneLoader.loadScene(new NodeSceneStrategy(btnAyuda), Paths.AYUDA, "/css/components.css", false);
    }

    @FXML
    void changeUser(ActionEvent event) {
         SceneLoader.loadScene(new NodeSceneStrategy(adminLogin), Paths.LOGIN, "/css/login.css", true);
    }

    @FXML
    void handleEventos(ActionEvent event) {
        SceneLoader.loadScene(new NodeSceneStrategy(btnEventos), Paths.EVENTOS, "/css/eventos.css", true);
    }

    @FXML
    void handleProductos(ActionEvent event) {
        SceneLoader.loadScene(new NodeSceneStrategy(btnProductos), Paths.PRODUCTOS, "/css/productos.css", true);
    }
}
