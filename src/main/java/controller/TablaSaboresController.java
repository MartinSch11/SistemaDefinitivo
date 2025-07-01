package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Sabor;
import persistence.dao.SaborDAO;
import utilities.ActionLogger;

import java.io.IOException;

public class TablaSaboresController {
    @FXML private Button btnAgregar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;
    @FXML private TableView<Sabor> tableSabores;
    @FXML private TableColumn<Sabor, Integer> colIdSabores;
    @FXML private TableColumn<Sabor, String> colSabores;

    private ObservableList<Sabor> listaSabores;
    private SaborDAO saborDAO;

    private SettingsController settingsController;

    public void setSettingsController(SettingsController settingsController) {
        this.settingsController = settingsController;
    }

    @FXML
    public void initialize() {
        // Configura las columnas
        colIdSabores.setCellValueFactory(new PropertyValueFactory<>("id_sabor"));
        colSabores.setCellValueFactory(new PropertyValueFactory<>("sabor"));

        // Inicializar la lista y cargar los datos
        listaSabores = FXCollections.observableArrayList();
        tableSabores.setItems(listaSabores);

        // Cargar los sabores desde la base de datos
        cargarSabores();

        // Permisos del usuario
        java.util.List<String> permisos = model.SessionContext.getInstance().getPermisos();
        boolean puedeCrear = permisos != null && permisos.contains("Sabores-crear");
        boolean puedeModificar = permisos != null && permisos.contains("Sabores-modificar");
        boolean puedeEliminar = permisos != null && permisos.contains("Sabores-eliminar");

        btnAgregar.setDisable(!puedeCrear);
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);

        // Agregar el listener para el cambio de selección en la tabla
        tableSabores.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnModificar.setDisable(!(puedeModificar && newSelection != null));
            btnEliminar.setDisable(!(puedeEliminar && newSelection != null));
        });
    }

    private void cargarSabores() {
        SaborDAO saborDAO = new SaborDAO();
        listaSabores.clear();
        listaSabores.addAll(saborDAO.findAll());  // Asegurarse de limpiar la lista antes de cargar nuevos datos
    }

    @FXML
    public void handleAgregar(ActionEvent event) {
        ActionLogger.log("El usuario hizo clic en 'Agregar' para crear un nuevo sabor.");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/DialogNuevoSabor.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nuevo Sabor");
            stage.setScene(new Scene(root));

            stage.showAndWait(); // Espera a que se cierre el diálogo

            cargarSabores();  // Recargar la lista después de agregar un nuevo sabor
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleModificar(ActionEvent event) {
        Sabor saborSeleccionado = tableSabores.getSelectionModel().getSelectedItem();

        if (saborSeleccionado != null) {
            ActionLogger.log("El usuario hizo clic en 'Modificar' para editar el sabor: " + saborSeleccionado.getSabor());
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/DialogNuevoSabor.fxml"));
                Parent root = loader.load();

                NuevoSaborController dialogController = loader.getController();
                dialogController.cargarSaborParaModificar(saborSeleccionado);

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Modificar Sabor");
                stage.setScene(new Scene(root));

                stage.showAndWait();

                cargarSabores();  // Recargar la lista después de modificar el sabor
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleEliminar(ActionEvent event) {
        Sabor saborSeleccionado = tableSabores.getSelectionModel().getSelectedItem();

        if (saborSeleccionado != null) {
            ActionLogger.log("El usuario hizo clic en 'Eliminar' para borrar el sabor: " + saborSeleccionado.getSabor());
            // Mostrar alerta de confirmación
            if (showAlert(Alert.AlertType.CONFIRMATION, "Confirmación de eliminación",
                    "¿Estás seguro de que quieres eliminar este sabor?",
                    "Una vez eliminado, no podrás recuperar este sabor.") == ButtonType.OK) {
                try {
                    SaborDAO saborDAO = new SaborDAO();
                    saborDAO.eliminarSabor(saborSeleccionado);  // Eliminar el sabor de la base de datos
                    listaSabores.remove(saborSeleccionado);  // Eliminar de la interfaz

                    // Mostrar alerta de éxito
                    showAlert(Alert.AlertType.INFORMATION, "Éxito", "Sabor eliminado correctamente", "");
                    ActionLogger.log("El sabor fue eliminado correctamente: " + saborSeleccionado.getSabor());
                } catch (Exception e) {
                    // Mostrar alerta de error
                    showAlert(Alert.AlertType.ERROR, "Error de eliminación",
                            "No se pudo eliminar el sabor", "Hubo un problema al intentar eliminar el sabor. Inténtalo de nuevo.");
                    ActionLogger.log("Hubo un error al intentar eliminar el sabor: " + saborSeleccionado.getSabor());
                }
            }
        } else {
            // Mostrar alerta de advertencia si no hay sabor seleccionado
            showAlert(Alert.AlertType.WARNING, "Advertencia", "No se ha seleccionado ningún sabor",
                    "Por favor, selecciona un sabor antes de intentar eliminar.");
        }
    }

    // Función externa para mostrar alertas
    private ButtonType showAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alerta = new Alert(alertType);
        alerta.setTitle(title);
        alerta.setHeaderText(headerText);
        alerta.setContentText(contentText);
        return alerta.showAndWait().orElse(ButtonType.CANCEL);
    }
}
