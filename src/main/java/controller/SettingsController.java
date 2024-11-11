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
import javafx.scene.layout.StackPane;
import utilities.Paths;
import utilities.SceneLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import persistence.dao.TrabajadorDAO;

import java.util.List;

public class SettingsController {
    @FXML private Button btnModificarEmpleado;
    @FXML private Button btnEliminarEmpleado;
    @FXML private Button btnAnadirEmpleado;
    @FXML private GridPane gridEmpleadosActuales;
    @FXML private Label title;
    @FXML private ComboBox<String> cmbEmpleadosActuales;
    @FXML private Button btnConfiguracionEmpleados;
    @FXML public StackPane contenedorDinamico;
    @FXML private Button btnSabores;
    @FXML private Button btnInsumos;
    @FXML public Pane paneConfigEmpleados;

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

    private void togglePaneConfigEmpleados() {
        // Cambiar visibilidad del Pane
        boolean isVisible = paneConfigEmpleados.isVisible();
        paneConfigEmpleados.setVisible(!isVisible);
        btnConfiguracionEmpleados.setDisable(true);

        // Limpiar contenedor dinámico si se muestra paneConfigEmpleados
        if (!isVisible) {
            contenedorDinamico.getChildren().clear();
        }
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.ADMIN_MAINMENU, "/css/loginAdmin.css", true);
    }

    @FXML
    void CRUDEmpleado(ActionEvent event) {
        btnSabores.setDisable(false);
        togglePaneConfigEmpleados();
    }

    void verificarVentanas() {
        // Limpia el contenido del contenedor dinámico para ocultar cualquier vista previa
        contenedorDinamico.getChildren().clear();

        // Activa todos los botones antes de desactivar el botón de la ventana activa
        btnModificarEmpleado.setDisable(false);
        btnEliminarEmpleado.setDisable(false);
        btnAnadirEmpleado.setDisable(false);
        btnSabores.setDisable(false);
        btnInsumos.setDisable(false);

        // Desactiva solo el botón correspondiente a la ventana abierta
        switch (verificarVentanasAbiertas) {
            case 1:
                btnAnadirEmpleado.setDisable(true);
                break;
            case 2:
                btnModificarEmpleado.setDisable(true);
                break;
            case 3:
                btnEliminarEmpleado.setDisable(true);
                break;
            case 4:
                btnSabores.setDisable(true);
                break;
            case 5:
                btnInsumos.setDisable(true);
                break;
            default:
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

    @FXML
    void mostrarVentanaSabores(ActionEvent event) {
        paneConfigEmpleados.setVisible(false);
        btnConfiguracionEmpleados.setDisable(false);
        verificarVentanasAbiertas = 4;
        verificarVentanas();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/TablaSabores.fxml"));
            Parent visualizarView = loader.load();
            contenedorDinamico.getChildren().setAll(visualizarView);

            TablaSaboresController controller = loader.getController();
            controller.setSettingsController(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cerrarTablaSabores() {
        contenedorDinamico.getChildren().clear();
        btnSabores.setDisable(false);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /*INSUMOS*/
    @FXML
    void mostrarVentanaInsumos(ActionEvent event) {
        paneConfigEmpleados.setVisible(false);
        btnConfiguracionEmpleados.setDisable(false);

        verificarVentanasAbiertas = 5;
        verificarVentanas();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/TableInsumos.fxml"));
            Parent visualizarView = loader.load();
            contenedorDinamico.getChildren().setAll(visualizarView);

            // Obtén el controlador de TableInsumosController sin necesidad de pasar SettingsController
            TableInsumosController controller = loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    }