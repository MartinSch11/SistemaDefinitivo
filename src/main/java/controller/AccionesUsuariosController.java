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
        datePicker.setDayCellFactory(_ -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(LocalDate.now())); // Deshabilita días futuros
            }
        });

        // Listener para cargar logs según la fecha seleccionada
        datePicker.valueProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                cargarLogsPorFecha(newValue);
            }
        });
    }

    private void cargarLogsPorFecha(LocalDate fecha) {
        String fechaSeleccionada = fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<ActionLog> logs = obtenerLogsDeArchivo(fechaSeleccionada);

        if (logs.isEmpty()) {
            // Ojo: Si es un día que no hubo actividad, es normal que esté vacío.
            // A veces es molesto el popup cada vez que clickeas un día vacío, 
            // pero dejalo si te sirve para confirmar.
            mostrarAlerta("Sin datos", "No se encontraron logs para la fecha seleccionada.");
        }

        tableAcciones.getItems().setAll(logs); 
    }

    private List<ActionLog> obtenerLogsDeArchivo(String fechaSeleccionada) {
        List<ActionLog> logs = new ArrayList<>();

        // -----------------------------------------------------------------------
        // CAMBIO IMPORTANTE: Usamos la ruta de usuario (user.home)
        // Esta ruta TIENE que coincidir con la que pusiste en ActionLogger.java
        // -----------------------------------------------------------------------
        String userHome = System.getProperty("user.home");
        
        // Si usaste la opción PRO (AppData):
        String directory = userHome + "\\AppData\\Local\\SistemaDefinitivo\\logs";
        
        // Si usaste la opción SIMPLE, descomentá esta y comentá la de arriba:
        // String directory = userHome + "\\SistemaDefinitivo_Logs";

        String fileName = "acciones-" + fechaSeleccionada + ".log";
        
        // Usamos el constructor (File parent, String child) para unir la ruta y el nombre
        File logFile = new File(directory, fileName);

        // Verificamos si el archivo existe
        if (!logFile.exists()) {
            // Si no existe el archivo, retornamos la lista vacía nomás
            return logs;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Verificar si la línea contiene la fecha seleccionada (filtro extra de seguridad)
                if (line.contains(fechaSeleccionada)) {
                    try {
                        // Extraer los datos mediante split y substring
                        // Ojo con los índices acá, si cambiás el formato del Log, esto se rompe.
                        String timestamp = line.substring(1, 20); // [YYYY-MM-DD HH:mm:ss]
                        String usuario = extractValue(line, "Usuario:", "(Rol:");
                        String rol = extractValue(line, "(Rol:", ") Acción:");
                        String accion = line.substring(line.indexOf("Acción:") + 7).trim();

                        logs.add(new ActionLog(timestamp, usuario, rol, accion));
                    } catch (Exception e) {
                        System.err.println("Error procesando la línea: " + line);
                        // No hacemos printStackTrace en producción para no ensuciar, 
                        // pero para debugear viene bien.
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de logs: " + e.getMessage());
            mostrarAlerta("Error de Lectura", "No se pudo leer el archivo de logs. Verificá permisos.");
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

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}