package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Insumo;
import persistence.dao.InsumoDAO;
import utilities.Paths;
import utilities.SceneLoader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class StockController {

    @FXML private Button btnAgregar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;
    @FXML private Button btnVolver;
    @FXML private TableView<Insumo> tableInsumos;
    @FXML private TableColumn<Insumo, String> colNombre;
    @FXML private TableColumn<Insumo, String> colCaducidad;
    @FXML private TableColumn<Insumo, String> colLote;
    @FXML private TableColumn<Insumo, Double> colCantidad;
    @FXML private TableColumn<Insumo, String> colProveedor;

    private ObservableList<Insumo> listaInsumos;
    private InsumoDAO insumoDAO;

    @FXML
    public void initialize() {
        insumoDAO = new InsumoDAO();
        listaInsumos = FXCollections.observableArrayList();

        // Configuración de columnas de la tabla
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCaducidad.setCellValueFactory(new PropertyValueFactory<>("fechaCaducidad"));
        colLote.setCellValueFactory(new PropertyValueFactory<>("lote"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedor"));

        cargarInsumos();

        // Listener para habilitar botones al seleccionar un insumo
        tableInsumos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean insumoSeleccionado = newSelection != null;
            btnModificar.setDisable(!insumoSeleccionado);
            btnEliminar.setDisable(!insumoSeleccionado);
        });
    }

    private void cargarInsumos() {
        List<Insumo> insumos = insumoDAO.findAll();
        listaInsumos.setAll(insumos);
        tableInsumos.setItems(listaInsumos);
    }

    @FXML
    public void handleAgregar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/stock_form.fxml"));
            DialogPane dialogPane = loader.load();

            // Crear diálogo
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Agregar Insumo");

            StockFormController controller = loader.getController();

            // Mostrar el diálogo y esperar respuesta
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                cargarInsumos(); // Actualiza la tabla si se agrega un nuevo insumo
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleModificar(ActionEvent event) {
        Insumo insumoSeleccionado = tableInsumos.getSelectionModel().getSelectedItem();
        if (insumoSeleccionado != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/stock_form.fxml"));
                DialogPane dialogPane = loader.load();

                // Crear diálogo
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(dialogPane);
                dialog.setTitle("Modificar Insumo");

                StockFormController controller = loader.getController();
                controller.setInsumoParaEditar(insumoSeleccionado);

                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    cargarInsumos(); // Recargar la tabla si se modifica un insumo
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "No se ha seleccionado ningún insumo", "Por favor, selecciona un insumo para modificar.");
        }
    }

    @FXML
    void handleEliminar(ActionEvent event) {
        Insumo insumoSeleccionado = tableInsumos.getSelectionModel().getSelectedItem();
        if (insumoSeleccionado != null) {
            // Mostrar un diálogo de confirmación antes de eliminar
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "¿Estás seguro de que deseas eliminar este insumo?");
            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                insumoDAO.delete(insumoSeleccionado);
                listaInsumos.remove(insumoSeleccionado);
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "No se ha seleccionado ningún insumo", "Por favor, selecciona un insumo para eliminar.");
        }
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.ADMIN_MAINMENU, "/css/loginAdmin.css", true);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
