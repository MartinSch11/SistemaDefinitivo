package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.Getter;
import utilities.Paths;

public class App extends Application {

    @Getter
    private static EntityManagerFactory entityManagerFactory;

    public static void main(String[] args) {
        entityManagerFactory = Persistence.createEntityManagerFactory("pasteleriaPU");
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        AnchorPane load = FXMLLoader.load(getClass().getResource(Paths.LOGIN));
        Scene scene = new Scene(load);
        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        // Cierra el EntityManagerFactory cuando la aplicación se detenga
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
        super.stop();
    }

}
