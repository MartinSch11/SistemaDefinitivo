package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utilities.Paths;
import utilities.SceneLoader;


import java.time.LocalDate;

public class PedidosController {


    /*--------------------------------crear nuevo pedido------------------------------------------------*/
    @FXML
    private VBox pedidosVBox; // Donde se mostrarán los pedidos

    @FXML
    private GridPane gridPedidos;

    @FXML
    private Pane sectionEnProceso;

    @FXML
    private Pane sectionHecho;

    @FXML
    private Pane sectionPorHacer;


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
}
    //campos obligatorios
