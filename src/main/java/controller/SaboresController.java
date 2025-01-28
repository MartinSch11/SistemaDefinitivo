package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Sabor;
import persistence.dao.SaborDAO;
import utilities.ActionLogger;
import utilities.NodeSceneStrategy;
import utilities.Paths;
import utilities.SceneLoader;

import java.util.List;

public class SaboresController {

    @FXML private GridPane gridSabores;
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;
    @FXML private Button btnAgregarSabor;

    private SaborDAO saborDAO = new SaborDAO();
    private ObservableList<Sabor> saboresSeleccionados = FXCollections.observableArrayList();
    private ProductoFormController parentController;

    public void setParentController(ProductoFormController parentController) {
        this.parentController = parentController;
    }

    public void setSaboresSeleccionados(List<Sabor> sabores) {
        this.saboresSeleccionados.setAll(sabores);  // Almacena los sabores seleccionados
        cargarSaboresSeleccionados();  // Cargar los sabores seleccionados en los checkboxes
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
                checkBox.setSelected(false);  // Limpiar selección previa
            }
        }

        // Marcar los checkboxes según los sabores seleccionados
        for (Sabor sabor : saboresSeleccionados) {
            for (Node node : gridSabores.getChildren()) {
                if (node instanceof CheckBox checkBox && checkBox.getText().equals(sabor.getSabor())) {
                    checkBox.setSelected(true);  // Marcar como seleccionado
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
        ActionLogger.log("El usuario quiere agregar un nuevo sabor.");
        SceneLoader.loadScene(new NodeSceneStrategy(btnAgregarSabor), Paths.NUEVOSABOR, "/css/components.css", false);
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        ActionLogger.log("El usuario canceló la selección de sabores.");
        ((Stage) btnCancelar.getScene().getWindow()).close();
    }
}
