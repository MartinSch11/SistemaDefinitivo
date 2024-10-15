package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import utilities.Paths;
import utilities.SceneLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


public class SettingsController {
    @FXML private Button btnModificarEmpleado; @FXML private Button btnEliminarEmpleado; @FXML private Button btnAnadirEmpleado;
    @FXML private GridPane gridEmpleadosActuales; @FXML private Label title;
    @FXML
    private Button btnConfiguracionEmpleado;
    @FXML
    public Pane contenedorDinamico;

    public static int verificarVentanasAbiertas = 0;

    @FXML
    private void initialize() {

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
    }


}
