package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utilities.Paths;
import utilities.SceneLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.time.LocalDate;

public class PedidosController {

    @FXML
    private Button btnVolver;

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.ADMIN_MAINMENU, "/css/loginAdmin.css", true);
    }

    @FXML
    void CRUDEmpleado(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/CrudAnadirEmpleado.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Modificar empleados");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnCreateNuevoPedido(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/DialogNuevoPedido.fxml"));
            Parent root = loader.load();
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
    @FXML
    private VBox pedidosVBox; // Donde se mostrarán los pedidos

    public void agregarNuevoPedido(String nombreCliente, String contactoCliente, String detallePedido, String empleadoAsignado, LocalDate fechaEntrega) {
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


    /*@FXML
    private Pane sectionPorHacer;

    public Pane sectionPorHacer() {
        return sectionPorHacer;
    }*/
}
