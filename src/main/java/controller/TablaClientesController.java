package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Cliente;
import persistence.dao.ClienteDAO;
import utilities.ActionLogger;

import java.io.IOException;
import java.util.List;

public class TablaClientesController {

    @FXML private TableView<Cliente> tableClientes;
    @FXML private TableColumn<Cliente, String> colDNI;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colApellido;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colDireccion;
    @FXML private TableColumn<Cliente, String> colCorreo;
    @FXML private Button btnAgregar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;

    private ObservableList<Cliente> clientesList;
    private ClienteDAO clienteDAO;

    public TablaClientesController() {
        this.clienteDAO = new ClienteDAO();
    }

    @FXML
    public void initialize() {
        configurarTabla();
        cargarClientes();
        configurarBotones();

        // Permisos del usuario
        java.util.List<String> permisos = model.SessionContext.getInstance().getPermisos();
        boolean puedeCrear = permisos != null && permisos.contains("Clientes-crear");
        boolean puedeModificar = permisos != null && permisos.contains("Clientes-modificar");
        boolean puedeEliminar = permisos != null && permisos.contains("Clientes-eliminar");

        btnAgregar.setDisable(!puedeCrear);
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);

        tableClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnModificar.setDisable(!(puedeModificar && newSelection != null));
            btnEliminar.setDisable(!(puedeEliminar && newSelection != null));
        });
    }

    private void configurarTabla() {
        colDNI.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDni()));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colApellido.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellido()));
        colTelefono.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTelefono()));
        colDireccion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDireccion()));
        colCorreo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCorreo()));

        // Listener para habilitar/deshabilitar botones de modificar y eliminar al seleccionar un cliente
        tableClientes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnModificar.setDisable(newValue == null);
            btnEliminar.setDisable(newValue == null);
        });
    }

    private void configurarBotones() {
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    @FXML
    private void handleAgregar() {
        // Registro de la acción del usuario
        ActionLogger.log("El usuario agregó un nuevo cliente.");
        abrirFormularioCliente(null);
    }

    @FXML
    private void handleModificar() {
        Cliente clienteSeleccionado = tableClientes.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado != null) {
            // Registro de la acción del usuario
            ActionLogger.log("El usuario modificó el cliente: " + clienteSeleccionado.getDni());
            abrirFormularioCliente(clienteSeleccionado);
        }
    }

    @FXML
    private void handleEliminar() {
        Cliente clienteSeleccionado = tableClientes.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado != null) {
            // Registro de la acción del usuario
            ActionLogger.log("El usuario eliminó el cliente: " + clienteSeleccionado.getDni());
            clienteDAO.delete(clienteSeleccionado.getDni());  // Usamos el DNI para eliminar
            clientesList.remove(clienteSeleccionado);
        }
    }

    private void abrirFormularioCliente(Cliente cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/FormularioCliente.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(cliente == null ? "Agregar Cliente" : "Modificar Cliente");
            stage.initModality(Modality.WINDOW_MODAL);

            NuevoClienteController controller = loader.getController();
            if (cliente != null) {
                controller.cargarClienteParaModificar(cliente);
            }

            // Usamos 'controller' en lugar de llamar a 'getController' nuevamente
            controller.setTableClientesController(this);  // Este es el paso correcto

            stage.showAndWait();
            cargarClientes();  // Recargamos la tabla después de cerrar el formulario
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void cargarClientes() {
        List<Cliente> clientes = clienteDAO.findAll();
        clientesList = FXCollections.observableArrayList(clientes);
        tableClientes.setItems(clientesList);
    }
}
