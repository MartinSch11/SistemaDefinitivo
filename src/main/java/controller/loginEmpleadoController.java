package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utilities.Paths;

public class loginEmpleadoController {

    @FXML
    private Button btnAceptarEmpleado;

    @FXML
    private Button btnCancelarEmpleado;

    @FXML
    private Button btnVolverDeEmpleado;

    @FXML
    private Label errorLabel;

    @FXML
    private PasswordField passwordFieldEmpleado;

    @FXML
    void handleVolverEmpleado(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(Paths.LOGIN));
            AnchorPane root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());
            Stage stage = (Stage) btnVolverDeEmpleado.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleAceptarEmpleado(ActionEvent event) {
        String password = passwordFieldEmpleado.getText();
        if (validatePassword(password)) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(Paths.EMPLEADO_MAINMENU));
                AnchorPane root = fxmlLoader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) btnAceptarEmpleado.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Datos erróneos. Inténtelo de nuevo.");
        }
    }

    private boolean validatePassword(String password) {
        String correctPassword = "empleado";
        return password.equals(correctPassword);
    }

}

