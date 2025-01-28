package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import model.Pedido;
import model.Trabajador;
import persistence.dao.PedidoDAO;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import java.time.LocalDate;
import java.util.Optional;

public class ModificarPedidosController {

    @FXML private Button btnCancelar;
    @FXML private Button btnCatalogo;
    @FXML private Button btnGuardar;
    @FXML private ComboBox<String> cmbFormaEntrega;
    @FXML private TextField contactoCliente;
    @FXML private TextField dniClientePedido;
    @FXML private ComboBox<String> empleadoAsignado;
    @FXML private DatePicker fechaEntregaPedido;
    @FXML private TextField nombreCliente;

    private PedidoDAO pedidoDAO = new PedidoDAO();
    private Pedido pedido;

    private ObservableList<Pedido> pedidos = FXCollections.observableArrayList();
    private Pedido pedidoSeleccionado;

    @Setter
    private PedidosController pedidosController;
    public void setPedidosController(PedidosController pedidosController) {
        this.pedidosController = pedidosController;
    }

    @FXML
    void CancelarModificacionPedido(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("Se perderán los cambios no guardados. ¿Desea salir?");

        // Mostrar y esperar la respuesta del usuario
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            /*
            vaciarCampos();
            */
            alert.close();
        } else {

        }
    }

    @FXML
    void obtenerDatos(Pedido pedidos){
        nombreCliente.setText(pedidos.getNombreCliente());
        contactoCliente.setText(pedidos.getContactoCliente());
        dniClientePedido.setText(pedidos.getDniCliente());
        //detalle.setText(pedidos.getDetallePedido()); no se como hacer para modificar el detalle del pedido
        fechaEntregaPedido.setValue(pedidos.getFechaEntrega());
        cmbFormaEntrega.setValue(pedidos.getFormaEntrega());
        empleadoAsignado.setValue(pedidos.getEmpleadoAsignado());
    }

    @FXML
    void GuardarModificacionPedido(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("Se modificará la información del pedido. ¿Desea guardar estos cambios?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (pedidoSeleccionado != null) {
                // Actualiza los datos del pedidoSeleccionado
                pedidoSeleccionado.setNombreCliente(nombreCliente.getText());
                pedidoSeleccionado.setContactoCliente(contactoCliente.getText());
                pedidoSeleccionado.setDniCliente(dniClientePedido.getText());
               // pedidoSeleccionado.setDetallePedido(detallePedido.getText()); // Asumiendo que ahora tienes este campo
                pedidoSeleccionado.setEmpleadoAsignado(empleadoAsignado.getValue());
                pedidoSeleccionado.setFormaEntrega(cmbFormaEntrega.getValue());
                pedidoSeleccionado.setFechaEntrega(fechaEntregaPedido.getValue());

                modificarPedido(pedidoSeleccionado); // Actualiza en la base de datos y la lista Observable

                showAlert(Alert.AlertType.INFORMATION, "Éxito", "El pedido fue actualizado correctamente.");
                Stage stage = (Stage) nombreCliente.getScene().getWindow();
                stage.close();
            } else {
                showAlert(Alert.AlertType.WARNING, "Error", "No se seleccionó ningún pedido para modificar.");
            }
        } else {
            alert.close();
        }
    }


    public void modificarPedido(Pedido pedido) {
        pedidoDAO.update(pedido);
        //Actualizar el pedido en la base de datos
        int index = pedidos.indexOf(pedido);
        if (index >= 0) {
            pedidos.set(index, pedido); // Actualizar el pedido en la ObservableList
            //mostrarPedidos(); // Actualizar la vista
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }


    @FXML
    void abrirCatalogoPedidos(ActionEvent event) {

    }

    public void setModificarPedidosController(PedidosController pedidosController) {
    }
}

