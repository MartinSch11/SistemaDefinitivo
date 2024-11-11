package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Insumo;
import model.Medida;
import model.Producto;
import model.Proveedor;
import persistence.dao.InsumoDAO;
import persistence.dao.MedidaDAO;
import persistence.dao.ProveedorDAO;
import persistence.dao.ProductoDAO;
import utilities.Paths;
import utilities.SceneLoader;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


//pasar datos guardados a stock
public class ProveedoresController {
    @FXML private Button btnBuscar;@FXML private Button btnEliminar;@FXML private Button btnModificar;

    @FXML private TableView<Proveedor> tableViewProveedores;
    @FXML private TableColumn<Producto, String> colNombreProveedor;
    @FXML private TableColumn<Producto, String> colInsumoProveedor;
    @FXML private TableColumn<Producto, String> colContactoProveedor;
    @FXML private TableColumn<Producto, String> colUbicacionProveedor;


    @FXML private StockController stockController;

    public void setStockController(StockController stockController) {
        this.stockController = stockController;
    }

    private ObservableList<Insumo> listaInsumos;

    private ObservableList<Proveedor> proveedoresList = FXCollections.observableArrayList();

    private InsumoDAO insumoDAO;
    private MedidaDAO medidaDAO;
    private ProveedorDAO proveedorDAO;
    private Insumo insumo;

    public ProveedoresController() {
        insumoDAO = new InsumoDAO();
        medidaDAO = new MedidaDAO();
        proveedorDAO = new ProveedorDAO();
    }

    @FXML
    public void initialize() {
        colNombreProveedor.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colInsumoProveedor.setCellValueFactory(new PropertyValueFactory<>("insumo"));
        colContactoProveedor.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colUbicacionProveedor.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        // Setear los datos iniciales (si tienes)
        tableViewProveedores.setItems(proveedoresList);


    }

    private void cargarInsumos() {
        List<Insumo> insumos = insumoDAO.findAll();
        listaInsumos.setAll(insumos);
        if (stockController != null) {
            TableView<Insumo> tableInsumos = stockController.getTableInsumos();
            tableInsumos.setItems(listaInsumos);
        }
    }
    @FXML
    void handleEliminar(ActionEvent event) {

    }

    @FXML
    void handleModificar(ActionEvent event) {

    }
    @FXML
    void handleAgregar(ActionEvent event) {
    }

    @FXML
    void handleNuevoProveedor(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/stock_form.fxml"));
            DialogPane dialogPane = loader.load();

            // Crear diálogo
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Completar información");

            StockFormController controller = loader.getController();

            // Mostrar el diálogo y esperar respuesta
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                cargarInsumos(); // Actualiza la tabla si se agrega un nuevo insumo
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/stock_form.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Completar información");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.ADMIN_MAINMENU, "/css/loginAdmin.css", true);
    }

    private void InsertarDatosTabla(){

    }


}
