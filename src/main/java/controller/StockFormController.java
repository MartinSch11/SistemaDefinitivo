package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
import model.Ingrediente;
import model.MovimientoStock;
import model.Lote;
import model.Proveedor;
import persistence.dao.IngredienteDAO;
import persistence.dao.HistorialCompraDAO;
import persistence.dao.InsumoDAO;
import persistence.dao.ProveedorDAO;
import utilities.ActionLogger;
import utilities.RecetaProcessor;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class StockFormController {

    @FXML private ComboBox<Ingrediente> cmbInsumos;
    @FXML private TextField cantidadField;
    @FXML private DatePicker fechaCaducidadData;
    @FXML private DatePicker fechaCompraData;
    @FXML private ChoiceBox<String> medidaChoiceBox;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private TextField precioTextField;
    @FXML private ComboBox<Proveedor> cmbProveedor;
    @FXML private Button btnAgregarInsumo;

    private InsumoDAO insumoDAO;
    private ProveedorDAO proveedorDAO;
    private IngredienteDAO catalogoInsumoDAO;
    private HistorialCompraDAO historialCompraDAO = new HistorialCompraDAO();
    private final RecetaProcessor recetaProcessor = new RecetaProcessor();

    private StockController stockController;

    public StockFormController() {
        this.insumoDAO = new InsumoDAO();
        this.proveedorDAO = new ProveedorDAO();
        this.catalogoInsumoDAO = new IngredienteDAO();
    }

    public void setStockController(StockController stockController) {
        this.stockController = stockController;
    }

    @FXML
    private void initialize() {
        medidaChoiceBox.getItems().addAll("GR", "KG", "ML", "L", "UNIDAD", "UNIDADES");

        cargarInsumos();
        cargarProveedores();

        cmbInsumos.valueProperty().addListener((_, _, newVal) -> {
            filtrarUnidadesPorEstado(newVal);
        });

        // CORRECCIÓN: Permitir decimales en cantidad (0.5 KG)
        cantidadField.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), 0.0, change -> {
            if (change.getControlNewText().matches("^[0-9]*\\.?[0-9]*$")) {
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

        fechaCompraData.valueProperty().addListener((_, _, newVal) -> {
            fechaCaducidadData.setDayCellFactory(_ -> new DateCell() {
                @Override
                public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (newVal != null && item.isBefore(newVal)) {
                        setDisable(true);
                        setStyle("-fx-background-color: #ffc0cb;");
                    } else {
                        setDisable(false);
                        setStyle("");
                    }
                }
            });
        });
    }

    private void filtrarUnidadesPorEstado(Ingrediente insumo) {
        medidaChoiceBox.getItems().clear();
        String estado = (insumo != null) ? insumo.getEstado() : null;
        if (estado == null) {
            medidaChoiceBox.getItems().addAll("GR", "KG", "ML", "L", "UNIDAD", "UNIDADES");
            return;
        }
        switch (estado) {
            case "LÍQUIDO": medidaChoiceBox.getItems().addAll("ML", "L"); break;
            case "SÓLIDO": medidaChoiceBox.getItems().addAll("GR", "KG"); break;
            case "UNIDAD": medidaChoiceBox.getItems().addAll("UNIDAD", "UNIDADES"); break;
            default: medidaChoiceBox.getItems().addAll("GR", "KG", "ML", "L", "UNIDAD", "UNIDADES");
        }
        if (!medidaChoiceBox.getItems().isEmpty()) {
            medidaChoiceBox.setValue(medidaChoiceBox.getItems().get(0));
        }
    }

    private void cargarProveedores() {
        List<Proveedor> proveedores = proveedorDAO.findAll();
        cmbProveedor.getItems().clear();
        cmbProveedor.getItems().addAll(proveedores);

        Callback<ListView<Proveedor>, ListCell<Proveedor>> cellFactory = _ -> new ListCell<Proveedor>() {
            @Override
            protected void updateItem(Proveedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        };
        cmbProveedor.setCellFactory(cellFactory);
        cmbProveedor.setButtonCell(cellFactory.call(null));
    }

    private void cargarInsumos() {
        List<Ingrediente> catalogo = catalogoInsumoDAO.findAll();
        cmbInsumos.getItems().clear();
        cmbInsumos.getItems().addAll(catalogo);

        cmbInsumos.setCellFactory(_ -> new ListCell<Ingrediente>() {
            @Override
            protected void updateItem(Ingrediente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        cmbInsumos.setButtonCell(new ListCell<Ingrediente>() {
            @Override
            protected void updateItem(Ingrediente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        Ingrediente insumoSeleccionado = cmbInsumos.getValue();
        Proveedor proveedorSeleccionado = cmbProveedor.getValue();
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
            // CORRECCIÓN: Parseamos a Double para soportar "0.5"
            double cantidadNumerica = Double.parseDouble(cantidad);
            double precioDouble = Double.parseDouble(precio);

            // CORRECCIÓN 1: Constructor V2.0 de Lote (Con Proveedor al final)
            Lote insumo = new Lote(
                insumoSeleccionado, 
                cantidadNumerica, 
                precioDouble, 
                medida, 
                fechaCompra,
                fechaCaducidad,
                proveedorSeleccionado
            );

            insumoDAO.save(insumo);
            ActionLogger.log("Insumo guardado: " + insumoSeleccionado.getNombre());

            // CORRECCIÓN 2: Constructor V2.0 de MovimientoStock
            // (Tipo, Ingrediente, Cantidad, Medida, Detalle, Costo)
            MovimientoStock compra = new MovimientoStock(
                    MovimientoStock.TipoMovimiento.COMPRA,
                    insumoSeleccionado.getNombre(),
                    cantidadNumerica,
                    medida,
                    proveedorSeleccionado.getNombre(), // Detalle = Proveedor
                    precioDouble
            );
            historialCompraDAO.save(compra);

            resolverFaltantesPorCatalogoInsumo(insumoSeleccionado);
            
            if (stockController != null) {
                stockController.recargarTablaInsumos();
            }
            cerrarFormulario();
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "La cantidad o el precio no son válidos.");
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void resolverFaltantesPorCatalogoInsumo(Ingrediente catalogoInsumo) {
        var resumen = recetaProcessor.resolverFaltantesPorCatalogoInsumo();
        if (stockController != null) {
            stockController.recargarTablaInsumos();
        }
        if (resumen != null && !resumen.isEmpty()) {
            StringBuilder msg = new StringBuilder("Faltantes resueltos automáticamente:\n");
            resumen.forEach((nombre, cantidad) -> msg.append("- ").append(nombre).append(": ").append(cantidad).append("\n"));
            showAlert(Alert.AlertType.INFORMATION, "Faltantes resueltos", msg.toString());
        }
    }

    @FXML
    private void abrirFormularioInsumo() {
        abrirFormularioInsumo(null);
        cargarInsumos();
    }
    
    private void abrirFormularioInsumo(Ingrediente insumo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/NuevoInsumo.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Nuevo Ingrediente");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showAndWait();
            cargarInsumos();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}