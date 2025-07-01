package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Setter;
import model.*;
import persistence.dao.*;
import utilities.ActionLogger;
import utilities.Paths;
import utilities.SceneLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import javafx.geometry.Insets;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javafx.scene.control.Alert;

public class PedidosController {
    @FXML
    private Label lblNOrden;
    @FXML
    private Label lblDNICliente;
    @FXML
    private Label lblDetallePedidoCliente;
    @FXML
    private Label lblEmpleadoAsignadoAlPedido;
    @FXML
    private Label lblFechaEntrega;
    @FXML
    private Label lblFormaEntrega;
    @FXML
    private Label lblNomCliente;
    @FXML
    private Label lblTotalPedido;
    @FXML
    private Label lblTelefonoCliente;
    @FXML
    private ComboBox<String> cmbEstadoDelPedido;
    @FXML
    private GridPane gridContenedorPedidos;
    @FXML
    private Pane paneVisualizarPedido;
    @FXML
    private Button btnBorrarPedido;
    @FXML
    private Button btnModificar;
    @FXML
    private VBox pedidosVBox; // Donde se mostrarán los pedidos
    //private PedidoDAO pedidoDAO;
    private RecetaDAO recetaDAO;
    private ClienteDAO clienteDAO;
    private TrabajadorDAO trabajadorDAO;
    private InsumoDAO insumoDAO;
    private List<Producto> productos;
    private ObservableList<Pedido> pedidos = FXCollections.observableArrayList();
    private Pedido pedidoSeleccionado;

    @Setter
    private NuevoPedidoController dialognuevopedidoController;

    public void setDialogNuevoPedidoController(NuevoPedidoController dialognuevopedidoController) {
        this.dialognuevopedidoController = dialognuevopedidoController;
    }

    public ObservableList<Pedido> getPedidos() {
        return pedidos;
    }

    private PedidoDAO pedidoDAO = new PedidoDAO();

    public PedidosController() {
        pedidoDAO = new PedidoDAO();
        recetaDAO = new RecetaDAO();
        insumoDAO = new InsumoDAO();
        clienteDAO = new ClienteDAO();
        trabajadorDAO = new TrabajadorDAO();
    }

    @FXML
    public void initialize() {
        // Cargar los pedidos existentes desde la base de datos al iniciar
        List<Pedido> pedidosExistentes = pedidoDAO.findAll();
        pedidos.setAll(pedidosExistentes);

        cmbEstadoDelPedido.getItems().addAll("Sin empezar", "En proceso", "Hecho");
        paneVisualizarPedido.setVisible(false);

        //Añadir un listener para cambiar el color del borde según la opción seleccionada
        cmbEstadoDelPedido.setOnAction(event -> actualizarColoresStackPane());

        cargarPedidos(); // Cargar y mostrar todos los pedidos guardados en la base de datos
    }

    private void cargarPedidos() {
        List<Pedido> pedidos = pedidoDAO.findAll();
        for (Pedido pedido : pedidos) {
            agregarNuevoPedido(
                    pedido.getNumeroPedido(),
                    pedido.getCliente().getDni(), // <-- Cambiado: primero DNI
                    pedido.getCliente().getNombre(), // <-- luego nombre
                    pedido.getCliente().getTelefono(),
                    pedido.generarDetalle(),
                    pedido.getEmpleadoAsignado().getNombre(),
                    pedido.getFormaEntrega(),
                    pedido.getFechaEntrega(),
                    pedido.getEstadoPedido(),
                    pedido.getTotalPedido().toString()
            );
        }
    }

    private void actualizarColoresStackPane() {
        List<Node> nodes = gridContenedorPedidos.getChildren();

        for (int i = 0; i < nodes.size(); i++) {
            // Verifica que el índice no sea mayor que el tamaño de la lista de pedidos
            if (i < pedidos.size()) {
                StackPane stackPane = (StackPane) nodes.get(i);
                Pedido pedido = pedidos.get(i); // Obtener el pedido correspondiente
                String estadoPedido = pedido.getEstadoPedido(); // Obtener el estado del pedido

                if (estadoPedido != null) {
                    switch (estadoPedido) {
                        case "Hecho":
                            stackPane.setStyle("-fx-border-color: green; -fx-border-radius: 5px; -fx-background-color: #FFF4F4;");
                            break;
                        case "En proceso":
                            stackPane.setStyle("-fx-border-color: blue; -fx-border-radius: 5px; -fx-background-color: #FFF4F4;");
                            break;
                        case "Sin Empezar":
                        default:
                            stackPane.setStyle("-fx-border-color: white; -fx-border-radius: 5px; -fx-background-color: #FFF4F4;");
                            break;
                    }
                }
            }
        }
    }

    @FXML
    void pedidoActualVisualizandose() {
        String DNIpedidoActual;
        DNIpedidoActual = lblDNICliente.getText();
    }

    public void agregarNuevoPedido(Long numeroPedido, String dniCliente, String nombre, String contacto,
                                   String detalle, String empleado, String formaEntrega, LocalDate fechaEntrega,
                                   String estadoPedido, String totalPedido) {
        try {
            Cliente cliente = clienteDAO.findByDni(dniCliente);
            if (cliente == null) {
                throw new RuntimeException("Cliente con DNI " + dniCliente + " no encontrado en la base de datos.");
            }

            Trabajador trabajador = trabajadorDAO.findByNombre(empleado);
            if (trabajador == null) {
                throw new RuntimeException("Trabajador con nombre " + empleado + " no encontrado en la base de datos.");
            }

            // Crear etiquetas para mostrar la información del pedido
            Label detalleCompletoPedido = new Label();

            // Crear un nuevo StackPane para el pedido
            StackPane nuevoPedidoStackPane = crearStackPanePedido(detalleCompletoPedido);

            // ✅ Convertir totalPedido de String a BigDecimal
            BigDecimal totalPedidoBD = new BigDecimal(totalPedido);

            // Crear el objeto Pedido
            Pedido pedido = new Pedido(numeroPedido, cliente, trabajador, formaEntrega, fechaEntrega, estadoPedido, detalle, totalPedidoBD);

            // Obtener el detalle generado
            String detalleGenerado = pedido.generarDetalle();

            // Mostrar el detalle en la etiqueta
            configurarLabelDetalle(detalleCompletoPedido, detalleGenerado, empleado, fechaEntrega);

            // Manejar el clic en el StackPane
            manejarClicStackPane(nuevoPedidoStackPane, pedido, numeroPedido, nombre, detalleGenerado, contacto, empleado, formaEntrega, fechaEntrega, dniCliente);

            // Agregar el StackPane al GridPane
            agregarStackPaneAlGridPane(nuevoPedidoStackPane);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private StackPane crearStackPanePedido(Label detalleCompletoPedido) {
        StackPane nuevoPedidoStackPane = new StackPane(detalleCompletoPedido);
        nuevoPedidoStackPane.setPrefWidth(140);
        nuevoPedidoStackPane.setPrefHeight(150);

        nuevoPedidoStackPane.setStyle("""
                    -fx-background-color: linear-gradient(to bottom right, #ffffff, #f9f9f9);
                    -fx-border-radius: 8px;
                    -fx-background-radius: 8px;
                    -fx-border-color: #d0d0d0;
                    -fx-border-width: 1;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.1, 2, 2);
                    -fx-padding: 8;
                """);
        return nuevoPedidoStackPane;
    }

    private void configurarLabelDetalle(Label detalleCompletoPedido, String detalleGenerado, String empleado, LocalDate fechaEntrega) {
        detalleCompletoPedido.setText(detalleGenerado + "\n👤 " + empleado + "  📅 " + fechaEntrega.toString());
        detalleCompletoPedido.setStyle("""
                    -fx-font-size: 13px;
                    -fx-text-fill: #444;
                    -fx-font-family: 'Segoe UI', sans-serif;
                    -fx-alignment: TOP_LEFT;
                """);
        detalleCompletoPedido.setWrapText(true);
    }

    private void manejarClicStackPane(StackPane nuevoPedidoStackPane, Pedido pedido, Long numeroPedido, String nombre, String detalleGenerado, String contacto, String empleado, String formaEntrega, LocalDate fechaEntrega, String dniCliente) {
        nuevoPedidoStackPane.setOnMouseClicked(event -> {
            // Asignar el pedido correspondiente
            pedidoSeleccionado = pedido;

            // Mostrar los detalles en la interfaz
            paneVisualizarPedido.setVisible(true);
            lblNOrden.setText(numeroPedido.toString());
            lblNomCliente.setText(nombre);
            lblDetallePedidoCliente.setText(detalleGenerado); // Usar el detalle generado
            lblTelefonoCliente.setText(contacto);
            lblEmpleadoAsignadoAlPedido.setText(empleado);
            lblFormaEntrega.setText(formaEntrega);
            lblFechaEntrega.setText(fechaEntrega.toString());
            lblDNICliente.setText(dniCliente);
            lblTotalPedido.setText(pedido.getTotalPedido().toString());

            // Llamar a la función para procesar la visualización del pedido
            pedidoActualVisualizandose();
            actualizarColoresStackPane(); // Cambia el borde del StackPane según el estado del pedido

            // Pasar los datos al controlador de modificación
            if (pedidoSeleccionado != null) {
                ModificarPedidosController modificarController = new ModificarPedidosController();
                modificarController.obtenerDatos(pedidoSeleccionado); // Pasar el pedido seleccionado
            }
        });
    }

    private void agregarStackPaneAlGridPane(StackPane stackPane) {
        int totalPedidos = gridContenedorPedidos.getChildren().size();
        int columna = totalPedidos % 3; // 3 columnas por fila
        int fila = totalPedidos / 3;

        gridContenedorPedidos.add(stackPane, columna, fila);
        GridPane.setMargin(stackPane, new Insets(4, 6, 4, 6)); // top, right, bottom, left
    }

    private void actualizarGridPane() {
        gridContenedorPedidos.getChildren().clear();
        cargarPedidos();
    }

    private void seleccionarPedido(Pedido pedido) {
        if (pedido == null) return;  // Evita errores si el pedido es null

        paneVisualizarPedido.setVisible(true);

        // Evita NullPointerException usando valores por defecto si los objetos son null
        lblNomCliente.setText(pedido.getCliente() != null ? pedido.getCliente().getNombre() : "Sin cliente");
        lblDetallePedidoCliente.setText(pedido.generarDetalle());
        lblTelefonoCliente.setText(pedido.getCliente() != null ? pedido.getCliente().getTelefono() : "No disponible");
        lblEmpleadoAsignadoAlPedido.setText(pedido.getEmpleadoAsignado() != null ? pedido.getEmpleadoAsignado().getNombre() : "No asignado");
        lblFormaEntrega.setText(pedido.getFormaEntrega());
        lblFechaEntrega.setText(pedido.getFechaEntrega() != null ? pedido.getFechaEntrega().toString() : "No definida");
        lblDNICliente.setText(pedido.getCliente() != null ? pedido.getCliente().getDni() : "No disponible");
        lblTotalPedido.setText(pedido.getTotalPedido() != null ? pedido.getTotalPedido().toString() : "0.00");

        pedidoSeleccionado = pedido;
        actualizarColoresStackPane();
    }


    public void agregarPedido(Pedido pedido) {
        // Guardar el pedido en la base de datos
        pedidoDAO.save(pedido);

        // Agregar el pedido a la ObservableList
        pedidos.add(pedido);

        // Llamar a actualizarGridPane para que se refleje visualmente en la interfaz
        actualizarGridPane();

        // Registrar la acción de agregar el pedido
        ActionLogger.log("Pedido creado: " + pedido.getCliente().getNombre() +
                " con productos: " + pedido.getProductos());
    }

    @Setter
    private ModificarPedidosController modificarPedidosController;

    @FXML
    void handleModificar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/ModificarPedidos.fxml"));
            Parent root = loader.load();
            ModificarPedidosController controller = loader.getController();
            controller.setModificarPedidosController(this);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modificacion de Pedidos");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Registrar la acción de modificación
            ActionLogger.log("Pedido modificado: DNI Cliente = " + pedidoSeleccionado);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleBorrarPedido(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación de Borrado");
        alert.setHeaderText("¿Desea eliminar este pedido?");
        alert.setContentText("Esta acción no se puede deshacer.");
        ButtonType buttonTypeSi = new ButtonType("Sí");
        ButtonType buttonTypeCancelar = new ButtonType("Cancelar");

        alert.getButtonTypes().setAll(buttonTypeSi, buttonTypeCancelar);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == buttonTypeSi) {
            // Ahora usamos 'pedidoSeleccionado' para obtener el número de pedido
            if (pedidoSeleccionado != null) {
                Pedido pedidoAEliminar = pedidoDAO.findByNumeroPedido(pedidoSeleccionado.getNumeroPedido());
                if (pedidoAEliminar != null) {
                    pedidoDAO.delete(pedidoAEliminar);
                    pedidos.remove(pedidoAEliminar);

                    actualizarGridPane();

                    paneVisualizarPedido.setVisible(false);

                    // Registrar la acción de eliminación
                    ActionLogger.log("Pedido eliminado: Número de Pedido = " + pedidoSeleccionado.getNumeroPedido());
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Pedido no encontrado");
                    errorAlert.setContentText("El pedido que intentas eliminar no existe.");
                    errorAlert.showAndWait();
                }
            }
        } else {
            alert.close();
        }
    }

    private void procesarReduccionInsumos(List<Producto> productos) {
        for (Producto producto : productos) {
            // Obtener la receta asociada al producto
            Receta receta = recetaDAO.findByProducto(producto);

            // Obtener los insumos de la receta y reducir sus cantidades
            if (receta != null) {
                List<Insumo> insumosDeLaReceta = receta.getInsumos();
                for (InsumoReceta insumoReceta : receta.getInsumosReceta()) {
                    Insumo insumo = insumoReceta.getInsumo();
                    double cantidadRequerida = insumoReceta.getCantidadUtilizada();
                    insumo.reducirCantidad(cantidadRequerida, insumoReceta.getUnidad());
                }

            }
        }
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/loginAdmin.css", true);
    }

    @FXML
    void btnCrearNuevoPedido(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/DialogNuevoPedido.fxml"));
            Parent root = loader.load();

            // Inicializar y configurar el controlador del diálogo
            NuevoPedidoController nuevoPedidoController = loader.getController();
            nuevoPedidoController.setPedidosController(this); // Pasar referencia al controlador principal

            Stage stage = new Stage();
            stage.setTitle("Información del pedido");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Recuperar el pedido creado
            Pedido nuevoPedido = nuevoPedidoController.getPedidoCreado();

            if (nuevoPedido == null) {
                mostrarAlerta("Error", "El pedido no se creó correctamente.");
                return;
            }

            // Validar y reducir insumos si es posible
            if (validarYReducirInsumos(nuevoPedido)) {
                agregarPedido(nuevoPedido);
                ActionLogger.log("Pedido creado: " + nuevoPedido.getCliente().getNombre() +
                        " con productos: " + nuevoPedido.getProductos());
            } else {
                mostrarAlerta("Error", "No se pudo completar el pedido por falta de insumos.");
            }

        } catch (Exception e) {
            // Mostrar el mensaje real de la excepción para depuración
            Throwable cause = e.getCause();
            String mensaje = (cause != null) ? cause.getMessage() : e.getMessage();
            mostrarAlerta("Error inesperado", "Ocurrió un error al crear el pedido:\n" + mensaje);
            e.printStackTrace();
        }
    }


    private boolean validarYReducirInsumos(Pedido pedido) {
        List<Producto> productosDelPedido = pedido.getProductos();

        if (productosDelPedido == null || productosDelPedido.isEmpty()) {
            ActionLogger.log("No hay productos en el pedido.");
            return false; // No hay productos, no se puede validar ni reducir insumos
        }

        for (Producto producto : productosDelPedido) {
            Receta receta = producto.getReceta();

            if (receta != null) {
                for (InsumoReceta insumoReceta : receta.getInsumosReceta()) {
                    Insumo insumo = insumoReceta.getInsumo();
                    double cantidadRequerida = insumoReceta.getCantidadUtilizada();

                    if (insumo.getCantidadDisponible() < cantidadRequerida) {
                        ActionLogger.log("Insumo insuficiente: " + insumo.getNombre() +
                                ". Requerido: " + cantidadRequerida + ", disponible: " + insumo.getCantidadDisponible());
                        return false; // Falta de insumos
                    }
                }
            }
        }

        for (Producto producto : productosDelPedido) {
            Receta receta = producto.getReceta();

            if (receta != null) {
                for (InsumoReceta insumoReceta : receta.getInsumosReceta()) {
                    Insumo insumo = insumoReceta.getInsumo();
                    double cantidadRequerida = insumoReceta.getCantidadUtilizada();

                    insumo.reducirCantidad(cantidadRequerida, insumoReceta.getUnidad());
                    insumoDAO.update(insumo); // Actualizar la base de datos

                    ActionLogger.log("Reducido " + cantidadRequerida + " unidades de " +
                            insumo.getNombre() + ". Cantidad restante: " +
                            insumo.getCantidadDisponible());
                }
            }
        }
        return true; // Pedido válido y procesado
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
