package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import utilities.Paths;
import utilities.SceneLoader;

import java.awt.*;

public class SettingsController {
    @FXML
    private Button btnModificarEmpleado;

    @FXML
    private void initialize() {

    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.ADMIN_MAINMENU, "/css/loginAdmin.css", true);
    }

    @FXML
    private Pane contenedorDinamico;
    @FXML
    void CRUDEmpleado(ActionEvent event) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/CrudAnadirEmpleado.fxml"));
            Parent visualizarView = loader.load();
            contenedorDinamico.getChildren().setAll(visualizarView); // Reemplazar el contenido actual con el nuevo
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
