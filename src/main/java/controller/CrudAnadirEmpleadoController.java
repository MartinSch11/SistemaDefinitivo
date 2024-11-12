package controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import java.math.BigDecimal;
import java.time.LocalDate;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import model.Credencial;
import model.Receta;
import model.Rol;
import persistence.dao.CredencialesDAO;
import persistence.dao.RecetaDAO;
import persistence.dao.RolesDAO;
import persistence.dao.TrabajadorDAO;
import model.Trabajador;

import java.util.List;
import java.util.Optional;
import javafx.scene.control.Alert;

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
    @FXML private ComboBox<Rol> cmbRol;
    @FXML private TextField txtContraseña;

    @FXML
    public void initialize() {
        nombreTxtField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z ]*")) { // Se añadió el espacio
                nombreTxtField.setText(newValue.replaceAll("[^a-zA-Z ]", "")); // Se añadió el espacio a la expresión regular
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
        cargarRoles();
    }

    private SettingsController settingsController;

    public void setSettingsController(SettingsController settingsController) {
        this.settingsController = settingsController;
    }

    @FXML
    private void visibilidadButtons() {
        settingsController.getBtnModificarEmpleado().setVisible(true);
        settingsController.getBtnEliminarEmpleado().setVisible(true);
    }

    void vaciarTodosCampos(){
        nombreTxtField.clear();
        telTxtField.clear();
        DNITxtField.clear();
        sueldoTxtField.clear();
        direccionTxtField.clear();
        dateFechaContratacion.setValue(null);
        cmbRol.setValue(null);
        txtContraseña.clear();
    }

    private void mensajeConfirmacion() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(null);
        alert.setContentText("¡Se ha añadido un nuevo empleado con éxito!");
        alert.showAndWait();
    }

    private boolean camposObligatorios() {
        if (DNITxtField == null || DNITxtField.getText().isEmpty()) {
            return false;
        }
        if (nombreTxtField == null || nombreTxtField.getText().isEmpty()) {
            return false;
        }
        if (sueldoTxtField == null || sueldoTxtField.getText().isEmpty()) {
            return false;
        }
        if (telTxtField == null || telTxtField.getText().isEmpty()) {
            return false;
        }
        if (direccionTxtField == null || direccionTxtField.getText().isEmpty()) {
            return false;
        }
        if (dateFechaContratacion.getValue() == null) {
            return false;
        }
        if (txtContraseña == null || txtContraseña.getText().isEmpty()) {
            return false;
        }
        return true;
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
        Rol rolSeleccionado = cmbRol.getValue(); // Rol seleccionado en el ComboBox
        String contraseña = txtContraseña.getText();

        TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
        CredencialesDAO credencialesDAO = new CredencialesDAO(); // DAO para la tabla de credenciales

        try {
            // Verificar si ya existe un empleado con el mismo DNI
            Trabajador empleadoExistente = trabajadorDAO.findByDNI(dniEmpleado);
            if (empleadoExistente != null) {
                showAlert(Alert.AlertType.ERROR, "Error de Validación", "Ya existe un empleado con ese DNI.");
                return;
            }

            // Crear nuevo trabajador y asociar el rol
            Trabajador nuevoEmpleado = new Trabajador();
            nuevoEmpleado.setDni(dniEmpleado);
            nuevoEmpleado.setNombre(nombreEmpleado);
            nuevoEmpleado.setDireccion(direccionEmpleado);
            nuevoEmpleado.setTelefono(telefonoEmpleado);
            nuevoEmpleado.setSueldo(sueldoEmpleado);
            nuevoEmpleado.setFechaContratacion(fechaContratoEmpleado);
            nuevoEmpleado.setRol(rolSeleccionado);  // Asociar el rol seleccionado

            // Guardar el trabajador
            trabajadorDAO.save(nuevoEmpleado);

            // Guardar la contraseña en la tabla 'credenciales' asociada al DNI del trabajador
            Credencial credenciales = new Credencial();
            credenciales.setDni(dniEmpleado);
            credenciales.setContraseña(contraseña);

            // Guardar las credenciales
            credencialesDAO.save(credenciales);

            // Actualizar ComboBox en SettingsController
            SettingsController.getInstance().cargarNombresEnComboBox();
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Empleado guardado exitosamente.");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error al guardar al empleado: " + e.getMessage());
        }
    }


    /*---------------------------------------------------------------------------------------*/

    @FXML
    void handleGuardarEmpleados(ActionEvent event) {
        if(camposObligatorios()){

            guardarDatos();
            //mensajeConfirmacion();
            vaciarTodosCampos();
            visibilidadButtons();
            if (settingsController != null) {
                settingsController.cerrarCrudAnadirEmpleado();
            }

        }else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia");
            alert.setHeaderText("Advertencia: campos sin completar.");
            alert.setContentText("Solución: completar campos vacíos.");

            ButtonType buttonOK = new ButtonType("OK");
            alert.getButtonTypes().setAll(buttonOK);

            alert.showAndWait();
        }
    }

    @FXML
    void handleCancelarEmpleados(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("Se perderán los cambios no guardados. ¿Desea salir?");

        // Mostrar y esperar la respuesta del usuario
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            SettingsController.verificarVentanasAbiertas = 0;

            vaciarTodosCampos();
            visibilidadButtons();

            // Llamar al metodo de SettingsController para limpiar el contenedor
            if (settingsController != null) {
                settingsController.cerrarCrudAnadirEmpleado();
            }
        } else {
            alert.close();
        }
    }
}