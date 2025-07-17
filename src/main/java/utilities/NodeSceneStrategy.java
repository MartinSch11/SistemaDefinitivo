package utilities;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NodeSceneStrategy implements SceneStrategy {

    private final Node sourceNode;

    public NodeSceneStrategy(Node sourceNode) {
        this.sourceNode = sourceNode;
    }

    @Override
    public void loadScene(Stage stage, Scene scene, boolean maximized) {
        Stage currentStage = (Stage) sourceNode.getScene().getWindow();
        currentStage.setMaximized(maximized);
        currentStage.setScene(scene);
        currentStage.centerOnScreen();
        currentStage.show();
    }
}
