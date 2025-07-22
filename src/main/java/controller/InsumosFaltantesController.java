package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.InsumoFaltante;
import persistence.dao.InsumoFaltanteDAO;

public class InsumosFaltantesController {
    @FXML private TableView<InsumoFaltante> tableInsumosFaltantes;
    @FXML private TableColumn<InsumoFaltante, String> colNombre;
    @FXML private TableColumn<InsumoFaltante, String> colCantidad;
    @FXML private TableColumn<InsumoFaltante, String> colUnidad;
    @FXML private TableColumn<InsumoFaltante, String> colProveedor;
    @FXML private Button btnCerrar;
    @FXML private TextField txtBuscar;

    private final InsumoFaltanteDAO insumoFaltanteDAO = new InsumoFaltanteDAO();
    private final ObservableList<InsumoFaltante> insumosFaltantes = FXCollections.observableArrayList();
    private final ObservableList<InsumoFaltante> insumosFaltantesFiltrados = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCatalogoInsumo() != null ? cellData.getValue().getCatalogoInsumo().getNombre() : ""));
        colCantidad.setCellValueFactory(cellData -> {
            double cantidad = cellData.getValue().getCantidadFaltante();
            String cantidadStr = (cantidad == Math.floor(cantidad)) ? String.format(java.util.Locale.ROOT, "%.0f", cantidad) : String.format(java.util.Locale.ROOT, "%.2f", cantidad);
            return new javafx.beans.property.SimpleStringProperty(cantidadStr);
        });
        colUnidad.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUnidad()));
        tableInsumosFaltantes.setItems(insumosFaltantesFiltrados);
        cargarInsumosFaltantes();
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> filtrarInsumosFaltantes(newVal));
    }

    private void cargarInsumosFaltantes() {
        insumosFaltantes.clear();
        // Solo mostrar insumos faltantes NO resueltos
        insumosFaltantes.addAll(
                insumoFaltanteDAO.findAll().stream()
                        .filter(i -> !i.isResuelto())
                        .toList()
        );
        filtrarInsumosFaltantes(txtBuscar != null ? txtBuscar.getText() : "");
    }

    private void filtrarInsumosFaltantes(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            insumosFaltantesFiltrados.setAll(insumosFaltantes);
            return;
        }
        String filtroLower = filtro.toLowerCase();
        insumosFaltantesFiltrados.setAll(
                insumosFaltantes.stream().filter(i -> {
                    String nombre = i.getCatalogoInsumo() != null ? i.getCatalogoInsumo().getNombre() : "";
                    return nombre.toLowerCase().contains(filtroLower);
                }).toList()
        );
    }

    @FXML
    private void handleCerrar(ActionEvent event) {
        Stage stage = (Stage) tableInsumosFaltantes.getScene().getWindow();
        stage.close();
    }
}
