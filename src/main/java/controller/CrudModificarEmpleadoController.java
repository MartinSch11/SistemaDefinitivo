package controller;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import model.Credencial;
import model.Rol;
import persistence.dao.CredencialesDAO;
import persistence.dao.RolesDAO;
import persistence.dao.TrabajadorDAO;
import model.Trabajador;
import utilities.ActionLogger;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class CrudModificarEmpleadoController {
    @FXML
    private Button btnCancelar;
    @FXML
    private Button btnGuardar;
    @FXML
    private ComboBox<String> cmbModifEmpExistente;
    @FXML
    private TextField DNIEmpExistente;
    @FXML
    private TextField NombreEmpExistente;
    @FXML
    private TextField direccionEmpExistente;
    @FXML
    private TextField SueldoEmpExistente;
    @FXML
    private TextField TelEmpExistente;
    @FXML
    private DatePicker FechaContratoExistente;
    @FXML
    private Pane paneModificarEmpleado;
    @FXML
    private ComboBox<Rol> cmbRolExistente;
    @FXML
    private TextField txtContraseñaExistente;
    @FXML
    private ComboBox<String> cmbSexoExistente;

    @FXML
    public void initialize() {
        NombreEmpExistente.textProperty().addListener((_, _, newValue) -> {
            if (!newValue.matches("[a-zA-Z ]*")) { // Se añadió el espacio
                NombreEmpExistente.setText(newValue.replaceAll("[^a-zA-Z ]", "")); // Se añadió el espacio a la
                                                                                   // expresión regular
            }
        });
        TelEmpExistente.textProperty().addListener((_, _, newValue) -> {
            if (!newValue.matches("\\d*")) {
                TelEmpExistente.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        DNIEmpExistente.textProperty().addListener((_, _, newValue) -> {
            if (!newValue.matches("\\d*")) {
                DNIEmpExistente.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        SueldoEmpExistente.textProperty().addListener((_, _, newValue) -> {
            if (!newValue.matches("[\\d,.]*")) {
                SueldoEmpExistente.setText(newValue.replaceAll("[^\\d,.]", ""));
            }
        });

        cargarNombresEnComboBox();
        cmbModifEmpExistente.setOnAction(_ -> cargarDatosTrabajador());
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

    private boolean camposObligatorios() {
        if (cmbModifEmpExistente.getValue() == null || cmbModifEmpExistente.getValue().isEmpty()) {
            return false;
        }
        if (DNIEmpExistente.getText().isEmpty()) {
            return false;
        }
        if (NombreEmpExistente.getText().isEmpty()) {
            return false;
        }
        // Permitir que el sueldo esté vacío (puede ser null)
        // if (SueldoEmpExistente.getText().isEmpty()) {
        // return false;
        // }
        if (TelEmpExistente.getText().isEmpty()) {
            return false;
        }
        if (FechaContratoExistente.getValue() == null) {
            return false;
        }
        return true;
    }

    void vaciarCampos() {
        cmbModifEmpExistente.setValue(null);
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
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudieron cargar los nombres de los empleados: " + e.getMessage());
        }
    }

    private void obtenerDatosDB(Trabajador trabajador) {
        DNIEmpExistente.setText(trabajador.getDni());
        NombreEmpExistente.setText(trabajador.getNombre());
        direccionEmpExistente.setText(trabajador.getDireccion());
        TelEmpExistente.setText(trabajador.getTelefono());
        if (trabajador.getSueldo() != null) {
            SueldoEmpExistente.setText(trabajador.getSueldo().toPlainString());
        } else {
            SueldoEmpExistente.setText(""); // Permitir campo vacío si es null
        }
        FechaContratoExistente.setValue(trabajador.getFechaContratacion());
        cmbRolExistente.setValue(trabajador.getRol());
        if (trabajador.getCredencial() != null) {
            txtContraseñaExistente.setText(trabajador.getCredencial().getContraseña());
        } else {
            txtContraseñaExistente.setText(""); // Si no tiene credencial, dejar vacío
        }
        // Cargar sexo en el ComboBox
        cmbSexoExistente.getItems().setAll("Femenino", "Masculino", "Otro");
        String sexoTrabajador = trabajador.getSexo();
        if (sexoTrabajador != null && !sexoTrabajador.isEmpty()) {
            // Buscar coincidencia ignorando mayúsculas/minúsculas y espacios
            for (String sexo : cmbSexoExistente.getItems()) {
                if (sexo.equalsIgnoreCase(sexoTrabajador.trim())) {
                    cmbSexoExistente.setValue(sexo);
                    return;
                }
            }
            // Si no hay coincidencia exacta, dejarlo vacío
            cmbSexoExistente.setValue(null);
        } else {
            cmbSexoExistente.setValue(null);
        }
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
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudieron cargar los datos del empleado: " + e.getMessage());
        }
    }

    private Trabajador trabajador;

    private TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    CredencialesDAO credencialesDAO = new CredencialesDAO(); // DAO para la tabla de credenciales

    @FXML
    private void guardarDatos() {
        try {
            if (trabajador != null) {
                String dniOriginal = trabajador.getDni(); // DNI antes de modificar
                String nuevoDni = DNIEmpExistente.getText().trim();
                String nuevaContraseña = txtContraseñaExistente.getText();

                // Actualizar datos del trabajador con los nuevos del formulario
                trabajador.setDni(nuevoDni);
                trabajador.setNombre(NombreEmpExistente.getText());
                trabajador.setDireccion(direccionEmpExistente.getText());
                trabajador.setTelefono(TelEmpExistente.getText());

                String sueldoText = SueldoEmpExistente.getText();
                if (sueldoText == null || sueldoText.trim().isEmpty()) {
                    trabajador.setSueldo(null);
                } else {
                    trabajador.setSueldo(new BigDecimal(sueldoText));
                }

                trabajador.setFechaContratacion(FechaContratoExistente.getValue());
                trabajador.setSexo(cmbSexoExistente.getValue());
                trabajador.setRol(cmbRolExistente.getValue());

                // Actualizar en BD
                trabajadorDAO.update(trabajador);

                // Obtener credencial por el DNI original (antes del cambio)
                Credencial credencial = credencialesDAO.findByUsername(dniOriginal);

                if (credencial != null) {
                    // Si el DNI cambió, validar que no esté duplicado
                    if (!dniOriginal.equals(nuevoDni)) {
                        if (credencialesDAO.existeOtroConDni(nuevoDni, credencial.getId())) {
                            showAlert(Alert.AlertType.ERROR, "Error",
                                    "Ya existe otra credencial con el DNI " + nuevoDni);
                            return; // 🚫 Evitar el update
                        }
                        credencial.setDni(nuevoDni);
                    }

                    credencial.setContraseña(nuevaContraseña);
                    credencial.setTrabajador(trabajador);

                    credencialesDAO.update(credencial);
                } else {
                    // No existía ninguna credencial antes → creamos nueva
                    credencial = new Credencial();
                    credencial.setDni(nuevoDni);
                    credencial.setContraseña(nuevaContraseña);
                    credencial.setTrabajador(trabajador);
                    credencialesDAO.save(credencial);
                }

                // Actualizar interfaz
                SettingsController.getInstance().cargarNombresEnComboBox();
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Empleado actualizado exitosamente.");
                ActionLogger.log("Empleado con DNI " + nuevoDni + " actualizado.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se encontró un empleado con ese DNI.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Error al actualizar al empleado o las credenciales: " + e.getMessage());
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

            // Registrar la acción de cancelar en el log
            ActionLogger.log("El usuario canceló la modificación del empleado.");
        } else {
            alert.close();
        }
    }

    @FXML
    void handleGuardarEmpleados(ActionEvent event) {
        if (camposObligatorios()) {
            guardarDatos();
            vaciarCampos();
            visibilidadButtons();
            // Llamar al metodo de SettingsController para limpiar el contenedor
            if (settingsController != null) {
                settingsController.cerrarCrudModificarEmpleado();
            }

        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pueden guardar los cambios debido a campos vacíos.");
        }
    }
}
