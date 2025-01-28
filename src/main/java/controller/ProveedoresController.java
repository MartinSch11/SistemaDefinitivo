package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Proveedor;
import persistence.dao.ProveedorDAO;
import utilities.ActionLogger;
import utilities.Paths;
import utilities.SceneLoader;

import java.io.IOException;
import java.util.List;

public class ProveedoresController {
    @FXML private Button btnBuscar;
    @FXML private Button btnEliminar;
    @FXML private Button btnModificar;
    @FXML private TableView<Proveedor> tableViewProveedores;
    @FXML private TableColumn<Proveedor, String> colCuit;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colInsumo;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colUbicacion;
    @FXML private TableColumn<Proveedor, String> colCorreo;

    private ObservableList<Proveedor> proveedoresList = FXCollections.observableArrayList();
    private ProveedorDAO proveedorDAO;

    public ProveedoresController() {
        proveedorDAO = new ProveedorDAO();
    }

    @FXML
    public void initialize() {
        // Configuración de las columnas
        colCuit.setCellValueFactory(new PropertyValueFactory<>("cuit"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colInsumo.setCellValueFactory(new PropertyValueFactory<>("insumosString"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));

        // Cargar los datos en la tabla
        cargarDatos();

        tableViewProveedores.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean productoSeleccionado = newSelection != null;
            btnModificar.setDisable(!productoSeleccionado);
            btnEliminar.setDisable(!productoSeleccionado);
        });
    }

    private void cargarDatos() {
        List<Proveedor> proveedores = proveedorDAO.findAll();
        proveedoresList.setAll(proveedores);
        tableViewProveedores.setItems(proveedoresList);
    }

    @FXML
    void handleEliminar(ActionEvent event) {
        Proveedor proveedorSeleccionado = tableViewProveedores.getSelectionModel().getSelectedItem();

        if (proveedorSeleccionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("Estás a punto de eliminar a " + proveedorSeleccionado.getNombre());
            alert.setContentText("¿Estás seguro de que deseas eliminar a este proveedor?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    proveedorDAO.delete(proveedorSeleccionado);
                    cargarDatos();
                    mostrarAlerta("Proveedor eliminado", "El proveedor ha sido eliminado exitosamente.", Alert.AlertType.INFORMATION);
                    ActionLogger.log("Proveedor eliminado: " + proveedorSeleccionado.getNombre());
                }
            });
        } else {
            mostrarAlerta("Selección requerida", "Por favor, selecciona un proveedor para eliminar.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    void handleModificar(ActionEvent event) {
        Proveedor proveedorSeleccionado = tableViewProveedores.getSelectionModel().getSelectedItem();

        if (proveedorSeleccionado != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/NuevoProveedor.fxml"));
                AnchorPane root = loader.load();

                NuevoProveedorController nuevoProveedorController = loader.getController();
                nuevoProveedorController.cargarProveedor(proveedorSeleccionado);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Modificar Proveedor");
                stage.show();

                ActionLogger.log("Formulario de modificación abierto para el proveedor: " + proveedorSeleccionado.getNombre());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Selección requerida", "Por favor, selecciona un proveedor para modificar.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    void handleAgregar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/NuevoProveedor.fxml"));
            AnchorPane root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Agregar Proveedor");
            stage.show();

            ActionLogger.log("Formulario de agregado de proveedor abierto.");
        } catch (IOException e) {
            e.printStackTrace();
            ActionLogger.log("Error al intentar abrir el formulario de agregado.");
        }
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/loginAdmin.css", true);
        ActionLogger.log("Volviendo al menú principal.");
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
