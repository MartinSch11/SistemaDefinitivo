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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.InsumoReceta;
import model.Receta;
import model.Insumo;
import persistence.dao.RecetaDAO;
import utilities.ActionLogger;
import utilities.Paths;
import utilities.SceneLoader;
import java.io.IOException;
import java.util.List;

public class RecetasController {

    @FXML private TableView<Receta> tableRecetas;
    @FXML private TableColumn<Receta, String> colNomReceta;
    @FXML private TableColumn<Receta, String> colIngReceta;
    @FXML private Button btnAgregar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;
    @FXML private Pane paneDetallesReceta;
    @FXML private Label labelNombreReceta;
    @FXML private VBox vboxIngredientes;
    private ObservableList<Receta> listaRecetas = FXCollections.observableArrayList();

    public void initialize() {
        cargarRecetas();
        configurarColumnas();
        tableRecetas.setItems(listaRecetas);

        // Permisos del usuario
        java.util.List<String> permisos = model.SessionContext.getInstance().getPermisos();
        boolean puedeCrear = permisos != null && permisos.contains("Recetas-crear");
        boolean puedeModificar = permisos != null && permisos.contains("Recetas-modificar");
        boolean puedeEliminar = permisos != null && permisos.contains("Recetas-eliminar");

        // Botón agregar (si existe)
        if (btnAgregar != null) btnAgregar.setDisable(!puedeCrear);
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);

        tableRecetas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
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
        listaRecetas.setAll(recetaDAO.findAll()); // Recarga las recetas desde la base de datos

        tableRecetas.setItems(listaRecetas);

        recetaDAO.close();
    }

    private void configurarColumnas() {
        colNomReceta.setCellValueFactory(cellData -> cellData.getValue().nombreRecetaProperty());
        colIngReceta.setCellValueFactory(cellData -> {
            Receta receta = cellData.getValue();
            List<String> nombresInsumos = receta.getInsumos().stream()
                    .map(Insumo::getNombre)
                    .toList();
            String ingredientesConcatenados = String.join(", ", nombresInsumos);
            return new SimpleStringProperty(ingredientesConcatenados);
        });
    }

    private void mostrarDetallesReceta(Receta receta) {
        paneDetallesReceta.setVisible(true);
        labelNombreReceta.setText(receta.getNombreReceta());
        vboxIngredientes.getChildren().clear();

        for (InsumoReceta insumoReceta : receta.getInsumosReceta()) {
            Insumo insumo = insumoReceta.getInsumo();
            String textoIngrediente = String.format("%s %d %s",
                    insumo.getNombre(),
                    (int) insumoReceta.getCantidadUtilizada(), // Casteo a int
                    insumoReceta.getUnidad());
            Label labelIngrediente = new Label(textoIngrediente);
            labelIngrediente.setStyle("-fx-font-size: 14; -fx-text-fill: #333;");
            vboxIngredientes.getChildren().add(labelIngrediente);
        }
    }

    private void abrirPanelReceta(Receta receta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/NuevaReceta.fxml"));
            Parent root = loader.load();

            NuevaRecetaController dialogController = loader.getController();

            if (receta != null) {
                // Carga los datos de la receta en el controlador de diálogo
                dialogController.cargarRecetaParaModificar(receta);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(receta == null ? "Agregar Receta" : "Modificar Receta");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            Receta recetaModificada = dialogController.getRecetaModificada();

            if (recetaModificada != null) {
                guardarRecetaModificada(receta, recetaModificada);
            }
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("No se pudo cargar el diálogo de la receta. Intenta nuevamente.");
        }
    }

    private void guardarRecetaModificada(Receta recetaOriginal, Receta recetaModificada) {
        RecetaDAO recetaDAO = new RecetaDAO();

        try {
            if (recetaOriginal == null) {
                // Caso de nueva receta
                recetaDAO.save(recetaModificada);
                listaRecetas.add(recetaModificada);
            } else {
                // Caso de receta existente: evitar duplicados
                List<InsumoReceta> insumosOriginales = recetaOriginal.getInsumosReceta();

                // Eliminar insumos eliminados
                for (InsumoReceta insumoOriginal : insumosOriginales) {
                    if (!recetaModificada.getInsumosReceta().contains(insumoOriginal)) {
                        recetaDAO.eliminarInsumoDeReceta(insumoOriginal.getId());
                    }
                }

                // Actualizar receta y sus insumos
                recetaDAO.update(recetaModificada);

                // Refrescar la lista observable
                int index = listaRecetas.indexOf(recetaOriginal);
                if (index != -1) {
                    listaRecetas.set(index, recetaModificada);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al guardar o modificar la receta. Intenta nuevamente.");
        } finally {
            recetaDAO.close();
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
        ActionLogger.log("El usuario regresó al menú principal.");
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/loginAdmin.css", false);
    }

    @FXML
    void handleAgregar(ActionEvent event) {
        ActionLogger.log("El usuario quiere agregar una nueva receta.");
        abrirPanelReceta(null);
    }

    @FXML
    void handleModificar(ActionEvent event) {
        Receta recetaSeleccionada = tableRecetas.getSelectionModel().getSelectedItem();
        if (recetaSeleccionada != null) {
            ActionLogger.log("El usuario quiere modificar la receta: " + recetaSeleccionada.getNombreReceta());
            abrirPanelReceta(recetaSeleccionada);
        } else {
            mostrarError("Por favor, selecciona una receta para modificar.");
        }
    }

    @FXML
    void handleEliminar(ActionEvent event) {
        Receta recetaSeleccionada = tableRecetas.getSelectionModel().getSelectedItem();
        if (recetaSeleccionada != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmación de Eliminación");
            alert.setHeaderText("¿Estás seguro de que deseas eliminar esta receta?");
            alert.setContentText("Esta acción no se puede deshacer.");

            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                ActionLogger.log("El usuario quiere eliminar la receta: " + recetaSeleccionada.getNombreReceta());
                RecetaDAO recetaDAO = new RecetaDAO();
                recetaDAO.delete(recetaSeleccionada);
                recetaDAO.close();

                cargarRecetas();
            }
        }
    }
}
