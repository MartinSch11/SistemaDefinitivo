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
import model.CatalogoInsumo;
import persistence.dao.CatalogoInsumoDAO;
import persistence.dao.ProveedorDAO;
import utilities.ActionLogger;

import java.io.IOException;
import java.util.List;

public class TableInsumosController {

    @FXML private TableView<CatalogoInsumo> tableInsumos;
    @FXML private TableColumn<CatalogoInsumo, String> colInsumo;
    @FXML private TableColumn<CatalogoInsumo, String> colProveedor;
    @FXML private Button btnAgregar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;

    private CatalogoInsumoDAO catalogoInsumoDAO;
    private ProveedorDAO proveedorDAO;  // Añadimos proveedorDAO
    private ObservableList<CatalogoInsumo> catalogoList;

    public TableInsumosController() {
        catalogoInsumoDAO = new CatalogoInsumoDAO();
    }

    @FXML
    public void initialize() {
        configurarTabla();
        cargarInsumos();
        configurarBotones();

        // Permisos del usuario
        java.util.List<String> permisos = model.SessionContext.getInstance().getPermisos();
        boolean puedeCrear = permisos != null && permisos.contains("Insumos-crear");
        boolean puedeModificar = permisos != null && permisos.contains("Insumos-modificar");
        boolean puedeEliminar = permisos != null && permisos.contains("Insumos-eliminar");

        btnAgregar.setDisable(!puedeCrear);
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);

        tableInsumos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnModificar.setDisable(!(puedeModificar && newSelection != null));
            btnEliminar.setDisable(!(puedeEliminar && newSelection != null));
        });
    }

    private void configurarTabla() {
        colInsumo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colProveedor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProveedor()));

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
        ActionLogger.log("El usuario agregó un nuevo insumo al catálogo.");
        abrirFormularioInsumo(null);
    }

    @FXML
    private void handleModificar() {
        CatalogoInsumo insumoSeleccionado = tableInsumos.getSelectionModel().getSelectedItem();
        if (insumoSeleccionado != null) {
            // Registro de la acción del usuario
            ActionLogger.log("El usuario modificó el insumo del catálogo: " + insumoSeleccionado.getNombre());
            abrirFormularioInsumo(insumoSeleccionado);
        }
    }

    @FXML
    private void handleEliminar() {
        CatalogoInsumo insumoSeleccionado = tableInsumos.getSelectionModel().getSelectedItem();
        if (insumoSeleccionado != null) {
            // Registro de la acción del usuario
            ActionLogger.log("El usuario eliminó el insumo del catálogo: " + insumoSeleccionado.getNombre());
            catalogoInsumoDAO.delete(insumoSeleccionado);
            catalogoList.remove(insumoSeleccionado);
        }
    }

    private void abrirFormularioInsumo(CatalogoInsumo insumo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/NuevoInsumo.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(insumo == null ? "Agregar Insumo al Catálogo" : "Modificar Insumo del Catálogo");
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
        List<CatalogoInsumo> catalogo = catalogoInsumoDAO.findAll();
        catalogoList = FXCollections.observableArrayList(catalogo);
        tableInsumos.setItems(catalogoList);
    }

}
