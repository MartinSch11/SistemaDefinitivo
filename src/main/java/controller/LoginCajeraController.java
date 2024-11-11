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

public class LoginCajeraController {
    @FXML
    private Button btnAceptar;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.LOGIN, "/css/login.css", true);
    }

    @FXML
    private void initialize() {
        // Establecer el foco en el passwordField al cargar la ventana
        passwordField.requestFocus();
    }

    @FXML
    void handleAceptar(ActionEvent event) {
        String password = passwordField.getText();
        if (validatePassword(password)) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(Paths.CAJERA_MAINMENU));
                AnchorPane root = fxmlLoader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) btnAceptar.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Menú Principal"); // Puedes poner el título que desees aquí
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Datos erróneos. Inténtelo de nuevo.");
        }
    }

    private boolean validatePassword(String password) {
        String correctPassword = "cajera";
        return password.equals(correctPassword);
    }

}
