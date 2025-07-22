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
            if (!newValue.matches("\\d*")) { // Permitir solo dígitos
                dniField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Sincronizar ambos campos
        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            if (!passwordVisible)
                passwordTextField.setText(newText);
        });
        passwordTextField.textProperty().addListener((obs, oldText, newText) -> {
            if (passwordVisible)
                passwordField.setText(newText);
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
        String dni = dniField.getText() != null ? dniField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText().trim() : "";

        if (dni.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Por favor, ingrese ambos campos.");
            return;
        }

        try {
            model.Credencial credencial = credencialesDAO.findById(dni);
            if (credencial == null) {
                errorLabel.setText("Usuario no encontrado.");
                ActionLogger.log("Intento de login fallido: usuario no encontrado (DNI: " + dni + ")");
                return;
            }
            if (credencial.getContraseña() == null) {
                errorLabel.setText("Contraseña no establecida para este usuario.");
                ActionLogger.log("Intento de login fallido: contraseña nula (DNI: " + dni + ")");
                return;
            }
            if (!credencial.getContraseña().equals(password)) {
                errorLabel.setText("Datos erróneos. Inténtelo de nuevo.");
                ActionLogger.log("Intento de login fallido: contraseña incorrecta (DNI: " + dni + ")");
                return;
            }
            // Obtener el idRol de la relación con Trabajador
            Integer idRol = null;
            if (credencial.getTrabajador() != null && credencial.getTrabajador().getRol() != null) {
                idRol = credencial.getTrabajador().getRol().getIdRol();
            }
            if (idRol == null) {
                errorLabel.setText("No se pudo determinar el rol del usuario.");
                ActionLogger.log("Intento de login fallido: rol no encontrado (DNI: " + dni + ")");
                return;
            }
            String userName = credencialesDAO.obtenerNombrePorDNI(dni); // Obtiene el nombre del trabajador
            RolesDAO rolDAO = new RolesDAO();
            String roleName = rolDAO.obtenerNombreRolPorId(idRol); // Obtiene el nombre del rol

            // Guardar en SessionContext
            SessionContext session = SessionContext.getInstance();
            session.setUserName(userName);
            session.setRoleName(roleName);

            // Cargar la vista del menú principal
            loadMainMenu(userName, roleName, dni);
        } catch (Exception ex) {
            errorLabel.setText("Error inesperado en el login. Consulte al administrador.");
            ActionLogger.log("Error inesperado en login (DNI: " + dni + "): " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadMainMenu(String userName, String roleName, String dni) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Paths.MAINMENU));
            AnchorPane root = loader.load();

            MainMenuController mainMenuController = loader.getController();

            // Configurar el contexto de sesión
            SessionContext session = SessionContext.getInstance();
            session.setUserName(userName);
            session.setRoleName(roleName);
            String sexo = credencialesDAO.obtenerSexoPorDNI(dni);
            session.setSexo(sexo);

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
            mainMenuController.setUserNameAndRole(userName, roleName, sexo);

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
