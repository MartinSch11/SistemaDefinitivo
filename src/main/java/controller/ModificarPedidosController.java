package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import model.Cliente;
import model.Pedido;
import model.Trabajador;
import persistence.dao.ClienteDAO;
import persistence.dao.PedidoDAO;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import persistence.dao.TrabajadorDAO;

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

    private ClienteDAO clienteDAO = new ClienteDAO();
    private TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    private PedidoDAO pedidoDAO = new PedidoDAO();
    private Pedido pedido;

    private ObservableList<Pedido> pedidos = FXCollections.observableArrayList();
    private Pedido pedidoSeleccionado;

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
        nombreCliente.setText(pedidos.getCliente().getNombre());
        contactoCliente.setText(pedidos.getCliente().getTelefono());
        dniClientePedido.setText(pedidos.getCliente().getDni());
        //detalle.setText(pedidos.getDetallePedido()); no se como hacer para modificar el detalle del pedido
        fechaEntregaPedido.setValue(pedidos.getFechaEntrega());
        cmbFormaEntrega.setValue(pedidos.getFormaEntrega());
        empleadoAsignado.setValue(pedidos.getEmpleadoAsignado().getNombre());
    }

    @FXML
    void GuardarModificacionPedido(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("Se modificará la información del pedido. ¿Desea guardar estos cambios?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (pedidoSeleccionado != null) {
                // Obtener el Cliente actual del Pedido
                Cliente cliente = pedidoSeleccionado.getCliente();
                if (cliente != null) {
                    cliente.setNombre(nombreCliente.getText());
                    cliente.setTelefono(contactoCliente.getText());
                    cliente.setDni(dniClientePedido.getText());
                    clienteDAO.update(cliente); // Guardar cambios en la base de datos
                }

                // Obtener el Trabajador actual del Pedido
                Trabajador trabajador = trabajadorDAO.findByNombre(empleadoAsignado.getValue());
                if (trabajador == null) {
                    showAlert(Alert.AlertType.ERROR, "Error", "El trabajador seleccionado no existe.");
                    return;
                }

                // Actualizar datos del Pedido
                pedidoSeleccionado.setEmpleadoAsignado(trabajador);
                pedidoSeleccionado.setFormaEntrega(cmbFormaEntrega.getValue());
                pedidoSeleccionado.setFechaEntrega(fechaEntregaPedido.getValue());

                // Guardar cambios en la base de datos
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

}

