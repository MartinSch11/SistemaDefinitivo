package controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import model.Insumo;
import model.Proveedor;
import persistence.dao.InsumoDAO;
import persistence.dao.ProveedorDAO;
import utilities.ActionLogger;

import java.time.LocalDate;
import java.util.List;

public class StockFormController {

    @FXML private ComboBox<Insumo> cmbInsumos;
    @FXML private TextField cantidadField;
    @FXML private DatePicker fechaCaducidadData;
    @FXML private DatePicker fechaCompraData;
    @FXML private ChoiceBox<String> medidaChoiceBox;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private TextField precioTextField;
    @FXML private ComboBox cmbProveedor;

    private InsumoDAO insumoDAO;
    private ProveedorDAO proveedorDAO;

    public StockFormController() {
        insumoDAO = new InsumoDAO();
        proveedorDAO = new ProveedorDAO();
    }

    @FXML
    private void initialize() {
        // Llenar el ChoiceBox con las unidades de medida
        medidaChoiceBox.getItems().addAll("GR", "KG", "ML", "L", "UNIDAD", "UNIDADES");

        // Llenar el ComboBox con los insumos
        cargarInsumos();
        cargarProveedores();

        // Configurar validadores para cantidadField y precioTextField
        cantidadField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, change -> {
            if (change.getControlNewText().matches("^[0-9]*$")) {
                return change;
            }
            return null;
        }));

        precioTextField.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), 0.0, change -> {
            if (change.getControlNewText().matches("^[0-9]*\\.?[0-9]*$")) {
                return change;
            }
            return null;
        }));
    }

    private void cargarProveedores() {
        List<Proveedor> proveedores = proveedorDAO.findAll();
        cmbProveedor.getItems().clear();
        cmbProveedor.getItems().addAll(proveedores);

        cmbProveedor.setCellFactory(param -> new ListCell<Proveedor>() {
            @Override
            protected void updateItem(Proveedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        cmbProveedor.setButtonCell(new ListCell<Proveedor>() {
            @Override
            protected void updateItem(Proveedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });
    }

    private void cargarInsumos() {
        List<Insumo> insumos = insumoDAO.findAll();
        cmbInsumos.getItems().clear();
        cmbInsumos.getItems().addAll(insumos);

        cmbInsumos.setCellFactory(param -> new ListCell<Insumo>() {
            @Override
            protected void updateItem(Insumo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombre());
                }
            }
        });

        cmbInsumos.setButtonCell(new ListCell<Insumo>() {
            @Override
            protected void updateItem(Insumo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombre());
                }
            }
        });
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        Insumo insumoSeleccionado = cmbInsumos.getValue();
        Proveedor proveedorSeleccionado = (Proveedor) cmbProveedor.getValue();
        String cantidad = cantidadField.getText();
        String precio = precioTextField.getText();
        LocalDate fechaCaducidad = fechaCaducidadData.getValue();
        LocalDate fechaCompra = fechaCompraData.getValue();
        String medida = medidaChoiceBox.getValue();

        if (insumoSeleccionado == null || proveedorSeleccionado == null || cantidad.isEmpty() || precio.isEmpty() ||
                fechaCaducidad == null || fechaCompra == null || medida == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Por favor, complete todos los campos.");
            ActionLogger.log("El usuario intentó guardar un insumo sin completar todos los campos.");
            return;
        }

        try {
            int cantidadNumerica = Integer.parseInt(cantidad);
            double precioDouble = Double.parseDouble(precio);

            Insumo insumo = new Insumo(insumoSeleccionado.getNombre(), cantidadNumerica, precioDouble, medida, fechaCompra, fechaCaducidad);
            insumo.setProveedor(proveedorSeleccionado);

            insumoDAO.save(insumo);
            ActionLogger.log("El usuario guardó correctamente un insumo: " + insumo.getNombre());

            cerrarFormulario();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "La cantidad o el precio no son válidos.");
            ActionLogger.log("El usuario intentó guardar un insumo con datos inválidos.");
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        cerrarFormulario();
        ActionLogger.log("El usuario canceló la operación y cerró el formulario de insumo.");
    }

    private void cerrarFormulario() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
