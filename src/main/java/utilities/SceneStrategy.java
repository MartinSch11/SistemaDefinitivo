package utilities;

import javafx.scene.Scene;
import javafx.stage.Stage;

public interface SceneStrategy {
    void loadScene(Stage stage, Scene scene, boolean maximized);
}
