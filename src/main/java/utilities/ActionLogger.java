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
        // CAMBIO CLAVE: Usamos user.home en vez de user.dir
        // En Windows esto te lleva a C:\Users\TuUsuario
        String userHome = System.getProperty("user.home");

        // Armamos una ruta segura donde SIEMPRE hay permisos de escritura.
        // En Windows lo estándar es AppData/Local, pero si querés simplificar
        // podés ponerlo directo en una carpeta en userHome.
        // Opción PRO (AppData):
        String directory = userHome + "\\AppData\\Local\\SistemaDefinitivo\\logs";

        // Opción SIMPLE (Carpeta visible en usuario):
        // String directory = userHome + "\\SistemaDefinitivo_Logs";

        java.nio.file.Path path = java.nio.file.Paths.get(directory);

        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                // Ojo: Este println en producción no lo vas a ver a menos que lances por
                // consola
                // System.out.println("Directorio creado: " + path.toAbsolutePath());
            }
        } catch (IOException e) {
            // Si esto falla acá, es crítico. Imprimimos en error estándar por si se lanza
            // por consola.
            e.printStackTrace();
            return null;
        }

        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return directory + "\\acciones-" + currentDate + ".log";
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
