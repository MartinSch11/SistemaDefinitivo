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
import utilities.SceneLoader;

public class loginAdminController {

    @FXML
    private Button btnAceptar; // Añadido para manejar el botón "Aceptar"

    @FXML
    private PasswordField passwordField; // Añadido para manejar el campo de contraseña

    @FXML
    private Label errorLabel; // Añadido para mostrar mensajes de error

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.LOGIN, "/css/login.css", true);
    }

    @FXML
    void handleAceptar(ActionEvent event) {
        String password = passwordField.getText();
        if (validatePassword(password)) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(Paths.ADMIN_MAINMENU));
                AnchorPane root = fxmlLoader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) btnAceptar.getScene().getWindow();
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
        // Implementa aquí la lógica de validación de la contraseña
        // Por ejemplo, podrías comparar con una contraseña hardcodeada o consultarla desde una base de datos
        String correctPassword = "admin"; // Cambia esta contraseña según sea necesario
        return password.equals(correctPassword);
    }
}