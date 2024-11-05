package controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import utilities.Paths;
import utilities.SceneLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import persistence.dao.TrabajadorDAO;
import model.Trabajador;

import java.util.List;

public class SettingsController {
    @FXML private Button btnModificarEmpleado; @FXML private Button btnEliminarEmpleado; @FXML private Button btnAnadirEmpleado;
    @FXML private GridPane gridEmpleadosActuales; @FXML private Label title; @FXML private ComboBox<String> cmbEmpleadosActuales;
    @FXML
    private Button btnConfiguracionEmpleado;
    @FXML
    public Pane contenedorDinamico;

    public static int verificarVentanasAbiertas = 0;


    private TrabajadorDAO trabajadorDAO = new TrabajadorDAO();

    private static SettingsController instance;
    public SettingsController() {
        instance = this;
    }
    public static SettingsController getInstance() {
        return instance;
    }

    @FXML
    private void initialize() {
        cargarNombresEnComboBox();
    }

    public Button getBtnModificarEmpleado() {
        return btnModificarEmpleado;
    }
    public Button getBtnAnadirEmpleado() {
        return btnAnadirEmpleado;
    }
    public Button getBtnEliminarEmpleado() {
        return btnEliminarEmpleado;
    }


    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.ADMIN_MAINMENU, "/css/loginAdmin.css", true);
    }



    @FXML
    void CRUDEmpleado(ActionEvent event) {
        btnModificarEmpleado.setVisible(true);
        btnEliminarEmpleado.setVisible(true);
        btnAnadirEmpleado.setVisible(true);
        gridEmpleadosActuales.setVisible(true);
        title.setVisible(true);

    }

    void verificarVentanas() {
        switch (verificarVentanasAbiertas) {
            case 1:
                btnEliminarEmpleado.setVisible(false);
                btnModificarEmpleado.setVisible(false);
                break;
            case 2:
                btnAnadirEmpleado.setVisible(false);
                btnEliminarEmpleado.setVisible(false);
                break;
            case 3:
                btnModificarEmpleado.setVisible(false);
                btnAnadirEmpleado.setVisible(false);
                break;
            default:
                // Opcional: manejar otros casos o valores no esperados
                break;
        }
    }

    @FXML
    void anadirEmpleado(ActionEvent event){
        verificarVentanasAbiertas = 1;
        verificarVentanas();
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/CrudAnadirEmpleado.fxml"));
            Parent visualizarView = loader.load();
            contenedorDinamico.getChildren().setAll(visualizarView); // Reemplazar el contenido actual con el nuevo

            CrudAnadirEmpleadoController controller = loader.getController();
            controller.setSettingsController(this);

            btnAnadirEmpleado.setDisable(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cerrarCrudAnadirEmpleado() {
        // Eliminar el contenido del Pane
        contenedorDinamico.getChildren().clear();
        btnAnadirEmpleado.setDisable(false);
    }

    @FXML
    void eliminarEmpleado(ActionEvent event){
        verificarVentanasAbiertas = 3;
        verificarVentanas();
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/CrudEliminarEmpleado.fxml"));
            Parent visualizarView = loader.load();
            contenedorDinamico.getChildren().setAll(visualizarView);

            // Pasar la referencia del controlador actual a CrudEliminarEmpleadoController
            CrudEliminarEmpleadoController controller = loader.getController();
            controller.setSettingsController(this);

            btnEliminarEmpleado.setDisable(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cerrarCrudEliminarEmpleado() {
        // Eliminar el contenido del Pane que contiene CrudEliminarEmpleado.fxml
        contenedorDinamico.getChildren().clear();
        btnEliminarEmpleado.setDisable(false);
    }

    @FXML
    void modificarEmpleado(ActionEvent event){
        verificarVentanasAbiertas = 2;
        verificarVentanas();
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/CrudModificarEmpleado.fxml"));
            Parent visualizarView = loader.load();
            contenedorDinamico.getChildren().setAll(visualizarView);

            CrudModificarEmpleadoController controller = loader.getController();
            controller.setSettingsController(this);

            btnModificarEmpleado.setDisable(true); //tengo que hacer esto desde el otro controller
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cerrarCrudModificarEmpleado() {
        // Eliminar el contenido del Pane
        contenedorDinamico.getChildren().clear();
        btnModificarEmpleado.setDisable(false);
    }


    public void cargarNombresEnComboBox() {
        try {
            List<String> nombres = trabajadorDAO.findAllNombres();
            cmbEmpleadosActuales.setItems(FXCollections.observableArrayList(nombres));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los nombres de los empleados: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}