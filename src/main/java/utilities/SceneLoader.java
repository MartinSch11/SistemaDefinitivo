package utilities;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class SceneLoader {

    public static void loadScene(SceneStrategy strategy, String fxmlPath, String stylesheetPath, boolean maximized) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(SceneLoader.class.getResource(fxmlPath));
            AnchorPane root = fxmlLoader.load();
            Scene scene = new Scene(root);

            if (stylesheetPath != null && !stylesheetPath.isEmpty()) {
                scene.getStylesheets().add(Objects.requireNonNull(SceneLoader.class.getResource(stylesheetPath)).toExternalForm());
            }

            Stage stage = new Stage();
            strategy.loadScene(stage, scene, maximized);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FXMLLoader loadDialogPane(String fxmlPath, String cssPath) {
        FXMLLoader loader = new FXMLLoader(SceneLoader.class.getResource(fxmlPath));
        try {
            DialogPane dialogPane = loader.load();
            if (cssPath != null) {
                dialogPane.getStylesheets().add(SceneLoader.class.getResource(cssPath).toExternalForm());
            }
            return loader;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void handleVolver(ActionEvent event, String fxmlPath, String stylesheetPath, boolean maximized) {
        Node sourceNode = (Node) event.getSource();
        SceneStrategy strategy = new NodeSceneStrategy(sourceNode);
        loadScene(strategy, fxmlPath, stylesheetPath, maximized);
    }

}
