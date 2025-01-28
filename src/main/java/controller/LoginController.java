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
import model.SessionContext;
import persistence.dao.CredencialesDAO;
import persistence.dao.RolesDAO;
import utilities.ActionLogger;
import utilities.Paths;

import java.util.List;

public class LoginController {

    @FXML private TextField dniField;
    @FXML private PasswordField passwordField;
    @FXML private Button btnLogin;
    @FXML private Label errorLabel;

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
            String userName = credencialesDAO.obtenerNombrePorDNI(dni); // Obtiene el nombre del trabajador
            RolesDAO rolDAO = new RolesDAO();
            String roleName = rolDAO.obtenerNombreRolPorId(idRol);      // Obtiene el nombre del rol

            // Guardar en SessionContext
            SessionContext session = SessionContext.getInstance();
            session.setUserName(userName);
            session.setRoleName(roleName);

            // Cargar la vista del menú principal
            loadMainMenu(userName, roleName);
        } else {
            errorLabel.setText("Datos erróneos. Inténtelo de nuevo.");
            // Registrar el log de intento fallido
            ActionLogger.log("Intento de login fallido con DNI: " + dni);
        }
    }

    private void loadMainMenu(String userName, String roleName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Paths.MAINMENU));
            AnchorPane root = loader.load();

            MainMenuController mainMenuController = loader.getController();

            // Configurar el contexto de sesión
            SessionContext session = SessionContext.getInstance();
            session.setUserName(userName);
            session.setRoleName(roleName);

            // Obtener el ID del rol y sus permisos
            RolesDAO rolesDAO = new RolesDAO();
            Integer idRol = rolesDAO.obtenerIdRolPorNombre(roleName);
            if (idRol != null) {
                List<String> permisos = rolesDAO.obtenerPermisosPorRol(idRol);
                session.setPermisos(permisos);

                // Configurar permisos en el menú principal
                mainMenuController.configurarPermisos(permisos);
            }

            // Configurar el nombre del usuario y el rol en la interfaz
            mainMenuController.setUserNameAndRole(userName, roleName);

            Scene scene = new Scene(root);
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            errorLabel.setText("Hubo un error al cargar la vista.");
            e.printStackTrace();
        }
    }
}