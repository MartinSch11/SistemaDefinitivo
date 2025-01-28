package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import utilities.ActionLogger;
import utilities.Paths;
import javafx.scene.control.Button;
import utilities.SceneLoader;

public class AyudaController {

    @FXML private Button btnVolver;

    @FXML
    void handleVolver(ActionEvent event) {
        ActionLogger.log("El usuario regresó al menú principal desde la pantalla de Ayuda.");
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/loginAdmin.css", true);
    }

}
