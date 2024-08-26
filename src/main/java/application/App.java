package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utilities.Paths;

public class App extends Application {

    public static void main(String[] args){
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        AnchorPane load = FXMLLoader.load(getClass().getResource(Paths.LOGIN));
        Scene scene = new Scene(load);
        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
