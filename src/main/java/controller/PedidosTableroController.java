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
import model.Combo;
import model.ComboProducto;
import model.PedidoCombo;
import java.time.LocalDate;
import java.util.Comparator;
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

    // Referencia al menú contextual actual
    private javafx.scene.control.ContextMenu contextMenuActual;

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
                .sorted(Comparator.comparing(Pedido::getFechaEntrega, Comparator.nullsLast(Comparator.naturalOrder())))
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
            // Cerrar el menú anterior si existe
            if (contextMenuActual != null && contextMenuActual.isShowing()) {
                contextMenuActual.hide();
            }
            javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
            javafx.scene.control.MenuItem verDetalles = new javafx.scene.control.MenuItem("Ver detalles del pedido");
            verDetalles.setOnAction(_ -> mostrarDetallesPedido(pedido));
            contextMenu.getItems().add(verDetalles);
            // Modificar y eliminar: mostrar siempre si el estado es Sin empezar, pero deshabilitar si no hay permiso
            if ("Sin empezar".equalsIgnoreCase(pedido.getEstadoPedido())) {
                javafx.scene.control.MenuItem modificarPedido = new javafx.scene.control.MenuItem("Modificar pedido");
                modificarPedido.setOnAction(_ -> modificarPedido(pedido));
                modificarPedido.setDisable(!puedeModificar);
                contextMenu.getItems().add(modificarPedido);
                javafx.scene.control.MenuItem eliminarPedido = new javafx.scene.control.MenuItem("Eliminar pedido");
                eliminarPedido.setOnAction(_ -> eliminarPedidoConConfirmacion(pedido));
                eliminarPedido.setDisable(!puedeEliminar);
                contextMenu.getItems().add(eliminarPedido);
            }
            // Agregar opción "Entregar pedido" si el estado es "Hecho"
            if ("Hecho".equalsIgnoreCase(pedido.getEstadoPedido() != null ? pedido.getEstadoPedido().trim() : "")) {
                javafx.scene.control.MenuItem entregarPedido = new javafx.scene.control.MenuItem("Entregar pedido");
                entregarPedido.setOnAction(_ -> entregarPedido(pedido));
                contextMenu.getItems().add(entregarPedido);
            }
            contextMenu.show(tarjeta, e.getScreenX(), e.getScreenY());
            contextMenuActual = contextMenu;
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
                // Devolver productos individuales
                for (PedidoProducto pp : pedido.getPedidoProductos()) {
                    productosParaDevolver.merge(pp.getProducto(), pp.getCantidad(), Integer::sum);
                }
                // Devolver productos de combos
                for (PedidoCombo pc : pedido.getPedidoCombos()) {
                    Combo combo = pc.getCombo();
                    int cantidadCombo = pc.getCantidad();
                    for (ComboProducto cp : combo.getProductos()) {
                        Producto prodCombo = cp.getProducto();
                        int cantidadEnCombo = cp.getCantidad() != null ? cp.getCantidad() : 1;
                        productosParaDevolver.merge(prodCombo, cantidadCombo * cantidadEnCombo, Integer::sum);
                    }
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

            // Si el usuario cerró el diálogo sin crear el pedido, no hacer nada
            if (nuevoPedido == null) {
                return;
            }

            // Validación extra para mostrar en alert si hay datos nulos
            if (nuevoPedido.getCliente() == null || nuevoPedido.getEmpleadoAsignado() == null || nuevoPedido.getPedidoProductos() == null || nuevoPedido.getPedidoCombos() == null) {
                StringBuilder sb = new StringBuilder("Datos faltantes al crear el pedido:\n");
                if (nuevoPedido.getCliente() == null) sb.append("- Cliente nulo\n");
                if (nuevoPedido.getEmpleadoAsignado() == null) sb.append("- Empleado nulo\n");
                if (nuevoPedido.getPedidoProductos() == null) sb.append("- Lista de productos nula\n");
                if (nuevoPedido.getPedidoCombos() == null) sb.append("- Lista de combos nula\n");
                mostrarAlerta("Error de datos", sb.toString());
                return;
            }

            if (validarStockInsumos(nuevoPedido)) {
                agregarPedido(nuevoPedido, true); // Descontar insumos al crear
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void agregarPedido(Pedido pedido, boolean descontarStock) {
        // Verifica si ya existe una tarjeta para este pedido
        StackPane tarjetaExistente = buscarTarjetaPorId(pedido.getNumeroPedido());
        if (tarjetaExistente != null) {
            // Si ya existe, no agregar otra tarjeta ni descontar stock nuevamente
            ActionLogger.log("Intento de agregar pedido duplicado: " + pedido.getNumeroPedido());
            return;
        }
        // Guardar el pedido SOLO aquí si es nuevo
        if (pedido.getNumeroPedido() == null || pedidoDAO.findByNumeroPedido(pedido.getNumeroPedido()) == null) {
            pedidoDAO.save(pedido);
        }
        // Descontar insumos SOLO si se indica
        if (descontarStock) {
            Map<Producto, Integer> productosMap = obtenerMapaProductosTotales(pedido);
            recetaProcessor.procesarRecetas(productosMap);
        }
        StackPane tarjeta = crearTarjetaPedidoKanban(pedido);
        tarjeta.setUserData(pedido.getNumeroPedido());
        agregarTarjetaAColumna(tarjeta, pedido.getEstadoPedido());
        agregarDragAndDrop(tarjeta, pedido);
        ActionLogger.log("Pedido creado: " + pedido.getCliente().getNombre() +
                " con productos: " + pedido.getProductos());
    }

    /**
     * Devuelve un mapa Producto -> cantidad total, desglosando combos en sus productos.
     */
    private Map<Producto, Integer> obtenerMapaProductosTotales(Pedido pedido) {
        Map<Producto, Integer> productosMap = new java.util.HashMap<>();
        // Procesar productos individuales
        for (PedidoProducto pp : pedido.getPedidoProductos()) {
            productosMap.merge(pp.getProducto(), pp.getCantidad(), Integer::sum);
        }
        // Procesar combos por separado
        for (PedidoCombo pc : pedido.getPedidoCombos()) {
            Combo combo = pc.getCombo();
            int cantidadCombo = pc.getCantidad();
            for (ComboProducto cp : combo.getProductos()) {
                Producto prodCombo = cp.getProducto();
                int cantidadEnCombo = cp.getCantidad() != null ? cp.getCantidad() : 1;
                productosMap.merge(prodCombo, cantidadCombo * cantidadEnCombo, Integer::sum);
            }
        }
        return productosMap;
    }

    private boolean validarStockInsumos(Pedido pedido) {
        // Usar el mapa total de productos (desglosando combos)
        Map<Producto, Integer> productosMap = obtenerMapaProductosTotales(pedido);
        // Validar stock suficiente usando simularFaltantes
        var faltantes = recetaProcessor.simularFaltantes(productosMap);
        if (!faltantes.isEmpty()) {
            ActionLogger.log("No hay insumos suficientes para el pedido. Faltantes: " + faltantes);
            return false;
        }
        return true;
    }

    public void agregarNuevoPedido(Pedido pedido) {
        // Solo actualizar si existe, NO guardar dos veces
        if (pedido.getNumeroPedido() != null && pedidoDAO.findByNumeroPedido(pedido.getNumeroPedido()) != null) {
            pedidoDAO.update(pedido);
        } else {
            pedidoDAO.save(pedido);
        }
        try {
            // Elimina cualquier tarjeta existente con el mismo numeroPedido
            StackPane tarjetaExistente = buscarTarjetaPorId(pedido.getNumeroPedido());
            if (tarjetaExistente != null) {
                ((TilePane) tarjetaExistente.getParent()).getChildren().remove(tarjetaExistente);
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
