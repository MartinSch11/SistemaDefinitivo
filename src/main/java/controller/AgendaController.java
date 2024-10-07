package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.awt.ScrollPane;
import javafx.scene.control.TextArea;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import javafx.scene.text.Text;
import utilities.Paths;
import utilities.SceneLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class AgendaController {
    @FXML
    private Pane paneAgregarTareaPendiente;
    @FXML
    private Spinner<Integer> Hora; @FXML private Spinner<Integer> Minutos;
    @FXML private RadioButton radioNo; @FXML private RadioButton radioSi;

    @FXML
    private ComboBox<String> cmbEmpleadoTarea;
    @FXML
    private DatePicker dateDiaTarea;
    @FXML
    private TextArea tareaPendiente;
    @FXML
    private GridPane daysGrid;
    @FXML
    private Label txtFechaSemanal;
    @FXML
    private LocalDate today = LocalDate.now();

    @FXML
    private String pendiente; @FXML private String empleadoTarea; @FXML private Integer horaPendiente; @FXML private Integer minutoPendiente;

    @FXML
    private LocalDate fechaPendiente;
    @FXML
    private GridPane gridPlanillaSemanal;

    @FXML private StackPane paneDomingo; @FXML private StackPane paneJueves; @FXML private StackPane paneLunes; @FXML private StackPane paneMartes; @FXML private StackPane paneMiercoles; @FXML private StackPane paneSabado; @FXML private StackPane paneViernes;


    @FXML
    private void initialize() {
        paneAgregarTareaPendiente.setVisible(false);
        radioNo.setSelected(true);

        // Inicializar la fecha actual
        // Mostrar la semana actual en el label y en los StackPanes
        FechaSemanal(today);
        setDaysInWeek(today.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)); // 1 para empezar en lunes

        // Configura el ToggleGroup para que solo uno esté seleccionado a la vez
        ToggleGroup toggleGroup = new ToggleGroup();
        radioSi.setToggleGroup(toggleGroup);
        radioNo.setToggleGroup(toggleGroup);
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            actualizarEstadoSpinner();
        });

        // Configuración del Spinner para horas (8 a 20)
        SpinnerValueFactory<Integer> valueFactoryHora = new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 20);
        Hora.setValueFactory(valueFactoryHora);
        valueFactoryHora.setValue(8);  // Valor inicial

        // Configuración del Spinner para minutos (0 a 59)
        SpinnerValueFactory<Integer> valueFactoryMinutos = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59);
        Minutos.setValueFactory(valueFactoryMinutos);
        valueFactoryMinutos.setValue(0);


        /*---------momentaneo------------*/
        cmbEmpleadoTarea.setItems(FXCollections.observableArrayList(
                "Jose",
                "Juan",
                "Antonio"
        ));

        /*--------controla las fechas de la semana----------*/
        // Obtener la fecha de hoy
        LocalDate today = LocalDate.now();

        // Calcular el lunes de la semana actual
        LocalDate monday = today.with(DayOfWeek.MONDAY);

        // Llamar al metodo que actualiza los panes con los días de la semana
        setDaysInWeek(monday);

        // Configurar la fila 0 del GridPane
        RowConstraints row0 = new RowConstraints();
        row0.setPrefHeight(120);
        row0.setMinHeight(120);
        row0.setMaxHeight(120);
        gridPlanillaSemanal.getRowConstraints().add(0, row0);

    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.CAJERA_MAINMENU, "/com/example/pasteleria/MainMenuCajera.fxml", true);
    }

    @FXML
    void anteriorSemana(ActionEvent event) {
        // Restar una semana a 'today'
        today = today.minusWeeks(1);
        // Actualizar la fecha semanal y los StackPane
        FechaSemanal(today);
    }

    @FXML
    void siguienteSemana(ActionEvent event) {
        // Sumar una semana a 'today'
        today = today.plusWeeks(1);
        // Actualizar la fecha semanal y los StackPane
        FechaSemanal(today);
    }

    // Función que habilita/deshabilita los Spinner según el RadioButton seleccionado
    private void actualizarEstadoSpinner() {
        if (radioSi.isSelected()) {
            Hora.setDisable(false);   // Habilitar Spinner Hora
            Minutos.setDisable(false); // Habilitar Spinner Minutos
            radioNo.setDisable(false);
        } else if (radioNo.isSelected()) {
            Hora.setDisable(true);    // Deshabilitar Spinner Hora
            Minutos.setDisable(true);  // Deshabilitar Spinner Minutos
            radioSi.setDisable(false);
        }
    }

    private boolean validarCamposObligatorios() {
        boolean valid = true;
        if (dateDiaTarea.getValue() == null) {
            valid = false;
        }
        if (tareaPendiente.getText() == null || tareaPendiente.getText().trim().isEmpty()) {
            valid = false;
        }
        if (radioSi.isSelected() && (Hora.getValue() == null || Hora.getValue() == 0) && (Minutos.getValue() == null || Minutos.getValue() == 0)) {
            valid = false;
        }
        if (cmbEmpleadoTarea.getValue() == null || cmbEmpleadoTarea.getValue().toString().trim().isEmpty()) {
            valid = false;
        }

        return valid;
    }

    private void vaciarCamposNuevaTarea() {
        // Restablecer Spinner de Hora
        Hora.getValueFactory().setValue(Hora.getValueFactory().getConverter().fromString("8")); // Suponiendo que 8 es el valor inicial de las horas
        // Restablecer Spinner de Minutos a su valor inicial
        Minutos.getValueFactory().setValue(Minutos.getValueFactory().getConverter().fromString("0"));

        radioSi.setSelected(false);
        radioNo.setSelected(true);
        tareaPendiente.clear();
        cmbEmpleadoTarea.setValue(null);
        dateDiaTarea.setValue(null);
    }

    @FXML
    private Button btnNuevaTarea;
    @FXML
    void NuevaTarea(ActionEvent event) {
        paneAgregarTareaPendiente.setVisible(true);
        btnNuevaTarea.setDisable(true);

        //guardar los datos de la Nueva Tarea

        //el radiobuttonNO  NO funciona en el caso de seleccionar y guardar la tareaPendiente

        //eliminar tareas hechas mediante una opción de "Marcar como hecho"

        //guardar LocalDate, guardar la fecha

        //añadir a la base de datos las tareas pendientes para que se guarden al salir de Agenda

    }

    @FXML
    void cancelarTarea(ActionEvent event) {
        Node source = (Node) event.getSource();
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
            //tarea pendiente
            pendiente = tareaPendiente.getText();
            empleadoTarea = cmbEmpleadoTarea.getValue();
            fechaPendiente = dateDiaTarea.getValue();
            if (radioSi.isSelected()) {
                horaPendiente = Hora.getValue();
                minutoPendiente = Minutos.getValue();
            }

            vaciarCamposNuevaTarea();

            paneAgregarTareaPendiente.setVisible(false);

            insertarNuevaTarea(empleadoTarea, fechaPendiente, horaPendiente, minutoPendiente, pendiente);

        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia");
            alert.setHeaderText("Campos obligatorios sin completar.");
            alert.setContentText("Solución: completar los campos vacíos.");
            alert.showAndWait();
        }
    }

    public void FechaSemanal(LocalDate selectedDate) {
        // Calcular el primer y último día de la semana basada en la fecha seleccionada
        LocalDate startOfWeek = selectedDate.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = selectedDate.with(DayOfWeek.SUNDAY);

        // Formatear las fechas para mostrarlas en el Label
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd"); // Ej: "Octubre 07"
        String weekRange = startOfWeek.format(formatter) + " - " + endOfWeek.format(formatter) + ", " + selectedDate.getYear();

        txtFechaSemanal.setText(txtFechaSemanal.getText().toUpperCase());
        txtFechaSemanal.setStyle("-fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold;");
        txtFechaSemanal.setText(weekRange);

        // Actualizar los StackPanes con las fechas de la semana
        setDaysInWeek(startOfWeek);
    }

    public void setDaysInWeek(LocalDate startOfWeek) {
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE"); // Día de la semana
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMMM"); // Día y mes, ej. "30 Septiembre"

        StackPane[] panes = {paneLunes, paneMartes, paneMiercoles, paneJueves, paneViernes, paneSabado, paneDomingo};

        // Iterar sobre los días de la semana
        for (int i = 0; i < 7; i++) {
            // Calcular la fecha de cada día (lunes + i días)
            LocalDate currentDay = startOfWeek.plusDays(i);

            // Crear el texto con formato
            String dayOfWeek = currentDay.format(dayFormatter).toUpperCase();
            String fullDate = currentDay.format(dateFormatter).toUpperCase();

            Text text = new Text(dayOfWeek + "\n" + fullDate);
            text.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: white; -fx-text-alignment: center;");

            // Limpia el StackPane y añade el texto
            panes[i].getChildren().clear();
            panes[i].getChildren().add(text);
            panes[i].setAlignment(text, Pos.CENTER);
        }
    }

    private StackPane getStackPaneForDay(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1: return paneLunes;
            case 2: return paneMartes;
            case 3: return paneMiercoles;
            case 4: return paneJueves;
            case 5: return paneViernes;
            case 6: return paneSabado;
            case 7: return paneDomingo;
            default: throw new IllegalArgumentException("Día de la semana inválido: " + dayOfWeek);
        }
    }

    private void insertarNuevaTarea(String empleadoTarea, LocalDate fechaPendiente, int horaPendiente, int minutoPendiente, String pendiente) {
        Label tareaLabel = new Label(pendiente + "\n" + empleadoTarea + "\n" + String.format("%02d:%02d hs", horaPendiente, minutoPendiente));
        tareaLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: black; -fx-text-alignment: left; -fx-padding-top: 5px");
        tareaLabel.setWrapText(true); // No permitir que el texto se ajuste al ancho del TextArea

        StackPane tareaPane = new StackPane(tareaLabel);
        tareaPane.setPrefHeight(150);
        tareaPane.setMaxHeight(150);
        tareaPane.setMinHeight(150);
        tareaPane.setStyle("-fx-background-color: #FFF4F4; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 10, 0.0, 2, 2); -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-margin-top: 5px;");

        // Obtener la columna correspondiente al día de la semana de fechaPendiente
        int columnIndex = fechaPendiente.getDayOfWeek().getValue() - 1;

        // Determinar la siguiente fila en el GridPane
        int newRowIndex = gridPlanillaSemanal.getRowConstraints().size();
        gridPlanillaSemanal.add(tareaPane, columnIndex, newRowIndex); // Insertar el StackPane en la nueva fila y columna correspondiente

        // Añadir una restricción de tamaño fijo a la nueva fila
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setPrefHeight(150);
        rowConstraints.setMaxHeight(150);
        rowConstraints.setMinHeight(150);
        gridPlanillaSemanal.getRowConstraints().add(rowConstraints);
    }

}