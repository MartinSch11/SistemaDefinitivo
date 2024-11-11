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
import model.Insumo;
import model.Pedido;
import model.Producto;
import model.Receta;
import persistence.dao.InsumoDAO;
import persistence.dao.PedidoDAO;
import persistence.dao.RecetaDAO;
import utilities.Paths;
import utilities.SceneLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.Alert;

public class PedidosController {
    @FXML private Label lblDNICliente;@FXML private Label lblDetallePedidoCliente; @FXML private Label lblEmpleadoAsignadoAlPedido; @FXML private Label lblFechaEntrega; @FXML  private Label lblFormaEntrega; @FXML private Label lblNomCliente; @FXML private Label lblTelefonoCliente;
    @FXML private ComboBox<String> cmbEstadoDelPedido; @FXML private GridPane gridContenedorPedidos; @FXML private Pane paneVisualizarPedido;
    @FXML private Button btnBorrarPedido;


    //private PedidoDAO pedidoDAO;
    private RecetaDAO recetaDAO;
    private InsumoDAO insumoDAO;

    @FXML
    private VBox pedidosVBox; // Donde se mostrarán los pedidos
    private List<Producto> productos;
    private ObservableList<Pedido> pedidos = FXCollections.observableArrayList();
    private Pedido pedidoSeleccionado;
    private String pedidoSeleccionadoDni;

    @Setter
    private DialogNuevoPedidoController dialognuevopedidoController;

    public void setDialogNuevoPedidoController(DialogNuevoPedidoController dialognuevopedidoController) {
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
            agregarNuevoPedido(pedido.getNombreCliente(), pedido.getContactoCliente(), pedido.getDniCliente(), pedido.getDetallePedido(), pedido.getEmpleadoAsignado(), pedido.getFormaEntrega(), pedido.getFechaEntrega(), pedido.getEstadoPedido());
        }
    }


    private void actualizarColoresStackPane() {
        List<Node> nodes = gridContenedorPedidos.getChildren();

        for (int i = 0; i < nodes.size(); i++) {
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

    @FXML void pedidoActualVisualizandose(){
        String DNIpedidoActual;
        DNIpedidoActual = lblDNICliente.getText();
    }

    public void agregarNuevoPedido(String nombre, String contacto, String dniCliente, String detalle, String empleado, String formaEntrega, LocalDate fechaEntrega, String estadoPedido) {
        try {
            // Crear etiquetas para mostrar la información del pedido
            Label detalleCompletoPedido = new Label();

            // Crear un nuevo StackPane para el pedido
            StackPane nuevoPedidoStackPane = new StackPane(detalleCompletoPedido);
            nuevoPedidoStackPane.setPrefWidth(480);
            nuevoPedidoStackPane.setPrefHeight(190);
            nuevoPedidoStackPane.setStyle("-fx-background-color: #FFF4F4; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 10, 0.0, 2, 2); -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-margin-top: 2px;");

            detalleCompletoPedido.setText(detalle + "\n" + empleado + "\n" + fechaEntrega.toString());
            detalleCompletoPedido.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding-top: 2px");
            detalleCompletoPedido.setWrapText(true); // permitir que el texto se ajuste al ancho del TextArea

            // Añadir un EventHandler para hacer click en el StackPane
            nuevoPedidoStackPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    paneVisualizarPedido.setVisible(true);
                    lblNomCliente.setText(nombre);
                    lblDetallePedidoCliente.setText(detalle);
                    lblTelefonoCliente.setText(contacto);
                    lblEmpleadoAsignadoAlPedido.setText(empleado);
                    lblFormaEntrega.setText(formaEntrega);
                    lblFechaEntrega.setText(fechaEntrega.toString());
                    lblDNICliente.setText(dniCliente);

                    pedidoActualVisualizandose();
                    actualizarColoresStackPane(); // cambian el borde del StackPane según el estado del pedido

                     //Pasar los datos al controlador de modificación
                    ModificarPedidosController modificarController = new ModificarPedidosController();
                    modificarController.obtenerDatos(pedidoSeleccionado);
                }
            });

            // Determinar la siguiente fila en el GridPane
            int newRowIndex = gridContenedorPedidos.getRowCount(); // Obtener el número de filas actuales

            // Añadir el StackPane al GridPane
            gridContenedorPedidos.add(nuevoPedidoStackPane, 0, newRowIndex); // Insertar el StackPane en la nueva fila y columna 0

            // Añadir una restricción de tamaño fijo a la nueva fila con margen
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPrefHeight(190 + 7); // Altura del StackPane más el margen
            rowConstraints.setMaxHeight(190 + 7);
            rowConstraints.setMinHeight(190 + 7);
            gridContenedorPedidos.getRowConstraints().add(rowConstraints);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void actualizarGridPane() {
        gridContenedorPedidos.getChildren().clear();
        cargarPedidos();
    }

    private void seleccionarPedido(Pedido pedido) {
        paneVisualizarPedido.setVisible(true);

        lblNomCliente.setText(pedido.getNombreCliente());
        lblDetallePedidoCliente.setText(pedido.getDetallePedido());
        lblTelefonoCliente.setText(pedido.getContactoCliente());
        lblEmpleadoAsignadoAlPedido.setText(pedido.getEmpleadoAsignado());
        lblFormaEntrega.setText(pedido.getFormaEntrega());
        lblFechaEntrega.setText(pedido.getFechaEntrega().toString());
        lblDNICliente.setText(pedido.getDniCliente());

        pedidoSeleccionadoDni = pedido.getDniCliente();

        // Asignar el DNI del pedido seleccionado
        actualizarColoresStackPane();
    }

    public void agregarPedido(Pedido pedido) {
        pedidoDAO.save(pedido);
        // Guardar el pedido en la base de datos
        pedidos.add(pedido); // Agregar el pedido a la ObservableList
        //mostrarPedidos(); // Actualizar la vista
    }

    @Setter
    private ModificarPedidosController modificarPedidosController;
    public void setModificarPedidosController(ModificarPedidosController modificarPedidosController) {
        this.modificarPedidosController = modificarPedidosController;
    }

    @FXML
    private Button btnModificar;
    @FXML
    void handleModificar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/ModificarPedidos.fxml"));
            Parent root = loader.load(); ModificarPedidosController controller = loader.getController();
            controller.setModificarPedidosController(this);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modificacion de Pedidos");
            stage.setScene(new Scene(root));
            stage.showAndWait();
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
            Pedido pedidoAEliminar = pedidoDAO.findByDni(pedidoSeleccionadoDni);
            if (pedidoAEliminar != null) {
                pedidoDAO.delete(pedidoAEliminar);
                pedidos.remove(pedidoAEliminar);

                actualizarGridPane();

                paneVisualizarPedido.setVisible(false);
            }else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Pedido no encontrado");
                errorAlert.setContentText("El pedido que intentas eliminar no existe.");
                errorAlert.showAndWait();
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
                for (Insumo insumo : insumosDeLaReceta) {
                    // Obtener la cantidad utilizada del insumo en la receta
                    int cantidadUtilizada = receta.getCantidadInsumo(insumo);
                    insumo.reducirCantidad(cantidadUtilizada);
                    insumoDAO.update(insumo); // Actualizar el insumo en la base de datos
                }
            }
        }
    }


    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.ADMIN_MAINMENU, "/css/loginAdmin.css", true);
    }

    @FXML
    void btnCrearNuevoPedido(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/DialogNuevoPedido.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del diálogo
            DialogNuevoPedidoController controller = loader.getController();

            // Establecer la referencia al controlador de pedidos
            controller.setPedidosController(this);

            Stage stage = new Stage();
            stage.setTitle("Información del pedido");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void abrirDialogoNuevoPedido() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DialogNuevoPedido.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del cuadro de diálogo
            DialogNuevoPedidoController dialogController = loader.getController();
            dialogController.setPedidosController(this); // Pasar la referencia del controlador actual

            // Mostrar el cuadro de diálogo
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
                /*if(nuevoPedidoStackPane != null){
                        // Añadir el nuevo pedido a la lista observable
                        pedidos.add(nuevoPedido);

                        // Procesar la reducción de insumos en base a las recetas de los productos
                        procesarReduccionInsumos(productos);

                        boolean pedidoHecho = false; //si el pedido se preparó se descuentan los insumos utilizados
                        if (pedidoHecho == true){
                            // Procesar la reducción de insumos en base a las recetas de los productos
                            procesarReduccionInsumos(productos);
                        }
                }*/