package controller;

import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.DatePicker;
import javafx.util.Callback;

import java.time.LocalDate;

public class EventoFormController {
    @FXML
    private TextField nombreEventoField;
    @FXML
    private TextArea descripcionEventoField;
    @FXML
    private TextField nombreClienteField;
    @FXML
    private TextField telefonoClienteField;
    @FXML
    private TextField direccionEventoField;
    @FXML
    private DatePicker fechaEventoPicker;  // Nuevo campo para la fecha

    public String getNombreEvento() {
        return nombreEventoField.getText();
    }

    public String getDescripcionEvento() {
        return descripcionEventoField.getText();
    }

    public String getNombreCliente() {
        return nombreClienteField.getText();
    }

    public String getTelefonoCliente() {
        return telefonoClienteField.getText();
    }

    public String getDireccionEvento() {
        return direccionEventoField.getText();
    }

    public LocalDate getFechaEvento() {
        return fechaEventoPicker.getValue();  // Obtener la fecha seleccionada
    }
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
                            // Lógica para deshabilitar fechas o personalizar el DatePicker
                            if (date.isBefore(LocalDate.now())) {
                                setDisable(true);
                                setStyle("-fx-background-color: #ffc0cb;"); // Ejemplo: Color para fechas pasadas
                            }
                        }
                    };
                }
            });
        }
    }
}
