package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Optional;

public class DialogNuevoPedidoController {

    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;
    @FXML private TextField contactoCliente;
    @FXML private TextArea detallePedido;
    @FXML private ComboBox<String> empleadoAsignado;
    @FXML private DatePicker fechaEntregaPedido;
    @FXML private TextField nombreCliente;
    @FXML private ComboBox cmbFormaEntrega;

    @Setter
    private PedidosController pedidosController;

    @FXML
    private void GuardarInfoPedido(ActionEvent event) {
        // Obtener los valores ingresados desde los controles
        String nombre = nombreCliente.getText();
        String contacto = contactoCliente.getText();
        String detalle = detallePedido.getText();
        String empleado = empleadoAsignado.getValue(); // Obtener el valor seleccionado del ComboBox
        String formaEntrega = (String) cmbFormaEntrega.getValue(); // Cast necesario para ComboBox genérico
        LocalDate fechaEntrega = fechaEntregaPedido.getValue();

        // Validar que los campos requeridos no estén vacíos
        if (nombre.isEmpty() || contacto.isEmpty() || detalle.isEmpty() || empleado == null || formaEntrega == null || fechaEntrega == null) {
            // Mostrar alerta si faltan campos por completar
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia");
            alert.setHeaderText("Campos obligatorios sin completar.");
            alert.setContentText("Solución: completar los campos vacíos.");
            alert.showAndWait();
        } else {
            // Validar que PedidosController no sea nulo antes de invocar el método
            if (pedidosController != null) {
                // Pasar los valores correctamente a PedidosController para agregar el nuevo pedido
                pedidosController.agregarNuevoPedido(nombre, contacto, detalle, empleado, formaEntrega, fechaEntrega);

                // Cerrar la ventana
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
            }
        }
    }

    /*----------------------------------------------------------*/

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
