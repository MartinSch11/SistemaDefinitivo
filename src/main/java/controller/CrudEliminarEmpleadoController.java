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

    void vaciarCampos(){
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

        // Mostrar y esperar la respuesta del usuario
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // El usuario confirmó que desea salir
            SettingsController.verificarVentanasAbiertas = 0;
            vaciarCampos();
            visibilidadButtons();

            // Llamar al metodo de SettingsController para limpiar el contenedor
            if (settingsController != null) {
                settingsController.cerrarCrudEliminarEmpleado();
            }
        } else {
            // El usuario canceló la acción, no se hace nada
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

                    // Acción a realizar si el usuario selecciona "Sí"
                    Trabajador trabajador = trabajadorDAO.findByNombre(eliminarEmpleadoSeleccionado);
                    if (trabajador != null) {
                        trabajadorDAO.delete(trabajador);

                        // Actualizar ComboBox después de eliminación
                        SettingsController.getInstance().cargarNombresEnComboBox();
                        cargarNombresEnComboBox();
                        showAlert(Alert.AlertType.INFORMATION, "Éxito", "Empleado eliminado exitosamente.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "No se encontró un trabajador con ese nombre.");
                    }

                    visibilidadButtons();

                    // Llamar al metodo de SettingsController para limpiar el contenedor
                    if (settingsController != null) {
                        settingsController.cerrarCrudEliminarEmpleado();
                    }
                }else{
                    alert.close();
                }

            } else {
                mensajeAdvertenciaCamposVacios();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los datos del empleado: " + e.getMessage());
        }
    }


}
