package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.HistorialCompra;
import persistence.dao.HistorialCompraDAO;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.control.TextField;

public class HistorialComprasDialogController {
    @FXML private TableView<HistorialCompra> tablaCompras;
    @FXML private TableColumn<HistorialCompra, String> colInsumo;
    @FXML private TableColumn<HistorialCompra, Double> colCantidad;
    @FXML private TableColumn<HistorialCompra, String> colMedida;
    @FXML private TableColumn<HistorialCompra, Double> colPrecio;
    @FXML private TableColumn<HistorialCompra, String> colProveedor;
    @FXML private TableColumn<HistorialCompra, String> colFechaCompra;
    @FXML private Button cerrarDialogo;
    @FXML private TextField txtBuscar;

    private final HistorialCompraDAO historialCompraDAO = new HistorialCompraDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @FXML
    public void initialize() {
        colInsumo.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getInsumo()));
        colCantidad.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getCantidad()).asObject());
        colMedida.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMedida()));
        colPrecio.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrecio()).asObject());
        colProveedor.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProveedor()));
        colFechaCompra.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFechaCompra() != null ? cellData.getValue().getFechaCompra().format(dateFormatter) : "-"));
        cargarCompras();
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            filtrarCompras(newVal);
        });
    }

    public void cargarCompras() {
        List<HistorialCompra> compras = historialCompraDAO.findAll();
        ObservableList<HistorialCompra> data = FXCollections.observableArrayList(compras);
        tablaCompras.setItems(data);
    }

    private void filtrarCompras(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            cargarCompras();
            return;
        }
        String filtroLower = filtro.toLowerCase();
        List<HistorialCompra> compras = historialCompraDAO.findAll();
        List<HistorialCompra> filtrados = compras.stream()
                .filter(c -> c.getInsumo().toLowerCase().contains(filtroLower)
                        || c.getProveedor().toLowerCase().contains(filtroLower)
                        || c.getMedida().toLowerCase().contains(filtroLower))
                .collect(java.util.stream.Collectors.toList());
        tablaCompras.setItems(FXCollections.observableArrayList(filtrados));
    }

    @FXML
    private void cerrarDialogo() {
        Stage stage = (Stage) tablaCompras.getScene().getWindow();
        stage.close();
    }
}
