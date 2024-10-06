package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import utilities.NodeSceneStrategy;
import utilities.Paths;
import utilities.SceneLoader;

public class MainMenuCajeraController {
    @FXML
    private Button btnAyuda;
    @FXML
    private Button btnAgenda;
    @FXML
    private Button adminLogin;

    @FXML
    void handleAyuda(ActionEvent event) {
        SceneLoader.loadScene(new NodeSceneStrategy(btnAyuda), Paths.AYUDA, "/css/components.css", false);
    }

    @FXML
    void changeUser(ActionEvent event) {
        SceneLoader.loadScene(new NodeSceneStrategy(adminLogin), Paths.LOGIN, "/css/login.css", true);
    }

    @FXML
    void viewAgenda(ActionEvent event) {
        SceneLoader.loadScene(new NodeSceneStrategy(btnAgenda), Paths.AGENDA, "/css/components.css", false);
    }
}
