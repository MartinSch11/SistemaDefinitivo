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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import persistence.dao.TrabajadorDAO;
import utilities.ActionLogger;
import utilities.Paths;
import utilities.SceneLoader;

import java.util.List;

public class SettingsController {
    @FXML private GridPane gridAjustes;
    @FXML private Button btnModificarEmpleado;
    @FXML private Button btnEliminarEmpleado;
    @FXML private Button btnAnadirEmpleado;
    @FXML private Button btnConfiguracionEmpleados;
    @FXML private Button btnSabores;
    @FXML private Button btnInsumos;
    @FXML private Button btnFuncionesUsuarios;
    @FXML private Button btnClientes;
    @FXML private Button btnAcciones;
    @FXML private GridPane gridEmpleadosActuales;
    @FXML private Label title;
    @FXML private ComboBox<String> cmbEmpleadosActuales;
    @FXML public StackPane contenedorDinamico;
    @FXML public StackPane contenedorDinamico2;
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

    private void mostrarEnContenedor(StackPane contenedor, Parent vista) {
        contenedor.getChildren().setAll(vista);
        contenedor.setVisible(true);
    }

    private void ocultarContenedores() {
        contenedorDinamico.setVisible(false);
        contenedorDinamico2.setVisible(false);
        paneConfigEmpleados.setVisible(false); // Ocultar paneConfigEmpleados
    }

    public void cerrarCrudAnadirEmpleado() {
        // Eliminar el contenido del Pane
        contenedorDinamico.getChildren().clear();
        btnAnadirEmpleado.setDisable(false);
    }

    public void cerrarCrudModificarEmpleado() {
        // Eliminar el contenido del Pane
        contenedorDinamico.getChildren().clear();
        btnModificarEmpleado.setDisable(false);
    }

    public void cerrarCrudEliminarEmpleado() {
        // Eliminar el contenido del Pane que contiene CrudEliminarEmpleado.fxml
        contenedorDinamico.getChildren().clear();
        btnEliminarEmpleado.setDisable(false);
    }

    @FXML
    void mostrarConfigEmpleados(ActionEvent event) {
        verificarVentanasAbiertas = 0; // Panel de configuración de empleados
        verificarVentanas();
        ocultarContenedores();
        paneConfigEmpleados.setVisible(true);
        btnConfiguracionEmpleados.setDisable(true);
    }

    @FXML
    void anadirEmpleado(ActionEvent event) {
        verificarVentanasAbiertas = 1;
        verificarVentanas();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/CrudAnadirEmpleado.fxml"));
            Parent visualizarView = loader.load();
            mostrarEnContenedor(contenedorDinamico, visualizarView);

            CrudAnadirEmpleadoController controller = loader.getController();
            controller.setSettingsController(this);

            btnAnadirEmpleado.setDisable(true);
            ActionLogger.log("El usuario abrió el formulario para registrar un nuevo empleado en el sistema.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void modificarEmpleado(ActionEvent event) {
        verificarVentanasAbiertas = 2;
        verificarVentanas();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/CrudModificarEmpleado.fxml"));
            Parent visualizarView = loader.load();
            mostrarEnContenedor(contenedorDinamico, visualizarView);

            CrudModificarEmpleadoController controller = loader.getController();
            controller.setSettingsController(this);

            btnModificarEmpleado.setDisable(true);
            ActionLogger.log("El usuario abrió el formulario para modificar los datos de un empleado existente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void eliminarEmpleado(ActionEvent event) {
        verificarVentanasAbiertas = 3;
        verificarVentanas();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/CrudEliminarEmpleado.fxml"));
            Parent visualizarView = loader.load();
            mostrarEnContenedor(contenedorDinamico, visualizarView);

            CrudEliminarEmpleadoController controller = loader.getController();
            controller.setSettingsController(this);

            btnEliminarEmpleado.setDisable(true);
            ActionLogger.log("El usuario abrió el formulario para eliminar un empleado del sistema.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void mostrarVentanaSabores(ActionEvent event) {
        verificarVentanasAbiertas = 4;
        verificarVentanas();
        ocultarContenedores();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/TablaSabores.fxml"));
            Parent visualizarView = loader.load();
            mostrarEnContenedor(contenedorDinamico2, visualizarView);

            TablaSaboresController controller = loader.getController();
            controller.setSettingsController(this);

            ActionLogger.log("El usuario abrió la ventana de gestión de sabores para administrar los sabores disponibles en la pastelería.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar la vista de sabores: " + e.getMessage());
        }
    }

    @FXML
    void mostrarVentanaInsumos(ActionEvent event) {
        verificarVentanasAbiertas = 5;
        verificarVentanas();
        ocultarContenedores();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/TableInsumos.fxml"));
            Parent visualizarView = loader.load();
            mostrarEnContenedor(contenedorDinamico2, visualizarView);

            ActionLogger.log("El usuario abrió la ventana de gestión de insumos para supervisar y registrar los ingredientes en stock.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void mostrarVentanaPermisos(ActionEvent event) {
        verificarVentanasAbiertas = 6;
        verificarVentanas();
        ocultarContenedores();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/ControlRoles.fxml"));
            Parent visualizarView = loader.load();
            mostrarEnContenedor(contenedorDinamico2, visualizarView);

            ActionLogger.log("El usuario accedió a la ventana de control de roles y permisos para gestionar los privilegios de los usuarios.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void mostrarVentanaClientes(ActionEvent event) {
        verificarVentanasAbiertas = 7;
        verificarVentanas();
        ocultarContenedores();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/TablaClientes.fxml"));
            Parent visualizarView = loader.load();
            mostrarEnContenedor(contenedorDinamico2, visualizarView);

            ActionLogger.log("El usuario abrió la ventana de gestión de clientes para administrar la información de los clientes.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void mostrarVentanaAcciones(ActionEvent event) {
        verificarVentanasAbiertas = 8;
        verificarVentanas();
        ocultarContenedores();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/TableAccionesUsuarios.fxml"));
            Parent visualizarView = loader.load();
            mostrarEnContenedor(contenedorDinamico2, visualizarView);

            ActionLogger.log("El usuario abrió la ventana de acciones de usuario para visualizar las acciones.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void verificarVentanas() {
        // Limpia el contenido de ambos contenedores dinámicos
        contenedorDinamico.getChildren().clear();
        contenedorDinamico2.getChildren().clear();

        // Activa todos los botones antes de desactivar el botón correspondiente
        btnConfiguracionEmpleados.setDisable(false);
        btnModificarEmpleado.setDisable(false);
        btnEliminarEmpleado.setDisable(false);
        btnAnadirEmpleado.setDisable(false);
        btnSabores.setDisable(false);
        btnInsumos.setDisable(false);
        btnFuncionesUsuarios.setDisable(false);
        btnClientes.setDisable(false);
        btnAcciones.setDisable(false);

        // Desactiva el botón correspondiente a la ventana activa
        switch (verificarVentanasAbiertas) {
            case 0 -> {
                btnConfiguracionEmpleados.setDisable(true);
                paneConfigEmpleados.setVisible(true); // Asegúrate de mostrarlo si es necesario
            }
            case 1 -> btnAnadirEmpleado.setDisable(true);
            case 2 -> btnModificarEmpleado.setDisable(true);
            case 3 -> btnEliminarEmpleado.setDisable(true);
            case 4 -> btnSabores.setDisable(true);
            case 5 -> btnInsumos.setDisable(true);
            case 6 -> btnFuncionesUsuarios.setDisable(true);
            case 7 -> btnClientes.setDisable(true);
            case 8 -> btnAcciones.setDisable(true);
        }
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

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/loginAdmin.css", true);
        ActionLogger.log("El usuario regresó al menú principal desde la vista de configuraciones.");
    }
}
