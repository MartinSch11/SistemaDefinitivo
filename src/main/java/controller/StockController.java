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
import persistence.dao.InsumoDAO;
import utilities.Paths;
import utilities.SceneLoader;

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

    public void initialize() {
        // Crear el objeto DAO
        insumoDAO = new InsumoDAO();

        // Configurar las columnas de la tabla
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colFechaCompra.setCellValueFactory(new PropertyValueFactory<>("fechaCompra"));
        colCaducidad.setCellValueFactory(new PropertyValueFactory<>("fechaCaducidad"));

        // Cambiar el PropertyValueFactory para que obtenga la cantidad concatenada con la medida
        colCantidad.setCellValueFactory(cellData -> {
            // Concatenar la cantidad con la medida usando SimpleStringProperty
            String cantidadConMedida = cellData.getValue().getCantidad() + " " + cellData.getValue().getMedida();
            return new SimpleStringProperty(cantidadConMedida);
        });

        colProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedor"));

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
            // Cargar el archivo FXML de la ventana del formulario de insumos
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/StockForm.fxml"));

            // Crear una nueva escena con el FXML cargado
            AnchorPane root = loader.load();

            // Obtener el controlador del formulario
            StockFormController formController = loader.getController();

            // Mostrar la nueva ventana
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Agregar Insumo");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.ADMIN_MAINMENU, "/css/loginAdmin.css", true);
    }
}
