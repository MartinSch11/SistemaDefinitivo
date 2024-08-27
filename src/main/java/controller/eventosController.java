package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import utilities.Paths;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class eventosController {

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Pane monthPane;

    private YearMonth currentYearMonth;
    private Map<LocalDate, String> events = new HashMap<>();

    void handleVolver(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(Paths.LOGIN));
            AnchorPane root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());
            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void initialize() {
        currentYearMonth = YearMonth.now();
        populateCalendar(currentYearMonth);
    }

    private void populateCalendar(YearMonth yearMonth) {
        calendarGrid.getChildren().clear(); // Limpiar el GridPane

        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday

        // Add headers for days of the week
        String[] weekDays = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        for (int i = 0; i < weekDays.length; i++) {
            Label dayLabel = new Label(weekDays[i]);
            dayLabel.setStyle("-fx-alignment: center; -fx-padding: 5; -fx-font-weight: bold;");
            calendarGrid.add(dayLabel, i, 0);
        }

        // Add days of the month
        for (int day = 1; day <= daysInMonth; day++) {
            final LocalDate date = yearMonth.atDay(day); // Use final here
            Label dayLabel = new Label(String.valueOf(day));
            int column = (dayOfWeek - 1 + day - 1) % 7; // Ajusta para el primer día de la semana
            int row = (dayOfWeek - 1 + day - 1) / 7 + 1; // Ajusta la fila en base a la columna

            // Añadir eventos para cada día
            dayLabel.setOnMouseClicked(event -> handleDayClick(date)); // Pasar la fecha final

            // Establecer estilo opcional
            dayLabel.setStyle("-fx-alignment: center; -fx-padding: 5; -fx-border-color: gray;");

            calendarGrid.add(dayLabel, column, row);
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
}
