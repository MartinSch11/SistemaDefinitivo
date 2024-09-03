package utilities;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class DirectStageStrategy implements SceneStrategy {

    // Implementación del método loadScene para manejar el Stage directamente
    @Override
    public void loadScene(Stage stage, Scene scene, boolean maximized) {
        // Maximizar si es necesario
        stage.setMaximized(maximized);

        // Cambiar la escena
        stage.setScene(scene);
        stage.show();
    }
}
