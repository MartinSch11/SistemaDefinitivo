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
import model.Lote;
import persistence.dao.InsumoDAO;
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
    private final javafx.collections.ObservableList<InsumoViewModel> insumosObservable = javafx.collections.FXCollections.observableArrayList();

    public void initialize() {
        colNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colFechaCompra.setCellValueFactory(cellData -> cellData.getValue().fechaCompraProperty());
        colCaducidad.setCellValueFactory(cellData -> cellData.getValue().fechaCaducidadProperty());
        colCantidad.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty());
        colProveedor.setCellValueFactory(cellData -> cellData.getValue().proveedorProperty());

        tableInsumos.setItems(insumosObservable);
        cargarInsumos();

        txtBuscar.textProperty().addListener((_, _, newValue) -> {
            filtrarInsumos(newValue);
        });
    }

    private void cargarInsumos() {
        insumosObservable.clear();

        List<Lote> insumosActualizados = insumoDAO.findAll(); 

        List<InsumoViewModel> viewModels = insumosActualizados.stream()
                // CORRECCIÓN: getCantidadActual()
                .filter(i -> i.getCantidadActual() > 0.0001)
                .map(insumo -> {
                    InsumoViewModel vm = new InsumoViewModel(insumo);
                    
                    // CORRECCIÓN: getCantidadActual()
                    double cantidad = insumo.getCantidadActual();
                    String medida = insumo.getMedida();
                    
                    String cantidadStr = (cantidad == Math.floor(cantidad)) 
                            ? String.format("%.0f", cantidad)
                            : String.format(java.util.Locale.ROOT, "%.2f", cantidad);
                    
                    if (cantidadStr.endsWith(".00"))
                        cantidadStr = cantidadStr.substring(0, cantidadStr.length() - 3);
                    
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
                .filter(i -> {
                    // CORRECCIÓN: Usamos getIngrediente() (o getNombre() helper si lo tiene)
                    String nombre = i.getIngrediente() != null ? i.getIngrediente().getNombre() : "";
                    String estado = i.getIngrediente() != null ? i.getIngrediente().getEstado() : "";
                    
                    return nombre.toLowerCase().contains(filtroLower)
                            || estado.toLowerCase().contains(filtroLower);
                })
                // CORRECCIÓN: getCantidadActual()
                .filter(i -> i.getCantidadActual() > 0.0001)
                .map(InsumoViewModel::new)
                .collect(Collectors.toList());
                
        tableInsumos.setItems(FXCollections.observableArrayList(filtrados));
    }

    @FXML
    void handleAgregar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/StockForm.fxml"));
            AnchorPane root = loader.load();
            
            // Ojo acá: StockFormController seguro necesitará ajuste si lo usás, 
            // pero por ahora que compile este archivo.
            controller.StockFormController formController = loader.getController();
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
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/mainMenu.css", false);
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
            stage.setTitle("Insumos Faltantes");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            ActionLogger.log("Error al abrir insumos faltantes: " + e.getMessage());
        }
    }
}