package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
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
        alert.setContentText("¡Los cambios se han realizado con éxito!");
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

    void eliminarEmpleadoBD(){
        //funcion para eliminar de la BD al empleado seleccionado

    }

    /*-----------------------------------------------------------------------------------------------*/

    @FXML
    void handleCancelarEmpleados(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("Se perderán los cambios no guardados. ¿Desea salir?");

        // Mostrar y esperar la respuesta del usuario
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // El usuario confirmó que desea salir
            //Stage stage = (Stage) btnCancelar.getScene().getWindow();

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


    @FXML
    void handleGuardarEmpleados(ActionEvent event) {
        String eliminarEmpleado = cmbEliminarEmpExistente.getValue();

        if (cmbEliminarEmpExistente.getValue() != null) {

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

                mensajeConfirmacion();
                visibilidadButtons();

                // Llamar al metodo de SettingsController para limpiar el contenedor
                if (settingsController != null) {
                    settingsController.cerrarCrudEliminarEmpleado();
                }

            } else {



                // Acción a realizar si el usuario selecciona "Cancelar" o cierra el diálogo

            }

        } else {
            mensajeAdvertenciaCamposVacios();
        }

    }


}
