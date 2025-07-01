package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.CatalogoInsumo;
import model.Proveedor;
import persistence.dao.CatalogoInsumoDAO;
import persistence.dao.ProveedorDAO;
import utilities.ActionLogger;

import java.util.List;

public class NuevoInsumoController {

    @FXML private TextField txtNomInsumo;
    @FXML private ComboBox<Proveedor> cmbProveedor;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Button btnCancelar;
    @FXML private Button btnAceptar;

    private ProveedorDAO proveedorDAO;
    private CatalogoInsumoDAO catalogoInsumoDAO;
    private TableInsumosController tableInsumosController; // Referencia al controlador de la tabla

    public NuevoInsumoController() {
        proveedorDAO = new ProveedorDAO();
        catalogoInsumoDAO = new CatalogoInsumoDAO(); // Recuerda ajustar el constructor según tu EntityManager
    }

    @FXML
    public void initialize() {
        configurarCampoNombreInsumo();
        cargarProveedores();
        cargarEstados();
    }

    private void configurarCampoNombreInsumo() {
        txtNomInsumo.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            // Permitir letras y espacios
            if (newText.matches("[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]*")) {
                change.setText(change.getText().toUpperCase());
                return change;
            } else {
                return null;
            }
        }));
    }

    private void cargarProveedores() {
        List<Proveedor> proveedores = proveedorDAO.findAll();
        ObservableList<Proveedor> proveedoresList = FXCollections.observableArrayList(proveedores);
        cmbProveedor.setItems(proveedoresList);
    }

    private void cargarEstados() {
        cmbEstado.setItems(FXCollections.observableArrayList("LÍQUIDO", "SÓLIDO", "UNIDAD"));
    }

    @FXML
    private void handleAceptar() {
        String nombreInsumo = txtNomInsumo.getText();
        Proveedor proveedorSeleccionado = cmbProveedor.getSelectionModel().getSelectedItem();
        String estadoSeleccionado = cmbEstado.getSelectionModel().getSelectedItem();

        if (nombreInsumo.isEmpty() || proveedorSeleccionado == null || estadoSeleccionado == null) {
            mostrarAlerta("Campos incompletos", "Por favor, completa todos los campos.", Alert.AlertType.WARNING);
        } else {
            // Guardar solo en catalogo_insumo si no existe
            if (catalogoInsumoDAO.findByNombre(nombreInsumo) == null) {
                CatalogoInsumo cat = new CatalogoInsumo(nombreInsumo, estadoSeleccionado, proveedorSeleccionado.getNombre());
                catalogoInsumoDAO.save(cat);
                ActionLogger.log("CatalogoInsumo " + nombreInsumo + " creado con éxito.");
            } else {
                mostrarAlerta("Duplicado", "Ya existe un insumo con ese nombre en el catálogo.", Alert.AlertType.WARNING);
                return;
            }
            mostrarAlerta("Éxito", "Insumo guardado exitosamente en el catálogo.", Alert.AlertType.INFORMATION);
            if (tableInsumosController != null) {
                tableInsumosController.cargarInsumos(); // Recargar la tabla si corresponde
            }
            cerrarVentana();
        }
    }

    @FXML
    private void handleCancelar() {
        // Log de acción: cancelación
        ActionLogger.log("Operación de insumo cancelada.");
        cerrarVentana();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    // Método para recibir un insumo para editarlo
    public void setInsumo(CatalogoInsumo insumo) {
        txtNomInsumo.setText(insumo.getNombre());
        // Buscar el proveedor por nombre y seleccionarlo en el ComboBox
        Proveedor proveedor = proveedorDAO.findAll().stream()
                .filter(p -> p.getNombre().equals(insumo.getProveedor()))
                .findFirst()
                .orElse(null);
        cmbProveedor.setValue(proveedor);
        cmbEstado.setValue(insumo.getEstado());
    }

    // Método para recibir el controlador de la tabla
    public void setTableInsumosController(TableInsumosController controller) {
        this.tableInsumosController = controller;
    }
}
