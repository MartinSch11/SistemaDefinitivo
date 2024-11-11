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

        // Deshabilitar los botones por defecto
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);

        // Agregar el listener para el cambio de selección en la tabla
        tableSabores.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            // Habilitar los botones si hay un sabor seleccionado
            boolean saborSeleccionado = newSelection != null;
            btnModificar.setDisable(!saborSeleccionado);
            btnEliminar.setDisable(!saborSeleccionado);
        });
    }

    private void cargarSabores() {
        SaborDAO saborDAO = new SaborDAO();
        listaSabores.addAll(saborDAO.findAll());
    }

    @FXML
    private void handleCerrarVentana() {
        if (settingsController != null) {
            settingsController.cerrarTablaSabores();
        }
    }

    @FXML
    public void handleAgregar(ActionEvent event) {
        try {
            // Cargar el archivo FXML de la ventana de nuevo sabor
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/DialogNuevoSabor.fxml"));
            Parent root = loader.load();

            // Configurar la ventana modal
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nuevo Sabor");
            stage.setScene(new Scene(root));

            // Mostrar la ventana y esperar a que se cierre
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleModificar(ActionEvent event) {
        // Obtener el sabor seleccionado
        Sabor saborSeleccionado = tableSabores.getSelectionModel().getSelectedItem();

        if (saborSeleccionado != null) {
            try {
                // Cargar el archivo FXML de la ventana de nuevo sabor
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/DialogNuevoSabor.fxml"));
                Parent root = loader.load();

                // Obtener el controlador de la ventana de nuevo sabor
                NuevoSaborController dialogController = loader.getController();

                // Pasar el sabor seleccionado al controlador de la ventana de nuevo sabor
                dialogController.cargarSaborParaModificar(saborSeleccionado);

                // Configurar la ventana modal
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Modificar Sabor");
                stage.setScene(new Scene(root));

                // Mostrar la ventana y esperar a que se cierre
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    public void handleEliminar(ActionEvent event) {
        // Obtener el sabor seleccionado
        Sabor saborSeleccionado = tableSabores.getSelectionModel().getSelectedItem();

        if (saborSeleccionado != null) {
            // Mostrar una alerta de confirmación para eliminar
            Alert alertaConfirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            alertaConfirmacion.setTitle("Confirmación de eliminación");
            alertaConfirmacion.setHeaderText("¿Estás seguro de que quieres eliminar este sabor?");
            alertaConfirmacion.setContentText("Una vez eliminado, no podrás recuperar este sabor.");

            // Si el usuario confirma la eliminación
            if (alertaConfirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                SaborDAO saborDAO = new SaborDAO();
                try {
                    // Eliminar el sabor de la base de datos
                    saborDAO.eliminarSabor(saborSeleccionado);

                    // Eliminar el sabor de la lista en la interfaz
                    listaSabores.remove(saborSeleccionado);
                } catch (Exception e) {
                    // Mostrar un mensaje de error en caso de fallo
                    Alert alertaError = new Alert(Alert.AlertType.ERROR);
                    alertaError.setTitle("Error de eliminación");
                    alertaError.setHeaderText("No se pudo eliminar el sabor");
                    alertaError.setContentText("Hubo un problema al intentar eliminar el sabor. Inténtalo de nuevo.");
                    alertaError.showAndWait();
                }
            }
        } else {
            // Si no se ha seleccionado ningún sabor
            Alert alertaError = new Alert(Alert.AlertType.WARNING);
            alertaError.setTitle("Advertencia");
            alertaError.setHeaderText("No se ha seleccionado ningún sabor");
            alertaError.setContentText("Por favor, selecciona un sabor antes de intentar eliminar.");
            alertaError.showAndWait();
        }
    }

}
