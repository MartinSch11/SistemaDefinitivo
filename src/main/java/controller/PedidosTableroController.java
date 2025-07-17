package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import model.Pedido;
import model.PedidoProducto;
import model.Producto;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import persistence.dao.PedidoDAO;
import javafx.scene.layout.TilePane;
import utilities.ActionLogger;
import utilities.Paths;
import utilities.RecetaProcessor;
import utilities.SceneLoader;
import model.SessionContext;

public class PedidosTableroController {
    @FXML private TilePane vboxPorHacer;
    @FXML private TilePane vboxEnProceso;
    @FXML private TilePane vboxHecho;
    @FXML private VBox colPorHacer;
    @FXML private VBox colEnProceso;
    @FXML private VBox colHecho;
    @FXML private javafx.scene.control.Button btnNuevoPedido;

    private PedidoDAO pedidoDAO = new PedidoDAO();
    private final RecetaProcessor recetaProcessor = new RecetaProcessor();

    // Permisos del usuario actual
    private final List<String> permisos = SessionContext.getInstance().getPermisos();
    private final boolean puedeModificar = permisos != null && permisos.contains("Pedidos-modificar");
    private final boolean puedeEliminar = permisos != null && permisos.contains("Pedidos-eliminar");
    private final boolean puedeCrear = permisos != null && permisos.contains("Pedidos-crear");

    @FXML
    public void initialize() {
        vboxPorHacer.setPrefColumns(2);
        vboxPorHacer.setPrefRows(2);
        vboxPorHacer.setTileAlignment(javafx.geometry.Pos.TOP_CENTER);
        vboxEnProceso.setPrefColumns(2);
        vboxEnProceso.setPrefRows(2);
        vboxEnProceso.setTileAlignment(javafx.geometry.Pos.TOP_CENTER);
        vboxHecho.setPrefColumns(2);
        vboxHecho.setPrefRows(2);
        vboxHecho.setTileAlignment(javafx.geometry.Pos.TOP_CENTER);
        // Cargar pedidos desde la base de datos, excluyendo los entregados y ordenando por fecha de entrega
        List<Pedido> pedidos = pedidoDAO.findAll();
        pedidos = pedidos.stream()
                .filter(p -> p.getEstadoPedido() == null || !p.getEstadoPedido().equalsIgnoreCase("Entregado"))
                .sorted((p1, p2) -> {
                    LocalDate f1 = p1.getFechaEntrega();
                    LocalDate f2 = p2.getFechaEntrega();
                    if (f1 == null && f2 == null) return 0;
                    if (f1 == null) return 1;
                    if (f2 == null) return -1;
                    return f1.compareTo(f2);
                })
                .toList();
        for (Pedido pedido : pedidos) {
            StackPane tarjeta = crearTarjetaPedidoKanban(pedido);
            tarjeta.setUserData(pedido.getNumeroPedido());
            agregarTarjetaAColumna(tarjeta, pedido.getEstadoPedido());
            agregarDragAndDrop(tarjeta, pedido);
        }
        setupDropTargetColumna(colPorHacer, vboxPorHacer, "Sin empezar");
        setupDropTargetColumna(colEnProceso, vboxEnProceso, "En proceso");
        setupDropTargetColumna(colHecho, vboxHecho, "Hecho");
        // Deshabilitar el botón si no tiene permiso
        btnNuevoPedido.setDisable(!puedeCrear);
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
            contextMenu.getItems().add(verDetalles);
            // Modificar y eliminar: mostrar siempre si el estado es Sin empezar, pero deshabilitar si no hay permiso
            if ("Sin empezar".equalsIgnoreCase(pedido.getEstadoPedido())) {
                javafx.scene.control.MenuItem modificarPedido = new javafx.scene.control.MenuItem("Modificar pedido");
                modificarPedido.setOnAction(ev -> modificarPedido(pedido));
                modificarPedido.setDisable(!puedeModificar);
                contextMenu.getItems().add(modificarPedido);
                javafx.scene.control.MenuItem eliminarPedido = new javafx.scene.control.MenuItem("Eliminar pedido");
                eliminarPedido.setOnAction(ev -> eliminarPedidoConConfirmacion(pedido));
                eliminarPedido.setDisable(!puedeEliminar);
                contextMenu.getItems().add(eliminarPedido);
            }
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
                // --- Devolver insumos al stock antes de eliminar el pedido ---
                Map<Producto, Integer> productosParaDevolver = new HashMap<>();
                for (PedidoProducto pp : pedido.getPedidoProductos()) {
                    productosParaDevolver.put(pp.getProducto(), pp.getCantidad());
                }
                StringBuilder resumenDevueltos = new StringBuilder();
                if (!productosParaDevolver.isEmpty()) {
                    Map<String, String> insumosDevueltos = recetaProcessor.devolverStockPorProductosConResumen(productosParaDevolver);
                    if (!insumosDevueltos.isEmpty()) {
                        resumenDevueltos.append("Insumos devueltos al stock:\n");
                        for (Map.Entry<String, String> entry : insumosDevueltos.entrySet()) {
                            resumenDevueltos.append("- ").append(entry.getKey()).append(": ")
                                    .append(entry.getValue()).append("\n");
                        }
                    }
                    // Intentar resolver faltantes automáticamente y mostrar resumen si corresponde
                    Map<String, String> faltantesResueltos = recetaProcessor.resolverFaltantesPorCatalogoInsumo();
                    if (faltantesResueltos != null && !faltantesResueltos.isEmpty()) {
                        resumenDevueltos.append("\nSe utilizaron estos insumos devueltos para resolver faltantes:\n");
                        faltantesResueltos.forEach((nombre, cantidad) -> resumenDevueltos.append("- ").append(nombre).append(": ").append(cantidad).append("\n"));
                    }
                }
                // --- Eliminar pedido ---
                pedidoDAO.delete(pedido);
                // Eliminar visualmente la tarjeta
                StackPane tarjeta = buscarTarjetaPorId(pedido.getNumeroPedido());
                if (tarjeta != null) {
                    ((TilePane) tarjeta.getParent()).getChildren().remove(tarjeta);
                }
                ActionLogger.log("El usuario a eliminado el pedido N°" + pedido.getNumeroPedido());
                if (resumenDevueltos.length() > 0) {
                    mostrarAlerta("Pedido eliminado", resumenDevueltos.toString());
                }
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
    void btnNuevoPedido(ActionEvent event) {
        if (!puedeCrear) {
            mostrarAlerta("Permiso denegado", "No tienes permiso para crear pedidos.");
            return;
        }
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

            if (validarStockInsumos(nuevoPedido)) {
                // Solo aquí, después de la validación, se descuenta el stock realmente
                Map<Producto, Integer> productosMap = new java.util.HashMap<>();
                for (PedidoProducto pp : nuevoPedido.getPedidoProductos()) {
                    productosMap.put(pp.getProducto(), pp.getCantidad());
                }
                recetaProcessor.procesarRecetas(productosMap);
                agregarPedido(nuevoPedido); // SOLO este método agrega la tarjeta visualmente
                ActionLogger.log("Pedido creado: " + nuevoPedido.getCliente().getNombre() +
                        " con productos: " + nuevoPedido.getProductos());
            }

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

    private boolean validarStockInsumos(Pedido pedido) {
        // Construir el mapa Producto -> cantidad
        Map<Producto, Integer> productosMap = new java.util.HashMap<>();
        for (PedidoProducto pp : pedido.getPedidoProductos()) {
            productosMap.put(pp.getProducto(), pp.getCantidad());
        }

        // Validar stock suficiente usando simularFaltantes
        var faltantes = recetaProcessor.simularFaltantes(productosMap);
        if (!faltantes.isEmpty()) {
            ActionLogger.log("No hay insumos suficientes para el pedido. Faltantes: " + faltantes);
            return false;
        }
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
