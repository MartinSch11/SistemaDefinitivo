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
import java.util.Optional;

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

        tableClientes.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
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

        tableClientes.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
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
        ActionLogger.log("El usuario agregó un nuevo cliente.");
        abrirFormularioCliente(null);
    }

    @FXML
    private void handleModificar() {
        Cliente clienteSeleccionado = tableClientes.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado != null) {
            ActionLogger.log("El usuario modificó el cliente: " + clienteSeleccionado.getDni());
            abrirFormularioCliente(clienteSeleccionado);
        }
    }

    @FXML
    private void handleEliminar() {
        Cliente clienteSeleccionado = tableClientes.getSelectionModel().getSelectedItem();

        if (clienteSeleccionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("Eliminar Cliente");
            alert.setContentText("¿Estás seguro de eliminar a " + clienteSeleccionado.getNombre() + " " + clienteSeleccionado.getApellido() + "?\nEsta acción no se puede deshacer.");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // 1. INTENTAMOS BORRAR
                    clienteDAO.delete(clienteSeleccionado.getDni());

                    // 2. ACTUALIZAMOS TABLA
                    clientesList.remove(clienteSeleccionado);
                    tableClientes.refresh(); // <--- REFRESH POR LAS DUDAS
                    
                    ActionLogger.log("El usuario eliminó el cliente: " + clienteSeleccionado.getDni());

                } catch (Exception e) {
                    // 3. ATAJAMOS EL ERROR (Si el cliente tiene PEDIDOS)
                    Alert errorAlert = new Alert(Alert.AlertType.WARNING);
                    errorAlert.setTitle("No se puede eliminar");
                    errorAlert.setHeaderText("Cliente con historial");
                    errorAlert.setContentText("No podés eliminar a este cliente porque ya tiene PEDIDOS registrados.\n\n" +
                                              "El sistema debe mantener el historial de quién compró.");
                    errorAlert.showAndWait();
                    
                    System.err.println("Error al eliminar cliente: " + e.getMessage());
                }
            }
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

            controller.setTableClientesController(this);

            stage.showAndWait(); // Espera a que cierres la ventana
            
            // --- ACÁ ESTÁ LA SOLUCIÓN ---
            cargarClientes();      // 1. Trae los datos frescos de la BD
            tableClientes.refresh(); // 2. OBLIGA a la tabla a repintarse visualmente
            
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