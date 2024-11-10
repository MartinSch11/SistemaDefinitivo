package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Insumo;
import model.Medida;
import model.Proveedor;
import persistence.dao.InsumoDAO;
import persistence.dao.MedidaDAO;
import persistence.dao.ProveedorDAO;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;


public class StockFormController {

    @FXML private TextField nombreInsumoField;
    @FXML private TextField cantidadField;
    @FXML private TextField loteField;
    @FXML private DatePicker fechaCaducidadData;
    @FXML private ChoiceBox<Medida> medidaChoiceBox;
    @FXML private ChoiceBox<Proveedor> proveedorChoiceBox;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private TextField telefonoProveedorField;
    @FXML private TextField ubicacionProveedorField;
    @FXML private TextField NombreProveedorField;
    @FXML private RadioButton radioNo;
    @FXML private RadioButton radioSi;
    @FXML private ComboBox<String> cmbBuscarProveedor;

    private ProveedoresController proveedoresController;
    private StockController stockController;

    private InsumoDAO insumoDAO;
    private MedidaDAO medidaDAO;
    private ProveedorDAO proveedorDAO;
    private Insumo insumo;

    public StockFormController() {
        insumoDAO = new InsumoDAO();
        medidaDAO = new MedidaDAO();
        proveedorDAO = new ProveedorDAO();
    }

    @FXML
    private void initialize() {
        cargarMedidas();
        cargarProveedores();

        radioNo.setSelected(true);

        try {
            FXMLLoader proveedoresLoader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/Proveedores.fxml"));
            proveedoresLoader.load();
            proveedoresController = proveedoresLoader.getController();

            FXMLLoader stockLoader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/Stock.fxml"));
            stockLoader.load();
            stockController = stockLoader.getController();

            //Pasar la referencia de StockController a ProveedoresController
            if (proveedoresController != null && stockController != null) {
                proveedoresController.setStockController(stockController);
            } else {
                System.out.println("Error: No se pudo inicializar los controladores.");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    private void controlRadiosButtons(){
        if (radioSi.isSelected()){
            cmbBuscarProveedor.setDisable(false);
            radioNo.setSelected(false);
            NombreProveedorField.clear();
            NombreProveedorField.setDisable(true);
        } else if (radioNo.isSelected()) {
            radioSi.setSelected(false);
        }
    }

    private void cargarMedidas() {
        List<Medida> medidas = medidaDAO.findAll();
        medidaChoiceBox.getItems().addAll(medidas);
    }

    private void cargarProveedores() {
        List<Proveedor> proveedores = proveedorDAO.findAll();
        proveedorChoiceBox.getItems().addAll(proveedores);
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        String nombreInsumo = nombreInsumoField.getText();
        String cantidad = cantidadField.getText();
        String lote = loteField.getText();
        LocalDate fechaCaducidad = fechaCaducidadData.getValue();
        Medida medidaSeleccionada = medidaChoiceBox.getValue();
        Proveedor proveedorSeleccionado = proveedorChoiceBox.getValue();

        if (radioSi.isSelected()){
            controlRadiosButtons();
            String nombreProveedor = cmbBuscarProveedor.getValue();
        }else if (radioNo.isSelected()){
            String nombreProveedor = NombreProveedorField.getText();
        }
        String telefonoProveedor = telefonoProveedorField.getText();
        String ubicacionProveedor = ubicacionProveedorField.getText();


        if (nombreInsumo.isEmpty() || cantidad.isEmpty() || lote.isEmpty() || fechaCaducidad == null || medidaSeleccionada == null || proveedorSeleccionado == null) {
            // Mostrar mensaje de error
            showAlert(Alert.AlertType.ERROR, "Error", "Por favor, complete todos los campos.");
            return;
        }

        try {
            double cantidadNumerica = Double.parseDouble(cantidad); // Cambiado a double
            Insumo insumo = new Insumo(nombreInsumo, cantidadNumerica, lote, fechaCaducidad, medidaSeleccionada, proveedorSeleccionado);
            insumoDAO.save(insumo);
            cerrarFormulario();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "La cantidad debe ser un número válido. " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        cerrarFormulario();
    }



    private void cerrarFormulario() {
        // Crear un mensaje de alerta de confirmación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("Se perderán los cambios no guardados. ¿Desea salir?");
        alert.setContentText("Seleccione su opción.");

        // Mostrar y esperar la respuesta del usuario
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Cerrar la ventana si el usuario confirma
            Stage stage = (Stage) btnCancelar.getScene().getWindow();
            stage.close();
        } else {
            // Cerrar la alerta si el usuario cancela
            alert.close();
        }
    }

    public void setInsumoParaEditar(Insumo insumo) {
        this.insumo = insumo; // Guarda el insumo a editar
        // Rellenar los campos del formulario con los datos del insumo
        nombreInsumoField.setText(insumo.getNombre());
        cantidadField.setText(String.valueOf(insumo.getCantidad()));
        loteField.setText(insumo.getLote());
        fechaCaducidadData.setValue(insumo.getFechaCaducidad());
        medidaChoiceBox.setValue(insumo.getMedida());
        proveedorChoiceBox.setValue(insumo.getProveedor());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

