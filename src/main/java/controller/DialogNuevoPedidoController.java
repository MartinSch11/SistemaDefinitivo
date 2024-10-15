package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import lombok.Setter;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.time.LocalDate;
import java.util.Optional;
import java.io.IOException;
import java.util.ResourceBundle;

public class DialogNuevoPedidoController {

    @FXML
    private Button btnCancelar; @FXML private Button btnGuardar; @FXML private TextField contactoCliente;
    @FXML
    private TextArea detallePedido; @FXML private TextField nombreCliente; @FXML private ComboBox<String> empleadoAsignado; @FXML private ComboBox<String> cmbFormaEntrega;
    @FXML
    private DatePicker fechaEntregaPedido;



    @Setter
    private PedidosController PedidosController;

    @FXML
    private void initialize() {
        empleadoAsignado.setItems(FXCollections.observableArrayList(
                "Jose",
                "Juan",
                "Antonio"
        ));
        cmbFormaEntrega.setItems(FXCollections.observableArrayList(
                "Retira del local",
                "Paga su envío"
        ));

        nombreCliente.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z]*")) {
                nombreCliente.setText(newValue.replaceAll("[^a-zA-Z]", ""));
            }
        });
        contactoCliente.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                contactoCliente.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

    }

    private boolean validarCamposObligatorios(){
        boolean valid = true;
        if (fechaEntregaPedido.getValue() == null) {
            valid = false;
        }
        if (nombreCliente.getText() == null || nombreCliente.getText().trim().isEmpty()) {
            valid = false;
        }
        if (empleadoAsignado.getValue() == null || empleadoAsignado.getValue().toString().trim().isEmpty()) {
            valid = false;
        }
        if (cmbFormaEntrega.getValue() == null || cmbFormaEntrega.getValue().toString().trim().isEmpty()) {
            valid = false;
        }
        if (detallePedido.getText() == null || detallePedido.getText().trim().isEmpty()) {
            valid = false;
        }
        if (contactoCliente.getText() == null || contactoCliente.getText().trim().isEmpty()) {
            valid = false;
        }
        return valid;
    }


    private PedidosController pedidosController;

    public void setPedidosController(PedidosController controller) {
        this.pedidosController = controller;
    }
        @FXML
    public void GuardarInfoPedido(ActionEvent event) {
        if(validarCamposObligatorios()){
            String nombre = nombreCliente.getText();
            String contacto = contactoCliente.getText();
            String detalle = detallePedido.getText();
            String empleado = empleadoAsignado.getValue();
            String formaEntrega = cmbFormaEntrega.getValue();
            LocalDate fechaEntrega = fechaEntregaPedido.getValue();

            // Validar que PedidosController no sea nulo antes de invocar el metodo
            if (pedidosController != null) {
                // Pasar los valores a PedidosController para que agregue el nuevo pedido
                pedidosController.agregarNuevoPedido(nombre, contacto, detalle, empleado, formaEntrega, fechaEntrega);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia");
            alert.setHeaderText("Campos obligatorios sin completar.");
            alert.setContentText("Solución: completar los campos vacíos.");
            alert.showAndWait();
        }
    }

    @FXML
    void CancelarPedido(ActionEvent event) {
        Node source = (Node) event.getSource();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("Se perderán los cambios realizados. ¿Desea salir?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage stage = (Stage) btnCancelar.getScene().getWindow();
            stage.close();
        } else {
            alert.close();
        }
    }
}
