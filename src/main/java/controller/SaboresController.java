package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Sabor;
import persistence.dao.SaborDAO;
import utilities.ActionLogger;

import java.io.IOException;
import java.util.List;

public class SaboresController {

    @FXML
    private GridPane gridSabores;
    @FXML
    private Button btnCancelar;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnAgregarSabor;

    private SaborDAO saborDAO = new SaborDAO();
    private ObservableList<Sabor> saboresSeleccionados = FXCollections.observableArrayList();
    private ProductoFormController parentController;

    public void setParentController(ProductoFormController parentController) {
        this.parentController = parentController;
    }

    public void setSaboresSeleccionados(List<Sabor> sabores) {
        this.saboresSeleccionados.setAll(sabores); // Almacena los sabores seleccionados
        cargarSaboresSeleccionados(); // Cargar los sabores seleccionados en los checkboxes
    }

    @FXML
    public void initialize() {
        ActionLogger.log("El usuario abrió la ventana de gestión de sabores.");
        cargarSabores();
        cargarSaboresSeleccionados(); // Asegúrate de que esto se llama después de cargar los sabores
    }

    private void cargarSabores() {
        List<Sabor> sabores = saborDAO.findAll();
        int column = 0;
        int row = 0;

        for (Sabor sabor : sabores) {
            CheckBox checkBox = new CheckBox(sabor.getSabor());
            checkBox.setStyle(
                    "-fx-background-color: #f7ede3; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: #B70505; -fx-padding: 4 10 4 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B70505; -fx-border-width: 1.2; -fx-cursor: hand;");
            checkBox.setOnMouseEntered(e -> checkBox.setStyle(
                    "-fx-background-color: #F6BBBB; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: #B70505; -fx-padding: 4 10 4 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B70505; -fx-border-width: 1.2; -fx-cursor: hand;"));
            checkBox.setOnMouseExited(e -> checkBox.setStyle(
                    "-fx-background-color: #f7ede3; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: #B70505; -fx-padding: 4 10 4 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B70505; -fx-border-width: 1.2; -fx-cursor: hand;"));
            checkBox.setOnMousePressed(e -> checkBox.setStyle(
                    "-fx-background-color: #B70505; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: white; -fx-padding: 4 10 4 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B70505; -fx-border-width: 1.2; -fx-cursor: hand;"));
            checkBox.setOnMouseReleased(e -> checkBox.setStyle(checkBox.isSelected()
                    ? "-fx-background-color: #B70505; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: white; -fx-padding: 4 10 4 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B70505; -fx-border-width: 1.2; -fx-cursor: hand;"
                    : "-fx-background-color: #f7ede3; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: #B70505; -fx-padding: 4 10 4 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B70505; -fx-border-width: 1.2; -fx-cursor: hand;"));
            checkBox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    checkBox.setStyle(
                            "-fx-background-color: #B70505; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: white; -fx-padding: 4 10 4 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B70505; -fx-border-width: 1.2; -fx-cursor: hand;");
                } else {
                    checkBox.setStyle(
                            "-fx-background-color: #f7ede3; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: #B70505; -fx-padding: 4 10 4 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B70505; -fx-border-width: 1.2; -fx-cursor: hand;");
                }
            });
            // Agregar el checkbox al GridPane
            gridSabores.add(checkBox, column, row);
            column++;
            if (column == 5) {
                column = 0;
                row++;
            }
            // Agregar acción al checkbox
            checkBox.setOnAction(e -> {
                if (checkBox.isSelected()) {
                    ActionLogger.log("El usuario seleccionó el sabor: " + sabor.getSabor());
                    saboresSeleccionados.add(sabor);
                } else {
                    ActionLogger.log("El usuario deseleccionó el sabor: " + sabor.getSabor());
                    saboresSeleccionados.remove(sabor);
                }
            });
        }
    }

    private void cargarSaboresSeleccionados() {
        // Limpiar selección previa
        for (Node node : gridSabores.getChildren()) {
            if (node instanceof CheckBox checkBox) {
                checkBox.setSelected(false); // Limpiar selección previa
            }
        }

        // Marcar los checkboxes según los sabores seleccionados
        for (Sabor sabor : saboresSeleccionados) {
            for (Node node : gridSabores.getChildren()) {
                if (node instanceof CheckBox checkBox && checkBox.getText().equals(sabor.getSabor())) {
                    checkBox.setSelected(true); // Marcar como seleccionado
                    break;
                }
            }
        }
    }

    public List<Sabor> getSaboresSeleccionados() {
        return saboresSeleccionados;
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        if (parentController != null) {
            ActionLogger.log("El usuario guardó los sabores seleccionados.");
            parentController.setSaboresSeleccionados(saboresSeleccionados);
            ((Stage) btnGuardar.getScene().getWindow()).close();
        }
    }

    @FXML
    void handleNuevoSabor(ActionEvent event) {
        try {
            ActionLogger.log("El usuario quiere agregar un nuevo sabor.");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/DialogNuevoSabor.fxml"));
            AnchorPane root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Agregar Nuevo Sabor");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initOwner(btnAgregarSabor.getScene().getWindow());
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();

            // Refrescar la lista de sabores después de cerrar el diálogo
            gridSabores.getChildren().clear();
            cargarSabores();
            cargarSaboresSeleccionados();
        } catch (IOException e) {
            ActionLogger.log("Error al abrir la ventana de nuevo sabor: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        ActionLogger.log("El usuario canceló la selección de sabores.");
        ((Stage) btnCancelar.getScene().getWindow()).close();
    }
}
