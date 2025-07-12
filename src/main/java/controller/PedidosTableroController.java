package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import model.Pedido;
import model.PedidoProducto;
import model.Producto;
import model.Cliente;
import model.Trabajador;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import persistence.dao.PedidoDAO;
import model.Pedido;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.Region;
import utilities.ActionLogger;
import utilities.Paths;
import utilities.RecetaProcessor;
import utilities.SceneLoader;

public class PedidosTableroController {
    @FXML
    private TilePane vboxPorHacer;
    @FXML
    private TilePane vboxEnProceso;
    @FXML
    private TilePane vboxHecho;
    @FXML
    private VBox colPorHacer;
    @FXML
    private VBox colEnProceso;
    @FXML
    private VBox colHecho;

    private PedidoDAO pedidoDAO = new PedidoDAO();
    private final RecetaProcessor recetaProcessor = new RecetaProcessor();

    @FXML
    public void initialize() {
        // Configurar el TilePane para que tenga 2 columnas fijas y ajuste filas
        // automáticamente
        vboxPorHacer.setPrefColumns(2);
        vboxPorHacer.setPrefRows(2);
        vboxPorHacer.setTileAlignment(javafx.geometry.Pos.TOP_CENTER);
        vboxEnProceso.setPrefColumns(2);
        vboxEnProceso.setPrefRows(2);
        vboxEnProceso.setTileAlignment(javafx.geometry.Pos.TOP_CENTER);
        vboxHecho.setPrefColumns(2);
        vboxHecho.setPrefRows(2);
        vboxHecho.setTileAlignment(javafx.geometry.Pos.TOP_CENTER);
        // Cargar pedidos desde la base de datos
        List<Pedido> pedidos = pedidoDAO.findAll();
        for (Pedido pedido : pedidos) {
            StackPane tarjeta = crearTarjetaPedidoKanban(pedido);
            tarjeta.setUserData(pedido.getNumeroPedido());
            agregarTarjetaAColumna(tarjeta, pedido.getEstadoPedido());
            agregarDragAndDrop(tarjeta, pedido);
        }
        setupDropTargetColumna(colPorHacer, vboxPorHacer, "Sin empezar");
        setupDropTargetColumna(colEnProceso, vboxEnProceso, "En proceso");
        setupDropTargetColumna(colHecho, vboxHecho, "Hecho");
    }

    private StackPane crearTarjetaPedidoKanban(Pedido pedido) {
        Label detalleProductos = new Label(pedido.generarDetalle());
        detalleProductos.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: #444; -fx-font-family: 'Segoe UI', sans-serif; -fx-alignment: center;");
        detalleProductos.setWrapText(true);

        String empleado = pedido.getEmpleadoAsignado() != null ? pedido.getEmpleadoAsignado().getNombre()
                : "Sin asignar";
        Label labelEmpleado = new Label("👤 " + empleado);
        labelEmpleado.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: #666; -fx-font-family: 'Segoe UI', sans-serif; -fx-alignment: center;");
        labelEmpleado.setWrapText(true);

        LocalDate fechaEntrega = pedido.getFechaEntrega();
        Label labelFecha = new Label("📅 " + (fechaEntrega != null ? fechaEntrega.toString() : "Sin fecha"));
        labelFecha.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: #666; -fx-font-family: 'Segoe UI', sans-serif; -fx-alignment: center;");
        labelFecha.setWrapText(true);

        VBox vbox = new VBox(4, detalleProductos, labelEmpleado, labelFecha);
        vbox.setAlignment(javafx.geometry.Pos.CENTER);
        StackPane tarjeta = new StackPane(vbox);
        tarjeta.setPrefWidth(180); // Más ancha para que se vea como en PedidoController
        tarjeta.setPrefHeight(150); // Más alta para igualar la estética
        tarjeta.setStyle("""
                    -fx-background-color: linear-gradient(to bottom right, #ffffff, #f9f9f9);
                    -fx-border-radius: 8px;
                    -fx-background-radius: 8px;
                    -fx-border-color: #d0d0d0;
                    -fx-border-width: 1;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.1, 2, 2);
                    -fx-padding: 8;
                    -fx-alignment: center;
                """);
        return tarjeta;
    }

    private void agregarTarjetaAColumna(StackPane tarjeta, String estado) {
        if ("Hecho".equalsIgnoreCase(estado)) {
            vboxHecho.getChildren().add(tarjeta);
        } else if ("En proceso".equalsIgnoreCase(estado)) {
            vboxEnProceso.getChildren().add(tarjeta);
        } else {
            vboxPorHacer.getChildren().add(tarjeta);
        }
    }

    private void agregarDragAndDrop(StackPane tarjeta, Pedido pedido) {
        tarjeta.setOnDragDetected(event -> {
            Dragboard db = tarjeta.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(pedido.getNumeroPedido()));
            db.setContent(content);
            event.consume();
        });
        // Menú contextual dinámico
        tarjeta.setOnContextMenuRequested(e -> {
            javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
            javafx.scene.control.MenuItem verDetalles = new javafx.scene.control.MenuItem("Ver detalles del pedido");
            verDetalles.setOnAction(ev -> mostrarDetallesPedido(pedido));
            javafx.scene.control.MenuItem modificarPedido = new javafx.scene.control.MenuItem("Modificar pedido");
            modificarPedido.setOnAction(ev -> modificarPedido(pedido));
            javafx.scene.control.MenuItem eliminarPedido = new javafx.scene.control.MenuItem("Eliminar pedido");
            eliminarPedido.setOnAction(ev -> eliminarPedidoConConfirmacion(pedido));
            contextMenu.getItems().addAll(verDetalles, modificarPedido, eliminarPedido);
            // Agregar opción "Entregar pedido" si el estado es "Hecho"
            if ("Hecho".equalsIgnoreCase(pedido.getEstadoPedido() != null ? pedido.getEstadoPedido().trim() : "")) {
                javafx.scene.control.MenuItem entregarPedido = new javafx.scene.control.MenuItem("Entregar pedido");
                entregarPedido.setOnAction(ev -> entregarPedido(pedido));
                contextMenu.getItems().add(entregarPedido);
            }
            contextMenu.show(tarjeta, e.getScreenX(), e.getScreenY());
            e.consume();
        });
    }

    private void mostrarDetallesPedido(Pedido pedido) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/DialogDetallesPedido.fxml"));
            Parent root = loader.load();
            DetallesPedidoDialogController controller = loader.getController();
            controller.setPedido(pedido);
            Stage stage = new Stage();
            stage.setTitle("Detalles del Pedido");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo mostrar el detalle del pedido: " + e.getMessage());
        }
    }

    private void modificarPedido(Pedido pedido) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/DialogNuevoPedido.fxml"));
            Parent root = loader.load();
            NuevoPedidoController controller = loader.getController();
            controller.setPedidosTableroController(this);
            // Debes implementar este método en NuevoPedidoController
            controller.cargarPedidoParaEdicion(pedido);
            // Debes implementar este método en NuevoPedidoController
            if (pedido.getEstadoPedido().equalsIgnoreCase("Sin empezar")) {
                controller.habilitarCatalogo(true);
            } else {
                controller.habilitarCatalogo(false);
            }
            Stage stage = new Stage();
            stage.setTitle("Modificar Pedido");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            Pedido modificado = controller.getPedidoCreado();
            if (modificado != null) {
                agregarNuevoPedido(modificado);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la edición del pedido: " + e.getMessage());
        }
    }

    private void eliminarPedidoConConfirmacion(Pedido pedido) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Desea eliminar este pedido?");
        alert.setContentText("Esta acción no se puede deshacer.");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                pedidoDAO.delete(pedido);
                // Eliminar visualmente la tarjeta
                StackPane tarjeta = buscarTarjetaPorId(pedido.getNumeroPedido());
                if (tarjeta != null) {
                    ((TilePane) tarjeta.getParent()).getChildren().remove(tarjeta);
                }
                ActionLogger.log("Pedido eliminado: " + pedido.getNumeroPedido());
            }
        });
    }

    private void setupDropTargetColumna(VBox columnaVBox, TilePane tilePane, String nuevoEstado) {
        columnaVBox.setOnDragOver(event -> {
            if (event.getGestureSource() != columnaVBox && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                columnaVBox.setStyle(columnaVBox.getStyle() + ";-fx-effect: dropshadow(gaussian, #888, 10, 0.2, 2, 2);");
            }
            event.consume();
        });
        columnaVBox.setOnDragExited(event -> {
            columnaVBox.setStyle(columnaVBox.getStyle().replaceAll(";?-fx-effect: dropshadow\\([^)]*\\);?", ""));
            event.consume();
        });
        columnaVBox.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                Long pedidoId = Long.parseLong(db.getString());
                StackPane tarjeta = buscarTarjetaPorId(pedidoId);
                if (tarjeta != null) {
                    ((TilePane) tarjeta.getParent()).getChildren().remove(tarjeta);
                    tilePane.getChildren().add(tarjeta);
                    // Actualizar estado en la base de datos y en el objeto
                    Pedido pedido = pedidoDAO.findByNumeroPedido(pedidoId);
                    if (pedido != null) {
                        pedido.setEstadoPedido(nuevoEstado);
                        pedidoDAO.update(pedido);
                        // Refrescar el objeto en memoria
                        tarjeta.setUserData(pedido.getNumeroPedido());
                        // Regenerar el menú contextual para reflejar el nuevo estado
                        agregarDragAndDrop(tarjeta, pedido);
                    }
                }
                success = true;
            }
            columnaVBox.setStyle(columnaVBox.getStyle().replaceAll(";?-fx-effect: dropshadow\\([^)]*\\);?", ""));
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private StackPane buscarTarjetaPorId(Long pedidoId) {
        for (TilePane columna : new TilePane[] { vboxPorHacer, vboxEnProceso, vboxHecho }) {
            for (javafx.scene.Node n : columna.getChildren()) {
                if (n instanceof StackPane && n.getUserData() != null && n.getUserData().equals(pedidoId)) {
                    return (StackPane) n;
                }
            }
        }
        return null;
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/loginAdmin.css", false);
    }

    @FXML
    void btnCrearNuevoPedido(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/pasteleria/DialogNuevoPedido.fxml"));
            Parent root = loader.load();

            NuevoPedidoController nuevoPedidoController = loader.getController();
            nuevoPedidoController.setPedidosTableroController(this);

            Stage stage = new Stage();
            stage.setTitle("Nuevo Pedido");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            Pedido nuevoPedido = nuevoPedidoController.getPedidoCreado();

            if (nuevoPedido == null) {
                mostrarAlerta("Error", "El pedido no se creó correctamente.");
                return;
            }

            if (validarYReducirInsumos(nuevoPedido)) {
                agregarPedido(nuevoPedido); // SOLO este método agrega la tarjeta visualmente
                ActionLogger.log("Pedido creado: " + nuevoPedido.getCliente().getNombre() +
                        " con productos: " + nuevoPedido.getProductos());
            } else {
                mostrarAlerta("Error", "No se pudo completar el pedido por falta de insumos.");
            }

            // NO LLAMES a agregarNuevoPedido aquí ni desde el diálogo

        } catch (Exception e) {
            Throwable cause = e.getCause();
            String mensaje = (cause != null) ? cause.getMessage() : e.getMessage();
            mostrarAlerta("Error inesperado", "Ocurrió un error al crear el pedido:\n" + mensaje);
            e.printStackTrace();
        }
    }

    public void agregarPedido(Pedido pedido) {
        pedidoDAO.save(pedido);
        StackPane tarjeta = crearTarjetaPedidoKanban(pedido);
        tarjeta.setUserData(pedido.getNumeroPedido());
        agregarTarjetaAColumna(tarjeta, pedido.getEstadoPedido());
        agregarDragAndDrop(tarjeta, pedido);
        ActionLogger.log("Pedido creado: " + pedido.getCliente().getNombre() +
                " con productos: " + pedido.getProductos());
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

    public void agregarNuevoPedido(Pedido pedido) {
        try {
            // Elimina cualquier tarjeta existente con el mismo numeroPedido
            StackPane tarjetaExistente = buscarTarjetaPorId(pedido.getNumeroPedido());
            if (tarjetaExistente != null) {
                ((TilePane) tarjetaExistente.getParent()).getChildren().remove(tarjetaExistente);
            }
            // Si el pedido ya existe en la base, actualizar; si no, guardar
            if (pedido.getNumeroPedido() != null && pedidoDAO.findByNumeroPedido(pedido.getNumeroPedido()) != null) {
                pedidoDAO.update(pedido);
            } else {
                pedidoDAO.save(pedido);
            }
            StackPane tarjeta = crearTarjetaPedidoKanban(pedido);
            tarjeta.setUserData(pedido.getNumeroPedido());
            agregarTarjetaAColumna(tarjeta, pedido.getEstadoPedido());
            agregarDragAndDrop(tarjeta, pedido);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void entregarPedido(Pedido pedido) {
        pedido.setEstadoPedido("Entregado");
        pedido.setFechaEntregado(LocalDate.now());
        pedidoDAO.update(pedido);
        // Eliminar visualmente la tarjeta de todas las columnas
        StackPane tarjeta = buscarTarjetaPorId(pedido.getNumeroPedido());
        if (tarjeta != null) {
            ((TilePane) tarjeta.getParent()).getChildren().remove(tarjeta);
        }
        ActionLogger.log("Pedido entregado: " + pedido.getNumeroPedido());
        mostrarAlerta("Pedido entregado", "El pedido ha sido marcado como entregado y retirado del tablero.");
    }

    @FXML
    void abrirHistorialPedidos(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/HistorialPedidosDialog.fxml"));
            Parent root = loader.load();
            // Si tienes un controlador específico para el historial:
            // HistorialPedidosDialogController controller = loader.getController();
            // controller.cargarPedidosEntregados();
            Stage stage = new Stage();
            stage.setTitle("Historial de pedidos entregados");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir el historial de pedidos: " + e.getMessage());
        }
    }
}
