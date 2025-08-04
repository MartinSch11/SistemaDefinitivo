package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class NuevaCategoriaController {
    @FXML
    private TextField txtNomCategoria;
    @FXML
    private Button btnAceptar;
    @FXML
    private Button btnCancelar;

    @FXML
    private void handleAceptar(ActionEvent event) {
        String nombre = txtNomCategoria.getText();
        if (nombre == null || nombre.trim().isEmpty()) {
            mostrarAlerta("El nombre de la categoría no puede estar vacío.");
            return;
        }
        // Persistir la categoría en la base de datos
        persistence.dao.CategoriaDAO categoriaDAO = new persistence.dao.CategoriaDAO();
        model.Categoria categoria = new model.Categoria();
        categoria.setNombre(nombre.trim());
        categoriaDAO.save(categoria);
        cerrarVentana();
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        cerrarVentana();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
