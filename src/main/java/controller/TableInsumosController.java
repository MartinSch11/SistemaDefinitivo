package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Insumo;
import persistence.dao.InsumoDAO;
import persistence.dao.ProveedorDAO;
import utilities.ActionLogger;

import java.io.IOException;
import java.util.List;

public class TableInsumosController {

    @FXML private TableView<Insumo> tableInsumos;
    @FXML private TableColumn<Insumo, String> colInsumo;
    @FXML private TableColumn<Insumo, String> colProveedor;
    @FXML private Button btnAgregar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;

    private InsumoDAO insumoDAO;
    private ProveedorDAO proveedorDAO;  // Añadimos proveedorDAO
    private ObservableList<Insumo> insumosList;

    public TableInsumosController() {
        insumoDAO = new InsumoDAO();
        proveedorDAO = new ProveedorDAO();  // Inicializamos el proveedorDAO
    }

    @FXML
    public void initialize() {
        configurarTabla();
        cargarInsumos();
        configurarBotones();
    }

    private void configurarTabla() {
        colInsumo.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colProveedor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombreProveedor()));

        // Listener para habilitar botones de modificar y eliminar al seleccionar un insumo
        tableInsumos.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnModificar.setDisable(newValue == null);
            btnEliminar.setDisable(newValue == null);
        });
    }


    private void configurarBotones() {
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    @FXML
    private void handleAgregar() {
        // Registro de la acción del usuario
        ActionLogger.log("El usuario agregó un nuevo insumo.");
        abrirFormularioInsumo(null);
    }

    @FXML
    private void handleModificar() {
        Insumo insumoSeleccionado = tableInsumos.getSelectionModel().getSelectedItem();
        if (insumoSeleccionado != null) {
            // Registro de la acción del usuario
            ActionLogger.log("El usuario modificó el insumo: " + insumoSeleccionado.getNombre());
            abrirFormularioInsumo(insumoSeleccionado);
        }
    }

    @FXML
    private void handleEliminar() {
        Insumo insumoSeleccionado = tableInsumos.getSelectionModel().getSelectedItem();
        if (insumoSeleccionado != null) {
            // Registro de la acción del usuario
            ActionLogger.log("El usuario eliminó el insumo: " + insumoSeleccionado.getNombre());
            insumoDAO.delete(insumoSeleccionado);
            insumosList.remove(insumoSeleccionado);
        }
    }

    private void abrirFormularioInsumo(Insumo insumo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/NuevoInsumo.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(insumo == null ? "Agregar Insumo" : "Modificar Insumo");
            stage.initModality(Modality.WINDOW_MODAL);

            NuevoInsumoController controller = loader.getController();
            if (insumo != null) {
                controller.setInsumo(insumo);
            }
            controller.setTableInsumosController(this); // Pasamos la referencia del controlador de la tabla

            stage.showAndWait();
            cargarInsumos(); // Recargar la tabla después de cerrar el formulario
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cargarInsumos() {
        List<Insumo> insumos = insumoDAO.findAll();
        ObservableList<Insumo> insumosList = FXCollections.observableArrayList(insumos);
        tableInsumos.setItems(insumosList);
    }

}
