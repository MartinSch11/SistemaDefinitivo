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

    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;
    @FXML private ComboBox<String> cmbEliminarEmpExistente;
    @FXML private Pane paneEliminarEmpleado;
    private SettingsController settingsController;

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

    private void mensajeConfirmacion() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(null);
        alert.setContentText("¡El empleado ha sido eliminado con éxito!");
        alert.showAndWait();
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
            List<String> nombres = trabajadorDAO.findAllNombres();
            cmbEliminarEmpExistente.setItems(FXCollections.observableArrayList(nombres));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los nombres de los empleados: " + e.getMessage());
            ActionLogger.log("Error al cargar los nombres de empleados: " + e.getMessage());
        }
    }

    private Trabajador trabajador;

    private TrabajadorDAO trabajadorDAO = new TrabajadorDAO();

    @FXML
    void handleGuardarEmpleados(ActionEvent event) {
        try {
            String eliminarEmpleadoSeleccionado = cmbEliminarEmpExistente.getValue();

            if (eliminarEmpleadoSeleccionado != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmación de eliminación");
                alert.setHeaderText(null);
                alert.setContentText("¿Desea eliminar el empleado seleccionado? Esta acción no se puede revertir.");

                ButtonType buttonSi = new ButtonType("Sí");
                ButtonType buttonCancelar = new ButtonType("Cancelar");

                alert.getButtonTypes().setAll(buttonSi, buttonCancelar);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == buttonSi) {
                    Trabajador trabajador = trabajadorDAO.findByNombre(eliminarEmpleadoSeleccionado);
                    if (trabajador != null) {
                        trabajadorDAO.delete(trabajador);
                        SettingsController.getInstance().cargarNombresEnComboBox();
                        cargarNombresEnComboBox();
                        showAlert(Alert.AlertType.INFORMATION, "Éxito", "Empleado eliminado exitosamente.");

                        ActionLogger.log("Empleado eliminado: " + eliminarEmpleadoSeleccionado);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "No se encontró un trabajador con ese nombre.");
                        ActionLogger.log("Error: No se encontró el empleado: " + eliminarEmpleadoSeleccionado);
                    }

                    visibilidadButtons();

                    if (settingsController != null) {
                        settingsController.cerrarCrudEliminarEmpleado();
                    }
                } else {
                    alert.close();
                }

            } else {
                mensajeAdvertenciaCamposVacios();
                ActionLogger.log("Advertencia: intento de eliminar empleado sin selección en el ComboBox");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los datos del empleado: " + e.getMessage());
            ActionLogger.log("Error al guardar los cambios del empleado: " + e.getMessage());
        }
    }
}
