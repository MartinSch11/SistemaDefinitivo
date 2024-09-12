package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import model.Evento;
import utilities.Paths;
import utilities.SceneLoader;

import java.io.IOException;
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
    @FXML
    private Pane PaneDetalleEvento;
    @FXML
    private TextField eventNameField;
    @FXML
    private TextArea eventDescriptionField;

    private YearMonth currentYearMonth;
    private Map<LocalDate, Evento> events = new HashMap<>();

    @FXML
    private void initialize() {
        currentYearMonth = YearMonth.now();
        PaneDetalleEvento.setVisible(false); // Ocultar el panel inicialmente
        llenarCalendario(currentYearMonth);
    }

    private void llenarCalendario(YearMonth mesAnio) {
        calendarGrid.getChildren().clear();

        LocalDate primerDiaDelMes = mesAnio.atDay(1);
        int diasEnElMes = mesAnio.lengthOfMonth();
        int diaDeLaSemana = primerDiaDelMes.getDayOfWeek().getValue(); // 1 = Lunes, 7 = Domingo

        Label etiquetaMes = (Label) monthPane.lookup("#monthLabel");
        Locale localEspanol = new Locale("es", "ES");
        String nombreMes = mesAnio.getMonth().getDisplayName(TextStyle.FULL, localEspanol);
        etiquetaMes.setText(nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1) + " " + mesAnio.getYear());

        int espaciosTotales = diaDeLaSemana - 1 + diasEnElMes;
        int filasNecesarias = (espaciosTotales / 7) + (espaciosTotales % 7 == 0 ? 0 : 1);

        calendarGrid.getRowConstraints().clear();
        for (int i = 0; i < filasNecesarias + 1; i++) {
            RowConstraints fila = new RowConstraints();
            calendarGrid.getRowConstraints().add(fila);
        }

        YearMonth mesAnterior = mesAnio.minusMonths(1);
        int diasEnElMesAnterior = mesAnterior.lengthOfMonth();

        int filaActual = 1;
        int columnaActual = diaDeLaSemana - 1;

        for (int i = columnaActual - 1; i >= 0; i--) {
            Label etiquetaDia = new Label(String.valueOf(diasEnElMesAnterior - (columnaActual - 1 - i)));
            etiquetaDia.getStyleClass().add("celda-dia-semana"); // Clase CSS
            calendarGrid.add(etiquetaDia, i, filaActual);
        }

        int numeroDia = 1;
        while (numeroDia <= diasEnElMes) {
            Label etiquetaDia = new Label(String.valueOf(numeroDia));
            LocalDate fechaActual = LocalDate.of(mesAnio.getYear(), mesAnio.getMonth(), numeroDia);

            if (events.containsKey(fechaActual)) {
                etiquetaDia.setStyle("-fx-text-fill: red;");
            }

            etiquetaDia.setOnMouseClicked(event -> handleDayClick(fechaActual));

            if (columnaActual == 5 || columnaActual == 6) {
                etiquetaDia.getStyleClass().add("celda-fin-de-semana");
            } else {
                etiquetaDia.getStyleClass().add("celda-dia-semana");
            }

            calendarGrid.add(etiquetaDia, columnaActual, filaActual);
            columnaActual++;

            if (columnaActual == 7) {
                columnaActual = 0;
                filaActual++;
            }

            numeroDia++;
        }

        numeroDia = 1;
        while (columnaActual < 7) {
            Label etiquetaDia = new Label(String.valueOf(numeroDia));
            etiquetaDia.getStyleClass().add("celda-dia-semana");
            calendarGrid.add(etiquetaDia, columnaActual, filaActual);
            numeroDia++;
            columnaActual++;
        }

        if (numeroDia - 1 <= 35) {
            calendarGrid.getRowConstraints().remove(calendarGrid.getRowConstraints().size() - 1);
        }
    }

    private void handleDayClick(LocalDate date) {
        Evento evento = events.get(date);

        if (evento != null) {
            PaneDetalleEvento.setVisible(true);

            // Suponiendo que tienes etiquetas (Labels) en PaneDetalleEvento para mostrar los detalles:
            Label nombreEventoLabel = (Label) PaneDetalleEvento.lookup("#nombreEventoLabel");
            Label nombreClienteLabel = (Label) PaneDetalleEvento.lookup("#nombreClienteLabel");
            Label telefonoClienteLabel = (Label) PaneDetalleEvento.lookup("#telefonoClienteLabel");
            Label direccionEventoLabel = (Label) PaneDetalleEvento.lookup("#direccionEventoLabel");

            nombreEventoLabel.setText(evento.getNombreEvento());
            nombreClienteLabel.setText(evento.getNombreCliente());
            telefonoClienteLabel.setText(evento.getTelefonoCliente());
            direccionEventoLabel.setText(evento.getDireccionEvento());
        } else {
            PaneDetalleEvento.setVisible(false); // Ocultar si no hay evento
        }
    }

    private void addEvent(LocalDate date, Evento event) {
        if (events.containsKey(date)) {
            // Mostrar una alerta si ya existe un evento en esta fecha
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Evento Duplicado");
            alert.setHeaderText("Ya existe un evento en esta fecha");
            alert.setContentText("Por favor, elige otra fecha o modifica el evento existente.");
            alert.showAndWait();
            return;
        }

        events.put(date, event);  // Almacenar el evento en el mapa
        if (currentYearMonth.equals(YearMonth.from(date))) {
            llenarCalendario(currentYearMonth);  // Repintar el calendario solo si el evento pertenece al mes actual
        }
    }

    @FXML
    private void handlePrevMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        llenarCalendario(currentYearMonth);
    }

    @FXML
    private void handleNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        llenarCalendario(currentYearMonth);
    }

    @FXML
    private void handleAddEvent() {
        try {
            // Cargar el formulario personalizado
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/evento_form.fxml"));
            DialogPane dialogPane = loader.load();

            // Crear un diálogo
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Agregar Evento");

            // Obtener el controlador del formulario
            EventoFormController controller = loader.getController();

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Recoger los datos del formulario
                String nombreEvento = controller.getNombreEvento();
                String descripcionEvento = controller.getDescripcionEvento();
                String nombreCliente = controller.getNombreCliente();
                String telefonoCliente = controller.getTelefonoCliente();
                String direccionEvento = controller.getDireccionEvento();
                LocalDate fechaEvento = controller.getFechaEvento();  // Recoger la fecha

                // Crear el nuevo evento
                Evento nuevoEvento = new Evento(nombreEvento, descripcionEvento, nombreCliente, telefonoCliente, direccionEvento);

                // Añadir el evento al mapa y actualizar el calendario
                addEvent(fechaEvento, nuevoEvento);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.ADMIN_MAINMENU, "/css/loginAdmin.css", true);
    }
}
