package utilities;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.MenuButton;

public class MenuItemSceneStrategy implements SceneStrategy {

    private final MenuButton sourceMenuButton;

    public MenuItemSceneStrategy(MenuButton sourceMenuButton) {
        this.sourceMenuButton = sourceMenuButton;
    }

    @Override
    public void loadScene(Stage stage, Scene scene, boolean maximized) {
        // Obtener el Stage desde el MenuButton
        Stage currentStage = (Stage) sourceMenuButton.getScene().getWindow();
        currentStage.setMaximized(maximized);
        currentStage.setScene(scene);

        // Centrar la ventana en la pantalla
        currentStage.centerOnScreen();

        currentStage.show();
    }
}
