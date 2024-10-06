package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.w3c.dom.Text;

import java.util.Optional;

public class CrudAnadirEmpleadoController {
    @FXML
    private Button btnCancelar;

    @FXML
    private TextField NombreEmpExistente;

    @FXML
    private TextField SueldoEmpExistente;

    @FXML
    private TextField TelEmpExistente;

    @FXML
    private TextField DNIEmpExistente;

    @FXML
    private TextField DNITxtField;

    @FXML
    private Label avisoEmpleadoGuardado;

    @FXML
    private ComboBox<String> cmbEliminarEmpExistente;

    @FXML
    private ComboBox<String> cmbModifEmpExistente;

    @FXML
    private ComboBox<String> cmbEmpleadosActuales;

    @FXML
    private TextField nombreTxtField;

    @FXML
    private TextField sueldoTxtField;

    @FXML
    private TextField telTxtField;

    private int verificarAcciones;
    private boolean modificarEnable = false;
    private boolean eliminarEnable = false;
    private boolean anadirEnable = false;

    private void habilitarObjetos(){
        switch (verificarAcciones) {
            case 1:
                nombreTxtField.setDisable(false);
                telTxtField.setDisable(false);
                DNITxtField.setDisable(false);
                sueldoTxtField.setDisable(false);
                cmbEliminarEmpExistente.setDisable(true);
                cmbModifEmpExistente.setDisable(true);
                NombreEmpExistente.setDisable(true);
                TelEmpExistente.setDisable(true);
                DNIEmpExistente.setDisable(true);
                SueldoEmpExistente.setDisable(true);
                break;
            case 2:
                cmbEliminarEmpExistente.setDisable(false);
                cmbModifEmpExistente.setDisable(true);
                NombreEmpExistente.setDisable(true);
                TelEmpExistente.setDisable(true);
                DNIEmpExistente.setDisable(true);
                SueldoEmpExistente.setDisable(true);
                nombreTxtField.setDisable(true);
                telTxtField.setDisable(true);
                DNITxtField.setDisable(true);
                sueldoTxtField.setDisable(true);
                break;
            case 3:
                cmbModifEmpExistente.setDisable(false);
                NombreEmpExistente.setDisable(false);
                TelEmpExistente.setDisable(false);
                DNIEmpExistente.setDisable(false);
                SueldoEmpExistente.setDisable(false);
                cmbEliminarEmpExistente.setDisable(true);
                nombreTxtField.setDisable(true);
                telTxtField.setDisable(true);
                DNITxtField.setDisable(true);
                sueldoTxtField.setDisable(true);
                break;
            default:
                break;
        }
    }

    public void verificarAccionModificar() {
        if (modificarEnable == true) {
            // Crear alerta de advertencia
            Alert alerta = new Alert(AlertType.CONFIRMATION);
            alerta.setTitle("Advertencia");
            alerta.setHeaderText(null);
            alerta.setContentText("No se pueden realizar ambas acciones al mismo tiempo, se perderán los cambios no guardados. ¿Desea continuar?");

            alerta.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    System.out.println("El usuario decidió salir.");
                    habilitarObjetos();
                } else {
                    System.out.println("No se habilitó modificar junto con otra acción");
                }
            });
        }
    }
    public void verificarAccionEliminar() {
        if (eliminarEnable == true) {
            // Crear alerta de advertencia
            Alert alerta = new Alert(AlertType.CONFIRMATION);
            alerta.setTitle("Advertencia");
            alerta.setHeaderText(null);
            alerta.setContentText("No se pueden realizar ambas acciones al mismo tiempo, se perderán los cambios no guardados. ¿Desea continuar?");

            alerta.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    System.out.println("El usuario decidió salir.");
                    habilitarObjetos();
                } else {
                    System.out.println("No se habilitó eliminar junto con otra acción");
                }
            });
        }
    }
    public void verificarAccionAnadir() {
        if (anadirEnable == true) {
            // Crear alerta de advertencia
            Alert alerta = new Alert(AlertType.CONFIRMATION);
            alerta.setTitle("Advertencia");
            alerta.setHeaderText(null);
            alerta.setContentText("No se pueden realizar ambas acciones al mismo tiempo, se perderán los cambios no guardados. ¿Desea continuar?");

            alerta.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    System.out.println("El usuario decidió salir.");
                    habilitarObjetos();
                } else {
                    System.out.println("se habilitó añadir junto con otra acción");
                }
            });
        }
    }



    @FXML
    void CancelarModificacionEmpleados(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("Se perderán los cambios no guardados. ¿Desea salir?");

        // Mostrar y esperar la respuesta del usuario
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // El usuario confirmó que desea salir
            Stage stage = (Stage) btnCancelar.getScene().getWindow();
            stage.close();
        } else {
            // El usuario canceló la acción, no se hace nada
            alert.close();
        }
    }
/*-----------botones de disable a enable-----------------*/
    @FXML
    void anadirEmpleado(ActionEvent event) {
        anadirEnable = true;
        verificarAcciones = 1;
        verificarAccionModificar();
        verificarAccionEliminar();
    }

    @FXML
    void eliminarEmpleado(ActionEvent event) {
        eliminarEnable = true;
        verificarAcciones = 2;
        verificarAccionAnadir();
        verificarAccionModificar();
    }

    @FXML
    void modificarEmpleado(ActionEvent event) {
        modificarEnable = true;
        verificarAcciones = 3;
        verificarAccionEliminar();
        verificarAccionEliminar();
    }


/*-----------------------------------------------------------*/

    @FXML
    void GuardarAnadirEmpleados(ActionEvent event) {
        String nombreEmpleado = nombreTxtField.getText();
        String telefonoEmpleado = telTxtField.getText();
        String dniEmpleado = DNITxtField.getText();
        String sueldoEmpleado = sueldoTxtField.getText();

        // Validar que el campo del nombre no esté vacío
        if (nombreEmpleado != null && !nombreEmpleado.isEmpty()) {
            // Agregar el nombre del empleado al ComboBox
            cmbEmpleadosActuales.getItems().add(nombreEmpleado);

            // Mostrar un mensaje de confirmación
            avisoEmpleadoGuardado.setText("El empleado ha sido guardado con éxito.");
            avisoEmpleadoGuardado.setStyle("-fx-fill: green;"); // Cambiar color del texto a verde

            // Limpiar los campos de texto
            nombreTxtField.clear();
            telTxtField.clear();
            DNITxtField.clear();
            sueldoTxtField.clear();
        } else {
            // Mostrar un mensaje de error si el nombre está vacío
            avisoEmpleadoGuardado.setText("Por favor, ingresa el nombre del empleado.");
            avisoEmpleadoGuardado.setStyle("-fx-text-fill: red;");

        }
    }

    @FXML
    private void handleCancelar() {
        // Limpiar todos los campos de texto
        nombreTxtField.clear();
        telTxtField.clear();
        DNITxtField.clear();
        sueldoTxtField.clear();

        // Limpiar el aviso de guardado
        avisoEmpleadoGuardado.setText("");
    }

    @FXML
    void GuardarEmpleadoEliminado(ActionEvent event) {

    }

    @FXML
    void GuardarModifEmpleados(ActionEvent event) {

    }

}
