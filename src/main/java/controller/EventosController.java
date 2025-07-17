package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import model.Evento;
import model.SessionContext;
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
    @FXML private ComboBox<String> comboEstado;
    @FXML private Button eventoButton; // Agregar Evento
    @FXML private Button btnClose; // Cerrar detalle
    @FXML private Button btnVolver; // Volver
    @FXML private Button btnEditar; // Editar evento
    @FXML private Button btnBorrar; // Borrar evento

    private YearMonth currentYearMonth;
    private Map<LocalDate, Evento> events = new HashMap<>();
    private LocalDate fechaSeleccionada;
    private javafx.beans.value.ChangeListener<String> estadoListener; // Listener único para el ComboBox
    private Evento eventoSeleccionado; // Evento actualmente seleccionado

    // Permisos del usuario actual
    private final List<String> permisos = SessionContext.getInstance().getPermisos();
    private final boolean puedeCrear = permisos != null && permisos.contains("Eventos-crear");
    private final boolean puedeModificar = permisos != null && permisos.contains("Eventos-modificar");
    private final boolean puedeEliminar = permisos != null && permisos.contains("Eventos-eliminar");

    @FXML
    private void initialize() {
        currentYearMonth = YearMonth.now(); // Inicializar primero
        reloadEvents(); // Actualiza estados antes de cualquier inicialización visual
        PaneDetalleEvento.setVisible(false);
        comboEstado.getItems().addAll("Agendado", "Realizado");
        comboEstado.setDisable(true); // Solo editable al editar
        llenarCalendario(currentYearMonth);
        ActionLogger.log("Calendario inicializado para el mes: " + currentYearMonth); // Log de acción

        if (eventoButton != null) eventoButton.setDisable(!puedeCrear);
        if (btnEditar != null) btnEditar.setDisable(!puedeModificar);
        if (btnBorrar != null) btnBorrar.setDisable(!puedeEliminar);
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
        eventoSeleccionado = evento; // Guardar el evento seleccionado
        LocalDate hoy = LocalDate.now();

        if (evento != null) {
            PaneDetalleEvento.setVisible(true);
            lblDetalleEvento.setText(evento.getDescripcion_evento());
            lblNEvento.setText(evento.getNombre_evento());
            lblNomCliente.setText(evento.getNombre_cliente());
            lblTelefono.setText(evento.getTelefono_cliente());
            lblDirecEvento.setText(evento.getDireccion_evento());
            lblCantPersonas.setText(String.valueOf(evento.getCant_personas()));
            lblPresupuesto.setText(evento.getPresupuesto().setScale(2, RoundingMode.HALF_UP).toString());
            comboEstado.setValue(evento.getEstado());
            // Deshabilitar edición si el evento ya pasó
            boolean eventoPasado = evento.getFecha_evento().isBefore(hoy);
            comboEstado.setDisable(eventoPasado);
            if (btnEditar != null) btnEditar.setDisable(eventoPasado || !puedeModificar);
            if (btnBorrar != null) btnBorrar.setDisable(eventoPasado || !puedeEliminar);
            // Eliminar listener anterior si existe
            if (estadoListener != null) {
                comboEstado.valueProperty().removeListener(estadoListener);
            }
            // Listener solo modifica el evento actualmente seleccionado
            estadoListener = (obs, oldVal, newVal) -> {
                if (eventoSeleccionado != null && newVal != null && !newVal.equals(eventoSeleccionado.getEstado())) {
                    eventoSeleccionado.setEstado(newVal);
                    EventoDAO eventoDAO = new EventoDAO();
                    eventoDAO.update(eventoSeleccionado);
                    eventoDAO.close();
                    ActionLogger.log("Estado de evento actualizado a: " + newVal);
                    // Repintar calendario para actualizar el color
                    llenarCalendario(currentYearMonth);
                }
            };
            comboEstado.valueProperty().addListener(estadoListener);
            ActionLogger.log("Detalles del evento cargados para la fecha: " + date); // Log de acción
        } else {
            PaneDetalleEvento.setVisible(false);
            eventoSeleccionado = null;
            if (estadoListener != null) {
                comboEstado.valueProperty().removeListener(estadoListener);
                estadoListener = null;
            }
            if (btnEditar != null) btnEditar.setDisable(!puedeModificar);
            if (btnBorrar != null) btnBorrar.setDisable(!puedeEliminar);
            comboEstado.setDisable(true);
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
            AnchorPane anchorPane = loader.load();

            // Usar Stage modal igual que en handleEditar
            Stage stage = new Stage();
            stage.setTitle("Agregar Evento");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setScene(new Scene(anchorPane));

            EventoFormController controller = loader.getController();
            // Si el controlador tiene setStage, pásalo
            try {
                controller.getClass().getMethod("setStage", Stage.class).invoke(controller, stage);
            } catch (Exception ignored) {}

            stage.showAndWait();
            // Siempre recarga después de cerrar el formulario
            EventoDAO eventoDAO = new EventoDAO();
            Evento ultimoEvento = eventoDAO.findUltimoEvento();
            eventoDAO.close();
            if (ultimoEvento != null) {
                currentYearMonth = YearMonth.from(ultimoEvento.getFecha_evento());
            }
            reloadEvents();
            ActionLogger.log("Evento agregado o modificado correctamente."); // Log de acción
        } catch (IOException e) {
            ActionLogger.log("Error al intentar cargar el formulario de evento: " + e.getMessage()); // Log de acción
        }
    }

    @FXML
    private void handleEditar(ActionEvent event) {
        Evento eventoSeleccionado = obtenerEventoSeleccionado();
        LocalDate hoy = LocalDate.now();
        if (eventoSeleccionado != null) {
            if (eventoSeleccionado.getFecha_evento().isBefore(hoy)) {
                mostrarAlerta("No permitido", "No se puede editar un evento que ya pasó.");
                return;
            }
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/evento_form.fxml"));
                AnchorPane anchorPane = loader.load();

                // Crear Stage modal en vez de Dialog
                Stage stage = new Stage();
                stage.setTitle("Editar Evento");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(false);
                stage.setScene(new Scene(anchorPane));

                EventoFormController controller = loader.getController();
                controller.setEvento(eventoSeleccionado);
                // Si el controlador tiene setStage, pásalo
                try {
                    controller.getClass().getMethod("setStage", Stage.class).invoke(controller, stage);
                } catch (Exception ignored) {}

                stage.showAndWait();
                // Al cerrar el modal, recargar eventos y actualizar panel
                reloadEvents();
                actualizarPaneDetalleEvento(eventoSeleccionado);
                ActionLogger.log("Evento editado correctamente para la fecha: " + eventoSeleccionado.getFecha_evento());
            } catch (IOException e) {
                ActionLogger.log("Error al intentar editar el evento: " + e.getMessage());
            }
        } else {
            mostrarAlerta("No se ha seleccionado ningún evento", "Por favor, selecciona un evento para editar.");
            ActionLogger.log("Intento de editar sin seleccionar un evento.");
        }
    }

    @FXML
    public void handleBorrar() {
        Evento eventoSeleccionado = obtenerEventoSeleccionado();
        LocalDate hoy = LocalDate.now();
        if (eventoSeleccionado != null) {
            if (eventoSeleccionado.getFecha_evento().isBefore(hoy)) {
                mostrarAlerta("No permitido", "No se puede borrar un evento que ya pasó.");
                return;
            }
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
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/loginAdmin.css", false);
        ActionLogger.log("El usuario volvió al menú principal."); // Log de acción
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

            Evento evento = events.get(fechaActual);
            if (evento != null) {
                if ("Realizado".equals(evento.getEstado())) {
                    etiquetaDia.getStyleClass().add("dia-evento-realizado");
                } else {
                    etiquetaDia.getStyleClass().add("dia-con-evento");
                }
            }

            etiquetaDia.setOnMouseClicked(e -> handleDayClick(fechaActual));

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
        int columnaActual = (diaDeLaSemana - 1 + diasEnElMes) % 7;
        // Si columnaActual es 0, significa que el mes terminó en domingo, no hay que agregar días del mes siguiente
        if (columnaActual == 0) {
            return;
        }
        int numeroDia = 1;
        int filaActual = filasNecesarias;
        YearMonth mesAnioSiguiente = YearMonth.now().plusMonths(1); // O puedes pasar el mes actual y sumarle 1

        while (columnaActual < 7) {
            Label etiquetaDia = new Label(String.valueOf(numeroDia));
            LocalDate fechaActual = LocalDate.of(mesAnioSiguiente.getYear(), mesAnioSiguiente.getMonth(), numeroDia);

            Evento evento = events.get(fechaActual);
            if (evento != null) {
                if ("Realizado".equals(evento.getEstado())) {
                    etiquetaDia.getStyleClass().add("dia-evento-realizado");
                } else {
                    etiquetaDia.getStyleClass().add("dia-con-evento");
                }
            }

            etiquetaDia.setOnMouseClicked(e -> handleDayClick(fechaActual));

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
        LocalDate hoy = LocalDate.now();
        for (Evento evento : eventos) {
            if (evento.getFecha_evento().isBefore(hoy) && !"Realizado".equals(evento.getEstado())) {
                evento.setEstado("Realizado");
                eventoDAO.update(evento);
            }
            addEvent(evento.getFecha_evento(), evento);
        }
        eventoDAO.close();
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
            comboEstado.setValue(evento.getEstado());
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