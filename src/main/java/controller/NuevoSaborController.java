package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Sabor;
import persistence.dao.SaborDAO;

public class NuevoSaborController {

    @FXML private TextField txtNuevoSabor;

    @FXML private void handleGuardar(ActionEvent event) {
        String nuevoSabor = txtNuevoSabor.getText();
        if (nuevoSabor != null && !nuevoSabor.trim().isEmpty()) {
            Sabor sabor = new Sabor(); sabor.setSabor(nuevoSabor);
            SaborDAO saborDAO = new SaborDAO();
            saborDAO.guardarSabor(sabor); // Limpia el campo de texto después de guardar
            txtNuevoSabor.clear();
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

}
