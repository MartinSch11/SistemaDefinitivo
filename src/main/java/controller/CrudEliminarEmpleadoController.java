package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.event.ActionEvent;
import model.Trabajador;
import persistence.dao.TrabajadorDAO;
import utilities.ActionLogger;
import java.util.List;
import java.util.Optional;

public class CrudEliminarEmpleadoController {

    @FXML
    private Button btnCancelar;
    @FXML
    private Button btnGuardar;
    @FXML
    private ComboBox<String> cmbEliminarEmpExistente;
    @FXML
    private Pane paneEliminarEmpleado;
    private SettingsController settingsController;

    private TrabajadorDAO trabajadorDAO = new TrabajadorDAO();

    @FXML
    public void initialize() {
        cargarNombresEnComboBox();
    }

    public void setSettingsController(SettingsController settingsController) {
        this.settingsController = settingsController;
    }

    @FXML
    private void visibilidadButtons() {
        settingsController.getBtnModificarEmpleado().setVisible(true);
        settingsController.getBtnAnadirEmpleado().setVisible(true);
    }

    void vaciarCampos() {
        cmbEliminarEmpExistente.setValue(null);
    }

    public void mensajeAdvertenciaCamposVacios() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText("Advertencia: campos sin completar.");
        alert.setContentText("Solución: completar campos vacíos.");

        ButtonType buttonOK = new ButtonType("OK");
        alert.getButtonTypes().setAll(buttonOK);

        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void handleCancelarEmpleados(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("Se perderán los cambios no guardados. ¿Desea salir?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SettingsController.verificarVentanasAbiertas = 0;
            vaciarCampos();
            visibilidadButtons();

            if (settingsController != null) {
                settingsController.cerrarCrudEliminarEmpleado();
            }

            ActionLogger.log("Cancelación de la eliminación de empleado");
        } else {
            alert.close();
        }
    }

    private void cargarNombresEnComboBox() {
        try {
            // excluye admins
            List<String> nombres = trabajadorDAO.findAllNombresSinAdministradores();
            cmbEliminarEmpExistente.setItems(FXCollections.observableArrayList(nombres));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudieron cargar los nombres de los empleados: " + e.getMessage());
            ActionLogger.log("Error al cargar los nombres de empleados: " + e.getMessage());
        }
    }

    @FXML
    void handleGuardarEmpleados(ActionEvent event) {
        String eliminarEmpleadoSeleccionado = cmbEliminarEmpExistente.getValue();

        if (eliminarEmpleadoSeleccionado == null) {
            mensajeAdvertenciaCamposVacios();
            ActionLogger.log("Advertencia: intento de eliminar empleado sin selección");
            return;
        }

        // Confirmación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación de baja");
        alert.setHeaderText(null);
        alert.setContentText(
                "¿Desea eliminar al empleado " + eliminarEmpleadoSeleccionado + "?\nEsta acción es irreversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) { // Comparar con ButtonType.OK es más seguro que el
                                                                   // texto "Sí"

            try {
                Trabajador trabajador = trabajadorDAO.findByNombre(eliminarEmpleadoSeleccionado);

                if (trabajador != null) {
                    // Validaciones de negocio (Admin y Auto-eliminación)
                    if (trabajador.getRol() != null
                            && "administrador".equalsIgnoreCase(trabajador.getRol().getNombre())) {
                        showAlert(Alert.AlertType.ERROR, "Acción denegada", "No podés eliminar a un Administrador.");
                        return;
                    }

                    String usuarioLogeado = model.SessionContext.getInstance().getUserName();
                    if (usuarioLogeado != null && trabajador.getNombre().equals(usuarioLogeado)) {
                        showAlert(Alert.AlertType.ERROR, "Acción denegada",
                                "No podés auto-eliminarte mientras estás logueado.");
                        return;
                    }

                    // INTENTO DE BORRADO
                    trabajadorDAO.delete(trabajador);

                    // ÉXITO
                    showAlert(Alert.AlertType.INFORMATION, "Éxito", "Empleado eliminado correctamente.");
                    ActionLogger.log("Empleado eliminado: " + eliminarEmpleadoSeleccionado);

                    // Actualizar interfaz
                    vaciarCampos();
                    cargarNombresEnComboBox();
                    if (settingsController != null) {
                        settingsController.cargarNombresEnComboBox();
                        settingsController.cerrarCrudEliminarEmpleado();
                    }
                    visibilidadButtons();

                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "No se encontró el trabajador en la base de datos.");
                }

            } catch (Exception ex) {
                // ERROR DE INTEGRIDAD (Tiene ventas/pedidos)
                Alert errorAlert = new Alert(Alert.AlertType.WARNING);
                errorAlert.setTitle("No se puede eliminar");
                errorAlert.setHeaderText("Empleado con historial");
                errorAlert.setContentText("No se puede eliminar a " + eliminarEmpleadoSeleccionado +
                        " porque tiene PEDIDOS o VENTAS registradas a su nombre.\n\n" +
                        "El sistema debe mantener el registro de quién hizo esas ventas.");
                errorAlert.showAndWait();

                ActionLogger.log("Error de integridad al eliminar empleado: " + ex.getMessage());
            }
        }
    }
}