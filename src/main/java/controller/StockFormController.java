package controller;

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

import java.time.LocalDate;
import java.util.List;

public class StockFormController{

    @FXML private ComboBox<Insumo> cmbInsumos; // Cambiado a ComboBox de Insumo
    @FXML private TextField cantidadField;
    @FXML private DatePicker fechaCaducidadData;
    @FXML private DatePicker fechaCompraData;
    @FXML private ChoiceBox<String> medidaChoiceBox;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private TextField precioTextField;
    @FXML private ComboBox cmbProveedor;

    private InsumoDAO insumoDAO;
    private ProveedorDAO proveedorDAO; // Agregar ProveedorDAO


    public StockFormController() {
        insumoDAO = new InsumoDAO();
        proveedorDAO = new ProveedorDAO(); // Inicializar ProveedorDAO

    }

    @FXML
    private void initialize() {
        // Llenar el ChoiceBox con las unidades de medida
        medidaChoiceBox.getItems().addAll("Litros", "Militros", "Kilos", "Gramos", "Unidades", "Unidad");

        // Llenar el ComboBox con los insumos
        cargarInsumos();
        cargarProveedores(); // Llenar el ComboBox de proveedores

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

        cmbProveedor.setCellFactory(param -> new ListCell<Proveedor>() { // Especificar el tipo completo
            @Override
            protected void updateItem(Proveedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        cmbProveedor.setButtonCell(new ListCell<Proveedor>() { // Especificar el tipo completo
            @Override
            protected void updateItem(Proveedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });
    }


    private void cargarInsumos() {
        // Recuperar la lista de insumos desde la base de datos
        List<Insumo> insumos = insumoDAO.findAll(); // Supón que este método devuelve todos los insumos
        cmbInsumos.getItems().clear();  // Limpiar cualquier dato anterior
        cmbInsumos.getItems().addAll(insumos); // Agregar todos los insumos al ComboBox

        // Configurar el texto que se mostrará en el ComboBox
        cmbInsumos.setCellFactory(param -> new ListCell<Insumo>() {
            @Override
            protected void updateItem(Insumo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombre());  // Mostrar solo el nombre del insumo
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
                    setText(item.getNombre());  // Mostrar solo el nombre en el botón del ComboBox
                }
            }
        });
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        Insumo insumoSeleccionado = cmbInsumos.getValue();
        Proveedor proveedorSeleccionado = (Proveedor) cmbProveedor.getValue(); // Obtener el proveedor seleccionado
        String cantidad = cantidadField.getText();
        String precio = precioTextField.getText();
        LocalDate fechaCaducidad = fechaCaducidadData.getValue();
        LocalDate fechaCompra = fechaCompraData.getValue();
        String medida = medidaChoiceBox.getValue();

        if (insumoSeleccionado == null || proveedorSeleccionado == null || cantidad.isEmpty() || precio.isEmpty() ||
                fechaCaducidad == null || fechaCompra == null || medida == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Por favor, complete todos los campos.");
            return;
        }

        try {
            int cantidadNumerica = Integer.parseInt(cantidad);
            double precioDouble = Double.parseDouble(precio);

            // Crear un objeto de Insumo con el proveedor seleccionado
            Insumo insumo = new Insumo(insumoSeleccionado.getNombre(), cantidadNumerica, precioDouble, medida, fechaCompra, fechaCaducidad);
            insumo.setProveedor(proveedorSeleccionado); // Asignar el proveedor al insumo

            // Guardar el insumo con el proveedor en la base de datos
            insumoDAO.save(insumo);

            cerrarFormulario(); // Cerrar el formulario
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "La cantidad o el precio no son válidos.");
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        cerrarFormulario();
    }

    private void cerrarFormulario() {
        // Cerrar la ventana
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
