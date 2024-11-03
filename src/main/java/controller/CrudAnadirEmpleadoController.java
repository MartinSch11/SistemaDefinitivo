package controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import java.math.BigDecimal;
import java.security.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import org.w3c.dom.Text;
import persistence.dao.TrabajadorDAO;
import model.Trabajador;
import java.time.LocalDateTime;
import java.util.Optional;
import javafx.scene.control.Alert;


public class CrudAnadirEmpleadoController {
    @FXML private Pane paneAnadirEmpleado;

    @FXML private Button btnCancelar; @FXML private Button btnGuardar;

    @FXML
    private TextField DNITxtField;
    @FXML
    private TextField nombreTxtField;
    @FXML
    private DatePicker dateFechaContratacion;
    @FXML
    private TextField direccionTxtField;
    @FXML
    private TextField sueldoTxtField; @FXML  private TextField telTxtField;


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

        //permite seleccionar una fecha de contratacion solo hasta el dia actual
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
        return true;
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void guardarDatos(){
        String dniEmpleado = DNITxtField.getText();
        String nombreEmpleado = nombreTxtField.getText();
        String direccionEmpleado = direccionTxtField.getText();
        String telefonoEmpleado = telTxtField.getText();
        BigDecimal sueldoEmpleado = new BigDecimal(sueldoTxtField.getText());
        LocalDate fechaContratoEmpleado = dateFechaContratacion.getValue();

        TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
        try {
            // Verificar si ya existe un empleado con el mismo DNI
            Trabajador empleadoExistente = trabajadorDAO.findByDNI(dniEmpleado);
            if (empleadoExistente != null) {
                showAlert(Alert.AlertType.ERROR, "Error de Validación", "Ya existe un empleado con ese DNI.");
                return;
            }

            // Crear nuevo empleado y guardar
            Trabajador nuevoEmpleado = new Trabajador(dniEmpleado, nombreEmpleado, direccionEmpleado, telefonoEmpleado, sueldoEmpleado, fechaContratoEmpleado, null);
            trabajadorDAO.save(nuevoEmpleado);

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
