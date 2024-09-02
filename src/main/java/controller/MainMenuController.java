package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utilities.Paths;

public class MainMenuController {

    @FXML
    public Button btnPedidos;

    @FXML
    private Button btnAyudaMenu;

    @FXML
    private Button btnEventos;

    @FXML
    private Button adminLogin;

    //Método genérico para cambiar la escena en base al FXML y hoja de estilos proporcionada.

    private void changeScene(String fxmlPath, String cssPath, Button button, boolean maximizeStage) {
        try {
            // Cargar el archivo FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane root = loader.load();
            Scene scene = new Scene(root);

            // Añadir el estilo si se proporciona
            if (cssPath != null && !cssPath.isEmpty()) {
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }

            // Obtener la ventana actual a partir de cualquier nodo de la escena
            Stage stage = (Stage) button.getScene().getWindow();

            // Maximizar la ventana si es necesario
            if (maximizeStage) {
                stage.setMaximized(true);
            }

            // Cambiar la escena
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void AyudaMenu(ActionEvent event) {
        changeScene(Paths.AYUDA_MENU, "/css/components.css", btnAyudaMenu, false);
    }

    @FXML
    void changeUser(ActionEvent event) {
        changeScene(Paths.LOGIN, "/css/login.css", adminLogin, false);
    }

    @FXML
    void handleEventos(ActionEvent event) {
        changeScene(Paths.EVENTOS, "/css/eventos.css", btnEventos, true);
    }
}
