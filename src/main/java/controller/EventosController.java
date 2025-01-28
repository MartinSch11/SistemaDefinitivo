package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import model.Evento;
import persistence.dao.EventoDAO;
import utilities.ActionLogger;
import utilities.Paths;
import utilities.SceneLoader;
import java.io.IOException;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

public class EventosController {

    @FXML private GridPane calendarGrid;
    @FXML private Pane monthPane;
    @FXML private Pane PaneDetalleEvento;
    @FXML private Label lblNEvento;
    @FXML private Label lblDetalleEvento;
    @FXML private Label lblNomCliente;
    @FXML private Label lblTelefono;
    @FXML private Label lblDirecEvento;
    @FXML private Label lblCantPersonas;
    @FXML private Label lblPresupuesto;

    private YearMonth currentYearMonth;
    private Map<LocalDate, Evento> events = new HashMap<>();
    private LocalDate fechaSeleccionada;

    @FXML
    private void initialize() {
        currentYearMonth = YearMonth.now();
        PaneDetalleEvento.setVisible(false);
        reloadEvents();
        llenarCalendario(currentYearMonth);
        ActionLogger.log("Calendario inicializado para el mes: " + currentYearMonth); // Log de acción
    }

    private void llenarCalendario(YearMonth mesAño) {
        calendarGrid.getChildren().clear();
        LocalDate primerDiaDelMes = mesAño.atDay(1);
        int diaDeLaSemana = primerDiaDelMes.getDayOfWeek().getValue();
        int diasEnElMes = mesAño.lengthOfMonth();

        actualizarEtiquetaMes(mesAño);
        int filasNecesarias = calcularFilasNecesarias(diaDeLaSemana, diasEnElMes);
        configurarFilasCalendario(filasNecesarias);

        YearMonth mesAnterior = mesAño.minusMonths(1);
        rellenarDiasMesAnterior(mesAnterior, diaDeLaSemana);

        rellenarDiasMesActual(mesAño, diaDeLaSemana, diasEnElMes);

        rellenarDiasMesSiguiente(diaDeLaSemana, diasEnElMes, filasNecesarias);

        ActionLogger.log("Calendario para el mes: " + mesAño + " generado correctamente."); // Log de acción
    }

    private void handleDayClick(LocalDate date) {
        fechaSeleccionada = date;
        Evento evento = events.get(date);

        if (evento != null) {
            PaneDetalleEvento.setVisible(true);
            lblDetalleEvento.setText(evento.getDescripcion_evento());
            lblNEvento.setText(evento.getNombre_evento());
            lblNomCliente.setText(evento.getNombre_cliente());
            lblTelefono.setText(evento.getTelefono_cliente());
            lblDirecEvento.setText(evento.getDireccion_evento());
            lblCantPersonas.setText(String.valueOf(evento.getCant_personas()));
            lblPresupuesto.setText(evento.getPresupuesto().setScale(2, RoundingMode.HALF_UP).toString());
            ActionLogger.log("Detalles del evento cargados para la fecha: " + date); // Log de acción
        } else {
            PaneDetalleEvento.setVisible(false);
        }
    }

    private void addEvent(LocalDate date, Evento event) {
        if (events.containsKey(date)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Evento Duplicado");
            alert.setHeaderText("Ya existe un evento en esta fecha");
            alert.setContentText("Por favor, elige otra fecha o modifica el evento existente.");
            alert.showAndWait();
            ActionLogger.log("Intento de agregar un evento duplicado en la fecha: " + date); // Log de acción
            return;
        }

        events.put(date, event);
        if (currentYearMonth.equals(YearMonth.from(date))) {
            llenarCalendario(currentYearMonth);
        }
        ActionLogger.log("Evento agregado para la fecha: " + date); // Log de acción
    }

    @FXML
    private void handlePrevMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        llenarCalendario(currentYearMonth);
        ActionLogger.log("Cambiado al mes anterior: " + currentYearMonth); // Log de acción
    }

    @FXML
    private void handleNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        llenarCalendario(currentYearMonth);
        ActionLogger.log("Cambiado al siguiente mes: " + currentYearMonth); // Log de acción
    }

    @FXML
    private void handleAddEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/evento_form.fxml"));
            DialogPane dialogPane = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Agregar Evento");

            EventoFormController controller = loader.getController();

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                reloadEvents();
                ActionLogger.log("Evento agregado o modificado correctamente."); // Log de acción
            }
        } catch (IOException e) {
            ActionLogger.log("Error al intentar cargar el formulario de evento: " + e.getMessage()); // Log de acción
        }
    }

    @FXML
    private void handleEditar(ActionEvent event) {
        Evento eventoSeleccionado = obtenerEventoSeleccionado();
        if (eventoSeleccionado != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/evento_form.fxml"));
                DialogPane dialogPane = loader.load();

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(dialogPane);
                dialog.setTitle("Editar Evento");

                EventoFormController controller = loader.getController();

                controller.setEvento(eventoSeleccionado);

                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    reloadEvents();
                    actualizarPaneDetalleEvento(eventoSeleccionado);
                    ActionLogger.log("Evento editado correctamente para la fecha: " + eventoSeleccionado.getFecha_evento()); // Log de acción
                }
            } catch (IOException e) {
                ActionLogger.log("Error al intentar editar el evento: " + e.getMessage()); // Log de acción
            }
        } else {
            mostrarAlerta("No se ha seleccionado ningún evento", "Por favor, selecciona un evento para editar.");
            ActionLogger.log("Intento de editar sin seleccionar un evento."); // Log de acción
        }
    }

    @FXML
    public void handleBorrar() {
        Evento eventoSeleccionado = obtenerEventoSeleccionado();

        if (eventoSeleccionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Borrado");
            alert.setHeaderText(null);
            alert.setContentText("¿Seguro quieres borrar este evento?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                EventoDAO eventoDAO = new EventoDAO();
                eventoDAO.delete(eventoSeleccionado);
                eventoDAO.close();

                mostrarAlerta("Éxito", "Evento borrado exitosamente.");
                reloadEvents();
                ActionLogger.log("Evento borrado para la fecha: " + eventoSeleccionado.getFecha_evento()); // Log de acción
            }
        } else {
            mostrarAlerta("Sin selección", "No se ha seleccionado ningún evento para borrar.");
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        PaneDetalleEvento.setVisible(false);
        ActionLogger.log("Panel de detalles cerrado."); // Log de acción
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/loginAdmin.css", true);
        ActionLogger.log("Volviendo al menú principal."); // Log de acción
    }

    private void actualizarEtiquetaMes(YearMonth mesAño) {
        Label etiquetaMes = (Label) monthPane.lookup("#monthLabel");
        @SuppressWarnings("deprecation") Locale localEspanol = new Locale("es", "ES");
        String nombreMes = mesAño.getMonth().getDisplayName(TextStyle.FULL, localEspanol);
        etiquetaMes.setText(nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1) + " " + mesAño.getYear());
    }

    private int calcularFilasNecesarias(int diaDeLaSemana, int diasEnElMes) {
        int espaciosTotales = diaDeLaSemana - 1 + diasEnElMes;
        return (espaciosTotales / 7) + (espaciosTotales % 7 == 0 ? 0 : 1);
    }

    private void configurarFilasCalendario(int filasNecesarias) {
        calendarGrid.getRowConstraints().clear();
        for (int i = 0; i < filasNecesarias + 1; i++) {
            RowConstraints fila = new RowConstraints();
            calendarGrid.getRowConstraints().add(fila);
        }
    }

    private void rellenarDiasMesAnterior(YearMonth mesAnterior, int diaDeLaSemana) {
        int diasEnElMesAnterior = mesAnterior.lengthOfMonth();
        int columnaActual = diaDeLaSemana - 1;
        int filaActual = 1;

        for (int i = columnaActual - 1; i >= 0; i--) {
            Label etiquetaDia = new Label(String.valueOf(diasEnElMesAnterior - (columnaActual - 1 - i)));

            if (i == 5 || i == 6) {
                etiquetaDia.getStyleClass().add("celda-mes-externo-fin-de-semana");
            } else {
                etiquetaDia.getStyleClass().add("celda-mes-externo");
            }
            calendarGrid.add(etiquetaDia, i, filaActual);
        }
    }

    private void rellenarDiasMesActual(YearMonth mesAnio, int diaDeLaSemana, int diasEnElMes) {
        int numeroDia = 1;
        int columnaActual = diaDeLaSemana - 1;
        int filaActual = 1;

        while (numeroDia <= diasEnElMes) {
            LocalDate fechaActual = LocalDate.of(mesAnio.getYear(), mesAnio.getMonth(), numeroDia);
            Label etiquetaDia = new Label(String.valueOf(numeroDia));

            if (events.containsKey(fechaActual)) {
                etiquetaDia.getStyleClass().add("dia-con-evento");
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
    }

    private void rellenarDiasMesSiguiente(int diaDeLaSemana, int diasEnElMes, int filasNecesarias) {
        int numeroDia = 1;
        int columnaActual = (diaDeLaSemana - 1 + diasEnElMes) % 7;
        int filaActual = filasNecesarias;

        YearMonth mesAnioSiguiente = YearMonth.now().plusMonths(1); // O puedes pasar el mes actual y sumarle 1

        while (columnaActual < 7) {
            Label etiquetaDia = new Label(String.valueOf(numeroDia));

            LocalDate fechaActual = LocalDate.of(mesAnioSiguiente.getYear(), mesAnioSiguiente.getMonth(), numeroDia);

            if (events.containsKey(fechaActual)) {
                etiquetaDia.getStyleClass().add("dia-con-evento");
            }

            etiquetaDia.setOnMouseClicked(event -> handleDayClick(fechaActual));

            if (columnaActual == 5 || columnaActual == 6) {
                etiquetaDia.getStyleClass().add("celda-mes-externo-fin-de-semana");
            } else {
                etiquetaDia.getStyleClass().add("celda-mes-externo");
            }

            calendarGrid.add(etiquetaDia, columnaActual, filaActual);
            numeroDia++;
            columnaActual++;
        }

        if (numeroDia - 1 <= 35) {
            calendarGrid.getRowConstraints().remove(calendarGrid.getRowConstraints().size() - 1);
        }
    }

    public void reloadEvents() {
        clearEvents();

        EventoDAO eventoDAO = new EventoDAO();
        List<Evento> eventos = eventoDAO.findAll();
        eventoDAO.close();

        for (Evento evento : eventos) {
            addEvent(evento.getFecha_evento(), evento);
        }

        llenarCalendario(currentYearMonth);
    }

    private void clearEvents() {
        for (Node node : calendarGrid.getChildren()) {
            if (node instanceof Pane) {
                ((Pane) node).getChildren().clear();
            }
        }
        events.clear();
    }

    private void actualizarPaneDetalleEvento(Evento evento) {
        if (PaneDetalleEvento.isVisible()) {
            lblNEvento.setText(evento.getNombre_evento());
            lblDetalleEvento.setText(evento.getDescripcion_evento());
            lblNomCliente.setText(evento.getNombre_cliente());
            lblTelefono.setText(evento.getTelefono_cliente());
            lblDirecEvento.setText(evento.getDireccion_evento());
            lblCantPersonas.setText(String.valueOf(evento.getCant_personas()));
            lblPresupuesto.setText(evento.getPresupuesto().setScale(2, RoundingMode.HALF_UP).toString());
        }
    }

    private Evento obtenerEventoSeleccionado() {
        if (fechaSeleccionada != null) {
            return events.get(fechaSeleccionada);
        }
        return null;
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }


}