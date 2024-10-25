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

import java.util.List;

public class SaboresController {

    @FXML private GridPane gridSabores;
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;

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
                    saboresSeleccionados.add(sabor);
                } else {
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
            parentController.setSaboresSeleccionados(saboresSeleccionados);
            ((Stage) btnGuardar.getScene().getWindow()).close();
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        ((Stage) btnCancelar.getScene().getWindow()).close();
    }
}
