package controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.*;
import persistence.dao.ClienteDAO;
import persistence.dao.PedidoDAO;
import persistence.dao.TrabajadorDAO;
import utilities.*;

import java.time.LocalDate;
import java.util.*;
import java.io.IOException;

public class NuevoPedidoController {

    @FXML private Button btnCatalogo;
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;
    @FXML private TextField contactoCliente;
    @FXML private ComboBox<String> empleadoAsignado;
    @FXML private TextField dniClienteField;
    @FXML private DatePicker fechaEntregaPedido;
    @FXML private TextField nombreCliente;
    @FXML private ComboBox<String> cmbFormaEntrega;
    @FXML private Button btnNuevoCliente;

    private RecetaProcessor recetaProcessor = new RecetaProcessor();
    private PedidoDAO pedidoDAO = new PedidoDAO();
    private Pedido pedidoCreado;
    private CatalogoPedidosController catalogoPedidosController;
    private PedidosController pedidosController;
    private TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();


    public void setPedidosController(PedidosController pedidosController) {
        this.pedidosController = pedidosController;
    }

    public void setCatalogoPedidosController(CatalogoPedidosController catalogoPedidosController) {
        this.catalogoPedidosController = catalogoPedidosController;
    }

    @FXML
    public void initialize() {
        cargarNombresEmpleadosEnComboBox();
        configurarCamposTexto();
        configurarFechaEntrega();

        // Configurar evento para el campo dniClienteField
        dniClienteField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                buscarYActualizarCliente(dniClienteField.getText());
            }
        });
        cmbFormaEntrega.setValue("Retira del local");
    }

    private void configurarCamposTexto() {
        nombreCliente.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-ZÁÉÍÓÚáéíóúÑñÜü\\s]*")) {
                nombreCliente.setText(oldValue);
            }
        });

        contactoCliente.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                contactoCliente.setText(oldValue);
            }
        });

        dniClienteField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                contactoCliente.setText(oldValue);
            }
        });

        cmbFormaEntrega.getItems().addAll("Retira del local", "Paga su envío");
    }

    private void configurarFechaEntrega() {
        fechaEntregaPedido.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        });
    }

    private void cargarNombresEmpleadosEnComboBox() {
        try {
            List<String> nombres = trabajadorDAO.findAllNombres();
            empleadoAsignado.setItems(FXCollections.observableArrayList(nombres));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los nombres de los empleados.");
        }
    }

    @FXML
    private void abrirCatalogoPedidos(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/CatalogoPedidos.fxml"));
            Parent root = loader.load();

            // Configurar el controlador del catálogo
            catalogoPedidosController = loader.getController();
            catalogoPedidosController.setDialogNuevoPedidoController(this);

            // Abrir el catálogo en un nuevo diálogo modal
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Catálogo de Pedidos");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            ActionLogger.log("Catálogo de pedidos abierto correctamente.");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo abrir el catálogo de pedidos: " + e.getMessage());
        }
    }

    @FXML
    private void GuardarInfoPedido(ActionEvent event) {
        if (!validarCampos()) return;

        Map<Producto, Integer> productos = obtenerProductosDelCatalogo();
        if (productos.isEmpty()) return;

        procesarRecetas(productos);
        String detalle = generarDetallePedido(productos);

        crearPedido(detalle);
        if (pedidoCreado == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo crear el pedido.");
            return;
        }

        guardarPedido();
        cerrarVentana(event);
    }

    private boolean validarCampos() {
        if (dniClienteField.getText().isEmpty() || nombreCliente.getText().isEmpty() ||
                contactoCliente.getText().isEmpty() || empleadoAsignado.getValue() == null ||
                cmbFormaEntrega.getValue() == null || fechaEntregaPedido.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Campos obligatorios sin completar.");
            return false;
        }
        return true;
    }

    private Map<Producto, Integer> obtenerProductosDelCatalogo() {
        if (catalogoPedidosController == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "El catálogo de productos no está disponible.");
            return new HashMap<>(); // Retorna un mapa mutable vacío
        }

        Map<Producto, Integer> productos = catalogoPedidosController.getProductosGuardados();
        if (productos.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Debe seleccionar al menos un producto.");
        }
        return productos;
    }

    private void procesarRecetas(Map<Producto, Integer> productos) {
        // Delegamos la lógica a la clase especializada
        recetaProcessor.procesarRecetas(productos);
    }

    private String generarDetallePedido(Map<Producto, Integer> productos) {
        StringBuilder detalleBuilder = new StringBuilder();
        productos.forEach((producto, cantidad) -> detalleBuilder
                .append("Producto: ").append(producto.getNombre())
                .append(", Cantidad: ").append(cantidad).append("\n"));
        return detalleBuilder.toString();
    }

    public void crearPedido(String detalle) {
        String estadoPedido = "Sin empezar";
        Long numeroPedido = null;  // O puedes obtener un número único si no usas auto-generación en la base de datos

        // Crear el pedido
        pedidoCreado = new Pedido(
                numeroPedido,  // Aquí pasas el valor de numeroPedido
                nombreCliente.getText(),
                contactoCliente.getText(),
                dniClienteField.getText(),
                empleadoAsignado.getValue(),
                cmbFormaEntrega.getValue(),
                fechaEntregaPedido.getValue(),
                estadoPedido
        );

        // Crear los objetos PedidoProducto y asociarlos al pedido
        Map<Producto, Integer> productos = obtenerProductosDelCatalogo();

        // Crear una lista de PedidoProducto para asociar los productos
        List<PedidoProducto> pedidoProductos = new ArrayList<>();
        for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
            Producto producto = entry.getKey();

            // Crear un nuevo PedidoProducto para cada producto
            PedidoProducto pedidoProducto = new PedidoProducto();
            PedidoProductoId id = new PedidoProductoId();
            id.setProductoId(producto.getId());
            id.setPedidoId(pedidoCreado.getNumeroPedido());

            pedidoProducto.setId(id);
            pedidoProducto.setProducto(producto);
            pedidoProducto.setPedido(pedidoCreado);

            pedidoProductos.add(pedidoProducto);  // Agregar a la lista de PedidoProducto
        }

        // Asociar la lista de PedidoProducto al pedido
        pedidoCreado.setPedidoProductos(pedidoProductos);

        // Persistir el pedido con la relación de productos
        // Guardar pedido en base de datos
    }



    private void guardarPedido() {
        try {
            pedidoDAO.save(pedidoCreado);

            if (pedidosController != null) {
                pedidosController.agregarNuevoPedido(
                        pedidoCreado.getNumeroPedido(),
                        pedidoCreado.getDniCliente(),
                        pedidoCreado.getNombreCliente(),
                        pedidoCreado.getContactoCliente(),
                        pedidoCreado.generarDetalle(), // Usar el método `generarDetalle()` en lugar de `getDetallePedido()`
                        pedidoCreado.getEmpleadoAsignado(),
                        pedidoCreado.getFormaEntrega(),
                        pedidoCreado.getFechaEntrega(),
                        pedidoCreado.getEstadoPedido()
                );
            }
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }


    private void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    void CancelarPedido(ActionEvent event) {
        if (confirmarCancelar()) {
            ActionLogger.log("Se canceló el pedido sin guardar.");
            cerrarVentana(event);
        }
    }

    private boolean confirmarCancelar() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("Se perderán los cambios realizados. ¿Desea salir?");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleNuevoCliente(ActionEvent event) {
        SceneLoader.loadScene(new NodeSceneStrategy(btnNuevoCliente), Paths.NUEVOCLIENTE, "/css/components.css", false);
        ActionLogger.log("Accedió a la pantalla de nuevo cliente.");
    }

    public String getDniCliente() {
        return dniClienteField.getText();
    }

    public Pedido getPedidoCreado() {
        return pedidoCreado;
    }

    private void buscarYActualizarCliente(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Debe ingresar un DNI válido.");
            return;
        }

        try {
            Cliente cliente = clienteDAO.findByDni(dni);

            if (cliente != null) {
                nombreCliente.setText(cliente.getNombre() + " " + cliente.getApellido());
                contactoCliente.setText(cliente.getTelefono());
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Información", "No se encontró un cliente con ese DNI.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error al buscar el cliente: " + e.getMessage());
        }
    }


}
