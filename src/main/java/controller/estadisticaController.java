package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import service.EstadisticasService;
import utilities.Paths;
import utilities.SceneLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFChart;

public class estadisticaController {

    private static final String ESTADISTICA_INGRESOS_EGRESOS = "Ingresos vs Egresos";
    private static final String ESTADISTICA_PRODUCTOS = "Productos más vendidos";

    @FXML private TabPane tabPane;
    @FXML private Tab tabIngresosEgresos;
    @FXML private Tab tabPieChart;
    @FXML private Tab tabProductos;
    @FXML private Tab tabCompararMeses;
    @FXML private Tab tabPieProductos;

    @FXML private BarChart<String, Number> barChart;
    @FXML private BarChart<String, Number> barChartProductos;
    @FXML private BarChart<String, Number> barChartMes1;
    @FXML private BarChart<String, Number> barChartMes2;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private PieChart pieChart;
    @FXML private PieChart pieChartProductos;

    @FXML private DatePicker datePickerDesde;
    @FXML private DatePicker datePickerHasta;
    @FXML private Button btnFiltrar;
    @FXML private ComboBox<String> comboFiltro;
    @FXML private Button btnHoy;
    @FXML private Button btnEsteMes;
    @FXML private Button btnEsteAnio;
    @FXML private Label labelResumen;
    @FXML private ComboBox<String> comboMes1;
    @FXML private ComboBox<String> comboMes2;
    @FXML private Button btnCompararMeses;
    @FXML private Label labelResumenMes1;
    @FXML private Label labelResumenMes2;
    @FXML private Label labelResumenComparacion;
    @FXML private Button btnGenerarReportes;

    private EstadisticasService estadisticasService;

    // Permisos del usuario actual
    private final java.util.List<String> permisos = model.SessionContext.getInstance().getPermisos();
    private final boolean puedeCrear = permisos != null && permisos.contains("Estadísticas-crear");

    @FXML
    public void initialize() {
        estadisticasService = new EstadisticasService();

        // Agregar opciones al ComboBox de filtro
        comboFiltro.getItems().addAll(ESTADISTICA_INGRESOS_EGRESOS, ESTADISTICA_PRODUCTOS);
        comboFiltro.setValue(ESTADISTICA_INGRESOS_EGRESOS);

        // Listener para cambios en el ComboBox de filtro
        comboFiltro.valueProperty().addListener((observable, oldValue, newValue) -> {
            filtrarDatos(null);
            if (newValue.equals(ESTADISTICA_INGRESOS_EGRESOS)) {
                if (!tabPane.getTabs().contains(tabCompararMeses)) {
                    tabPane.getTabs().add(tabCompararMeses);
                }
            } else {
                tabPane.getTabs().remove(tabCompararMeses);
            }
        });

        // Ocultar pestañas de productos al inicio
        tabPane.getTabs().remove(tabProductos);
        tabPane.getTabs().remove(tabPieProductos);

        // Mejorar apariencia de los gráficos
        barChart.setStyle("-fx-background-color: #fffdfc; -fx-border-color: #B70505; -fx-border-width: 2; -fx-background-radius: 12; -fx-border-radius: 12;");
        barChartProductos.setStyle("-fx-background-color: #fffdfc; -fx-border-color: #B70505; -fx-border-width: 2; -fx-background-radius: 12; -fx-border-radius: 12;");
        pieChart.setStyle("-fx-background-color: #fffdfc; -fx-border-color: #B70505; -fx-border-width: 2; -fx-background-radius: 12; -fx-border-radius: 12;");
        xAxis.setTickLabelFont(javafx.scene.text.Font.font("Inter", 14));
        yAxis.setTickLabelFont(javafx.scene.text.Font.font("Inter", 14));
        barChart.setLegendVisible(true);
        barChartProductos.setLegendVisible(false);
        pieChart.setLegendVisible(true);

        // Opcional: inicializar los botones de rango rápido si están en el FXML
        if (btnHoy != null) btnHoy.setOnAction(e -> setRangoHoy());
        if (btnEsteMes != null) btnEsteMes.setOnAction(e -> setRangoMes());
        if (btnEsteAnio != null) btnEsteAnio.setOnAction(e -> setRangoAnio());

        // Listener para limpiar gráficos y cargar PieChart de productos al cambiar de tab
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tabPieChart) {
                barChart.getData().clear();
            } else if (newTab == tabIngresosEgresos) {
                pieChart.getData().clear();
            } else if (newTab == tabProductos) {
                barChart.getData().clear();
                pieChart.getData().clear();
            } else if (newTab == tabPieProductos) {
                cargarPieChartProductos();
            }
        });

        // Inicializar combos de meses para comparación
        java.time.format.TextStyle style = java.time.format.TextStyle.FULL;
        java.util.Locale locale = java.util.Locale.getDefault();
        comboMes1.getItems().clear();
        comboMes2.getItems().clear();
        for (int m = 1; m <= 12; m++) {
            String nombreMes = java.time.Month.of(m).getDisplayName(style, locale) + " " + java.time.LocalDate.now().getYear();
            comboMes1.getItems().add(nombreMes);
            comboMes2.getItems().add(nombreMes);
        }
        comboMes1.setValue(comboMes1.getItems().get(0));
        comboMes2.setValue(comboMes2.getItems().get(1));
        btnCompararMeses.setOnAction(e -> compararMeses());

        // Deshabilitar el botón de exportar si no hay fechas seleccionadas
        btnFiltrar.setDisable(true);
        javafx.beans.value.ChangeListener<Object> fechasListener = (obs, oldVal, newVal) -> {
            boolean fechasOk = datePickerDesde.getValue() != null && datePickerHasta.getValue() != null;
            btnFiltrar.setDisable(!fechasOk);
        };
        datePickerDesde.valueProperty().addListener(fechasListener);
        datePickerHasta.valueProperty().addListener(fechasListener);

        // Deshabilitar el botón de generar reportes si no hay fechas seleccionadas o no tiene permiso
        btnGenerarReportes.setDisable(!puedeCrear);
        javafx.beans.value.ChangeListener<Object> fechasListenerReporte = (obs, oldVal, newVal) -> {
            boolean fechasOk = datePickerDesde.getValue() != null && datePickerHasta.getValue() != null;
            btnGenerarReportes.setDisable(!fechasOk || !puedeCrear);
        };
        datePickerDesde.valueProperty().addListener(fechasListenerReporte);
        datePickerHasta.valueProperty().addListener(fechasListenerReporte);
        btnGenerarReportes.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                boolean fechasOk = datePickerDesde.getValue() != null && datePickerHasta.getValue() != null;
                btnGenerarReportes.setDisable(!fechasOk || !puedeCrear);
            }
        });
    }

    private void cargarProductosMasVendidos() {
        if (datePickerDesde.getValue() == null || datePickerHasta.getValue() == null) {
            barChartProductos.getData().clear();
            return;
        }
        XYChart.Series<String, Number> seriesProductos = new XYChart.Series<>();
        seriesProductos.setName("Productos más vendidos");
        Map<String, Integer> productosVendidos = estadisticasService.obtenerProductosVendidosPorCantidad(datePickerDesde.getValue(), datePickerHasta.getValue());
        productosVendidos.forEach((producto, cantidad) ->
                seriesProductos.getData().add(new XYChart.Data<>(producto, cantidad))
        );
        barChartProductos.getData().clear();
        barChartProductos.getData().add(seriesProductos);
        agregarTooltips(barChartProductos, false);
        animarBarras(barChartProductos);
    }

    private void cargarPieChartProductos() {
        if (datePickerDesde.getValue() == null || datePickerHasta.getValue() == null) {
            pieChartProductos.getData().clear();
            return;
        }
        pieChartProductos.getData().clear();
        Map<String, Integer> productosVendidos = estadisticasService.obtenerProductosVendidosPorCantidad(datePickerDesde.getValue(), datePickerHasta.getValue());
        double total = productosVendidos.values().stream().mapToInt(Integer::intValue).sum();
        productosVendidos.forEach((producto, cantidad) -> {
            PieChart.Data data = new PieChart.Data(producto, cantidad);
            pieChartProductos.getData().add(data);
        });
        for (PieChart.Data data : pieChartProductos.getData()) {
            double porcentaje = total > 0 ? (data.getPieValue() / total) * 100 : 0;
            String texto = String.format("%s: %d (%.1f%%)", data.getName(), (int)data.getPieValue(), porcentaje);
            Tooltip.install(data.getNode(), new Tooltip(texto));
        }
    }

    @FXML
    void filtrarDatos(ActionEvent event) {
        LocalDate fechaDesde = datePickerDesde.getValue();
        LocalDate fechaHasta = datePickerHasta.getValue();

        // Si las fechas son nulas y el evento es null (cambio de filtro/tab), limpiar gráficos y resúmenes, no cargar datos ni mostrar alertas
        if ((fechaDesde == null || fechaHasta == null) && event == null) {
            barChart.getData().clear();
            pieChart.getData().clear();
            barChartProductos.getData().clear();
            pieChartProductos.getData().clear();
            labelResumen.setText("");
            labelResumenMes1.setText("");
            labelResumenMes2.setText("");
            labelResumenComparacion.setText("");
            return;
        }

        // Solo mostrar error si el evento es por botón (no por cambio de filtro/tab)
        if (event != null) {
            if (fechaDesde == null || fechaHasta == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Fechas incompletas", "Por favor, seleccione ambas fechas.");
                if (fechaDesde == null) datePickerDesde.setStyle("-fx-border-color: #E02020; -fx-border-width: 2;");
                if (fechaHasta == null) datePickerHasta.setStyle("-fx-border-color: #E02020; -fx-border-width: 2;");
                return;
            } else {
                datePickerDesde.setStyle("");
                datePickerHasta.setStyle("");
            }

            if (fechaDesde.isAfter(fechaHasta)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Rango de fechas inválido", "La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.");
                datePickerDesde.setStyle("-fx-border-color: #E02020; -fx-border-width: 2;");
                datePickerHasta.setStyle("-fx-border-color: #E02020; -fx-border-width: 2;");
                return;
            }
        }

        if (comboFiltro.getValue().equals(ESTADISTICA_INGRESOS_EGRESOS)) {
            mostrarIngresosEgresos(fechaDesde, fechaHasta);
        } else {
            mostrarProductosMasVendidos();
        }
        // Habilitar/deshabilitar btnGenerarReportes según fechas y permiso
        boolean fechasOk = fechaDesde != null && fechaHasta != null;
        btnGenerarReportes.setDisable(!fechasOk || !puedeCrear);
    }

    private void mostrarIngresosEgresos(LocalDate fechaDesde, LocalDate fechaHasta) {
        actualizarVisibilidadPestañas(true);

        Map<String, Double> ingresosAgrupados = estadisticasService.obtenerIngresosPorFecha(fechaDesde, fechaHasta);
        Map<String, Double> egresosAgrupados = estadisticasService.obtenerEgresosPorFecha(fechaDesde, fechaHasta);

        // Mostrar ingresos separados por fuente
        Map<String, Double> ingresosSeparados = estadisticasService.obtenerIngresosSeparadosPorFuente(fechaDesde, fechaHasta);
        double ingresosPedidos = ingresosSeparados.getOrDefault("pedidos", 0.0);
        double ingresosEventos = ingresosSeparados.getOrDefault("eventos", 0.0);
        System.out.println("Ingresos por pedidos: " + ingresosPedidos);
        System.out.println("Ingresos por eventos: " + ingresosEventos);

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
        java.util.List<XYChart.Series<String, Number>> seriesList = new java.util.ArrayList<>();
        seriesList.add(seriesIngresos);
        seriesList.add(seriesEgresos);
        barChart.getData().addAll(seriesList);
        agregarTooltips(barChart, true);
        animarBarras(barChart);
        // NO forzar colores manualmente, dejar que JavaFX y el CSS los asignen para sincronizar con la leyenda

        double totalIngresos = ingresosAgrupados.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalEgresos = egresosAgrupados.values().stream().mapToDouble(Double::doubleValue).sum();
        double diferencia = totalIngresos - totalEgresos;

        // Calcular período anterior
        long dias = java.time.temporal.ChronoUnit.DAYS.between(fechaDesde, fechaHasta) + 1;
        LocalDate anteriorDesde = fechaDesde.minusDays(dias);
        LocalDate anteriorHasta = fechaDesde.minusDays(1);
        Map<String, Double> ingresosPrev = estadisticasService.obtenerIngresosPorFecha(anteriorDesde, anteriorHasta);
        Map<String, Double> egresosPrev = estadisticasService.obtenerEgresosPorFecha(anteriorDesde, anteriorHasta);
        double totalIngresosPrev = ingresosPrev.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalEgresosPrev = egresosPrev.values().stream().mapToDouble(Double::doubleValue).sum();
        String varIng = (totalIngresosPrev > 0)
                ? String.format("%+.1f%%", 100.0 * (totalIngresos - totalIngresosPrev) / totalIngresosPrev)
                : "N/A";
        String varEgr = (totalEgresosPrev > 0)
                ? String.format("%+.1f%%", 100.0 * (totalEgresos - totalEgresosPrev) / totalEgresosPrev)
                : "N/A";
        String resumen = String.format("Total ingresos: $%,.2f | Total egresos: $%,.2f | Diferencia: $%,.2f | Ingresos pedidos: $%,.2f | Ingresos eventos: $%,.2f | Variación ingresos: %s | Variación egresos: %s",
                totalIngresos, totalEgresos, diferencia, ingresosPedidos, ingresosEventos, varIng, varEgr);
        labelResumen.setText(resumen);

        pieChart.getData().clear();
        PieChart.Data ingresosData = new PieChart.Data("Ingresos", totalIngresos);
        PieChart.Data egresosData = new PieChart.Data("Egresos", totalEgresos);
        pieChart.getData().addAll(ingresosData, egresosData);
        agregarTooltipsPie(pieChart);
        // Forzar colores del PieChart (esto sí, porque JavaFX no sincroniza leyenda en PieChart)
        for (PieChart.Data data : pieChart.getData()) {
            if (data.getName().equals("Ingresos")) {
                data.getNode().setStyle("-fx-pie-color: #4CAF50;");
            } else if (data.getName().equals("Egresos")) {
                data.getNode().setStyle("-fx-pie-color: #F44336;");
            }
        }
    }

    private void mostrarProductosMasVendidos() {
        actualizarVisibilidadPestañas(false);
        cargarProductosMasVendidos();
        cargarPieChartProductos(); // <-- Agregado para actualizar el gráfico de pastel al filtrar
    }

    private void actualizarVisibilidadPestañas(boolean mostrarIngresosEgresos) {
        if (mostrarIngresosEgresos) {
            tabPane.getTabs().remove(tabProductos);
            tabPane.getTabs().remove(tabPieProductos);
            if (!tabPane.getTabs().contains(tabIngresosEgresos)) {
                tabPane.getTabs().add(tabIngresosEgresos);
            }
            if (!tabPane.getTabs().contains(tabPieChart)) {
                tabPane.getTabs().add(tabPieChart);
            }
            if (!tabPane.getTabs().contains(tabCompararMeses)) {
                tabPane.getTabs().add(tabCompararMeses);
            }
        } else {
            tabPane.getTabs().removeAll(tabIngresosEgresos, tabPieChart, tabCompararMeses);
            if (!tabPane.getTabs().contains(tabProductos)) {
                tabPane.getTabs().add(tabProductos);
            }
            if (!tabPane.getTabs().contains(tabPieProductos)) {
                tabPane.getTabs().add(tabPieProductos);
            }
        }
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/loginAdmin.css", false);
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header); // Mensaje principal como encabezado
        alert.setContentText(content); // Mensaje secundario abajo
        alert.showAndWait();
    }

    // Sobrecarga para mantener compatibilidad con llamadas antiguas
    private void showAlert(Alert.AlertType type, String title, String header) {
        showAlert(type, title, header, "");
    }

    private void agregarTooltips(BarChart<String, Number> barChart, boolean esMoneda) {
        for (XYChart.Series<String, Number> series : barChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                String texto = esMoneda
                        ? String.format("%s: $%,.2f", data.getXValue(), data.getYValue().doubleValue())
                        : String.format("%s: %d", data.getXValue(), data.getYValue().intValue());
                Tooltip.install(data.getNode(), new Tooltip(texto));
            }
        }
    }

    private void agregarTooltipsPie(PieChart pieChart) {
        double total = pieChart.getData().stream().mapToDouble(PieChart.Data::getPieValue).sum();
        for (PieChart.Data data : pieChart.getData()) {
            double porcentaje = total > 0 ? (data.getPieValue() / total) * 100 : 0;
            String texto = String.format("%s: $%,.2f (%.1f%%)", data.getName(), data.getPieValue(), porcentaje);
            Tooltip.install(data.getNode(), new Tooltip(texto));
        }
    }

    private void animarBarras(BarChart<String, Number> barChart) {
        for (XYChart.Series<String, Number> series : barChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                data.getNode().setScaleY(0);
                javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(700), data.getNode());
                st.setToY(1);
                st.setFromY(0);
                st.play();
            }
        }
    }

    private void setRangoHoy() {
        java.time.LocalDate hoy = java.time.LocalDate.now();
        datePickerDesde.setValue(hoy);
        datePickerHasta.setValue(hoy);
        filtrarDatos(null);
    }

    private void setRangoMes() {
        java.time.LocalDate hoy = java.time.LocalDate.now();
        java.time.LocalDate primeroMes = hoy.withDayOfMonth(1);
        datePickerDesde.setValue(primeroMes);
        datePickerHasta.setValue(hoy);
        filtrarDatos(null);
    }

    private void setRangoAnio() {
        java.time.LocalDate hoy = java.time.LocalDate.now();
        java.time.LocalDate primeroAnio = hoy.withDayOfYear(1);
        datePickerDesde.setValue(primeroAnio);
        datePickerHasta.setValue(hoy);
        filtrarDatos(null);
    }

    private void compararMeses() {
        // Obtener meses seleccionados
        int year = java.time.LocalDate.now().getYear();
        int mes1 = comboMes1.getSelectionModel().getSelectedIndex() + 1;
        int mes2 = comboMes2.getSelectionModel().getSelectedIndex() + 1;
        if (mes1 < 1 || mes2 < 1) return;
        java.time.LocalDate desde1 = java.time.LocalDate.of(year, mes1, 1);
        java.time.LocalDate hasta1 = desde1.withDayOfMonth(desde1.lengthOfMonth());
        java.time.LocalDate desde2 = java.time.LocalDate.of(year, mes2, 1);
        java.time.LocalDate hasta2 = desde2.withDayOfMonth(desde2.lengthOfMonth());
        // Datos mes 1
        Map<String, Double> ingresos1 = estadisticasService.obtenerIngresosPorFecha(desde1, hasta1);
        Map<String, Double> egresos1 = estadisticasService.obtenerEgresosPorFecha(desde1, hasta1);
        double totalIng1 = ingresos1.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalEgr1 = egresos1.values().stream().mapToDouble(Double::doubleValue).sum();
        // Datos mes 2
        Map<String, Double> ingresos2 = estadisticasService.obtenerIngresosPorFecha(desde2, hasta2);
        Map<String, Double> egresos2 = estadisticasService.obtenerEgresosPorFecha(desde2, hasta2);
        double totalIng2 = ingresos2.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalEgr2 = egresos2.values().stream().mapToDouble(Double::doubleValue).sum();
        // Graficar mes 1
        XYChart.Series<String, Number> sIng1 = new XYChart.Series<>();
        sIng1.setName("Ingresos");
        ingresos1.forEach((fecha, monto) -> sIng1.getData().add(new XYChart.Data<>(fecha, monto)));
        XYChart.Series<String, Number> sEgr1 = new XYChart.Series<>();
        sEgr1.setName("Egresos");
        egresos1.forEach((fecha, monto) -> sEgr1.getData().add(new XYChart.Data<>(fecha, monto)));
        barChartMes1.getData().clear();
        java.util.List<XYChart.Series<String, Number>> seriesMes1 = new java.util.ArrayList<>();
        seriesMes1.add(sIng1);
        seriesMes1.add(sEgr1);
        barChartMes1.getData().addAll(seriesMes1);
        agregarTooltips(barChartMes1, true);
        // Graficar mes 2
        XYChart.Series<String, Number> sIng2 = new XYChart.Series<>();
        sIng2.setName("Ingresos");
        ingresos2.forEach((fecha, monto) -> sIng2.getData().add(new XYChart.Data<>(fecha, monto)));
        XYChart.Series<String, Number> sEgr2 = new XYChart.Series<>();
        sEgr2.setName("Egresos");
        egresos2.forEach((fecha, monto) -> sEgr2.getData().add(new XYChart.Data<>(fecha, monto)));
        barChartMes2.getData().clear();
        java.util.List<XYChart.Series<String, Number>> seriesMes2 = new java.util.ArrayList<>();
        seriesMes2.add(sIng2);
        seriesMes2.add(sEgr2);
        barChartMes2.getData().addAll(seriesMes2);
        agregarTooltips(barChartMes2, true);
        // Resúmenes
        labelResumenMes1.setText(String.format("Ingresos: $%,.2f | Egresos: $%,.2f | Dif: $%,.2f", totalIng1, totalEgr1, totalIng1-totalEgr1));
        labelResumenMes2.setText(String.format("Ingresos: $%,.2f | Egresos: $%,.2f | Dif: $%,.2f", totalIng2, totalEgr2, totalIng2-totalEgr2));
        // Comparación
        String varIng = (totalIng1 > 0) ? String.format("%+.1f%%", 100.0 * (totalIng2-totalIng1)/totalIng1) : "N/A";
        String varEgr = (totalEgr1 > 0) ? String.format("%+.1f%%", 100.0 * (totalEgr2-totalEgr1)/totalEgr1) : "N/A";
        labelResumenComparacion.setText(String.format("Variación ingresos: %s | Variación egresos: %s", varIng, varEgr));
    }

    @FXML
    public void exportarExcel(ActionEvent event) {
        // Configurar FileChooser con escritorio como ubicación inicial
        String userHome = System.getProperty("user.home");
        String desktopPath = userHome + "\\Desktop";
        String fechaActual = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String fileName;
        if (comboFiltro.getValue().equals(ESTADISTICA_INGRESOS_EGRESOS)) {
            fileName = "reporte-ingresos-egresos-" + fechaActual + ".xlsx";
        } else {
            fileName = "reporte-productos-" + fechaActual + ".xlsx";
        }
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Guardar reporte como Excel");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Archivo Excel (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialDirectory(new java.io.File(desktopPath));
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(tabPane.getScene().getWindow());
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte");
            int rowIdx = 0;
            LocalDate desde = datePickerDesde.getValue();
            LocalDate hasta = datePickerHasta.getValue();
            if (desde == null || hasta == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Fechas incompletas", "Debe seleccionar ambas fechas antes de exportar el reporte.");
                return;
            }

            // --- ESTILOS ---
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short)11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setAlignment(HorizontalAlignment.LEFT);

            // --- ESTILO PARA TÍTULOS DE SECCIÓN ---
            org.apache.poi.ss.usermodel.CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 13);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            titleStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            titleStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            titleStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            titleStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            titleStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            titleStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

            if (comboFiltro.getValue().equals(ESTADISTICA_INGRESOS_EGRESOS)) {
                // Obtener ingresos y egresos agrupados por fecha para el gráfico
                Map<String, Double> ingresosAgrupados = estadisticasService.obtenerIngresosPorFecha(desde, hasta);
                Map<String, Double> egresosAgrupados = estadisticasService.obtenerEgresosPorFecha(desde, hasta);
                // --- TÍTULO INGRESOS ---
                Row ingresosTitleRow = sheet.createRow(rowIdx++);
                org.apache.poi.ss.usermodel.Cell ingresosTitleCell = ingresosTitleRow.createCell(0);
                ingresosTitleCell.setCellValue("INGRESOS");
                ingresosTitleCell.setCellStyle(titleStyle); // Usar la instancia definida arriba
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                        ingresosTitleRow.getRowNum(), ingresosTitleRow.getRowNum(), 0, 6));

                // --- INGRESOS ---
                Row header = sheet.createRow(rowIdx++);
                String[] ingresosHeaders = {"Fecha", "Nº Pedido", "Cliente", "Producto", "Cantidad", "Precio Unitario", "Total Pedido"};
                for (int i = 0; i < ingresosHeaders.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = header.createCell(i);
                    cell.setCellValue(ingresosHeaders[i]);
                    cell.setCellStyle(headerStyle);
                }
                java.util.List<model.IngresoDetallado> ingresosDetallados = estadisticasService.obtenerIngresosDetallados(desde, hasta);
                for (model.IngresoDetallado ingreso : ingresosDetallados) {
                    Row row = sheet.createRow(rowIdx++);
                    java.util.List<Object> valuesList = new java.util.ArrayList<>();
                    valuesList.add(ingreso.getFecha() != null ? ingreso.getFecha().toString() : "");
                    valuesList.add(ingreso.getNumeroPedido());
                    valuesList.add(ingreso.getCliente());
                    valuesList.add(ingreso.getProducto());
                    valuesList.add(ingreso.getCantidad());
                    valuesList.add(ingreso.getPrecioUnitario());
                    valuesList.add(ingreso.getTotalPedido());
                    Object[] values = valuesList.toArray(new Object[0]);
                    for (int i = 0; i < values.length; i++) {
                        org.apache.poi.ss.usermodel.Cell cell = row.createCell(i);
                        if (values[i] instanceof Number) {
                            cell.setCellValue(((Number) values[i]).doubleValue());
                        } else {
                            cell.setCellValue(values[i] != null ? values[i].toString() : "");
                        }
                        cell.setCellStyle(cellStyle);
                    }
                }
                rowIdx++;
                // --- TÍTULO EGRESOS ---
                Row egresosTitleRow = sheet.createRow(rowIdx++);
                org.apache.poi.ss.usermodel.Cell egresosTitleCell = egresosTitleRow.createCell(0);
                egresosTitleCell.setCellValue("EGRESOS");
                egresosTitleCell.setCellStyle(titleStyle);
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                        egresosTitleRow.getRowNum(), egresosTitleRow.getRowNum(), 0, 5));

                // --- EGRESOS ---
                Row egresoHeader = sheet.createRow(rowIdx++);
                String[] egresosHeaders = {"Fecha", "Insumo", "Cantidad", "Medida", "Proveedor", "Total Egreso"};
                for (int i = 0; i < egresosHeaders.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = egresoHeader.createCell(i);
                    cell.setCellValue(egresosHeaders[i]);
                    cell.setCellStyle(headerStyle);
                }
                java.util.List<model.EgresoDetallado> egresosDetallados = estadisticasService.obtenerEgresosDetallados(desde, hasta);
                for (model.EgresoDetallado egreso : egresosDetallados) {
                    Row row = sheet.createRow(rowIdx++);
                    java.util.List<Object> valuesList = new java.util.ArrayList<>();
                    valuesList.add(egreso.getFechaCompra() != null ? egreso.getFechaCompra().toString() : "");
                    valuesList.add(egreso.getInsumo());
                    valuesList.add(egreso.getCantidad());
                    valuesList.add(egreso.getMedida());
                    valuesList.add(egreso.getProveedor());
                    valuesList.add(egreso.getTotalEgreso());
                    Object[] values = valuesList.toArray(new Object[0]);
                    for (int i = 0; i < values.length; i++) {
                        org.apache.poi.ss.usermodel.Cell cell = row.createCell(i);
                        if (values[i] instanceof Number) {
                            cell.setCellValue(((Number) values[i]).doubleValue());
                        } else {
                            cell.setCellValue(values[i] != null ? values[i].toString() : "");
                        }
                        cell.setCellStyle(cellStyle);
                    }
                }
                rowIdx++;
                // --- TÍTULO EVENTOS REALIZADOS ---
                Row eventosTitleRow = sheet.createRow(rowIdx++);
                org.apache.poi.ss.usermodel.Cell eventosTitleCell = eventosTitleRow.createCell(0);
                eventosTitleCell.setCellValue("EVENTOS REALIZADOS");
                eventosTitleCell.setCellStyle(titleStyle);
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                        eventosTitleRow.getRowNum(), eventosTitleRow.getRowNum(), 0, 3));
                // --- TABLA EVENTOS ---
                Row eventosHeader = sheet.createRow(rowIdx++);
                String[] eventosHeaders = {"Fecha", "Nombre", "Estado", "Monto"};
                for (int i = 0; i < eventosHeaders.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = eventosHeader.createCell(i);
                    cell.setCellValue(eventosHeaders[i]);
                    cell.setCellStyle(headerStyle);
                }
                java.util.List<model.Evento> eventos = estadisticasService.obtenerEventosRealizados(desde, hasta);
                for (model.Evento evento : eventos) {
                    Row row = sheet.createRow(rowIdx++);
                    java.util.List<Object> valuesList = new java.util.ArrayList<>();
                    valuesList.add(evento.getFecha_evento() != null ? evento.getFecha_evento().toString() : "");
                    valuesList.add(evento.getNombre_evento());
                    valuesList.add(evento.getEstado());
                    valuesList.add(evento.getPresupuesto() != null ? evento.getPresupuesto().doubleValue() : 0.0);
                    Object[] values = valuesList.toArray(new Object[0]);
                    for (int i = 0; i < values.length; i++) {
                        org.apache.poi.ss.usermodel.Cell cell = row.createCell(i);
                        if (values[i] instanceof Number) {
                            cell.setCellValue(((Number) values[i]).doubleValue());
                        } else {
                            cell.setCellValue(values[i] != null ? values[i].toString() : "");
                        }
                        cell.setCellStyle(cellStyle);
                    }
                }
            } else {
                // --- TÍTULO PRODUCTOS MÁS VENDIDOS ---
                Row productosTitleRow = sheet.createRow(rowIdx++);
                org.apache.poi.ss.usermodel.Cell productosTitleCell = productosTitleRow.createCell(0);
                productosTitleCell.setCellValue("PRODUCTOS MÁS VENDIDOS");
                productosTitleCell.setCellStyle(titleStyle);
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                        productosTitleRow.getRowNum(), productosTitleRow.getRowNum(), 0, 1));
                // --- TABLA PRODUCTOS MÁS VENDIDOS ---
                Row productosHeader = sheet.createRow(rowIdx++);
                String[] productosHeaders = {"Producto", "Cantidad Vendida"};
                for (int i = 0; i < productosHeaders.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = productosHeader.createCell(i);
                    cell.setCellValue(productosHeaders[i]);
                    cell.setCellStyle(headerStyle);
                }
                Map<String, Integer> productosVendidos = estadisticasService.obtenerProductosVendidosPorCantidad(desde, hasta);
                for (Map.Entry<String, Integer> entry : productosVendidos.entrySet()) {
                    Row row = sheet.createRow(rowIdx++);
                    org.apache.poi.ss.usermodel.Cell cell0 = row.createCell(0);
                    cell0.setCellValue(entry.getKey());
                    cell0.setCellStyle(cellStyle);
                    org.apache.poi.ss.usermodel.Cell cell1 = row.createCell(1);
                    cell1.setCellValue(entry.getValue());
                    cell1.setCellStyle(cellStyle);
                }
                // --- GRÁFICO DE PASTEL: SOLO en reporte-productos ---
                if (productosVendidos.size() > 1) {
                    rowIdx++;
                    int chartRowStart = productosHeader.getRowNum();
                    int chartRowEnd = rowIdx - 2;
                    int chartColStart = 0;
                    int chartColEnd = 1;
                    XSSFDrawing drawing = ((XSSFSheet) sheet).createDrawingPatriarch();
                    org.apache.poi.ss.usermodel.ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 3, rowIdx, 10, rowIdx + 16);
                    XSSFChart chart = drawing.createChart(anchor);
                    chart.setTitleText("Distribución de productos vendidos");
                    chart.setTitleOverlay(false);
                    XDDFChartLegend legend = chart.getOrAddLegend();
                    legend.setPosition(LegendPosition.RIGHT);
                    XDDFDataSource<String> dsProductos = XDDFDataSourcesFactory.fromStringCellRange(
                            (XSSFSheet) sheet,
                            new org.apache.poi.ss.util.CellRangeAddress(chartRowStart + 1, chartRowEnd, chartColStart, chartColStart));
                    XDDFNumericalDataSource<Double> dsCantidad = XDDFDataSourcesFactory.fromNumericCellRange(
                            (XSSFSheet) sheet,
                            new org.apache.poi.ss.util.CellRangeAddress(chartRowStart + 1, chartRowEnd, chartColEnd, chartColEnd));
                    XDDFChartData data = chart.createData(ChartTypes.PIE, null, null);
                    data.addSeries(dsProductos, dsCantidad);
                    chart.plot(data);
                }
            }
            for (int i = 0; i < 7; i++) sheet.autoSizeColumn(i);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            showAlert(Alert.AlertType.CONFIRMATION, "Éxito", "Exportación exitosa", "El reporte se guardó correctamente");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error al exportar", "No se pudo guardar el archivo", e.getMessage());
        }
    }
}
