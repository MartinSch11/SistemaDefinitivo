package controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Setter;
import persistence.dao.TrabajadorDAO;
import utilities.Paths;
import model.Trabajador;
import persistence.dao.TrabajadorDAO;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    @FXML
    private ComboBox cmbFormaEntrega;

    @Setter
    private PedidosController pedidosController;

    //private TrabajadorDAO trabajadorDAO;

    @FXML
    public void initialize() {
        cargarNombresEmpleadosEnComboBox();
    }

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

    @FXML
    private Button btnCatalogo;

    @FXML
    void catalogoPedido(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(Paths.CATALOGOPEDIDOS));
            AnchorPane root = fxmlLoader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnCatalogo.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    private void cargarNombresEmpleadosEnComboBox() {
        try {
            List<String> nombres = trabajadorDAO.findAllNombres();
            empleadoAsignado.setItems(FXCollections.observableArrayList(nombres));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los nombres de los empleados: " + e.getMessage());
        }
    }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
