package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Insumo;
import model.InsumoFaltante;
import model.Proveedor;
import persistence.dao.InsumoDAO;
import persistence.dao.InsumoFaltanteDAO;
import utilities.Paths;
import utilities.SceneLoader;
import utilities.ActionLogger;

import java.io.IOException;
import java.util.List;

public class StockController {

    @FXML private TableView<Insumo> tableInsumos;
    @FXML private TableColumn<Insumo, String> colNombre;
    @FXML private TableColumn<Insumo, String> colFechaCompra;
    @FXML private TableColumn<Insumo, String> colCaducidad;
    @FXML private TableColumn<Insumo, String> colCantidad;  // Cambio: Ahora es un String
    @FXML private TableColumn<Insumo, String> colProveedor;

    private InsumoDAO insumoDAO;
    private final InsumoFaltanteDAO insumoFaltanteDAO = new InsumoFaltanteDAO();

    public void initialize() {
        // Crear el objeto DAO
        insumoDAO = new InsumoDAO();

        // Configurar las columnas de la tabla
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colFechaCompra.setCellValueFactory(new PropertyValueFactory<>("fechaCompra"));
        colCaducidad.setCellValueFactory(new PropertyValueFactory<>("fechaCaducidad"));

        // Cambiar el PropertyValueFactory para que obtenga la cantidad concatenada con la medida
        colCantidad.setCellValueFactory(cellData -> {
            String cantidadConMedida = cellData.getValue().getCantidad() + " " + cellData.getValue().getMedida();
            return new SimpleStringProperty(cantidadConMedida);
        });

        // Configurar el PropertyValueFactory de colProveedor para mostrar el nombre del proveedor
        colProveedor.setCellValueFactory(cellData -> {
            Proveedor proveedor = cellData.getValue().getProveedor();
            return new SimpleStringProperty(proveedor != null ? proveedor.getNombre() : ""); // Si el proveedor es null, muestra una cadena vacía
        });

        // Cargar los insumos de la base de datos
        cargarInsumos();
    }

    // Método para cargar los insumos desde la base de datos y mostrarlos en la tabla
    private void cargarInsumos() {
        // Obtener los insumos desde la base de datos
        List<Insumo> insumos = insumoDAO.findAll();
        // Agregar los insumos a la tabla
        tableInsumos.getItems().addAll(insumos);
    }

    @FXML
    void handleAgregar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/StockForm.fxml"));
            AnchorPane root = loader.load();
            // Obtener el controlador del formulario
            StockFormController formController = loader.getController();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Agregar Insumo");
            stage.show();
            ActionLogger.log("El usuario abrió el formulario para agregar un nuevo insumo.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/loginAdmin.css", true);
        ActionLogger.log("El usuario regresó al menú principal desde la pantalla de gestión de insumos.");
    }

}
