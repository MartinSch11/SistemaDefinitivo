package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Insumo;
import model.Proveedor;
import persistence.dao.InsumoDAO;
import persistence.dao.ProveedorDAO;

import java.time.LocalDate;
import java.util.List;

public class StockFormController {

    @FXML private TextField nombreInsumoField;
    @FXML private TextField cantidadField;
    @FXML private TextField loteField;
    @FXML private DatePicker fechaCaducidadData;
    @FXML private ChoiceBox<Proveedor> proveedorChoiceBox;
    @FXML private Button btnAceptar;
    @FXML private Button btnCancelar;

    private InsumoDAO insumoDAO;
    private ProveedorDAO proveedorDAO;
    private Insumo insumo;

    public StockFormController() {
        insumoDAO = new InsumoDAO();
        proveedorDAO = new ProveedorDAO();
    }

    @FXML
    private void initialize() {
        cargarMedidas();
        cargarProveedores();
    }

    private void cargarMedidas() {
//
    }

    private void cargarProveedores() {
        List<Proveedor> proveedores = proveedorDAO.findAll();
        proveedorChoiceBox.getItems().addAll(proveedores);
    }

    @FXML
    private void handleAceptar(ActionEvent event) {
        String nombreInsumo = nombreInsumoField.getText();
        String cantidad = cantidadField.getText();  // Cantidad como String
        String lote = loteField.getText();
        LocalDate fechaCaducidad = fechaCaducidadData.getValue();
        //Medida medidaSeleccionada = medidaChoiceBox.getValue();
        Proveedor proveedorSeleccionado = proveedorChoiceBox.getValue();

        if (nombreInsumo.isEmpty() || cantidad.isEmpty() || lote.isEmpty() || fechaCaducidad == null || /*medidaSeleccionada == null ||*/ proveedorSeleccionado == null) {
            // Mostrar mensaje de error
            showAlert(Alert.AlertType.ERROR, "Error", "Por favor, complete todos los campos.");
            return;
        }

        try {
            // Convertir la cantidad a entero
            int cantidadNumerica = Integer.parseInt(cantidad);  // Cambiado a int
            Insumo insumo = new Insumo(nombreInsumo, cantidadNumerica, lote, fechaCaducidad, /*medidaSeleccionada,*/ proveedorSeleccionado);
            insumoDAO.save(insumo);
            cerrarFormulario();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "La cantidad debe ser un número entero válido. " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        cerrarFormulario();
    }

    private void cerrarFormulario() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    public void setInsumoParaEditar(Insumo insumo) {
        this.insumo = insumo; // Guarda el insumo a editar
        // Rellenar los campos del formulario con los datos del insumo
        nombreInsumoField.setText(insumo.getNombre());
        cantidadField.setText(String.valueOf(insumo.getCantidad()));
        loteField.setText(insumo.getLote());
        fechaCaducidadData.setValue(insumo.getFechaCaducidad());
        //medidaChoiceBox.setValue(insumo.getMedida());
        proveedorChoiceBox.setValue(insumo.getProveedor());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

