package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import persistence.dao.CredencialesDAO;
import utilities.Paths;

public class LoginController {

    @FXML
    private TextField dniField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button btnLogin;
    @FXML
    private Label errorLabel;

    private CredencialesDAO credencialesDAO = new CredencialesDAO();

    @FXML
    void handleLogin(ActionEvent event) {
        String dni = dniField.getText().trim();
        String password = passwordField.getText().trim();

        if (dni.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Por favor, ingrese ambos campos.");
            return;
        }

        Integer idRol = credencialesDAO.validateCredentials(dni, password);
        if (idRol != null) {
            loadMainMenu(idRol);
        } else {
            errorLabel.setText("Datos erróneos. Inténtelo de nuevo.");
        }
    }

    private void loadMainMenu(Integer idRol) {
        try {
            String path;
            switch (idRol) {
                case 1: // Admin
                    path = Paths.ADMIN_MAINMENU;
                    break;
                case 2: // Cajera
                    path = Paths.CAJERA_MAINMENU;
                    break;
                case 3: // Empleado
                    path = Paths.EMPLEADO_MAINMENU;
                    break;
                default:
                    throw new IllegalArgumentException("Rol desconocido: " + idRol);
            }

            // Cargar la vista correspondiente
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            AnchorPane root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            errorLabel.setText("Hubo un error al cargar la vista.");
            e.printStackTrace(); // Puedes imprimir más detalles en el log
        }

    }
}
