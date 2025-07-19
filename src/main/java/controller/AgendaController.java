package controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import model.Agenda;
import persistence.dao.TrabajadorDAO;
import persistence.dao.AgendaDAO;
import javafx.scene.control.TextArea;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.scene.text.Text;
import utilities.Paths;
import utilities.SceneLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class AgendaController {
    // =================== ATRIBUTOS PRINCIPALES ===================
    @FXML private Pane paneAgregarTareaPendiente;
    @FXML private Spinner<Integer> Hora;
    @FXML private Spinner<Integer> Minutos;
    @FXML private RadioButton radioNo;
    @FXML private RadioButton radioSi;
    @FXML private ComboBox<String> cmbEmpleadoTarea;
    @FXML private DatePicker dateDiaTarea;
    @FXML private TextArea tareaPendiente;
    @FXML private GridPane gridDias;
    @FXML private GridPane gridTareas;
    @FXML private Label txtFechaSemanal;
    @FXML private LocalDate today = LocalDate.now();
    @FXML private GridPane gridPlanillaSemanal;
    @FXML private Button btnNuevaTarea;

    // =================== DAOs Y SERVICIOS ===================
    private final AgendaDAO agendaDAO = new AgendaDAO();
    private final TrabajadorDAO trabajadorDAO = new TrabajadorDAO();

    // =================== MÉTODOS DE INICIALIZACIÓN ===================
    @FXML
    private void initialize() {
        paneAgregarTareaPendiente.setVisible(false);
        radioNo.setSelected(true);
        cargarNombresEnComboBox();
        sincronizarColumnasGrid();
        setDaysInWeek(today.with(DayOfWeek.MONDAY));
        cargarTareasSemana(today.with(DayOfWeek.MONDAY));
        ToggleGroup toggleGroup = new ToggleGroup();
        radioSi.setToggleGroup(toggleGroup);
        radioNo.setToggleGroup(toggleGroup);
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> actualizarEstadoSpinner());
        SpinnerValueFactory<Integer> valueFactoryHora = new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 20);
        Hora.setValueFactory(valueFactoryHora);
        valueFactoryHora.setValue(8);
        SpinnerValueFactory<Integer> valueFactoryMinutos = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59);
        Minutos.setValueFactory(valueFactoryMinutos);
        valueFactoryMinutos.setValue(0);
        setDaysInWeek(today.with(DayOfWeek.MONDAY));
        // --- Limitar fechas posteriores al día de hoy en el DatePicker de tareas ---
        if (dateDiaTarea != null) {
            dateDiaTarea.setDayCellFactory(new javafx.util.Callback<DatePicker, DateCell>() {
                @Override
                public DateCell call(DatePicker param) {
                    return new DateCell() {
                        @Override
                        public void updateItem(LocalDate date, boolean empty) {
                            super.updateItem(date, empty);
                            if (date.isBefore(LocalDate.now())) {
                                setDisable(true);
                                setStyle("-fx-background-color: #ffc0cb;");
                            }
                        }
                    };
                }
            });
        }
    }

    // =================== EVENTOS DE UI ===================
    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/components.css", false);
    }
    @FXML
    void anteriorSemana(ActionEvent event) {
        today = today.minusWeeks(1).with(DayOfWeek.MONDAY);
        setDaysInWeek(today);
        cargarTareasSemana(today);
    }
    @FXML
    void siguienteSemana(ActionEvent event) {
        today = today.plusWeeks(1).with(DayOfWeek.MONDAY);
        setDaysInWeek(today);
        cargarTareasSemana(today);
    }
    @FXML
    void NuevaTarea(ActionEvent event) {
        paneAgregarTareaPendiente.setVisible(true);
        btnNuevaTarea.setDisable(true);
    }
    @FXML
    void cancelarTarea(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("Se perderán los cambios realizados. ¿Desea salir?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            vaciarCamposNuevaTarea();
            paneAgregarTareaPendiente.setVisible(false);
            btnNuevaTarea.setDisable(false);
        }
    }
    @FXML
    void guardarTarea(ActionEvent event) {
        if (validarCamposObligatorios()) {
            btnNuevaTarea.setDisable(false);
            String pendiente = tareaPendiente.getText();
            String empleadoTarea = cmbEmpleadoTarea.getValue();
            LocalDate fechaPendiente = dateDiaTarea.getValue();
            int horaPendiente = radioSi.isSelected() ? Hora.getValue() : 0;
            int minutoPendiente = radioSi.isSelected() ? Minutos.getValue() : 0;
            String duranteElDia = radioNo.isSelected() ? "Durante el dia" : null;
            Time hora = (duranteElDia == null) ? Time.valueOf(String.format("%02d:%02d:00", horaPendiente, minutoPendiente)) : Time.valueOf("23:59:59");
            Agenda nuevaTarea = new Agenda(pendiente, fechaPendiente, hora, "Pendiente", obtenerIdEmpleadoPorNombre(empleadoTarea));
            agendaDAO.save(nuevaTarea);
            // Solo agregar visualmente si la fecha está en la semana actual
            LocalDate semanaInicio = today.with(DayOfWeek.MONDAY);
            LocalDate semanaFin = semanaInicio.plusDays(6);
            if (!fechaPendiente.isBefore(semanaInicio) && !fechaPendiente.isAfter(semanaFin)) {
                int columna = (fechaPendiente.getDayOfWeek().getValue() + 6) % 7;
                // Buscar la primera fila vacía en la columna
                int fila = -1;
                for (int f = 0; f < gridTareas.getRowConstraints().size(); f++) {
                    boolean ocupado = false;
                    for (Node node : gridTareas.getChildren()) {
                        Integer colIndex = GridPane.getColumnIndex(node);
                        Integer rowIndex = GridPane.getRowIndex(node);
                        // Solo considerar ocupado si el StackPane NO es un placeholder
                        if (colIndex != null && rowIndex != null && colIndex == columna && rowIndex == f && node instanceof StackPane) {
                            Object userData = node.getUserData();
                            if (userData == null || !"placeholder".equals(userData)) {
                                ocupado = true;
                                break;
                            }
                        }
                    }
                    if (!ocupado) {
                        fila = f;
                        break;
                    }
                }
                if (fila != -1) {
                    agregarTareaADia(empleadoTarea, fechaPendiente, horaPendiente, minutoPendiente, pendiente, duranteElDia, "Pendiente", columna, fila);
                }
            }
            vaciarCamposNuevaTarea();
            paneAgregarTareaPendiente.setVisible(false);
        } else {
            showAlert(Alert.AlertType.WARNING, "Advertencia", "Campos obligatorios sin completar.\nSolución: completar los campos vacíos.");
        }
    }

    // =================== MÉTODOS DE GESTIÓN DE TAREAS ===================
    public void setDaysInWeek(LocalDate startOfWeek) {
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMMM");
        gridDias.getChildren().clear();
        gridDias.getColumnConstraints().clear();
        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7);
            gridDias.getColumnConstraints().add(col);
        }
        // Encabezados
        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = startOfWeek.plusDays(i);
            String dayOfWeek = currentDay.format(dayFormatter).toUpperCase();
            String fullDate = currentDay.format(dateFormatter).toUpperCase();
            Text text = new Text(dayOfWeek + "\n" + fullDate);
            text.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: white; -fx-text-alignment: center;");
            StackPane headerPane = new StackPane(text);
            headerPane.setStyle("-fx-background-color: #B70505; -fx-border-radius: 5px; -fx-background-radius: 5px;");
            headerPane.setAlignment(Pos.CENTER);
            headerPane.setPrefHeight(100);
            gridDias.add(headerPane, i, 0);
        }
    }

    private void cargarTareasSemana(LocalDate semanaInicio) {
        gridTareas.getChildren().clear();
        gridTareas.getRowConstraints().clear();
        int maxFilas = 10; // Máximo de tareas por día (ajustable)
        for (int f = 0; f < maxFilas; f++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            gridTareas.getRowConstraints().add(rc);
        }
        LocalDate semanaFin = semanaInicio.plusDays(6);
        List<Agenda> tareasSemana = agendaDAO.findByFechaBetween(semanaInicio, semanaFin);
        // Mapear tareas por día
        List<Agenda>[] tareasPorDia = new List[7];
        for (int i = 0; i < 7; i++) tareasPorDia[i] = new java.util.ArrayList<>();
        for (Agenda tarea : tareasSemana) {
            if (!tarea.getFecha().isBefore(semanaInicio) && !tarea.getFecha().isAfter(semanaFin)) {
                int columna = (tarea.getFecha().getDayOfWeek().getValue() + 6) % 7;
                tareasPorDia[columna].add(tarea);
            }
        }
        // Agregar tareas y placeholders
        for (int col = 0; col < 7; col++) {
            for (int fila = 0; fila < maxFilas; fila++) {
                if (fila < tareasPorDia[col].size()) {
                    Agenda tarea = tareasPorDia[col].get(fila);
                    String empleado = obtenerNombreEmpleadoPorId(tarea.getIdTrabajador());
                    int hora = tarea.getHora().toLocalTime().getHour();
                    int minuto = tarea.getHora().toLocalTime().getMinute();
                    String duranteElDia = (hora == 23 && minuto == 59) ? "Durante el dia" : null;
                    agregarTareaADia(empleado, tarea.getFecha(), hora, minuto, tarea.getDescripcion(), duranteElDia, tarea.getEstado(), col, fila);
                } else {
                    // Placeholder invisible para mantener estructura
                    StackPane placeholder = new StackPane();
                    placeholder.setStyle("-fx-background-color: transparent;");
                    placeholder.setUserData("placeholder");
                    gridTareas.add(placeholder, col, fila);
                }
            }
        }
    }

    private void agregarTareaADia(String empleadoTarea, LocalDate fechaPendiente, int horaPendiente, int minutoPendiente, String pendiente, String duranteElDia, String estado, int columna, int fila) {
        StackPane tareaPane = crearTarjetaTarea(empleadoTarea, fechaPendiente, horaPendiente, minutoPendiente, pendiente, duranteElDia, estado);
        gridTareas.add(tareaPane, columna, fila);
        tareaPane.setMaxWidth(Double.MAX_VALUE);
        tareaPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
        GridPane.setHgrow(tareaPane, Priority.ALWAYS);
        tareaPane.setStyle(tareaPane.getStyle() + "; -fx-padding: 15px 0 15px 12px;"); // Espacio interior entre tareas
        GridPane.setMargin(tareaPane, new Insets(5, 0, 5, 0)); // Margen inferior entre tarjetas
    }
    /**
     * Genera una tarjeta visual para una tarea de la agenda.
     */
    private StackPane crearTarjetaTarea(String empleadoTarea, LocalDate fechaPendiente, int horaPendiente, int minutoPendiente, String pendiente, String duranteElDia, String estado) {
        VBox contenido = new VBox(4);
        contenido.setAlignment(Pos.CENTER);
        boolean realizado = "Realizado".equalsIgnoreCase(estado);
        String eventIconPath = realizado ? "/com.example.image/event_list-white.png" : "/com.example.image/event_list.png";
        String empleadoIconPath = realizado ? "/com.example.image/man-white.png" : "/com.example.image/man-black.png";
        String clockIconPath = realizado ? "/com.example.image/clock-white.png" : "/com.example.image/clock.png";
        String textColor = realizado ? "white" : "black";
        String subTextColor = realizado ? "white" : "#333";
        Label tareaLabel = new Label(pendiente);
        tareaLabel.setWrapText(true);
        tareaLabel.setMaxWidth(200);
        tareaLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: " + textColor + "; -fx-text-alignment: left;");
        HBox tareaBox = new HBox(6);
        tareaBox.setAlignment(Pos.CENTER_LEFT);
        ImageView eventIcon = new ImageView();
        java.io.InputStream eventStream = getClass().getResourceAsStream(eventIconPath);
        if (eventStream != null) {
            eventIcon.setImage(new Image(eventStream));
        }
        eventIcon.setFitHeight(16);
        eventIcon.setFitWidth(16);
        tareaBox.getChildren().addAll(eventIcon, tareaLabel);
        contenido.getChildren().add(tareaBox);
        Label empleadoLabel = new Label(empleadoTarea);
        empleadoLabel.setWrapText(true);
        empleadoLabel.setMaxWidth(200);
        empleadoLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: " + subTextColor + "; -fx-text-alignment: left;");
        HBox trabajadorBox = new HBox(6);
        trabajadorBox.setAlignment(Pos.CENTER_LEFT);
        ImageView empleadoIcon = new ImageView();
        java.io.InputStream empleadoStream = getClass().getResourceAsStream(empleadoIconPath);
        if (empleadoStream != null) {
            empleadoIcon.setImage(new Image(empleadoStream));
        }
        empleadoIcon.setFitHeight(16);
        empleadoIcon.setFitWidth(16);
        trabajadorBox.getChildren().addAll(empleadoIcon, empleadoLabel);
        contenido.getChildren().add(trabajadorBox);
        Label horarioLabel;
        if ("Durante el dia".equals(duranteElDia)) {
            horarioLabel = new Label("Durante el día");
        } else {
            horarioLabel = new Label(String.format("%02d:%02d hs", horaPendiente, minutoPendiente));
        }
        horarioLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: " + subTextColor + "; -fx-text-alignment: left;");
        HBox horarioBox = new HBox(6);
        horarioBox.setAlignment(Pos.CENTER_LEFT);
        ImageView clockIcon = new ImageView();
        java.io.InputStream clockStream = getClass().getResourceAsStream(clockIconPath);
        if (clockStream != null) {
            clockIcon.setImage(new Image(clockStream));
        }
        clockIcon.setFitHeight(16);
        clockIcon.setFitWidth(16);
        horarioBox.getChildren().addAll(clockIcon, horarioLabel);
        contenido.getChildren().add(horarioBox);
        StackPane tareaPane = new StackPane(contenido);

        if (realizado) {
            tareaPane.setStyle("-fx-background-color: #4E703F; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 10, 0.0, 2, 2); -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-margin-top: 5px;");
        } else {
            tareaPane.setStyle("-fx-background-color: #FFF4F4; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 10, 0.0, 2, 2); -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-margin-top: 5px;");
        }
        ContextMenu contextMenu = new ContextMenu();
        MenuItem marcarHecho = new MenuItem("Marcar como hecho");
        marcarHecho.setDisable(realizado);
        marcarHecho.setOnAction(ev -> {
            tareaPane.setStyle("-fx-background-color: #4E703F; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 10, 0.0, 2, 2); -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-margin-top: 5px; -fx-padding: 15px 0 15px 12px;");
            java.io.InputStream eventStream2 = getClass().getResourceAsStream("/com.example.image/event_list-white.png");
            if (eventStream2 != null) eventIcon.setImage(new Image(eventStream2));
            java.io.InputStream empleadoStream2 = getClass().getResourceAsStream("/com.example.image/man-white.png");
            if (empleadoStream2 != null) empleadoIcon.setImage(new Image(empleadoStream2));
            java.io.InputStream clockStream2 = getClass().getResourceAsStream("/com.example.image/clock-white.png");
            if (clockStream2 != null) clockIcon.setImage(new Image(clockStream2));
            tareaLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: white; -fx-text-alignment: left;");
            tareaLabel.setWrapText(true);
            tareaLabel.setMaxWidth(200);
            empleadoLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: white; -fx-text-alignment: left;");
            empleadoLabel.setWrapText(true);
            empleadoLabel.setMaxWidth(200);
            horarioLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: white; -fx-text-alignment: left;");
            Agenda tarea = agendaDAO.findByCampos(empleadoTarea, fechaPendiente, horaPendiente, minutoPendiente, pendiente);
            if (tarea != null) {
                tarea.setEstado("Realizado");
                agendaDAO.update(tarea);
            }
        });
        contextMenu.getItems().add(marcarHecho);
        tareaPane.setOnContextMenuRequested(event -> contextMenu.show(tareaPane, event.getScreenX(), event.getScreenY()));
        // --- Ajustes visuales para estirar la tarjeta ---
        tareaPane.setMaxWidth(Double.MAX_VALUE);
        tareaPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
        VBox.setVgrow(tareaPane, Priority.NEVER);
        StackPane.setAlignment(contenido, Pos.CENTER_LEFT);
        tareaPane.widthProperty().addListener((_, __, newVal) -> {
            contenido.setPrefWidth(newVal.doubleValue());
        });
        tareaPane.parentProperty().addListener((_, __, newParent) -> {
            if (newParent instanceof VBox contenedorVBox) {
                tareaPane.prefWidthProperty().bind(contenedorVBox.widthProperty());
            }
        });
        return tareaPane;
    }

    // =================== MÉTODOS DE UTILIDAD ===================
    private void cargarNombresEnComboBox() {
        try {
            List<String> nombres = trabajadorDAO.findAllNombres();
            cmbEmpleadoTarea.setItems(FXCollections.observableArrayList(nombres));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los nombres de los empleados: " + e.getMessage());
        }
    }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void vaciarCamposNuevaTarea() {
        Hora.getValueFactory().setValue(Hora.getValueFactory().getConverter().fromString("8"));
        Minutos.getValueFactory().setValue(Minutos.getValueFactory().getConverter().fromString("0"));
        radioSi.setSelected(false);
        radioNo.setSelected(true);
        tareaPendiente.clear();
        cmbEmpleadoTarea.setValue(null);
        dateDiaTarea.setValue(null);
    }
    private boolean validarCamposObligatorios() {
        if (dateDiaTarea.getValue() == null) return false;
        if (tareaPendiente.getText() == null || tareaPendiente.getText().trim().isEmpty()) return false;
        if (radioSi.isSelected() && (Hora.getValue() == null || Hora.getValue() == 0) && (Minutos.getValue() == null || Minutos.getValue() == 0)) return false;
        if (cmbEmpleadoTarea.getValue() == null || cmbEmpleadoTarea.getValue().toString().trim().isEmpty()) return false;
        return radioNo.isSelected() || true;
    }
    private void actualizarEstadoSpinner() {
        Hora.setDisable(!radioSi.isSelected());
        Minutos.setDisable(!radioSi.isSelected());
        radioNo.setDisable(false);
        radioSi.setDisable(false);
    }
    private Integer obtenerIdEmpleadoPorNombre(String nombreEmpleado) {
        EntityManager em = agendaDAO.getEntityManager();
        try {
            return em.createQuery("SELECT e.id FROM Trabajador e WHERE e.nombre = :nombre", Integer.class)
                    .setParameter("nombre", nombreEmpleado)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    private String obtenerNombreEmpleadoPorId(Integer idTrabajador) {
        return trabajadorDAO.findNombreById(idTrabajador);
    }

    private void sincronizarColumnasGrid() {
        gridDias.getColumnConstraints().clear();
        gridTareas.getColumnConstraints().clear();
        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7.0);
            col.setHgrow(Priority.ALWAYS);
            gridDias.getColumnConstraints().add(col);
            gridTareas.getColumnConstraints().add(col);
        }
        gridTareas.setMaxWidth(Double.MAX_VALUE);
        gridTareas.setPrefWidth(Region.USE_COMPUTED_SIZE);
        gridTareas.setMinWidth(Region.USE_COMPUTED_SIZE);
        gridDias.setMaxWidth(Double.MAX_VALUE);
        gridDias.setPrefWidth(Region.USE_COMPUTED_SIZE);
        gridDias.setMinWidth(Region.USE_COMPUTED_SIZE);
    }

}