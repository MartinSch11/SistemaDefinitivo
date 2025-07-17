package utilities;

import model.SessionContext;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;

public class ActionLogger {

    private static String getLogFileName() {
        String directory = System.getProperty("user.dir") + "/logs";
        java.nio.file.Path path = java.nio.file.Paths.get(directory);

        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                System.out.println("Directorio creado: " + path.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error al crear el directorio de logs: " + e.getMessage());
            return null;
        }

        // Nombre del archivo basado en la fecha actual
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return directory + "/acciones-" + currentDate + ".log";
    }

    public static void log(String accion) {
        SessionContext session = SessionContext.getInstance();
        String usuario = session.getUserName();
        String rol = session.getRoleName() != null ? session.getRoleName() : "Sin rol";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = String.format("[%s] Usuario: %s (Rol: %s) Acción: %s%n", timestamp, usuario, rol, accion);

        String logFile = getLogFileName();
        if (logFile == null) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(logEntry);
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo de log: " + e.getMessage());
        }
    }
}
