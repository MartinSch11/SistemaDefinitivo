package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Sabor;
import persistence.dao.SaborDAO;
import utilities.ActionLogger;

public class NuevoSaborController {

    @FXML private TextField txtNuevoSabor;
    private Sabor saborParaModificar;

    @FXML
    private void initialize() {
        // Establecer el foco en txtNuevoSabor al cargar la ventana
        txtNuevoSabor.requestFocus();
    }

    // Método para cargar los datos de un sabor ya existente
    public void cargarSaborParaModificar(Sabor sabor) {
        this.saborParaModificar = sabor;
        // Cargar el sabor en el campo de texto
        txtNuevoSabor.setText(sabor.getSabor());
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        String nuevoSabor = txtNuevoSabor.getText();

        // Validación de que el campo no esté vacío
        if (nuevoSabor != null && !nuevoSabor.trim().isEmpty()) {
            if (saborParaModificar != null) {
                // Si es un sabor para modificar
                saborParaModificar.setSabor(nuevoSabor);
                // Actualizar el sabor en la base de datos
                SaborDAO saborDAO = new SaborDAO();
                saborDAO.actualizarSabor(saborParaModificar);

                // Loguear la acción de modificar sabor
                ActionLogger.log("Sabor modificado: " + saborParaModificar.getSabor());
            } else {
                // Si es un nuevo sabor
                Sabor sabor = new Sabor();
                sabor.setSabor(nuevoSabor);
                // Guardar el nuevo sabor en la base de datos
                SaborDAO saborDAO = new SaborDAO();
                saborDAO.guardarSabor(sabor);

                // Loguear la acción de guardar nuevo sabor
                ActionLogger.log("Nuevo sabor guardado: " + sabor.getSabor());
            }

            txtNuevoSabor.clear(); // Limpiar el campo de texto después de save
            // Cerrar la ventana
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        } else {
            // Agregar aquí un mensaje para indicar que el sabor no puede estar vacío
            System.out.println("Por favor, ingrese un sabor válido.");
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        // Loguear la acción de cancelar
        ActionLogger.log("El usuario ha cancelado la acción de agregar o modificar sabor.");

        // Cerrar la ventana sin guardar cambios
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
