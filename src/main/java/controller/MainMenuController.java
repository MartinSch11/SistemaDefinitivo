package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utilities.Paths;

public class MainMenuController {
    public Button btnPedidos;

    @FXML
    private Button btnAyudaMenu;

    @FXML
    private Button btnEventos;

    @FXML
    private Button adminLogin;


    @FXML
    void AyudaMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Paths.AYUDA_MENU));
            AnchorPane root = loader.load();
            Scene scene = new Scene(root);


            scene.getStylesheets().add(getClass().getResource("/css/components.css").toExternalForm());


            Stage stage = (Stage) btnAyudaMenu.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void changeUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Paths.LOGIN));
            AnchorPane root = loader.load();
            Scene scene = new Scene(root);


            scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());


            Stage stage = (Stage) adminLogin.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
