package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import utilities.Paths;
import utilities.SceneLoader;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class EventosController {

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Pane monthPane;

    private YearMonth currentYearMonth;
    private Map<LocalDate, String> events = new HashMap<>();

    @FXML
    private void initialize() {
        currentYearMonth = YearMonth.now();
        populateCalendar(currentYearMonth);
    }

    private void populateCalendar(YearMonth yearMonth) {
        // Limpiar el GridPane
        calendarGrid.getChildren().clear();
        calendarGrid.setGridLinesVisible(true);

        // Obtener el primer día del mes y cuántos días tiene el mes
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue(); // 1 = Lunes, 7 = Domingo

        // Actualizar el label con el mes y año en español
        Label monthLabel = (Label) monthPane.lookup("#monthLabel");
        Locale spanishLocale = new Locale("es", "ES");
        String monthName = yearMonth.getMonth().getDisplayName(TextStyle.FULL, spanishLocale);
        monthLabel.setText(monthName.substring(0, 1).toUpperCase() + monthName.substring(1) + " " + yearMonth.getYear());

        // Calcular el número de filas necesarias
        int totalSlots = dayOfWeek - 1 + daysInMonth;
        int rowsNeeded = (totalSlots / 7) + (totalSlots % 7 == 0 ? 0 : 1);

        // Asegurarse de que hay suficientes filas
        calendarGrid.getRowConstraints().clear();
        for (int i = 0; i < rowsNeeded + 1; i++) { // +1 para la fila de los días de la semana
            RowConstraints row = new RowConstraints();
            row.setPrefHeight(50);
            calendarGrid.getRowConstraints().add(row);
        }

        // Añadir encabezados de los días de la semana
        String[] weekDays = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        for (int i = 0; i < weekDays.length; i++) {
            Label dayLabel = new Label(weekDays[i]);
            dayLabel.setStyle("-fx-alignment: center; -fx-padding: 5; -fx-font-weight: bold;");
            calendarGrid.add(dayLabel, i, 0);
        }

        // Obtener el mes anterior y sus días
        YearMonth previousMonth = yearMonth.minusMonths(1);
        int daysInPreviousMonth = previousMonth.lengthOfMonth();

        // Variables para seguir la posición en el grid
        int currentRow = 1; // Comienza en la primera fila de los días
        int currentColumn = dayOfWeek - 1; // Comienza en la columna del primer día del mes

        // Colocar los últimos días del mes anterior si el primer día no es lunes
        for (int i = currentColumn - 1; i >= 0; i--) {
            Label dayLabel = new Label(String.valueOf(daysInPreviousMonth - (currentColumn - 1 - i)));
            dayLabel.setStyle("-fx-alignment: center; -fx-padding: 5; -fx-text-fill: lightgray;");
            calendarGrid.add(dayLabel, i, currentRow); // En la primera fila
        }

        // Añadir días del mes actual
        int dayNumber = 1;
        while (dayNumber <= daysInMonth) {
            Label dayLabel = new Label(String.valueOf(dayNumber));

            // Colorear fines de semana
            if (currentColumn == 5 || currentColumn == 6) { // Sábado y Domingo
                dayLabel.getStyleClass().add("grid-cell");
                dayLabel.getStyleClass().add("weekend-cell");
            } else {
                dayLabel.getStyleClass().add("grid-cell");
                dayLabel.getStyleClass().add("weekday-cell");
            }

            calendarGrid.add(dayLabel, currentColumn, currentRow);

            // Mover al siguiente día
            dayNumber++;
            currentColumn++;

            // Si llegamos al final de la semana, saltamos a la siguiente fila
            if (currentColumn == 7) {
                currentColumn = 0;
                currentRow++;
            }
        }

        // Añadir días del mes siguiente en las celdas vacías después del final del mes actual
        dayNumber = 1;
        while (currentColumn < 7) {
            Label dayLabel = new Label(String.valueOf(dayNumber));
            dayLabel.setStyle("-fx-alignment: center; -fx-padding: 5; -fx-text-fill: lightgray;");
            calendarGrid.add(dayLabel, currentColumn, currentRow);
            dayNumber++;
            currentColumn++;
        }

        // Quitar la fila extra si no es necesaria (cuando el total de días no excede las 5 filas)
        if (dayNumber - 1 <= 35) {
            calendarGrid.getRowConstraints().remove(calendarGrid.getRowConstraints().size() - 1);
        }
    }

    private void handleDayClick(LocalDate date) {
        String existingEvent = getEvent(date);
        String eventText = existingEvent != null ? existingEvent : "No events";

        // Mostrar detalles del evento en una alerta
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Event Details");
        alert.setHeaderText("Events on " + date);
        alert.setContentText(eventText);
        alert.showAndWait();
    }

    private void addEvent(LocalDate date, String event) {
        events.put(date, event);
    }

    private String getEvent(LocalDate date) {
        return events.get(date);
    }

    @FXML
    private void handlePrevMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        populateCalendar(currentYearMonth);
    }

    @FXML
    private void handleNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        populateCalendar(currentYearMonth);
    }

    @FXML
    private void handleAddEvent() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Agregar Evento");
        dialog.setHeaderText("Añadir evento a la fecha seleccionada");
        dialog.setContentText("Evento:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(event -> {
            // Suponemos que la fecha seleccionada es hoy para simplificar
            LocalDate selectedDate = LocalDate.now(); // Cambia esto por la fecha seleccionada
            addEvent(selectedDate, event);
            populateCalendar(currentYearMonth); // Actualiza el calendario
        });
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.ADMIN_MAINMENU, "/css/loginAdmin.css", true);
    }
}
