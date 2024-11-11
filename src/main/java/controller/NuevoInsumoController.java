package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Insumo;
import model.Proveedor;
import persistence.dao.InsumoDAO;
import persistence.dao.ProveedorDAO;
import java.util.List;

public class NuevoInsumoController {

    @FXML private TextField txtNomInsumo;
    @FXML private ComboBox<Proveedor> cmbProveedor;
    @FXML private Button btnCancelar;
    @FXML private Button btnAceptar;

    private ProveedorDAO proveedorDAO;
    private InsumoDAO insumoDAO;

    private Insumo insumo; // Variable de instancia para el insumo a modificar
    private TableInsumosController tableInsumosController; // Referencia al controlador de la tabla

    public NuevoInsumoController() {
        proveedorDAO = new ProveedorDAO();
        insumoDAO = new InsumoDAO();
    }

    @FXML
    public void initialize() {
        configurarCampoNombreInsumo();
        cargarProveedores();
    }

    private void configurarCampoNombreInsumo() {
        txtNomInsumo.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[a-zA-Z]*")) {
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

    @FXML
    private void handleAceptar() {
        String nombreInsumo = txtNomInsumo.getText();
        Proveedor proveedorSeleccionado = cmbProveedor.getSelectionModel().getSelectedItem();

        if (nombreInsumo.isEmpty() || proveedorSeleccionado == null) {
            mostrarAlerta("Campos incompletos", "Por favor, completa todos los campos.", Alert.AlertType.WARNING);
        } else {
            if (insumo == null) {
                insumo = new Insumo();
            }

            insumo.setNombre(nombreInsumo);
            insumo.setProveedor(proveedorSeleccionado);

            if (insumo.getId() == null) {
                insumoDAO.save(insumo);
            } else {
                insumoDAO.update(insumo);
            }

            mostrarAlerta("Éxito", "Insumo guardado exitosamente.", Alert.AlertType.INFORMATION);
            if (tableInsumosController != null) {
                tableInsumosController.cargarInsumos(); // Recargar la tabla
            }
            cerrarVentana();
        }
    }

    @FXML
    private void handleCancelar() {
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
    public void setInsumo(Insumo insumo) {
        this.insumo = insumo;
        txtNomInsumo.setText(insumo.getNombre());
        cmbProveedor.setValue(insumo.getProveedor());
    }

    // Método para recibir el controlador de la tabla
    public void setTableInsumosController(TableInsumosController controller) {
        this.tableInsumosController = controller;
    }
}
