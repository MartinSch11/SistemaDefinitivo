package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Rol;
import model.Trabajador;
import persistence.dao.RolesDAO;
import persistence.dao.TrabajadorDAO;
import persistence.dao.CredencialesDAO;

public class PrimerAdminController {
    @FXML private TextField txtDni;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private ComboBox<String> cmbSexo;
    @FXML private TextField txtContrasena;
    @FXML private TextField txtRol;
    @FXML private Button btnRegistrar;

    private final RolesDAO rolesDAO = new RolesDAO();
    private final TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    private final CredencialesDAO credencialesDAO = new CredencialesDAO();

    @FXML
    public void initialize() {
        cmbSexo.getItems().addAll("Masculino", "Femenino", "Otro");
        txtRol.setText("Administrador");
        txtRol.setEditable(false);
        btnRegistrar.setOnAction(_ -> registrarPrimerAdmin());
    }

    private void registrarPrimerAdmin() {
        try {
            // Validación básica
            if (txtDni.getText().isEmpty() || txtNombre.getText().isEmpty() || txtDireccion.getText().isEmpty() ||
                txtTelefono.getText().isEmpty() || cmbSexo.getValue() == null || txtContrasena.getText().isEmpty()) {
                mostrarAlerta(Alert.AlertType.ERROR, "Campos obligatorios", "Por favor, complete todos los campos.");
                return;
            }

            // Verificar/crear rol Administrador
            Rol rolAdmin = rolesDAO.findByName("Administrador");
            if (rolAdmin == null) {
                rolAdmin = new Rol();
                rolAdmin.setNombre("Administrador");
                rolesDAO.save(rolAdmin);
            }

            // Crear trabajador
            Trabajador trabajador = new Trabajador();
            trabajador.setDni(txtDni.getText());
            trabajador.setNombre(txtNombre.getText());
            trabajador.setDireccion(txtDireccion.getText());
            trabajador.setTelefono(txtTelefono.getText());
            trabajador.setSexo(cmbSexo.getValue());
            trabajador.setRol(rolAdmin);
            trabajadorDAO.save(trabajador);

            // Crear credencial
            model.Credencial credencial = new model.Credencial();
            credencial.setDni(trabajador.getDni());
            credencial.setContraseña(txtContrasena.getText());
            credencial.setTrabajador(trabajador);
            credencialesDAO.save(credencial);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Administrador registrado correctamente.");
            cerrarVentana();
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo registrar el administrador: " + ex.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnRegistrar.getScene().getWindow();
        stage.close();
    }
}
