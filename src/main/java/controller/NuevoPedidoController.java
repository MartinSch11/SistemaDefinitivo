package controller;

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
import model.*;
import persistence.dao.ClienteDAO;
import persistence.dao.TrabajadorDAO;
import service.PedidoService;
import utilities.*;
import java.time.LocalDate;
import java.util.*;
import java.io.IOException;

public class NuevoPedidoController {

    @FXML
    private Button btnCatalogo, btnCancelar, btnGuardar, btnNuevoCliente;
    @FXML
    private TextField contactoCliente, dniClienteField, nombreCliente;
    @FXML
    private ComboBox<String> empleadoAsignado, cmbFormaEntrega;
    @FXML
    private DatePicker fechaEntregaPedido;
    @FXML
    private Label lblTotalPedido;

    private Pedido pedidoCreado;
    private CatalogoPedidosController catalogoPedidosController;
    private PedidosTableroController pedidosTableroController;
    private Map<Producto, Integer> productosOriginales = new HashMap<>();
    private Map<Combo, Integer> combosOriginales = new HashMap<>();

    private final PedidoService pedidoService = new PedidoService();

    public void setPedidosTableroController(PedidosTableroController pedidosTableroController) {
        this.pedidosTableroController = pedidosTableroController;
    }

    public void setCatalogoPedidosController(CatalogoPedidosController catalogoPedidosController) {
        this.catalogoPedidosController = catalogoPedidosController;
    }

    @FXML
    public void initialize() {
        cargarNombresEmpleadosEnComboBox();
        configurarCamposTexto();
        configurarFechaEntrega();
        dniClienteField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                buscarYActualizarCliente(dniClienteField.getText());
            }
        });
        cmbFormaEntrega.setValue("Retira del local");
        actualizarTotalPedido(); // Inicializa el total en 0
    }

    private void actualizarTotalPedido() {
        double total = 0.0;
        boolean tieneProductos = false;
        if (catalogoPedidosController != null) {
            Map<Producto, Integer> productos = catalogoPedidosController.getProductosGuardados();
            Map<Combo, Integer> combos = catalogoPedidosController.getCombosGuardados();
            if (productos != null && !productos.isEmpty()) {
                tieneProductos = true;
                for (Map.Entry<Producto, Integer> entry : productos.entrySet()) {
                    if (entry.getKey() != null && entry.getKey().getPrecio() != null) {
                        total += entry.getKey().getPrecio().doubleValue() * entry.getValue();
                    }
                }
            }
            if (combos != null && !combos.isEmpty()) {
                tieneProductos = true;
                for (Map.Entry<Combo, Integer> entry : combos.entrySet()) {
                    if (entry.getKey() != null && entry.getKey().getPrecio() != null) {
                        total += entry.getKey().getPrecio().doubleValue() * entry.getValue();
                    }
                }
            }
        }
        // Si no hay productos ni combos cargados, mostrar el total del pedido si existe
        if (lblTotalPedido != null) {
            if (!tieneProductos && pedidoCreado != null && pedidoCreado.getTotalPedido() != null) {
                lblTotalPedido.setText("Total: $" + String.format("%.2f", pedidoCreado.getTotalPedido()));
            } else {
                lblTotalPedido.setText("Total: $" + String.format("%.2f", total));
            }
        }
    }

    private void configurarCamposTexto() {
        nombreCliente.textProperty().addListener((_, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-ZÁÉÍÓÚáéíóúÑñÜü\\s]*"))
                nombreCliente.setText(oldVal);
        });

        contactoCliente.textProperty().addListener((_, oldVal, newVal) -> {
            if (!newVal.matches("\\d*"))
                contactoCliente.setText(oldVal);
        });

        dniClienteField.textProperty().addListener((_, oldVal, newVal) -> {
            if (!newVal.matches("\\d*"))
                dniClienteField.setText(oldVal);
        });

        cmbFormaEntrega.getItems().addAll("Retira del local", "Paga su envío");
    }

    private void configurarFechaEntrega() {
        fechaEntregaPedido.setDayCellFactory(_ -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });
    }

    private void cargarNombresEmpleadosEnComboBox() {
        try {
            List<String> nombres = new TrabajadorDAO().findAllNombres();
            empleadoAsignado.setItems(FXCollections.observableArrayList(nombres));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los empleados.", "Error de empleados");
        }
    }

    @FXML
    private void abrirCatalogoPedidos(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/CatalogoPedidos.fxml"));
            Parent root = loader.load();

            catalogoPedidosController = loader.getController();
            catalogoPedidosController.setDialogNuevoPedidoController(this);

            // Si estamos editando un pedido y tiene productos o combos, cargar SOLO una vez
            // los productos y combos originales
            if (pedidoCreado != null && pedidoCreado.getNumeroPedido() != null) {
                // Consolidar cantidades por producto
                if (pedidoCreado.getPedidoProductos() != null) {
                    Map<Producto, Integer> productos = new HashMap<>();
                    for (PedidoProducto pp : pedidoCreado.getPedidoProductos()) {
                        productos.merge(pp.getProducto(), pp.getCantidad(), Integer::sum);
                    }
                    catalogoPedidosController.cargarProductosGuardados(productos);
                }
                // Consolidar cantidades por combo
                if (pedidoCreado.getPedidoCombos() != null) {
                    Map<Combo, Integer> combos = new HashMap<>();
                    for (PedidoCombo pc : pedidoCreado.getPedidoCombos()) {
                        combos.merge(pc.getCombo(), pc.getCantidad(), Integer::sum);
                    }
                    catalogoPedidosController.cargarCombosGuardados(combos);
                }
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Catálogo de Pedidos");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            ActionLogger.log("Catálogo abierto correctamente.");
            actualizarTotalPedido(); // Actualiza el total al cerrar el catálogo
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo abrir el catálogo.", "Error de catálogo");
        }
    }

    // --- NUEVO: Obtener combos guardados del catálogo ---
    private Map<Combo, Integer> obtenerCombosDelCatalogo() {
        if (catalogoPedidosController != null)
            return new HashMap<>(catalogoPedidosController.getCombosGuardados());

        // Si no se abrió el catálogo, usar los combos originales (solo en modo edición)
        return new HashMap<>(combosOriginales);
    }

    @FXML
    private void GuardarInfoPedido(ActionEvent event) {
        if (!validarCampos())
            return;

        try {
            Map<Producto, Integer> productos = obtenerProductosDelCatalogo();
            Map<Combo, Integer> combos = obtenerCombosDelCatalogo();

            PedidoService.PedidoConFaltantes resultado;
            boolean esEdicion = (pedidoCreado != null && pedidoCreado.getNumeroPedido() != null);
            if (esEdicion) {
                resultado = pedidoService.actualizarPedido(
                        pedidoCreado,
                        dniClienteField.getText(),
                        empleadoAsignado.getValue(),
                        cmbFormaEntrega.getValue(),
                        fechaEntregaPedido.getValue(),
                        productos,
                        combos);
            } else {
                resultado = pedidoService.crearPedido(
                        dniClienteField.getText(),
                        empleadoAsignado.getValue(),
                        cmbFormaEntrega.getValue(),
                        fechaEntregaPedido.getValue(),
                        productos,
                        combos);
            }

            Pedido pedidoCreado = resultado.getPedido();
            this.pedidoCreado = pedidoCreado;

            if (resultado.getMensajeDevolucion() != null && !resultado.getMensajeDevolucion().isBlank()) {
                Alert devueltosAlert = new Alert(Alert.AlertType.INFORMATION);
                devueltosAlert.setTitle("Stock actualizado");
                devueltosAlert.setHeaderText("Insumos devueltos al stock");
                devueltosAlert.setContentText(resultado.getMensajeDevolucion());
                devueltosAlert.showAndWait();
            }

            if (resultado.getMensajeFaltantes() != null && !resultado.getMensajeFaltantes().isBlank()) {
                Alert faltantesAlert = new Alert(Alert.AlertType.WARNING);
                faltantesAlert.setTitle("Insumos faltantes");
                faltantesAlert.setHeaderText("Algunos insumos estaban incompletos");
                faltantesAlert.setContentText(resultado.getMensajeFaltantes());
                faltantesAlert.showAndWait();
            }

            // SOLO llamar al método correcto según si es nuevo o edición
            if (pedidosTableroController != null) {
                if (esEdicion) {
                    pedidosTableroController.agregarNuevoPedido(pedidoCreado);
                } else {
                    pedidosTableroController.agregarPedido(pedidoCreado, true);
                }
            }

            cerrarVentana(event);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error al guardar",
                    "Ha ocurrido un error al guardar el pedido. Por favor, revise los datos ingresados o intente nuevamente. Si el problema persiste, contacte al administrador.",
                    "Error de guardado");
        }
    }

    private boolean validarCampos() {
        return !(dniClienteField.getText().isEmpty()
                || nombreCliente.getText().isEmpty()
                || contactoCliente.getText().isEmpty()
                || empleadoAsignado.getValue() == null
                || cmbFormaEntrega.getValue() == null
                || fechaEntregaPedido.getValue() == null);
    }

    private Map<Producto, Integer> obtenerProductosDelCatalogo() {
        if (catalogoPedidosController != null)
            return new HashMap<>(catalogoPedidosController.getProductosGuardados());

        // Si no se abrió el catálogo, usar los productos originales (solo en modo
        // edición)
        return new HashMap<>(productosOriginales);
    }

    private void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void CancelarPedido(ActionEvent event) {
        if (confirmarCancelar()) {
            ActionLogger.log("Pedido cancelado por el usuario.");
            pedidoCreado = null; // Evita que se agregue/modifique el pedido si se cancela
            cerrarVentana(event);
        }
    }

    private boolean confirmarCancelar() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("¿Desea cancelar el pedido? Se perderán los cambios.");
        return alert.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    private void showAlert(Alert.AlertType type, String title, String content, String header) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleNuevoCliente(ActionEvent event) {
        SceneLoader.loadScene(new NodeSceneStrategy(btnNuevoCliente), Paths.NUEVOCLIENTE, "/css/components.css", false);
        ActionLogger.log("Accedió a nuevo cliente.");
    }

    private void buscarYActualizarCliente(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Debe ingresar un DNI válido.", "DNI inválido");
            return;
        }

        try {
            Cliente cliente = new ClienteDAO().findByDni(dni);

            if (cliente != null) {
                nombreCliente.setText(cliente.getNombre() + " " + cliente.getApellido());
                contactoCliente.setText(cliente.getTelefono());
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se encontró ningún cliente con ese DNI.",
                        "Cliente no encontrado");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error al buscar cliente", "Error de búsqueda");
        }
    }

    public String getDniCliente() {
        return dniClienteField.getText();
    }

    public Pedido getPedidoCreado() {
        return pedidoCreado;
    }

    /**
     * Carga los datos de un pedido existente en el formulario para edición.
     */
    public void cargarPedidoParaEdicion(Pedido pedido) {
        if (pedido == null)
            return;

        this.pedidoCreado = pedido;

        // Cliente
        if (pedido.getCliente() != null) {
            dniClienteField.setText(pedido.getCliente().getDni());
            nombreCliente.setText(pedido.getCliente().getNombre()
                    + (pedido.getCliente().getApellido() != null ? " " + pedido.getCliente().getApellido() : ""));
            contactoCliente.setText(pedido.getCliente().getTelefono());
        }

        // Empleado
        if (pedido.getEmpleadoAsignado() != null) {
            empleadoAsignado.setValue(pedido.getEmpleadoAsignado().getNombre());
        }

        // Forma de entrega
        cmbFormaEntrega.setValue(pedido.getFormaEntrega());

        // Fecha de entrega
        fechaEntregaPedido.setValue(pedido.getFechaEntrega());

        // Mostrar el total del pedido desde la base de datos
        if (lblTotalPedido != null && pedido.getTotalPedido() != null) {
            lblTotalPedido.setText("Total: $" + String.format("%.2f", pedido.getTotalPedido()));
        }

        // Cargar productos originales en memoria (aunque no se abra catálogo)
        productosOriginales.clear();
        if (pedido.getPedidoProductos() != null) {
            for (PedidoProducto pp : pedido.getPedidoProductos()) {
                productosOriginales.merge(pp.getProducto(), pp.getCantidad(), Integer::sum);
            }
        }

        combosOriginales.clear();
        if (pedido.getPedidoCombos() != null) {
            for (PedidoCombo pc : pedido.getPedidoCombos()) {
                combosOriginales.merge(pc.getCombo(), pc.getCantidad(), Integer::sum);
            }
        }

        // Actualizar total después de precargar
        actualizarTotalPedido();
    }

    /**
     * Habilita o deshabilita el botón de catálogo de productos.
     */
    public void habilitarCatalogo(boolean habilitar) {
        if (btnCatalogo != null) {
            btnCatalogo.setDisable(!habilitar);
        }
    }

    // --- NUEVO: Permite que el catálogo notifique el total actualizado ---
    public void notificarTotalActualizado() {
        actualizarTotalPedido();
    }
}
