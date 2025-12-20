package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Ingrediente; // Nuevo modelo
import model.RecetaDetalle;
import model.Receta;
import persistence.dao.RecetaDAO;
import utilities.ActionLogger;
import utilities.Paths;
import utilities.SceneLoader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class RecetasController {

    @FXML private TableView<Receta> tableRecetas;
    @FXML private TableColumn<Receta, String> colNomReceta;
    @FXML private TableColumn<Receta, String> colIngReceta;
    @FXML private Button btnAgregar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;
    @FXML private Pane paneDetallesReceta;
    @FXML private Label labelNombreReceta;
    @FXML private FlowPane flowIngredientes;
    @FXML private ScrollPane scrollIngredientes;

    private ObservableList<Receta> listaRecetas = FXCollections.observableArrayList();

    public void initialize() {
        cargarRecetas();
        configurarColumnas();
        tableRecetas.setItems(listaRecetas);

        // Ajuste responsivo del FlowPane
        if (flowIngredientes != null && scrollIngredientes != null) {
            flowIngredientes.prefWrapLengthProperty().bind(
                    scrollIngredientes.viewportBoundsProperty().map(b -> b.getWidth() - 20)
            );
        }

        // Permisos (Mantengo tu lógica original)
        java.util.List<String> permisos = model.SessionContext.getInstance().getPermisos();
        boolean puedeCrear = permisos != null && permisos.contains("Recetas-crear");
        boolean puedeModificar = permisos != null && permisos.contains("Recetas-modificar");
        boolean puedeEliminar = permisos != null && permisos.contains("Recetas-eliminar");

        if (btnAgregar != null) btnAgregar.setDisable(!puedeCrear);
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);

        tableRecetas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newSelection) -> {
            if (newSelection != null) {
                mostrarDetallesReceta(newSelection);
                btnModificar.setDisable(!puedeModificar);
                btnEliminar.setDisable(!puedeEliminar);
            } else {
                paneDetallesReceta.setVisible(false);
                btnModificar.setDisable(true);
                btnEliminar.setDisable(true);
            }
        });
    }

    private void cargarRecetas() {
        RecetaDAO recetaDAO = new RecetaDAO();
        listaRecetas.clear();
        listaRecetas.setAll(recetaDAO.findAll());
        tableRecetas.setItems(listaRecetas);
    }

    private void configurarColumnas() {
        // CORRECCIÓN 1: Creamos la propiedad al vuelo porque el modelo no tiene Property
        colNomReceta.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombreReceta()));

        // Celda con wrap + tooltip
        colIngReceta.setCellFactory(column -> new TableCell<Receta, String>() {
            private final javafx.scene.text.Text text = new javafx.scene.text.Text();
            {
                text.wrappingWidthProperty().bind(column.widthProperty().subtract(10));
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                    setTooltip(new Tooltip(item));
                }
            }
        });

        // CORRECCIÓN 2: Adaptado a la nueva lista de ingredientes (V2.0)
        colIngReceta.setCellValueFactory(cellData -> {
            Receta receta = cellData.getValue();
            // Mapeamos los nombres de los ingredientes desde RecetaDetalle -> Ingrediente
            List<String> nombres = receta.getIngredientes().stream()
                    .map(detalle -> detalle.getIngrediente().getNombre())
                    .collect(Collectors.toList());
            
            return new SimpleStringProperty(String.join(", ", nombres));
        });
    }

    // MÉTODO ACTUALIZADO: Usa getIngredientes() y getCantidad()
    private void mostrarDetallesReceta(Receta receta) {
        paneDetallesReceta.setVisible(true);
        labelNombreReceta.setText(receta.getNombreReceta());
        flowIngredientes.getChildren().clear();

        for (RecetaDetalle detalle : receta.getIngredientes()) { // CAMBIO: getIngredientes
            Ingrediente ingrediente = detalle.getIngrediente();  // CAMBIO: getIngrediente

            // Formateo lindo para decimales
            double cantidad = detalle.getCantidad(); // CAMBIO: getCantidad (double)
            String cantStr = (cantidad == Math.floor(cantidad)) 
                    ? String.format("%.0f", cantidad) 
                    : String.valueOf(cantidad);

            String texto = String.format("%s %s %s", ingrediente.getNombre(), cantStr, detalle.getUnidad());

            Label chip = new Label(texto);
            chip.getStyleClass().add("chip"); // Asegurate de tener .chip en tu CSS o usásetStyle
            chip.setTooltip(new Tooltip(texto));
            
            // Si no tenés la clase chip, le damos un estilo básico inline para que se vea bien igual
            if (chip.getStyleClass().size() == 1) { 
                chip.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 15; -fx-padding: 5 10; -fx-border-color: #bdbdbd; -fx-border-radius: 15;");
            }
            
            flowIngredientes.getChildren().add(chip);
        }
    }

    private void abrirPanelReceta(Receta receta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/NuevaReceta.fxml"));
            Parent root = loader.load();
            NuevaRecetaController dialogController = loader.getController();
            
            if (receta != null) {
                dialogController.cargarRecetaParaModificar(receta);
                dialogController.setTitulo("Editar Receta");
            } else {
                dialogController.setTitulo("Nueva Receta");
            }
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(receta == null ? "Agregar Receta" : "Modificar Receta");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            cargarRecetas(); // Recargar tabla al volver
            
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("No se pudo cargar la ventana de receta.");
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    void handleVolver(ActionEvent event) {
        ActionLogger.log("Regreso al menú principal.");
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/mainMenu.css", false);
    }

    @FXML
    void handleAgregar(ActionEvent event) {
        ActionLogger.log("Abriendo nueva receta.");
        abrirPanelReceta(null);
    }

    @FXML
    void handleModificar(ActionEvent event) {
        Receta seleccionada = tableRecetas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            ActionLogger.log("Modificando receta: " + seleccionada.getNombreReceta());
            abrirPanelReceta(seleccionada);
        } else {
            mostrarError("Seleccioná una receta primero.");
        }
    }

    @FXML
    void handleEliminar(ActionEvent event) {
        Receta seleccionada = tableRecetas.getSelectionModel().getSelectedItem();

        if (seleccionada != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, 
                "¿Eliminar '" + seleccionada.getNombreReceta() + "'?\nEsta acción no se puede deshacer.",
                ButtonType.YES, ButtonType.NO);
            alert.setHeaderText("Confirmar eliminación");

            if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                try {
                    RecetaDAO dao = new RecetaDAO();
                    dao.delete(seleccionada);
                    
                    ActionLogger.log("Receta eliminada: " + seleccionada.getNombreReceta());
                    cargarRecetas();
                    paneDetallesReceta.setVisible(false);

                } catch (Exception e) {
                    // Manejo específico si hay restricción de FK (Producto usando Receta)
                    Alert errorAlert = new Alert(Alert.AlertType.WARNING);
                    errorAlert.setTitle("No se puede eliminar");
                    errorAlert.setHeaderText("Receta en uso");
                    errorAlert.setContentText("No podés borrar esta receta porque está asociada a un PRODUCTO activo.\n\n" +
                            "Primero desvinculá o eliminá el producto.");
                    errorAlert.showAndWait();
                }
            }
        }
    }
}