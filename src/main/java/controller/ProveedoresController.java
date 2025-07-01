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
    @FXML private Button btnAgregar;
    @FXML private Button btnEliminar;
    @FXML private Button btnModificar;
    @FXML private TableView<Proveedor> tableViewProveedores;
    @FXML private TableColumn<Proveedor, String> colCuit;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colInsumo;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colUbicacion;
    @FXML private TableColumn<Proveedor, String> colCorreo;
    @FXML private TextField txtFiltrar;

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

        // Permisos del usuario
        java.util.List<String> permisos = model.SessionContext.getInstance().getPermisos();
        boolean puedeCrear = permisos != null && permisos.contains("Proveedores-crear");
        boolean puedeModificar = permisos != null && permisos.contains("Proveedores-modificar");
        boolean puedeEliminar = permisos != null && permisos.contains("Proveedores-eliminar");

        btnAgregar.setDisable(!puedeCrear);
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);
        // btnAgregar puede que no exista, si existe agregar lógica similar

        // Listener para habilitar los botones solo si hay selección y permiso
        tableViewProveedores.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnModificar.setDisable(!(puedeModificar && newSelection != null));
            btnEliminar.setDisable(!(puedeEliminar && newSelection != null));
        });

        // Búsqueda en tiempo real
        txtFiltrar.textProperty().addListener((obs, oldText, newText) -> filtrarProveedores(newText));
    }

    private void cargarDatos() {
        List<Proveedor> proveedores = proveedorDAO.findAll();
        proveedoresList.setAll(proveedores);
        tableViewProveedores.setItems(proveedoresList);
    }

    private void filtrarProveedores(String filtro) {
        if (filtro == null || filtro.isEmpty()) {
            tableViewProveedores.setItems(proveedoresList);
        } else {
            String filtroLower = filtro.toLowerCase();
            ObservableList<Proveedor> filtrados = proveedoresList.filtered(p -> {
                boolean matchNombre = p.getNombre() != null && p.getNombre().toLowerCase().contains(filtroLower);
                boolean matchInsumo = p.getInsumosString() != null && p.getInsumosString().toLowerCase().contains(filtroLower);
                boolean matchTelefono = p.getTelefono() != null && p.getTelefono().toLowerCase().contains(filtroLower);
                return matchNombre || matchInsumo || matchTelefono;
            });
            tableViewProveedores.setItems(filtrados);
        }
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
                    ActionLogger.log("El usuario eliminó el proveedor: " + proveedorSeleccionado.getNombre());
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

                ActionLogger.log("El usuario accedió al formulario para modificar el proveedor: " + proveedorSeleccionado.getNombre());
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

            ActionLogger.log("El usuario accedió al formulario para crear un proveedor.");
        } catch (IOException e) {
            e.printStackTrace();
            ActionLogger.log("Error al intentar abrir el formulario de agregado.");
        }
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/loginAdmin.css", false);
        ActionLogger.log("El usuario volvió al menú principal.");
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
