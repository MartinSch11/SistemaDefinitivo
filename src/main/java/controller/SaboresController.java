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
import javafx.scene.layout.FlowPane; // <--- CAMBIO DE IMPORT
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
    private FlowPane flowSabores;
    @FXML
    private Button btnCancelar;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnAgregarSabor;

    private SaborDAO saborDAO = new SaborDAO();

    private static ObservableList<Sabor> saboresSeleccionados = FXCollections.observableArrayList();

    private ProductoFormController parentController;

    private Map<String, CheckBox> checkBoxMap = new HashMap<>();

    public void setParentController(ProductoFormController parentController) {
        this.parentController = parentController;
    }

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

    private void cargarSabores() {
        List<Sabor> sabores = saborDAO.findAll();

        Set<String> seleccionados = saboresSeleccionados.stream()
                .map(Sabor::getSabor)
                .collect(Collectors.toSet());

        flowSabores.getChildren().clear();
        checkBoxMap.clear();

        for (Sabor sabor : sabores) {
            CheckBox checkBox = new CheckBox(sabor.getSabor());

            // --- DEFINICIÓN DE ESTILOS ---
            // 1. Estilo Base (Inactivo): Fondo BLANCO (#FFFFFF) para que contraste con el
            // fondo beige
            String estiloInactivo = "-fx-background-color: #FFFFFF; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: #B70505; -fx-padding: 4 10 4 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B70505; -fx-border-width: 1.2; -fx-cursor: hand;";

            // 2. Estilo Hover: Un rojo muy suave (#FFE5E5) o el que tenías (#F6BBBB)
            String estiloHover = "-fx-background-color: #F6BBBB; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: #B70505; -fx-padding: 4 10 4 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B70505; -fx-border-width: 1.2; -fx-cursor: hand;";

            // 3. Estilo Seleccionado: Rojo fuerte (#B70505) y texto blanco
            String estiloSeleccionado = "-fx-background-color: #B70505; -fx-font-family: 'Inter'; -fx-font-size: 12px; -fx-text-fill: white; -fx-padding: 4 10 4 10; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #B70505; -fx-border-width: 1.2; -fx-cursor: hand;";

            // Aplicar estilo inicial
            checkBox.setStyle(estiloInactivo);

            // --- EVENTOS DEL MOUSE ---

            // Entrar: Si no está seleccionado, mostrar color hover
            checkBox.setOnMouseEntered(e -> {
                if (!checkBox.isSelected())
                    checkBox.setStyle(estiloHover);
            });

            // Salir: Si está seleccionado mantiene el rojo, si no, vuelve a BLANCO
            checkBox.setOnMouseExited(
                    e -> checkBox.setStyle(checkBox.isSelected() ? estiloSeleccionado : estiloInactivo));

            // Presionar: Feedback visual inmediato (como seleccionado)
            checkBox.setOnMousePressed(e -> checkBox.setStyle(estiloSeleccionado));

            // Soltar: Depende de si quedó seleccionado o no
            checkBox.setOnMouseReleased(
                    e -> checkBox.setStyle(checkBox.isSelected() ? estiloSeleccionado : estiloHover)); 

            // Cambio de estado lógico (por clic o por código)
            checkBox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    checkBox.setStyle(estiloSeleccionado);
                } else {
                    checkBox.setStyle(estiloInactivo); // Vuelve a blanco
                }
            });

            // Seleccionar si ya estaba en la lista al abrir
            if (seleccionados.contains(sabor.getSabor())) {
                checkBox.setSelected(true);
                checkBox.setStyle(estiloSeleccionado); // Forzar estilo visual
            }

            // Acción al clickear (Lógica de negocio)
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

            flowSabores.getChildren().add(checkBox);
            checkBoxMap.put(sabor.getSabor(), checkBox);
        }
    }

    private void cargarSaboresSeleccionados() {
        // Limpiar selección visual previa
        for (Node node : flowSabores.getChildren()) {
            if (node instanceof CheckBox checkBox) {
                checkBox.setSelected(false);
            }
        }

        // Marcar los checkboxes según los sabores seleccionados
        for (Sabor sabor : saboresSeleccionados) {
            for (Node node : flowSabores.getChildren()) {
                if (node instanceof CheckBox checkBox && checkBox.getText().equals(sabor.getSabor())) {
                    checkBox.setSelected(true);
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
            ObservableList<Sabor> seleccionados = FXCollections.observableArrayList();
            for (Node node : flowSabores.getChildren()) {
                if (node instanceof CheckBox checkBox && checkBox.isSelected()) {
                    for (Sabor sabor : saborDAO.findAll()) {
                        if (sabor.getSabor().equals(checkBox.getText())) {
                            seleccionados.add(sabor);
                            break;
                        }
                    }
                }
            }
            saboresSeleccionados.setAll(seleccionados);
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

            // Refrescar la lista
            flowSabores.getChildren().clear();
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