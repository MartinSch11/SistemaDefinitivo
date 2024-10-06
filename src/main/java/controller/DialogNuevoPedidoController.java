package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;

import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.io.IOException;
import java.util.ResourceBundle;

public class DialogNuevoPedidoController {

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnGuardar;

    @FXML
    private TextField contactoCliente;

    @FXML
    private TextArea detallePedido;

    @FXML
    private ComboBox<String> empleadoAsignado;

    @FXML
    private DatePicker fechaEntregaPedido;

    @FXML
    private TextField nombreCliente;

    private PedidosController pedidosController;

    public void setPedidosController(PedidosController pedidosController) {
        this.pedidosController = pedidosController;
    }

    @FXML
    private void GuardarInfoPedido(ActionEvent event) {
        // Obtener los valores ingresados
        String nombre = nombreCliente.getText();
        String contacto = contactoCliente.getText();
        String detalle = detallePedido.getText();
        String empleado = empleadoAsignado.getValue(); // Obtener el valor seleccionado del ComboBox
        LocalDate fechaEntrega = fechaEntregaPedido.getValue();

        // Validar que PedidosController no sea nulo antes de invocar el método
        if (pedidosController != null) {
            // Pasar los valores a PedidosController para que agregue el nuevo pedido
            pedidosController.agregarNuevoPedido(nombre, contacto, detalle, empleado, fechaEntrega);

            // Cerrar el cuadro de diálogo
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
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
