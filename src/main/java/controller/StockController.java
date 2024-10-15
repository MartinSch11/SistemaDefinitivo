package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import utilities.Paths;
import utilities.SceneLoader;

import java.io.IOException;
import java.util.Optional;

public class StockController {

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.ADMIN_MAINMENU, "/css/loginAdmin.css", true);
    }
    @FXML
    public void handleAgregar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/stock_form.fxml"));
            DialogPane dialogPane = loader.load();

            // Crear un diálogo
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Agregar Insumo");

            StockFormController controller = loader.getController();  // Cambia ProductoFormController a InsumoFormController

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                //cargarInsumos(); // Actualiza la tabla de insumos si se agrega uno nuevo
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void handleModificar (ActionEvent event) {

    }
    @FXML
    void handleEliminar (ActionEvent event) {

    }
}
