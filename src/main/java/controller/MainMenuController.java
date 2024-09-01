package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utilities.Paths;

public class MainMenuController {
    public Button btnPedidos;
    @FXML
    private Button btnEventos;

    @FXML
    void handleEventos(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Paths.EVENTOS));
            AnchorPane root = loader.load();
            Scene scene = new Scene(root);

            // Cargar estilos específicos para la escena de login admin
            scene.getStylesheets().add(getClass().getResource("/css/eventos.css").toExternalForm());

            Stage stage = (Stage) btnEventos.getScene().getWindow();
            stage.setMaximized(true);
            stage.setScene(scene);
            stage.show();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
