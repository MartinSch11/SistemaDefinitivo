package controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Insumo;
import model.Pedido;
import model.Producto;
import model.Receta;
import persistence.dao.InsumoDAO;
import persistence.dao.PedidoDAO;
import persistence.dao.RecetaDAO;
import persistence.dao.TrabajadorDAO;
import utilities.Paths;
import utilities.SceneLoader;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class PedidosController {

    private PedidoDAO pedidoDAO;
    private RecetaDAO recetaDAO;
    private InsumoDAO insumoDAO;

    @FXML
    private VBox pedidosVBox; // Donde se mostrarán los pedidos

    public PedidosController() {
        pedidoDAO = new PedidoDAO();
        recetaDAO = new RecetaDAO();
        insumoDAO = new InsumoDAO();

    }


    /*--------------------------------crear nuevo pedido------------------------------------------------*/

    @FXML private GridPane gridPedidos;
    @FXML private Pane sectionEnProceso;
    @FXML private Pane sectionHecho;
    @FXML private Pane sectionPorHacer;

    public void agregarNuevoPedido(String nombre, String contacto, String detalle, String empleado, String formaEntrega, LocalDate fechaEntrega) {
        // Crear etiquetas para mostrar la información del pedido
        Label detalleCompletoPedido = new Label();
        detalleCompletoPedido.setText(nombre + "\n" + contacto + "\n" + detalle + "\n" + empleado + "\n" + formaEntrega + "\n" + fechaEntrega.toString());
        detalleCompletoPedido.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-alignment: left; -fx-padding-top: 2px");
        detalleCompletoPedido.setWrapText(true); // permitir que el texto se ajuste al ancho del TextArea

        // Crear un nuevo StackPane para el pedido
        StackPane nuevoPedidoStackPane = new StackPane(detalleCompletoPedido);
        nuevoPedidoStackPane.setPrefHeight(265);
        nuevoPedidoStackPane.setMaxHeight(265);
        nuevoPedidoStackPane.setMinHeight(265);
        nuevoPedidoStackPane.setStyle("-fx-background-color: #FFF4F4; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 10, 0.0, 2, 2); -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-margin-top: 2px;");

        // Determinar la siguiente fila en el GridPane
        int newRowIndex = gridPedidos.getRowCount(); // Obtener el número de filas actuales
        gridPedidos.add(nuevoPedidoStackPane, 0, newRowIndex); // Insertar el StackPane en la nueva fila y columna 0 (sectionPorHacer)

        // Añadir una restricción de tamaño fijo a la nueva fila
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setPrefHeight(260);
        rowConstraints.setMaxHeight(260);
        rowConstraints.setMinHeight(260);
        gridPedidos.getRowConstraints().add(rowConstraints);
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

    /*--------------------------------crear nuevo pedido------------------------------------------------*/

    public void agregarNuevoPedido(String nombreCliente, String contactoCliente, String detallePedido, String empleadoAsignado, LocalDate fechaEntrega, List<Producto> productos) {
        // Crear un nuevo Pane para el pedido
        Pane nuevoPedidoPane = new Pane();
        nuevoPedidoPane.setStyle("-fx-padding: 10; -fx-border-color: black; -fx-background-color: #f2f2f2;");

        // Crear etiquetas para mostrar la información del pedido
        Label nombreClienteLabel = new Label("Cliente: " + nombreCliente);
        Label contactoClienteLabel = new Label("Contacto: " + contactoCliente);
        Label detallePedidoLabel = new Label("Detalle del Pedido: " + detallePedido);
        Label empleadoAsignadoLabel = new Label("Empleado Asignado: " + empleadoAsignado);
        Label fechaEntregaLabel = new Label("Fecha de Entrega: " + fechaEntrega.toString());

        // Añadir las etiquetas a un VBox para que estén organizadas
        VBox pedidoInfo = new VBox(5);
        pedidoInfo.getChildren().addAll(nombreClienteLabel, contactoClienteLabel, detallePedidoLabel, empleadoAsignadoLabel, fechaEntregaLabel);

        // Añadir el VBox al nuevo Pane
        nuevoPedidoPane.getChildren().add(pedidoInfo);

        // Añadir el nuevo Pane al VBox que contiene todos los pedidos
        pedidosVBox.getChildren().add(nuevoPedidoPane);

        // Guardar el pedido en la base de datos
        Pedido nuevoPedido = new Pedido(nombreCliente, contactoCliente, detallePedido, empleadoAsignado, fechaEntrega, productos);
        pedidoDAO.save(nuevoPedido);

        // Procesar la reducción de insumos en base a las recetas de los productos
        procesarReduccionInsumos(productos);
    }

    private void procesarReduccionInsumos(List<Producto> productos) {
        for (Producto producto : productos) {
            // Obtener la receta asociada al producto
            Receta receta = recetaDAO.findByProducto(producto);

            // Obtener los insumos de la receta y reducir sus cantidades
            if (receta != null) {
                List<Insumo> insumosDeLaReceta = receta.getInsumos();
                for (Insumo insumo : insumosDeLaReceta) {
                    // Restar la cantidad utilizada del insumo
                    double cantidadUtilizada = receta.getCantidadInsumo(insumo);
                    insumo.reducirCantidad(cantidadUtilizada);
                    insumoDAO.update(insumo); // Actualizar el insumo en la base de datos
                }
            }
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

