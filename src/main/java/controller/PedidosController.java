package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import model.*;
import persistence.dao.*;
import utilities.ActionLogger;
import utilities.Paths;
import utilities.SceneLoader;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import utilities.RecetaProcessor;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.scene.control.Alert;

public class PedidosController {
    @FXML private Label lblNOrden;
    @FXML private Label lblDNICliente;
    @FXML private Label lblDetallePedidoCliente;
    @FXML private Label lblEmpleadoAsignadoAlPedido;
    @FXML private Label lblFechaEntrega;
    @FXML private Label lblFormaEntrega;
    @FXML private Label lblNomCliente;
    @FXML private Label lblTotalPedido;
    @FXML private Label lblTelefonoCliente;
    @FXML private ComboBox<String> cmbEstadoDelPedido;
    @FXML private GridPane gridContenedorPedidos;
    @FXML private Pane paneVisualizarPedido;
    @FXML private Button btnBorrarPedido;
    @FXML private Button btnModificar;
    @FXML private VBox pedidosVBox; // Donde se mostrarán los pedidos
    //private PedidoDAO pedidoDAO;
    private RecetaDAO recetaDAO;
    private ClienteDAO clienteDAO;
    private TrabajadorDAO trabajadorDAO;
    private InsumoDAO insumoDAO;
    private List<Producto> productos;
    private ObservableList<Pedido> pedidos = FXCollections.observableArrayList();
    private Pedido pedidoSeleccionado;

    public ObservableList<Pedido> getPedidos() {
        return pedidos;
    }

    private PedidoDAO pedidoDAO = new PedidoDAO();
    private final RecetaProcessor recetaProcessor = new RecetaProcessor();

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
        cmbEstadoDelPedido.setOnAction(event -> {
            String nuevoEstado = cmbEstadoDelPedido.getValue();
            if (pedidoSeleccionado != null && nuevoEstado != null) {
                pedidoSeleccionado.setEstadoPedido(nuevoEstado);
                pedidoDAO.update(pedidoSeleccionado); // Actualiza en la base de datos
                // Actualizar también el objeto en la lista pedidos
                for (Pedido p : pedidos) {
                    if (p.getNumeroPedido().equals(pedidoSeleccionado.getNumeroPedido())) {
                        p.setEstadoPedido(nuevoEstado);
                        break;
                    }
                }
                // Buscar y actualizar solo el StackPane correspondiente al pedido seleccionado
                for (int i = 0; i < gridContenedorPedidos.getChildren().size(); i++) {
                    StackPane stackPane = (StackPane) gridContenedorPedidos.getChildren().get(i);
                    // Buscar el pedido correspondiente por número de pedido
                    if (i < pedidos.size() && pedidos.get(i).getNumeroPedido().equals(pedidoSeleccionado.getNumeroPedido())) {
                        String colorBorde;
                        switch (nuevoEstado) {
                            case "Hecho":
                                colorBorde = "green";
                                break;
                            case "En proceso":
                                colorBorde = "blue";
                                break;
                            case "Sin empezar":
                            default:
                                colorBorde = "white";
                                break;
                        }
                        String estiloBase = "-fx-background-color: linear-gradient(to bottom right, #ffffff, #f9f9f9);"
                                + "-fx-border-radius: 8px;"
                                + "-fx-background-radius: 8px;"
                                + "-fx-border-width: 1;"
                                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.1, 2, 2);"
                                + "-fx-padding: 8;"
                                + "-fx-alignment: center;";
                        stackPane.setStyle(estiloBase + "-fx-border-color: " + colorBorde + ";");
                        break; // Solo uno debe coincidir
                    }
                }
            }
        });
        paneVisualizarPedido.setVisible(false);

        cargarPedidos(); // Cargar y mostrar todos los pedidos guardados en la base de datos
    }

    private void cargarPedidos() {
        List<Pedido> pedidos = pedidoDAO.findAll();
        // Ordenar por fecha de entrega ascendente (más próxima primero)
        pedidos.sort(Comparator.comparing(Pedido::getFechaEntrega));
        for (Pedido pedido : pedidos) {
            agregarNuevoPedido(pedido);
        }
        actualizarColoresStackPane(); // <- Aplica los colores después de cargar todos los pedidos
    }

    // --- MÉTODO ÚNICO Y SEGURO PARA AGREGAR PEDIDOS A LA VISTA ---
    // Siempre recibe el objeto Pedido completo, con productos cargados
    public void agregarNuevoPedido(Pedido pedido) {
        try {
            Label detalleProductos = new Label();
            Label labelEmpleado = new Label();
            Label labelFecha = new Label();

            // Generar el detalle SIEMPRE desde el objeto Pedido (productos ya cargados)
            String detalleGenerado = pedido.generarDetalle();

            StackPane nuevoPedidoStackPane = crearStackPanePedido(detalleProductos, labelEmpleado, labelFecha);
            configurarLabelsTarjeta(detalleProductos, labelEmpleado, labelFecha, detalleGenerado,
                    pedido.getEmpleadoAsignado().getNombre(), pedido.getFechaEntrega());

            manejarClicStackPane(nuevoPedidoStackPane, pedido,
                    pedido.getNumeroPedido(),
                    pedido.getCliente().getNombre(),
                    pedido.getCliente().getApellido(),
                    detalleGenerado,
                    pedido.getCliente().getTelefono(),
                    pedido.getEmpleadoAsignado().getNombre(),
                    pedido.getFormaEntrega(),
                    pedido.getFechaEntrega(),
                    pedido.getCliente().getDni());

            agregarStackPaneAlGridPane(nuevoPedidoStackPane);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void actualizarColoresStackPane() {
        List<Node> nodes = gridContenedorPedidos.getChildren();
        String estiloBase = "-fx-background-color: linear-gradient(to bottom right, #ffffff, #f9f9f9);"
                + "-fx-border-radius: 8px;"
                + "-fx-background-radius: 8px;"
                + "-fx-border-width: 1;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.1, 2, 2);"
                + "-fx-padding: 8;"
                + "-fx-alignment: center;";

        for (int i = 0; i < nodes.size(); i++) {
            if (i < pedidos.size()) {
                StackPane stackPane = (StackPane) nodes.get(i);
                Pedido pedido = pedidos.get(i);
                String estadoPedido = pedido.getEstadoPedido();
                String colorBorde;
                switch (estadoPedido != null ? estadoPedido : "") {
                    case "Hecho":
                        colorBorde = "green";
                        break;
                    case "En proceso":
                        colorBorde = "blue";
                        break;
                    case "Sin Empezar":
                    default:
                        colorBorde = "white";
                        break;
                }
                stackPane.setStyle(estiloBase + "-fx-border-color: " + colorBorde + ";");
            }
        }
    }

    private StackPane crearStackPanePedido(Label detalleProductos, Label labelEmpleado, Label labelFecha) {
        VBox vbox = new VBox(4, detalleProductos, labelEmpleado, labelFecha);
        vbox.setAlignment(javafx.geometry.Pos.CENTER);
        StackPane nuevoPedidoStackPane = new StackPane(vbox);
        nuevoPedidoStackPane.setPrefWidth(140);
        nuevoPedidoStackPane.setPrefHeight(150);
        nuevoPedidoStackPane.setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #ffffff, #f9f9f9);
            -fx-border-radius: 6px;
            -fx-background-radius: 6px;
            -fx-border-color: #d0d0d0;
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.1, 2, 2);
            -fx-padding: 8;
            -fx-alignment: center;
        """);
        return nuevoPedidoStackPane;
    }

    private void configurarLabelsTarjeta(Label detalleProductos, Label labelEmpleado, Label labelFecha, String detalleGenerado, String empleado, LocalDate fechaEntrega) {
        detalleProductos.setText(detalleGenerado);
        detalleProductos.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #444;
            -fx-font-family: 'Segoe UI', sans-serif;
            -fx-alignment: center;
        """);
        detalleProductos.setWrapText(true);

        labelEmpleado.setText("👤 " + empleado);
        labelEmpleado.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #666;
            -fx-font-family: 'Segoe UI', sans-serif;
            -fx-alignment: center;
        """);
        labelEmpleado.setWrapText(true);

        labelFecha.setText("📅 " + fechaEntrega.toString());
        labelFecha.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #666;
            -fx-font-family: 'Segoe UI', sans-serif;
            -fx-alignment: center;
        """);
        labelFecha.setWrapText(true);
    }

    @FXML
    void pedidoActualVisualizandose() {
        String DNIpedidoActual;
        DNIpedidoActual = lblDNICliente.getText();
    }

    public void agregarNuevoPedido(Long numeroPedido, String dniCliente, String nombre, String apellido, String contacto,
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
            Label detalleProductos = new Label();
            Label labelEmpleado = new Label();
            Label labelFecha = new Label();

            // Crear un nuevo StackPane para el pedido
            StackPane nuevoPedidoStackPane = crearStackPanePedido(detalleProductos, labelEmpleado, labelFecha);

            // ✅ Convertir totalPedido de String a BigDecimal
            BigDecimal totalPedidoBD = new BigDecimal(totalPedido);

            // Crear el objeto Pedido
            Pedido pedido = new Pedido(numeroPedido, cliente, trabajador, formaEntrega, fechaEntrega, estadoPedido, detalle, totalPedidoBD);
            // Ya no es necesario volver a cargar los productos aquí

            // Obtener el detalle generado
            String detalleGenerado = pedido.generarDetalle();

            // Mostrar el detalle en las etiquetas
            configurarLabelsTarjeta(detalleProductos, labelEmpleado, labelFecha, detalleGenerado, empleado, fechaEntrega);

            // Manejar el clic en el StackPane (ahora pasando también el apellido)
            manejarClicStackPane(nuevoPedidoStackPane, pedido, numeroPedido, nombre, apellido, detalleGenerado, contacto, empleado, formaEntrega, fechaEntrega, dniCliente);

            // Agregar el StackPane al GridPane
            agregarStackPaneAlGridPane(nuevoPedidoStackPane);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private StackPane crearStackPanePedido(Label detalleCompletoPedido) {
        VBox vbox = new VBox(4, detalleCompletoPedido);
        vbox.setAlignment(javafx.geometry.Pos.CENTER);
        StackPane nuevoPedidoStackPane = new StackPane(vbox);
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
            -fx-alignment: center;
        """);
        return nuevoPedidoStackPane;
    }

    private void manejarClicStackPane(StackPane nuevoPedidoStackPane, Pedido pedido, Long numeroPedido, String nombre, String apellido, String detalleGenerado, String contacto, String empleado, String formaEntrega, LocalDate fechaEntrega, String dniCliente) {
        nuevoPedidoStackPane.setOnMouseClicked(event -> {
            // Asignar el pedido correspondiente
            pedidoSeleccionado = pedido;
            // Mostrar los detalles en la interfaz
            paneVisualizarPedido.setVisible(true);
            lblNOrden.setText(numeroPedido.toString());
            lblNomCliente.setText(nombre + " " + apellido); // Mostrar nombre y apellido
            lblDetallePedidoCliente.setText(detalleGenerado); // Usar el detalle generado
            lblTelefonoCliente.setText(contacto);
            lblEmpleadoAsignadoAlPedido.setText(empleado);
            lblFormaEntrega.setText(formaEntrega);
            lblFechaEntrega.setText(fechaEntrega.toString());
            lblDNICliente.setText(dniCliente);
            lblTotalPedido.setText(pedido.getTotalPedido().toString());
            cmbEstadoDelPedido.setValue(pedido.getEstadoPedido()); // <-- Mostrar el estado actual
            pedidoActualVisualizandose();
            actualizarColoresStackPane(); // Cambia el borde del StackPane según el estado del pedido
            // Ya NO se abre la ventana de modificación aquí
        });
    }

    private void agregarStackPaneAlGridPane(StackPane stackPane) {
        int totalPedidos = gridContenedorPedidos.getChildren().size();
        int columna = totalPedidos % 4; // 4 columnas por fila
        int fila = totalPedidos / 4;

        gridContenedorPedidos.add(stackPane, columna, fila);
        GridPane.setMargin(stackPane, new Insets(3, 5, 3, 5)); // top, right, bottom, left
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

    @FXML
    void handleModificar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/ModificarPedidos.fxml"));
            Parent root = loader.load();
            ModificarPedidosController controller = loader.getController();
            // PASAR EL PEDIDO SELECCIONADO PARA QUE SE MUESTREN LOS DATOS
            controller.obtenerDatos(pedidoSeleccionado);
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

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/loginAdmin.css", false);
    }

    @FXML
    void btnCrearNuevoPedido(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/DialogNuevoPedido.fxml"));
            Parent root = loader.load();

            // Inicializar y configurar el controlador del diálogo
            NuevoPedidoController nuevoPedidoController = loader.getController();
            //nuevoPedidoController.setPedidosController(this); // Pasar referencia al controlador principal

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
        // Construir el mapa Producto -> cantidad
        Map<Producto, Integer> productosMap = new java.util.HashMap<>();
        for (PedidoProducto pp : pedido.getPedidoProductos()) {
            productosMap.put(pp.getProducto(), pp.getCantidad());
        }

        // Validar stock suficiente
        if (!recetaProcessor.validarInsumosSuficientes(productosMap)) {
            ActionLogger.log("No hay insumos suficientes para el pedido.");
            return false;
        }

        // Procesar reducción y registrar faltantes (si los hubiera)
        recetaProcessor.procesarRecetas(productosMap);
        return true;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
