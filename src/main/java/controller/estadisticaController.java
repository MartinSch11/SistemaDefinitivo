package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import utilities.Paths;
import utilities.SceneLoader;

public class estadisticaController {
    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.ADMIN_MAINMENU, "/css/loginAdmin.css", true);
    }
}
