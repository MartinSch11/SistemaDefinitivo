package controller;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import model.Rol;
import persistence.dao.CredencialesDAO;
import persistence.dao.RolesDAO;
import persistence.dao.TrabajadorDAO;
import model.Trabajador;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class CrudModificarEmpleadoController {
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;
    @FXML private ComboBox<String> cmbModifEmpExistente;
    @FXML private TextField DNIEmpExistente;
    @FXML private TextField NombreEmpExistente;
    @FXML private TextField direccionEmpExistente;
    @FXML private TextField SueldoEmpExistente;
    @FXML private TextField TelEmpExistente;
    @FXML private DatePicker FechaContratoExistente;
    @FXML private Pane paneModificarEmpleado;
    @FXML private ComboBox<Rol> cmbRolExistente;
    @FXML private TextField txtContraseñaExistente;

    @FXML
    public void initialize() {
        NombreEmpExistente.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z ]*")) { // Se añadió el espacio
                NombreEmpExistente.setText(newValue.replaceAll("[^a-zA-Z ]", "")); // Se añadió el espacio a la expresión regular
            }
        });
        TelEmpExistente.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                TelEmpExistente.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        DNIEmpExistente.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                DNIEmpExistente.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        SueldoEmpExistente.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[\\d,.]*")) {
                SueldoEmpExistente.setText(newValue.replaceAll("[^\\d,.]", ""));
            }
        });

        cargarNombresEnComboBox();
        cmbModifEmpExistente.setOnAction(e -> cargarDatosTrabajador());
        cargarRoles();
    }

    private SettingsController settingsController;

    public void setSettingsController(SettingsController settingsController) {
        this.settingsController = settingsController;
    }


    private void visibilidadButtons() {
        settingsController.getBtnEliminarEmpleado().setVisible(true);
        settingsController.getBtnAnadirEmpleado().setVisible(true);
    }

    private void cargarRoles() {
        RolesDAO rolesDAO = new RolesDAO();
        List<Rol> listaRoles = rolesDAO.findAll();
        cmbRolExistente.getItems().clear();
        cmbRolExistente.getItems().addAll(listaRoles);
    }

    private boolean camposObligatorios(){
        if (cmbModifEmpExistente.getValue() == null || cmbModifEmpExistente.getValue().isEmpty()) {
            return false;
        }
        if (DNIEmpExistente.getText().isEmpty()) {
            return false;
        }
        if (NombreEmpExistente.getText().isEmpty()) {
            return false;
        }
        if (SueldoEmpExistente.getText().isEmpty()) {
            return false;
        }
        if (TelEmpExistente.getText().isEmpty()) {
            return false;
        }
        if (FechaContratoExistente.getValue() == null){
            return false;
        }
        return true;
    }

    void vaciarCampos(){
        cmbModifEmpExistente.setValue(null);
    }

    private void mensajeConfirmacion() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(null);
        alert.setContentText("¡Los cambios se han realizado con éxito!");
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void cargarNombresEnComboBox() {
        try {
            List<String> nombres = trabajadorDAO.findAllNombres();
            cmbModifEmpExistente.setItems(FXCollections.observableArrayList(nombres));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los nombres de los empleados: " + e.getMessage());
        }
    }

    private void obtenerDatosDB(Trabajador trabajador) {
        DNIEmpExistente.setText(trabajador.getDni());
        NombreEmpExistente.setText(trabajador.getNombre());
        direccionEmpExistente.setText(trabajador.getDireccion());
        TelEmpExistente.setText(trabajador.getTelefono());
        SueldoEmpExistente.setText(trabajador.getSueldo().toPlainString());
        FechaContratoExistente.setValue(trabajador.getFechaContratacion());
        cmbRolExistente.setValue(trabajador.getRol());
        txtContraseñaExistente.setText(trabajador.getCredencial().getContraseña());
    }

    private void cargarDatosTrabajador() {
        try {
            String nombreSeleccionado = cmbModifEmpExistente.getValue();
            if (nombreSeleccionado != null) {
                Trabajador trabajador = trabajadorDAO.findByNombre(nombreSeleccionado);
                if (trabajador != null) {
                    obtenerDatosDB(trabajador);
                    this.trabajador = trabajador; // Almacena el trabajador seleccionado en una variable de instancia
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "No se encontró un trabajador con ese nombre.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los datos del empleado: " + e.getMessage());
        }
    }

    private Trabajador trabajador;

    private TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    CredencialesDAO credencialesDAO = new CredencialesDAO(); // DAO para la tabla de credenciales

    @FXML
    private void guardarDatos() {
        try {
            if (trabajador != null) {
                // Actualizar los datos del trabajador seleccionado con los nuevos datos del formulario
                trabajador.setDni(DNIEmpExistente.getText());
                trabajador.setNombre(NombreEmpExistente.getText());
                trabajador.setDireccion(direccionEmpExistente.getText());
                trabajador.setTelefono(TelEmpExistente.getText());
                trabajador.setSueldo(new BigDecimal(SueldoEmpExistente.getText()));
                trabajador.setFechaContratacion(FechaContratoExistente.getValue());
                Rol rolSeleccionado = cmbRolExistente.getValue(); // Rol seleccionado en el ComboBox
                String contraseña = txtContraseñaExistente.getText();

                // Guardar los cambios en la base de datos del trabajador
                trabajadorDAO.update(trabajador);

                // Si la contraseña no está vacía, actualizar las credenciales
                if (contraseña != null && !contraseña.trim().isEmpty()) {
                    // Actualizar las credenciales usando el DNI y la nueva contraseña
                    credencialesDAO.update(trabajador.getDni(), contraseña);
                }

                // Actualizar ComboBox en SettingsController
                SettingsController.getInstance().cargarNombresEnComboBox();

                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Empleado y credenciales actualizados exitosamente.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se encontró un empleado con ese DNI.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error al actualizar al empleado o las credenciales: " + e.getMessage());
        }
    }

    /*--------------------------------------------------------------------------------------------*/

    @FXML
    void handleCancelarEmpleados(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("Se perderán los cambios no guardados. ¿Desea salir?");

        // Mostrar y esperar la respuesta del usuario
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            SettingsController.verificarVentanasAbiertas = 0;
            vaciarCampos();
            visibilidadButtons();

            // Llamar al metodo de SettingsController para limpiar el contenedor
            if (settingsController != null) {
                settingsController.cerrarCrudModificarEmpleado();
            }
        } else {
            alert.close();
        }
    }

    @FXML
    void handleGuardarEmpleados(ActionEvent event) {
        if (camposObligatorios()){

            guardarDatos();
            vaciarCampos();
            visibilidadButtons();
            // Llamar al metodo de SettingsController para limpiar el contenedor
            if (settingsController != null) {
                settingsController.cerrarCrudModificarEmpleado();
            }

        }else{
            showAlert(Alert.AlertType.ERROR, "Error", "No se pueden guardar los cambios debido a campos vacíos.");
        }
    }

}