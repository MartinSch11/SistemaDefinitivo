package controller;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import utilities.Paths;
import javafx.scene.control.Button;

public class AyudaController {

    @FXML
    private Button btnVolverAyudaMenu;

    /*
    public static class FullScreenViewApp extends Application {
        @Override
        public void start(Stage primaryStage) {
            Pane pane = new Pane();
            pane.getStyleClass().add("pane-principal");
            Scene scene = new Scene(pane, 1366, 768);
            scene.getStylesheets().add(getClass().getResource("@/css/components.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setFullScreen(true);
            primaryStage.show();
        }

        public static void main(String[] args) {
            launch(args);
        }
    }
*/
    /*----------------------------------------------*/

    @FXML
    void backMainMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Paths.MAINMENU));
            AnchorPane root = loader.load();
            Scene scene = new Scene(root);


            scene.getStylesheets().add(getClass().getResource("/css/mainMenu.css").toExternalForm());


            Stage stage = (Stage) btnVolverAyudaMenu.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
