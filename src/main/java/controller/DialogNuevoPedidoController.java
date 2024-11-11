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
import javafx.util.Callback;
import lombok.Setter;
import model.Producto;
import model.Trabajador;
import persistence.dao.PedidoDAO;
import persistence.dao.ProductoDAO;
import model.Pedido;
import persistence.dao.TrabajadorDAO;
import utilities.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.io.IOException;

public class DialogNuevoPedidoController {
    @FXML
    private Button btnCatalogo;
    @FXML
    private Button btnCancelar;
    @FXML
    private Button btnGuardar;
    @FXML private TextField contactoCliente;@FXML private ComboBox<String> empleadoAsignado; @FXML private TextField dniClientePedido; @FXML private DatePicker fechaEntregaPedido;@FXML private TextField nombreCliente;@FXML private ComboBox cmbFormaEntrega;

    @FXML
    public void initialize() {
        cargarNombresEmpleadosEnComboBox();
        cmbFormaEntrega.getItems().addAll("Retira del local", "Paga su envío");

        nombreCliente.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z\\s]*")) {
            nombreCliente.setText(oldValue);
            }
        });
        contactoCliente.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
            contactoCliente.setText(oldValue);
        }
        });
        dniClientePedido.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                contactoCliente.setText(oldValue);
            }
        });
        // Establecer el DayCellFactory para deshabilitar las fechas anteriores a hoy
        fechaEntregaPedido.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty); if (item.isBefore(LocalDate.now())) {
                            setDisable(true); setStyle("-fx-background-color: #ffc0cb;"); // Color de fondo para fechas deshabilitadas
                            }
                    }
                };
            }
        });
    }

    @Setter
    private PedidosController pedidosController;
    public void setPedidosController(PedidosController pedidosController) {
        this.pedidosController = pedidosController;
    }

    private CatalogoPedidosController catalogoPedidosController;
    public void setCatalogoPedidosController(CatalogoPedidosController catalogoPedidosController) {
        this.catalogoPedidosController = catalogoPedidosController;
    }


    @FXML private void abrirCatalogoPedidos(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/CatalogoPedidos.fxml"));
            Parent root = loader.load(); CatalogoPedidosController controller = loader.getController();
            controller.setDialogNuevoPedidoController(this);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Catálogo de Pedidos");
            stage.setScene(new Scene(root));
            stage.showAndWait();
    } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PedidoDAO pedidoDAO= new PedidoDAO();

    @FXML
    private void GuardarInfoPedido(ActionEvent event) {
        // Obtener los valores ingresados desde los controles
        String estadoPedido = "Sin empezar";
        String nombre = nombreCliente.getText();
        String contacto = contactoCliente.getText();
        String dniCliente = dniClientePedido.getText();

        //Obtener los productos guardados del catálogo y formatearlos en un detalle
        StringBuilder detalleBuilder = new StringBuilder();
        Map<Producto, Integer> productos = productosGuardadados.getProductos();
        for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
            detalleBuilder.append("Producto: ").append(entry.getKey().getNombre()) .append(", Cantidad: ").append(entry.getValue()).append("\n");
        }
        String detalle = detalleBuilder.toString();

        String empleado = empleadoAsignado.getValue(); // Obtener el valor seleccionado del ComboBox
        String formaEntrega = (String) cmbFormaEntrega.getValue(); // Cast necesario para ComboBox genérico
        LocalDate fechaEntrega = fechaEntregaPedido.getValue();

        // Validar que los campos requeridos no estén vacíos
        if (dniCliente.isEmpty() || nombre.isEmpty() || contacto.isEmpty() || detalle.isEmpty() || empleado == null || formaEntrega == null || fechaEntrega == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia");
            alert.setHeaderText("Campos obligatorios sin completar.");
            alert.setContentText("Solución: completar los campos vacíos.");
            alert.showAndWait();
        } else {
            Pedido nuevoPedidoGuardado = new Pedido(nombre, contacto, dniCliente, detalle, empleado, formaEntrega, fechaEntrega, estadoPedido);

            pedidoDAO.save(nuevoPedidoGuardado);
            
            // Validar que PedidosController no sea nulo antes de invocar el metodo
            if (pedidosController != null) {
                // Pasar los valores correctamente a PedidosController para agregar el nuevo pedido
                pedidosController.agregarNuevoPedido(dniCliente, nombre, contacto, detalle, empleado, formaEntrega, fechaEntrega, estadoPedido);

                // Cerrar la ventana
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
            }
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
/*@FXML
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
    }*/