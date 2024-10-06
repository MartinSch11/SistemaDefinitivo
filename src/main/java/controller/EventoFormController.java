package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Evento;
import persistence.dao.EventoDAO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EventoFormController {

    @FXML private TextField nombreEventoField;
    @FXML private TextArea descripcionEventoField;
    @FXML private TextField nombreClienteField;
    @FXML private TextField telefonoClienteField;
    @FXML private TextField direccionEventoField;
    @FXML private DatePicker fechaEventoPicker;
    @FXML private TextField cantPersonasField;
    @FXML private TextField presupuestoField;

    private Evento eventoActual;

    @FXML
    public void initialize() {
        if (fechaEventoPicker != null) {
            fechaEventoPicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
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

    public void setEvento(Evento evento) {
        this.eventoActual = evento;
        if (evento != null) {
            cargarDatosEvento(evento);
        }
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        String nombreEvento = nombreEventoField.getText();
        String descripcionEvento = descripcionEventoField.getText();
        String nombreCliente = nombreClienteField.getText();
        String telefonoCliente = telefonoClienteField.getText();
        String direccionEvento = direccionEventoField.getText();
        LocalDate fechaEvento = fechaEventoPicker.getValue();

        // Convertir los valores de los TextFields
        int cantPersonas;
        BigDecimal presupuesto;
        try {
            cantPersonas = Integer.parseInt(cantPersonasField.getText());
            presupuesto = new BigDecimal(presupuestoField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "Por favor, ingresa valores válidos para la cantidad de personas y el presupuesto.");
            return;
        }

        if (!validateFields(nombreEvento, descripcionEvento, nombreCliente, telefonoCliente, direccionEvento, fechaEvento, cantPersonas, presupuesto)) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "Todos los campos deben ser llenados.");
            return;
        }

        try {
            EventoDAO eventoDAO = new EventoDAO();

            // Verificar si ya existe un evento en la fecha seleccionada (solo si es creación)
            if (eventoActual == null) {
                Evento eventoExistente = eventoDAO.findByFecha(fechaEvento);
                if (eventoExistente != null) {
                    showAlert(Alert.AlertType.ERROR, "Error de Validación", "Ya existe un evento programado para esta fecha.");
                    return;
                }
            }

            if (eventoActual != null) {
                // Actualizar el evento existente
                eventoActual.setNombre_evento(nombreEvento);
                eventoActual.setDescripcion_evento(descripcionEvento);
                eventoActual.setNombre_cliente(nombreCliente);
                eventoActual.setTelefono_cliente(telefonoCliente);
                eventoActual.setDireccion_evento(direccionEvento);
                eventoActual.setFecha_evento(fechaEvento);
                eventoActual.setCant_personas(cantPersonas);
                eventoActual.setPresupuesto(presupuesto);

                eventoDAO.update(eventoActual);  // Método update para actualizar el evento
            } else {
                // Crear un nuevo evento
                Evento nuevoEvento = new Evento(nombreEvento, descripcionEvento, nombreCliente, telefonoCliente, direccionEvento, fechaEvento, cantPersonas, presupuesto);
                eventoDAO.save(nuevoEvento);
            }

            eventoDAO.close();

            // Cerrar el formulario
            Stage stage = (Stage) nombreEventoField.getScene().getWindow();
            stage.close();

            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Evento guardado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error al guardar el evento: " + e.getMessage());
        }
    }

    private boolean validateFields(String nombreEvento, String descripcionEvento, String nombreCliente, String telefonoCliente, String direccionEvento, LocalDate fechaEvento, int cantPersonas, BigDecimal presupuesto) {
        return nombreEvento != null && !nombreEvento.isEmpty() &&
                descripcionEvento != null && !descripcionEvento.isEmpty() &&
                nombreCliente != null && !nombreCliente.isEmpty() &&
                telefonoCliente != null && !telefonoCliente.isEmpty() &&
                direccionEvento != null && !direccionEvento.isEmpty() &&
                fechaEvento != null &&
                cantPersonas > 0 &&
                presupuesto != null && presupuesto.compareTo(BigDecimal.ZERO) > 0;
    }

    public void cargarDatosEvento(Evento evento) {
        nombreEventoField.setText(evento.getNombre_evento());
        descripcionEventoField.setText(evento.getDescripcion_evento());
        nombreClienteField.setText(evento.getNombre_cliente());
        telefonoClienteField.setText(evento.getTelefono_cliente());
        direccionEventoField.setText(evento.getDireccion_evento());
        fechaEventoPicker.setValue(evento.getFecha_evento());
        cantPersonasField.setText(String.valueOf(evento.getCant_personas()));
        presupuestoField.setText(evento.getPresupuesto().toString());
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
