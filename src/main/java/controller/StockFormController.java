package controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import model.CatalogoInsumo;
import model.HistorialCompra;
import model.Insumo;
import model.InsumoFaltante;
import model.Proveedor;
import persistence.dao.CatalogoInsumoDAO;
import persistence.dao.HistorialCompraDAO;
import persistence.dao.InsumoDAO;
import persistence.dao.InsumoFaltanteDAO;
import persistence.dao.ProveedorDAO;
import utilities.ActionLogger;
import utilities.RecetaProcessor;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class StockFormController {

    @FXML private ComboBox<CatalogoInsumo> cmbInsumos;
    @FXML private TextField cantidadField;
    @FXML private DatePicker fechaCaducidadData;
    @FXML private DatePicker fechaCompraData;
    @FXML private ChoiceBox<String> medidaChoiceBox;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private TextField precioTextField;
    @FXML private ComboBox cmbProveedor;
    @FXML private Button btnAgregarInsumo;

    private InsumoDAO insumoDAO;
    private ProveedorDAO proveedorDAO;
    private InsumoFaltanteDAO insumoFaltanteDAO;
    private CatalogoInsumoDAO catalogoInsumoDAO;
    private HistorialCompraDAO historialCompraDAO = new HistorialCompraDAO();
    private final RecetaProcessor recetaProcessor = new RecetaProcessor();

    // Referencia al controlador principal para refrescar la tabla
    private StockController stockController;

    // Constructor vacío requerido por JavaFX/FXML
    public StockFormController() {
        this.insumoDAO = new InsumoDAO();
        this.proveedorDAO = new ProveedorDAO();
        this.insumoFaltanteDAO = new InsumoFaltanteDAO();
        this.catalogoInsumoDAO = new CatalogoInsumoDAO();
    }

    public void setStockController(StockController stockController) {
        this.stockController = stockController;
    }

    @FXML
    private void initialize() {
        // Llenar el ChoiceBox con las unidades de medida (todas, para inicialización)
        medidaChoiceBox.getItems().addAll("GR", "KG", "ML", "L", "UNIDAD", "UNIDADES");

        // Llenar el ComboBox con los insumos
        cargarInsumos();
        cargarProveedores();

        // Listener para filtrar unidades según el estado del insumo seleccionado
        cmbInsumos.valueProperty().addListener((obs, oldVal, newVal) -> {
            filtrarUnidadesPorEstado(newVal);
        });

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

        // Validación: la fecha de caducidad no puede ser inferior a la de compra, y se
        // muestra en rojo
        fechaCompraData.valueProperty().addListener((obs, oldVal, newVal) -> {
            fechaCaducidadData.setDayCellFactory(picker -> new DateCell() {
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

    /**
     * Filtra las unidades de medida disponibles según el estado del insumo
     * seleccionado.
     * LÍQUIDO: ML, L
     * SÓLIDO: GR, KG
     * UNIDAD: UNIDAD, UNIDADES
     */
    private void filtrarUnidadesPorEstado(CatalogoInsumo insumo) {
        medidaChoiceBox.getItems().clear();
        String estado = (insumo != null) ? insumo.getEstado() : null;
        if (estado == null) {
            // Si no hay insumo seleccionado, mostrar todas
            medidaChoiceBox.getItems().addAll("GR", "KG", "ML", "L", "UNIDAD", "UNIDADES");
            return;
        }
        switch (estado) {
            case "LÍQUIDO":
                medidaChoiceBox.getItems().addAll("ML", "L");
                break;
            case "SÓLIDO":
                medidaChoiceBox.getItems().addAll("GR", "KG");
                break;
            case "UNIDAD":
                medidaChoiceBox.getItems().addAll("UNIDAD", "UNIDADES");
                break;
            default:
                medidaChoiceBox.getItems().addAll("GR", "KG", "ML", "L", "UNIDAD", "UNIDADES");
        }
        // Seleccionar la primera opción por defecto
        if (!medidaChoiceBox.getItems().isEmpty()) {
            medidaChoiceBox.setValue(medidaChoiceBox.getItems().get(0));
        }
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
        List<CatalogoInsumo> catalogo = catalogoInsumoDAO.findAll();
        cmbInsumos.getItems().clear();
        cmbInsumos.getItems().addAll(catalogo);

        cmbInsumos.setCellFactory(param -> new ListCell<CatalogoInsumo>() {
            @Override
            protected void updateItem(CatalogoInsumo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        cmbInsumos.setButtonCell(new ListCell<CatalogoInsumo>() {
            @Override
            protected void updateItem(CatalogoInsumo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        CatalogoInsumo insumoSeleccionado = cmbInsumos.getValue();
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

            Insumo insumo = new Insumo(insumoSeleccionado, cantidadNumerica, precioDouble, medida, fechaCompra,
                    fechaCaducidad);
            insumo.setProveedor(proveedorSeleccionado);

            insumoDAO.save(insumo);
            ActionLogger.log("El usuario guardó correctamente un insumo: " + insumoSeleccionado.getNombre());

            // Guardar en historial de compras
            HistorialCompra compra = new HistorialCompra(
                    insumoSeleccionado.getNombre(),
                    cantidadNumerica,
                    medida,
                    fechaCompra,
                    proveedorSeleccionado.getNombre(),
                    precioDouble);
            historialCompraDAO.save(compra);

            resolverFaltantesPorCatalogoInsumo(insumoSeleccionado);
            // Refrescar la tabla de insumos si se abrió desde StockController
            if (stockController != null) {
                stockController.recargarTablaInsumos();
            }
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

    /**
     * Resuelve faltantes pendientes para un insumo de catálogo usando la lógica centralizada de RecetaProcessor.
     */
    public void resolverFaltantesPorCatalogoInsumo(CatalogoInsumo catalogoInsumo) {
        // Llama a la lógica centralizada (resuelve todos los faltantes posibles)
        var resumen = recetaProcessor.resolverFaltantesPorCatalogoInsumo();
        // Refrescar la tabla de insumos si se abrió desde StockController
        if (stockController != null) {
            stockController.recargarTablaInsumos();
        }
        // Mostrar resumen profesional si se resolvió algún faltante
        if (resumen != null && !resumen.isEmpty()) {
            StringBuilder msg = new StringBuilder("Faltantes resueltos automáticamente:\n");
            resumen.forEach((nombre, cantidad) -> msg.append("- ").append(nombre).append(": ").append(cantidad).append("\n"));
            showAlert(Alert.AlertType.INFORMATION, "Faltantes resueltos", msg.toString());
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

            stage.showAndWait();
            cargarInsumos(); // Recargar la tabla después de cerrar el formulario
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirFormularioInsumo() {
        abrirFormularioInsumo(null);
        cargarInsumos(); // Recargar ComboBox de insumos después de cerrar el formulario
    }

}
