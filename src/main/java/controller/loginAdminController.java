package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utilities.Paths;

public class loginAdminController {

    @FXML
    private Button btnVolver;

    @FXML
    void handleVolver(ActionEvent event) {
        try {
            // Usar fxmlLoader en lugar de loader
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(Paths.LOGIN));
            AnchorPane root = fxmlLoader.load(); // Cambié loader por fxmlLoader
            Scene scene = new Scene(root);

            // Verifica que la ruta al archivo CSS sea correcta
            scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
