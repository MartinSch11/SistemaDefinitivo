package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import java.math.BigDecimal;
import java.time.LocalDate;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.control.Alert.AlertType;
import model.Credencial;
import model.Trabajador;
import model.Rol;
import persistence.dao.CredencialesDAO;
import persistence.dao.TrabajadorDAO;
import persistence.dao.RolesDAO;
import utilities.ActionLogger;

import java.util.List;
import java.util.Optional;

public class CrudAnadirEmpleadoController {
    @FXML private Pane paneAnadirEmpleado;
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;
    @FXML private TextField DNITxtField;
    @FXML private TextField nombreTxtField;
    @FXML private DatePicker dateFechaContratacion;
    @FXML private TextField direccionTxtField;
    @FXML private TextField sueldoTxtField;
    @FXML private TextField telTxtField;
    @FXML private ComboBox<String> cmbSexo;
    @FXML private ComboBox<Rol> cmbRol;
    @FXML private TextField txtContraseña;

    private SettingsController settingsController;

    public void setSettingsController(SettingsController settingsController) {
        this.settingsController = settingsController;
    }

    @FXML
    public void initialize() {
        nombreTxtField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z ]*")) {
                nombreTxtField.setText(newValue.replaceAll("[^a-zA-Z ]", ""));
            }
        });
        telTxtField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                telTxtField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        DNITxtField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                DNITxtField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        sueldoTxtField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[\\d,.]*")) {
                sueldoTxtField.setText(newValue.replaceAll("[^\\d,.]", ""));
            }
        });
        dateFechaContratacion.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #EEEEEE;");
                }
            }
        });
        // Inicializar opciones de sexo
        cmbSexo.getItems().clear();
        cmbSexo.getItems().addAll("Femenino", "Masculino", "Otro");
        cargarRoles();
    }

    @FXML
    private void visibilidadButtons() {
        settingsController.getBtnModificarEmpleado().setVisible(true);
        settingsController.getBtnEliminarEmpleado().setVisible(true);
    }

    private void vaciarTodosCampos() {
        nombreTxtField.clear();
        telTxtField.clear();
        DNITxtField.clear();
        sueldoTxtField.clear();
        direccionTxtField.clear();
        dateFechaContratacion.setValue(null);
        cmbRol.setValue(null);
        txtContraseña.clear();
    }

    private boolean camposObligatorios() {
        return DNITxtField != null && !DNITxtField.getText().isEmpty()
                && nombreTxtField != null && !nombreTxtField.getText().isEmpty()
                && sueldoTxtField != null && !sueldoTxtField.getText().isEmpty()
                && telTxtField != null && !telTxtField.getText().isEmpty()
                && direccionTxtField != null && !direccionTxtField.getText().isEmpty()
                && dateFechaContratacion.getValue() != null
                && txtContraseña != null && !txtContraseña.getText().isEmpty();
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void cargarRoles() {
        RolesDAO rolesDAO = new RolesDAO();
        List<Rol> listaRoles = rolesDAO.findAll();
        cmbRol.getItems().clear();
        cmbRol.getItems().addAll(listaRoles);
    }

    @FXML
    private void guardarDatos() {
        String dniEmpleado = DNITxtField.getText();
        String nombreEmpleado = nombreTxtField.getText();
        String direccionEmpleado = direccionTxtField.getText();
        String telefonoEmpleado = telTxtField.getText();
        BigDecimal sueldoEmpleado = new BigDecimal(sueldoTxtField.getText());
        LocalDate fechaContratoEmpleado = dateFechaContratacion.getValue();
        Rol rolSeleccionado = cmbRol.getValue();
        String contraseña = txtContraseña.getText();

        TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
        CredencialesDAO credencialesDAO = new CredencialesDAO();

        try {
            Trabajador empleadoExistente = trabajadorDAO.findByDNI(dniEmpleado);
            if (empleadoExistente != null) {
                showAlert(Alert.AlertType.ERROR, "Error de Validación", "Ya existe un empleado con ese DNI.");
                return;
            }

            Trabajador nuevoEmpleado = new Trabajador();
            nuevoEmpleado.setDni(dniEmpleado);
            nuevoEmpleado.setNombre(nombreEmpleado);
            nuevoEmpleado.setDireccion(direccionEmpleado);
            nuevoEmpleado.setTelefono(telefonoEmpleado);
            nuevoEmpleado.setSueldo(sueldoEmpleado);
            nuevoEmpleado.setFechaContratacion(fechaContratoEmpleado);
            nuevoEmpleado.setRol(rolSeleccionado);

            trabajadorDAO.save(nuevoEmpleado);

            Credencial credenciales = new Credencial();
            credenciales.setDni(dniEmpleado);
            credenciales.setContraseña(contraseña);
            credencialesDAO.save(credenciales);

            ActionLogger.log("Empleado guardado: " + nuevoEmpleado);

            SettingsController.getInstance().cargarNombresEnComboBox();
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Empleado guardado exitosamente.");
        } catch (Exception e) {
            ActionLogger.log("Error al guardar empleado: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Error al guardar al empleado: " + e.getMessage());
        }
    }

    @FXML
    void handleGuardarEmpleados(ActionEvent event) {
        if (camposObligatorios()) {
            guardarDatos();
            vaciarTodosCampos();
            visibilidadButtons();
            if (settingsController != null) {
                settingsController.cerrarCrudAnadirEmpleado();
            }
        } else {
            showAlert(AlertType.WARNING, "Advertencia", "Completar campos vacíos.");
            ActionLogger.log("Advertencia: intento de guardar con campos incompletos.");
        }
    }

    @FXML
    void handleCancelarEmpleados(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("Se perderán los cambios no guardados. ¿Desea salir?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SettingsController.verificarVentanasAbiertas = 0;
            vaciarTodosCampos();
            visibilidadButtons();
            if (settingsController != null) {
                settingsController.cerrarCrudAnadirEmpleado();
            }
            ActionLogger.log("Empleado cancelado y formulario cerrado.");
        } else {
            alert.close();
            ActionLogger.log("Cancelación de cierre del formulario anulada.");
        }
    }
}
