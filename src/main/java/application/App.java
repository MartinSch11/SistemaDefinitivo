package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utilities.Paths;
import java.util.Objects;
import persistence.dao.TrabajadorDAO;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.stream.Collectors;
import persistence.dao.RolesDAO;
import utilities.JpaUtil;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class App extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Verificar si la tabla de roles está vacía
        RolesDAO rolesDAO = new RolesDAO();
        boolean rolesVacios = rolesDAO.findAll().isEmpty();

        if (rolesVacios) {
            // Ejecutar import.sql solo si la tabla de roles está vacía
            String url = "jdbc:h2:./data/miappdb;MODE=MySQL";
            String user = "sa";
            String password = "";
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("import.sql")) {
                if (is != null) {
                    String sql = new BufferedReader(new InputStreamReader(is))
                            .lines()
                            .filter(line -> !line.trim().startsWith("--") && !line.trim().isEmpty())
                            .collect(Collectors.joining("\n"));
                    try (Connection conn = DriverManager.getConnection(url, user, password);
                         Statement stmt = conn.createStatement()) {
                        for (String query : sql.split(";")) {
                            if (!query.trim().isEmpty()) {
                                stmt.execute(query);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Verificar si hay trabajadores
        TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
        boolean hayTrabajadores = !trabajadorDAO.findAll().isEmpty();

        if (!hayTrabajadores) {
            // Mostrar formulario de primer admin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/PrimerAdmin.fxml"));
            AnchorPane root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/login.css")).toExternalForm());
            Stage modal = new Stage();
            modal.setTitle("Registro de primer usuario");
            modal.setScene(scene);
            modal.initOwner(stage);
            modal.showAndWait();
            // Volver a verificar si hay trabajadores después de cerrar el modal
            hayTrabajadores = !trabajadorDAO.findAll().isEmpty();
            if (!hayTrabajadores) {
                // Si sigue sin haber trabajadores, cerrar la aplicación
                System.exit(0);
            }
        }

        // Luego mostrar el login normalmente
        AnchorPane load = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(Paths.LOGIN)));
        Scene scene = new Scene(load);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/login.css")).toExternalForm());
        stage.setTitle("Diseño de Sabores");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com.example.image/iconoPasteleria.png"))));
        stage.setMaximized(false);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        // Cierra el EntityManagerFactory cuando la aplicación se detenga
        JpaUtil.close();
        super.stop();
    }
}
