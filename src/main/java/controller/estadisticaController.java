package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import model.EstadisticasService;
import utilities.Paths;
import utilities.SceneLoader;

import java.time.LocalDate;
import java.util.Map;

public class estadisticaController {

    private static final String ESTADISTICA_INGRESOS_EGRESOS = "Ingresos vs Egresos";
    private static final String ESTADISTICA_PRODUCTOS = "Productos más vendidos";

    @FXML private TabPane tabPane;
    @FXML private Tab tabIngresosEgresos;
    @FXML private Tab tabPieChart;
    @FXML private Tab tabProductos;

    @FXML private BarChart<String, Number> barChart;
    @FXML private BarChart<String, Number> barChartProductos;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private PieChart pieChart;

    @FXML private DatePicker datePickerDesde;
    @FXML private DatePicker datePickerHasta;
    @FXML private Button btnFiltrar;
    @FXML private ComboBox<String> comboFiltro;

    private EstadisticasService estadisticasService;

    @FXML
    public void initialize() {
        estadisticasService = new EstadisticasService();

        // Agregar opciones al ComboBox de filtro
        comboFiltro.getItems().addAll(ESTADISTICA_INGRESOS_EGRESOS, ESTADISTICA_PRODUCTOS);
        comboFiltro.setValue(ESTADISTICA_INGRESOS_EGRESOS);

        // Listener para cambios en el ComboBox de filtro
        comboFiltro.valueProperty().addListener((observable, oldValue, newValue) -> filtrarDatos(null));

        // Ocultar pestaña de productos al inicio
        tabPane.getTabs().remove(tabProductos);
    }

    @FXML
    void filtrarDatos(ActionEvent event) {
        LocalDate fechaDesde = datePickerDesde.getValue();
        LocalDate fechaHasta = datePickerHasta.getValue();

        if (fechaDesde == null || fechaHasta == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Por favor, seleccione ambas fechas.");
            return;
        }

        if (fechaDesde.isAfter(fechaHasta)) {
            showAlert(Alert.AlertType.ERROR, "Error", "La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.");
            return;
        }

        if (comboFiltro.getValue().equals(ESTADISTICA_INGRESOS_EGRESOS)) {
            mostrarIngresosEgresos(fechaDesde, fechaHasta);
        } else {
            mostrarProductosMasVendidos();
        }
    }

    private void mostrarIngresosEgresos(LocalDate fechaDesde, LocalDate fechaHasta) {
        actualizarVisibilidadPestañas(true);

        Map<String, Double> ingresosAgrupados = estadisticasService.obtenerIngresosPorFecha(fechaDesde, fechaHasta);
        Map<String, Double> egresosAgrupados = estadisticasService.obtenerEgresosPorFecha(fechaDesde, fechaHasta);

        XYChart.Series<String, Number> seriesIngresos = new XYChart.Series<>();
        seriesIngresos.setName("Ingresos");
        ingresosAgrupados.forEach((fecha, monto) ->
                seriesIngresos.getData().add(new XYChart.Data<>(fecha, monto))
        );

        XYChart.Series<String, Number> seriesEgresos = new XYChart.Series<>();
        seriesEgresos.setName("Egresos");
        egresosAgrupados.forEach((fecha, monto) ->
                seriesEgresos.getData().add(new XYChart.Data<>(fecha, monto))
        );

        barChart.getData().clear();
        barChart.getData().addAll(seriesIngresos, seriesEgresos);
        agregarTooltips(barChart);

        double totalIngresos = ingresosAgrupados.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalEgresos = egresosAgrupados.values().stream().mapToDouble(Double::doubleValue).sum();

        pieChart.getData().clear();
        pieChart.getData().add(new PieChart.Data("Ingresos", totalIngresos));
        pieChart.getData().add(new PieChart.Data("Egresos", totalEgresos));
    }

    private void mostrarProductosMasVendidos() {
        actualizarVisibilidadPestañas(false);
        cargarProductosMasVendidos();
    }

    private void actualizarVisibilidadPestañas(boolean mostrarIngresosEgresos) {
        if (mostrarIngresosEgresos) {
            tabPane.getTabs().remove(tabProductos);
            if (!tabPane.getTabs().contains(tabIngresosEgresos)) {
                tabPane.getTabs().addAll(tabIngresosEgresos, tabPieChart);
            }
        } else {
            tabPane.getTabs().removeAll(tabIngresosEgresos, tabPieChart);
            if (!tabPane.getTabs().contains(tabProductos)) {
                tabPane.getTabs().add(tabProductos);
            }
        }
    }

    private void cargarProductosMasVendidos() {
        XYChart.Series<String, Number> seriesProductos = new XYChart.Series<>();
        seriesProductos.setName("Productos más vendidos");

        Map<String, Integer> productosVendidos = estadisticasService.generarDatosPruebaProductosVendidos();
        productosVendidos.forEach((producto, cantidad) ->
                seriesProductos.getData().add(new XYChart.Data<>(producto, cantidad))
        );

        barChartProductos.getData().clear();
        barChartProductos.getData().add(seriesProductos);
        agregarTooltips(barChartProductos);
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/loginAdmin.css", true);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void agregarTooltips(BarChart<String, Number> barChart) {
        for (XYChart.Series<String, Number> series : barChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Tooltip.install(data.getNode(), new Tooltip(String.format("%s: %.2f", data.getXValue(), data.getYValue().doubleValue())));
            }
        }
    }
}
