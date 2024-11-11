package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import utilities.MenuItemSceneStrategy;
import utilities.NodeSceneStrategy;
import utilities.Paths;
import utilities.SceneLoader;

public class MainMenuCajeraController {
    @FXML private MenuItem btnCambiarUsuario;
    @FXML private MenuItem btnAyuda;
    @FXML private Button btnAgenda;
    @FXML private MenuButton adminLogin;

    @FXML
    void handleAyuda(ActionEvent event) {
        SceneLoader.loadScene(new MenuItemSceneStrategy(adminLogin), Paths.AYUDA, "/css/components.css", true);
    }

    @FXML
    void changeUser(ActionEvent event) {
        SceneLoader.loadScene(new MenuItemSceneStrategy(adminLogin), Paths.LOGIN, "/css/login.css", true);
    }

    @FXML
    void viewAgenda(ActionEvent event) {
        SceneLoader.loadScene(new NodeSceneStrategy(btnAgenda), Paths.AGENDA, "/css/components.css", true);
    }

}
