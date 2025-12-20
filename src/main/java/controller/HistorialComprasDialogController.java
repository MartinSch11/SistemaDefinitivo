package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.MovimientoStock;
import persistence.dao.HistorialCompraDAO; // O MovimientoStockDAO si lo renombraste

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class HistorialComprasDialogController {
    
    @FXML private TableView<MovimientoStock> tablaCompras;
    @FXML private TableColumn<MovimientoStock, String> colInsumo;
    @FXML private TableColumn<MovimientoStock, String> colCantidad;
    @FXML private TableColumn<MovimientoStock, String> colMedida;
    @FXML private TableColumn<MovimientoStock, Double> colPrecio; // Precio Unitario Calculado
    @FXML private TableColumn<MovimientoStock, String> colProveedor;
    @FXML private TableColumn<MovimientoStock, String> colFechaCompra;
    
    @FXML private Button cerrarDialogo;
    @FXML private TextField txtBuscar;

    // Nota: Si renombraste el DAO a MovimientoStockDAO, cambiá el tipo acá
    private final HistorialCompraDAO historialCompraDAO = new HistorialCompraDAO(); 
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @FXML
    public void initialize() {
        // 1. Insumo -> nombreIngrediente
        colInsumo.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombreIngrediente()));

        // 2. Cantidad (Formateada)
        colCantidad.setCellValueFactory(cellData -> {
            double cantidad = cellData.getValue().getCantidad();
            String cantidadStr = (cantidad == Math.floor(cantidad)) 
                    ? String.format("%.0f", cantidad) 
                    : String.format(java.util.Locale.ROOT, "%.2f", cantidad);
            return new javafx.beans.property.SimpleStringProperty(cantidadStr);
        });

        // 3. Medida
        colMedida.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMedida()));

        // 4. Precio Unitario (Calculado: Costo Total / Cantidad)
        colPrecio.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getCostoTotal();
            double cant = cellData.getValue().getCantidad();
            double unitario = (cant > 0) ? (total / cant) : 0.0;
            return new javafx.beans.property.SimpleDoubleProperty(unitario).asObject();
        });

        // 5. Proveedor -> Detalle
        colProveedor.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDetalle()));

        // 6. Fecha -> fechaMovimiento
        colFechaCompra.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFechaMovimiento() != null 
                ? cellData.getValue().getFechaMovimiento().format(dateFormatter) 
                : "-"));

        cargarCompras();

        txtBuscar.textProperty().addListener((_, _, newVal) -> filtrarCompras(newVal));
        
        setupCierreConEscape();
    }

    public void cargarCompras() {
        List<MovimientoStock> movimientos = historialCompraDAO.findAll();
        
        // Filtramos SOLO las COMPRAS (porque ahora la tabla tiene de todo)
        // y que tengan costo mayor a 0
        List<MovimientoStock> compras = movimientos.stream()
                .filter(m -> m.getTipo() == MovimientoStock.TipoMovimiento.COMPRA)
                .filter(m -> m.getCostoTotal() > 0)
                .collect(Collectors.toList());

        ObservableList<MovimientoStock> data = FXCollections.observableArrayList(compras);
        tablaCompras.setItems(data);
    }

    private void filtrarCompras(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            cargarCompras();
            return;
        }
        
        String filtroLower = filtro.toLowerCase();
        List<MovimientoStock> movimientos = historialCompraDAO.findAll();

        List<MovimientoStock> filtrados = movimientos.stream()
                .filter(m -> m.getTipo() == MovimientoStock.TipoMovimiento.COMPRA) // Solo Compras
                .filter(c -> c.getNombreIngrediente().toLowerCase().contains(filtroLower)
                        || (c.getDetalle() != null && c.getDetalle().toLowerCase().contains(filtroLower))
                        || c.getMedida().toLowerCase().contains(filtroLower))
                .collect(Collectors.toList());
        
        tablaCompras.setItems(FXCollections.observableArrayList(filtrados));
    }

    private void setupCierreConEscape() {
        if (cerrarDialogo.getScene() != null) {
            cerrarDialogo.getScene().setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    cerrarDialogo();
                }
            });
        }
        // Listener por si la escena aún no está lista al inicializar
        cerrarDialogo.sceneProperty().addListener((_, _, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                        cerrarDialogo();
                    }
                });
            }
        });
    }

    @FXML
    private void cerrarDialogo() {
        Stage stage = (Stage) cerrarDialogo.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}