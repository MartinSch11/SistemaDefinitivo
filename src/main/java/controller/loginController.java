package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utilities.Paths;

public class loginController {

    @FXML
    private Button btnAdmin;

    @FXML
    private Button btnEmple;

    @FXML
    void click(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Paths.ADMIN_LOGIN));
            AnchorPane root = loader.load();
            Scene scene = new Scene(root);

            // Cargar estilos específicos para la escena de login admin
            scene.getStylesheets().add(getClass().getResource("/css/loginAdmin.css").toExternalForm());

            Stage stage = (Stage) btnAdmin.getScene().getWindow();
            stage.setMaximized(true);
            stage.setScene(scene);
            stage.show();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void clickEmpleado(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Paths.EMPLEADO_LOGIN));
            AnchorPane root = loader.load();
            Scene scene = new Scene(root);

            scene.getStylesheets().add(getClass().getResource("/css/loginEmpleado.css").toExternalForm());

            Stage stage = (Stage) btnEmple.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
