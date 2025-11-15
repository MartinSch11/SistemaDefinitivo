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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    //private ObservableList<Sabor> saboresSeleccionados = FXCollections.observableArrayList();
    private static ObservableList<Sabor> saboresSeleccionados = FXCollections.observableArrayList();


    private ProductoFormController parentController;

    private Map<String, CheckBox> checkBoxMap = new HashMap<>();


    public void setParentController(ProductoFormController parentController) {
        this.parentController = parentController;
    }



    /*@FXML
    public void initialize() {
        ActionLogger.log("El usuario abrió la ventana de gestión de sabores.");
        cargarSabores();
        cargarSaboresSeleccionados(); // Asegúrate de que esto se llama después de cargar los sabores
    }*/
    @FXML
    public void initialize() {
        ActionLogger.log("El usuario abrió la ventana de gestión de sabores.");
        cargarSabores();
        cargarSaboresSeleccionados();
    }


    public void setSaboresSeleccionados(List<Sabor> sabores) {
        this.saboresSeleccionados.setAll(sabores);

        for (Sabor sabor : sabores) {
            CheckBox checkBox = checkBoxMap.get(sabor.getSabor());
            if (checkBox != null) {
                checkBox.setSelected(true);
            }
        }
    }

    /*
    private void cargarSabores() {
        List<Sabor> sabores = saborDAO.findAll();
        int column = 0;
        int row = 0;

        for (Sabor sabor : sabores) {
            CheckBox checkBox = new CheckBox(sabor.getSabor());
            checkBox.setStyle(
                    "-fx-background-color: #f7ede3; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: #B70505; -fx-padding: 4 10 4 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B70505; -fx-border-width: 1.2; -fx-cursor: hand;");
            checkBox.setOnMouseEntered(_ -> checkBox.setStyle(
                    "-fx-background-color: #F6BBBB; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: #B70505; -fx-padding: 4 10 4 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B70505; -fx-border-width: 1.2; -fx-cursor: hand;"));
            checkBox.setOnMouseExited(_ -> checkBox.setStyle(
                    "-fx-background-color: #f7ede3; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: #B70505; -fx-padding: 4 10 4 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B70505; -fx-border-width: 1.2; -fx-cursor: hand;"));
            checkBox.setOnMousePressed(_ -> checkBox.setStyle(
                    "-fx-background-color: #B70505; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: white; -fx-padding: 4 10 4 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B70505; -fx-border-width: 1.2; -fx-cursor: hand;"));
            checkBox.setOnMouseReleased(_ -> checkBox.setStyle(checkBox.isSelected()
                    ? "-fx-background-color: #B70505; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: white; -fx-padding: 4 10 4 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B70505; -fx-border-width: 1.2; -fx-cursor: hand;"
                    : "-fx-background-color: #f7ede3; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: #B70505; -fx-padding: 4 10 4 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B70505; -fx-border-width: 1.2; -fx-cursor: hand;"));
            checkBox.selectedProperty().addListener((_, _, isNowSelected) -> {
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
            checkBox.setOnAction(_ -> {
                if (checkBox.isSelected()) {
                    ActionLogger.log("El usuario seleccionó el sabor: " + sabor.getSabor());
                    saboresSeleccionados.add(sabor);
                } else {
                    ActionLogger.log("El usuario deseleccionó el sabor: " + sabor.getSabor());
                    saboresSeleccionados.remove(sabor);
                }
            });
        }
    }*/

    private void cargarSabores() {
        List<Sabor> sabores = saborDAO.findAll();

        // Crear un Set con los nombres de sabores seleccionados para facilitar la comparación
        Set<String> seleccionados = saboresSeleccionados.stream()
                .map(Sabor::getSabor)
                .collect(Collectors.toSet());


        gridSabores.getChildren().clear(); // Limpiar grid
        checkBoxMap.clear();

        //gridSabores.getChildren().clear(); // Limpiar grid antes de agregar

        int column = 0;
        int row = 0;

        for (Sabor sabor : sabores) {
            CheckBox checkBox = new CheckBox(sabor.getSabor());

            // Estilos personalizados para el checkbox
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

            // Seleccionar el checkbox si está en la lista de seleccionados
            if (seleccionados.contains(sabor.getSabor())) {
                checkBox.setSelected(true);
            }

            // Agregar acción para actualizar saboresSeleccionados
            checkBox.setOnAction(e -> {
                if (checkBox.isSelected()) {
                    if (!saboresSeleccionados.contains(sabor)) {
                        saboresSeleccionados.add(sabor);
                        ActionLogger.log("El usuario seleccionó el sabor: " + sabor.getSabor());
                    }
                } else {
                    saboresSeleccionados.remove(sabor);
                    ActionLogger.log("El usuario deseleccionó el sabor: " + sabor.getSabor());
                }
            });

            // Agregar el checkbox al GridPane
            gridSabores.add(checkBox, column, row);

            checkBoxMap.put(sabor.getSabor(), checkBox); // GUARDAR CHECKBOX EN EL MAPA

            column++;
            if (column == 5) {
                column = 0;
                row++;
            }
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
            // Reconstruir la lista de sabores seleccionados según los checkboxes
            ObservableList<Sabor> seleccionados = FXCollections.observableArrayList();
            for (Node node : gridSabores.getChildren()) {
                if (node instanceof CheckBox checkBox && checkBox.isSelected()) {
                    // Buscar el sabor correspondiente por el texto del checkbox
                    for (Sabor sabor : saborDAO.findAll()) {
                        if (sabor.getSabor().equals(checkBox.getText())) {
                            seleccionados.add(sabor);
                            break;
                        }
                    }
                }
            }
            saboresSeleccionados.setAll(seleccionados); // Actualizar la lista interna
            ActionLogger.log("El usuario guardó los sabores seleccionados.");
            System.out.println("DEBUG SaboresController: saboresSeleccionados=" + saboresSeleccionados);
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
