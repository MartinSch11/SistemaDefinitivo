package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
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
    @FXML private TextField passwordTextField;
    @FXML private Button togglePasswordBtn;
    @FXML private ImageView ojoImageView;

    private final CredencialesDAO credencialesDAO = new CredencialesDAO();
    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        // Restringir dniField a solo números
        dniField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {  // Permitir solo dígitos
                dniField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Sincronizar ambos campos
        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            if (!passwordVisible) passwordTextField.setText(newText);
        });
        passwordTextField.textProperty().addListener((obs, oldText, newText) -> {
            if (passwordVisible) passwordField.setText(newText);
        });

        // Inicializar ícono
        setOjoIcon(false);

        // Presionar "Enter" en el campo de contraseña ejecuta handleLogin
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleLogin(new ActionEvent());
            }
        });
        passwordTextField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleLogin(new ActionEvent());
            }
        });
    }

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

    @FXML
    void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
        }
        setOjoIcon(passwordVisible);
    }

    private void setOjoIcon(boolean visible) {
        // Ajuste de ruta para recursos en Windows y estructura del proyecto
        String iconPath = visible ? "/com.example.image/ojo_abierto.png" : "/com.example.image/ojo_cerrado.png";
        java.io.InputStream is = getClass().getResourceAsStream(iconPath);
        if (is != null) {
            ojoImageView.setImage(new javafx.scene.image.Image(is));
        } else {
            // Si no encuentra la imagen, no lanza excepción
            ojoImageView.setImage(null);
        }
    }
}
