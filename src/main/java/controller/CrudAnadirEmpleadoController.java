package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.w3c.dom.Text;

import java.util.Optional;

public class CrudAnadirEmpleadoController {
    @FXML private Pane paneAnadirEmpleado;

    @FXML private Button btnCancelar; @FXML private Button btnGuardar;

    @FXML
    private TextField DNITxtField;

    @FXML
    private TextField nombreTxtField;

    @FXML
    private TextField sueldoTxtField; @FXML  private TextField telTxtField;


    @FXML
    public void initialize() {

        nombreTxtField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z]*")) {
                nombreTxtField.setText(newValue.replaceAll("[^a-zA-Z]", ""));
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
            if (!newValue.matches("\\d*")) {
                sueldoTxtField.setText(newValue.replaceAll("[^\\d]", ""));
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
        return true;
    }


    private void guardarDatos(){
        String nombreEmpleado = nombreTxtField.getText();
        String telefonoEmpleado = telTxtField.getText();
        String dniEmpleado = DNITxtField.getText();
        String sueldoEmpleado = sueldoTxtField.getText();
    }

    void anadirEmpleadoBD(){
        //funcion para añadir a la BD al nuevo empleado

    }

    /*---------------------------------------------------------------------------------------*/

    @FXML
    void handleGuardarEmpleados(ActionEvent event) {

        if(camposObligatorios()){

            guardarDatos();
            mensajeConfirmacion();
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




    //guardar modificaciones: eliminar, añadir, modificar
    //mejorar el diseño
    //eliminar de pedidoController

}
