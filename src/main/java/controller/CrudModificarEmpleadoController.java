package controller;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Optional;

public class CrudModificarEmpleadoController {
    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnGuardar;

    @FXML
    private ComboBox<String> cmbModifEmpExistente; @FXML private TextField DNIEmpExistente; @FXML private TextField NombreEmpExistente; @FXML private TextField SueldoEmpExistente; @FXML private TextField TelEmpExistente; @FXML private Pane paneModificarEmpleado;

    @FXML
    public void initialize() {
        NombreEmpExistente.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z]*")) {
                NombreEmpExistente.setText(newValue.replaceAll("[^a-zA-Z]", ""));
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
            if (!newValue.matches("\\d*")) {
                SueldoEmpExistente.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });


    }


    private SettingsController settingsController;

    public void setSettingsController(SettingsController settingsController) {
        this.settingsController = settingsController;
    }


    private void visibilidadButtons() {
        settingsController.getBtnEliminarEmpleado().setVisible(true);
        settingsController.getBtnAnadirEmpleado().setVisible(true);
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

    private void obtenerDatosBD(){
        //funcion para obtener e insertar los datos guardados en la BD a los textField.

    }

    private void modificarDatosBD(){
        //funcion para modificar los datos del empleado viejo por los datos nuevos
    }

    private void guardarDatos(){
        String modificarEmpleadoExistente = cmbModifEmpExistente.getValue();
        String nuevoNombre = NombreEmpExistente.getText();
        String nuevoTelefono = TelEmpExistente.getText();
        Integer nuevoDNI = Integer.parseInt(DNIEmpExistente.getText());
        Integer nuevoSueldo = Integer.parseInt(SueldoEmpExistente.getText());
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
            mensajeConfirmacion();
            vaciarCampos();
            visibilidadButtons();
            // Llamar al metodo de SettingsController para limpiar el contenedor
            if (settingsController != null) {
                settingsController.cerrarCrudModificarEmpleado();
            }

        }else{



        }

    }


}
