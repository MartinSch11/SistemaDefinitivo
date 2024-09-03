package utilities;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NodeSceneStrategy implements SceneStrategy {
    private final Node node;

    // Constructor que acepta un Node
    public NodeSceneStrategy(Node node) {
        this.node = node;
    }

    // Implementación del método loadScene para manejar el Stage a partir del Node
    @Override
    public void loadScene(Stage stage, Scene scene, boolean maximized) {
        // Obtener el Stage desde el Node
        Stage currentStage = (Stage) node.getScene().getWindow();

        // Maximizar si es necesario
        currentStage.setMaximized(maximized);

        // Cambiar la escena
        currentStage.setScene(scene);
        currentStage.show();
    }
}
