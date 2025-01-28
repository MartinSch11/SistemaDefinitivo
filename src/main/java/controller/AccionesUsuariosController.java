package controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import model.ActionLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AccionesUsuariosController {

    @FXML private DatePicker datePicker;
    @FXML private TableView<ActionLog> tableAcciones;
    @FXML private TableColumn<ActionLog, String> colFecha;
    @FXML private TableColumn<ActionLog, String> colUsuario;
    @FXML private TableColumn<ActionLog, String> colRol;
    @FXML private TableColumn<ActionLog, String> colAccion;

    @FXML
    public void initialize() {
        // Enlazar columnas con propiedades de ActionLog
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colAccion.setCellValueFactory(new PropertyValueFactory<>("accion"));

        // Filtrar fechas futuras en el DatePicker
        datePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(LocalDate.now())); // Deshabilita días futuros
            }
        });

        // Listener para cargar logs según la fecha seleccionada
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                cargarLogsPorFecha(newValue);
            }
        });
    }

    private void cargarLogsPorFecha(LocalDate fecha) {
        String fechaSeleccionada = fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<ActionLog> logs = obtenerLogsDeArchivo(fechaSeleccionada);

        if (logs.isEmpty()) {
            mostrarAlerta("Sin datos", "No se encontraron logs para la fecha seleccionada.");
        }

        tableAcciones.getItems().setAll(logs); // Actualizar la tabla con los nuevos datos
    }

    private List<ActionLog> obtenerLogsDeArchivo(String fechaSeleccionada) {
        List<ActionLog> logs = new ArrayList<>();

        // Utilizamos la ruta absoluta, asumiendo que la carpeta logs está en la raíz del proyecto
        String logFileName = "logs/acciones-" + fechaSeleccionada + ".log";
        File logFile = new File(logFileName);

        // Verificamos si el archivo existe y si la carpeta 'logs' existe
        if (!logFile.exists()) {
            mostrarAlerta("Archivo no encontrado", "No existe un archivo de logs para la fecha seleccionada.");
            return logs;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Verificar si la línea contiene la fecha seleccionada
                if (line.contains(fechaSeleccionada)) {
                    try {
                        // Extraer los datos mediante split y substring
                        String timestamp = line.substring(1, 20); // [YYYY-MM-DD HH:mm:ss]
                        String usuario = extractValue(line, "Usuario:", "(Rol:");
                        String rol = extractValue(line, "(Rol:", ") Acción:");
                        String accion = line.substring(line.indexOf("Acción:") + 7).trim();

                        logs.add(new ActionLog(timestamp, usuario, rol, accion));
                    } catch (Exception e) {
                        System.err.println("Error procesando la línea: " + line);
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de logs: " + e.getMessage());
        }

        return logs;
    }

    // Método auxiliar para extraer valores delimitados en una línea de log
    private String extractValue(String line, String startDelimiter, String endDelimiter) {
        int start = line.indexOf(startDelimiter) + startDelimiter.length();
        int end = line.indexOf(endDelimiter, start);
        if (start > -1 && end > -1 && start < end) {
            return line.substring(start, end).trim();
        }
        return "Desconocido";
    }

    // Método para mostrar alertas al usuario
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
