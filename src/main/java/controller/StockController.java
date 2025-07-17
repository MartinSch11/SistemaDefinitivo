package controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Insumo;
import persistence.dao.InsumoDAO;
import persistence.dao.InsumoFaltanteDAO;
import utilities.Paths;
import utilities.SceneLoader;
import utilities.ActionLogger;
import model.InsumoViewModel;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class StockController {

    @FXML private TableView<InsumoViewModel> tableInsumos;
    @FXML private TableColumn<InsumoViewModel, String> colNombre;
    @FXML private TableColumn<InsumoViewModel, String> colFechaCompra;
    @FXML private TableColumn<InsumoViewModel, String> colCaducidad;
    @FXML private TableColumn<InsumoViewModel, String> colCantidad;
    @FXML private TableColumn<InsumoViewModel, String> colProveedor;
    @FXML private TextField txtBuscar;

    private final InsumoDAO insumoDAO = new InsumoDAO();
    private final InsumoFaltanteDAO insumoFaltanteDAO = new InsumoFaltanteDAO();
    private final javafx.collections.ObservableList<InsumoViewModel> insumosObservable =
            javafx.collections.FXCollections.observableArrayList();

    public void initialize() {
        colNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colFechaCompra.setCellValueFactory(cellData -> cellData.getValue().fechaCompraProperty());
        colCaducidad.setCellValueFactory(cellData -> cellData.getValue().fechaCaducidadProperty());
        colCantidad.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty());
        colProveedor.setCellValueFactory(cellData -> cellData.getValue().proveedorProperty());

        tableInsumos.setItems(insumosObservable);
        cargarInsumos();

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarInsumos(newValue);
        });
    }

    private void cargarInsumos() {
        insumosObservable.clear();

        // Asegurate de que InsumoDAO haga una consulta nueva a la DB
        List<Insumo> insumosActualizados = insumoDAO.findAll(); // asegurate de no usar objetos cacheados

        List<InsumoViewModel> viewModels = insumosActualizados.stream()
                .filter(i -> i.getCantidad() > 0.0001)
                .map(insumo -> {
                    InsumoViewModel vm = new InsumoViewModel(insumo);
                    // Formatear cantidad: sin decimales si es entero, con dos decimales si no
                    double cantidad = insumo.getCantidad();
                    String medida = insumo.getMedida();
                    String cantidadStr = (cantidad == Math.floor(cantidad)) ? String.format("%.0f", cantidad) : String.format(java.util.Locale.ROOT, "%.2f", cantidad);
                    vm.setCantidad(cantidadStr + " " + (medida != null ? medida : ""));
                    return vm;
                })
                .collect(Collectors.toList());

        insumosObservable.addAll(viewModels);
    }

    private void filtrarInsumos(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            cargarInsumos();
            return;
        }
        String filtroLower = filtro.toLowerCase();
        List<InsumoViewModel> filtrados = insumoDAO.findAll().stream()
                .filter(i -> i.getNombre().toLowerCase().contains(filtroLower)
                        || (i.getCatalogoInsumo() != null && i.getCatalogoInsumo().getEstado() != null
                        && i.getCatalogoInsumo().getEstado().toLowerCase().contains(filtroLower)))
                .filter(i -> i.getCantidad() > 0.0001)
                .map(InsumoViewModel::new)
                .collect(Collectors.toList());
        tableInsumos.setItems(FXCollections.observableArrayList(filtrados));
    }
    @FXML
    void handleAgregar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/StockForm.fxml"));
            AnchorPane root = loader.load();
            StockFormController formController = loader.getController();
            formController.setStockController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Agregar Insumo");
            stage.show();
            ActionLogger.log("El usuario abrió el formulario para agregar un nuevo insumo.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recargarTablaInsumos() {
        cargarInsumos();
        tableInsumos.refresh();
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/loginAdmin.css", false);
        ActionLogger.log("El usuario regresó al menú principal.");
    }

    @FXML
    private void abrirHistorialCompras(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/HistorialComprasDialog.fxml"));
            AnchorPane root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Historial de compras de insumos");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            ActionLogger.log("Error al abrir el historial de compras: " + e.getMessage());
        }
    }

    @FXML
    private void abrirInsumosFaltantes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/InsumosFaltantesDialog.fxml"));
            AnchorPane root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Historial de compras de insumos");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            ActionLogger.log("Error al abrir el historial de compras: " + e.getMessage());
        }
    }
}
