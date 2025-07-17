package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import model.Cliente;
import persistence.dao.ClienteDAO;
import utilities.ActionLogger;

public class NuevoClienteController {

    @FXML private TextField txtDNI;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCorreo;
    @FXML private Button btnAceptar;
    @FXML private Button btnCancelar;

    private ClienteDAO clienteDAO = new ClienteDAO();
    private Cliente cliente; // Cliente a modificar o crear
    // Método para recibir el controlador de la tabla
    @Setter
    private TablaClientesController tableClientesController; // Referencia al controlador de la tabla

    @FXML
    private void initialize() {
        // Validar que solo permita números
        txtDNI.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtDNI.setText(oldVal);
            }
        });

        txtTelefono.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtTelefono.setText(oldVal);
            }
        });

        // Validar que solo permita letras
        txtNombre.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*")) {
                txtNombre.setText(oldVal);
            }
        });

        txtApellido.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*")) {
                txtApellido.setText(oldVal);
            }
        });

        txtDNI.requestFocus();
    }

    @FXML
    private void handleAceptar() {
        if (!validarCampos()) {
            return; // Salir si hay campos inválidos
        }

        // Verificar si ya existe un cliente con el mismo DNI
        if (existeClienteConDni(txtDNI.getText().trim())) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Ya existe un cliente con ese DNI.");
            return; // Salir si ya existe un cliente con ese DNI
        }

        if (cliente == null) {
            cliente = new Cliente();
        }

        // Asignar los valores de los campos al cliente
        cliente.setDni(txtDNI.getText().trim());
        cliente.setNombre(txtNombre.getText().trim());
        cliente.setApellido(txtApellido.getText().trim());
        cliente.setTelefono(txtTelefono.getText().trim());
        cliente.setDireccion(txtDireccion.getText().trim().isEmpty() ? null : txtDireccion.getText().trim());
        cliente.setCorreo(txtCorreo.getText().trim().isEmpty() ? null : txtCorreo.getText().trim());

        try {
            if (cliente.getDni() == null) {
                clienteDAO.save(cliente);  // Crear nuevo cliente
                ActionLogger.log("Cliente creado: " + cliente.getNombre() + " " + cliente.getApellido());
            } else {
                clienteDAO.update(cliente);  // Actualizar cliente existente
                ActionLogger.log("Cliente actualizado: " + cliente.getNombre() + " " + cliente.getApellido());
            }

            if (tableClientesController != null) {
                tableClientesController.cargarClientes(); // Recargar la tabla
            }
            cerrarVentana();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo guardar el cliente: " + e.getMessage());
        }
    }


    @FXML
    private void handleCancelar() {
        ActionLogger.log("Operación de cliente cancelada.");
        cerrarVentana();
    }

    private boolean validarCampos() {
        if (txtDNI.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validación", "El campo DNI es obligatorio.");
            return false;
        }
        if (txtNombre.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validación", "El campo Nombre es obligatorio.");
            return false;
        }
        if (txtApellido.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validación", "El campo Apellido es obligatorio.");
            return false;
        }
        if (txtTelefono.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validación", "El campo Teléfono es obligatorio.");
            return false;
        }
        if (!txtCorreo.getText().trim().isEmpty() && !txtCorreo.getText().matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showAlert(Alert.AlertType.WARNING, "Validación", "El correo electrónico no es válido.");
            return false;
        }
        return true;
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    // Método para recibir un cliente para editarlo
    public void cargarClienteParaModificar(Cliente cliente) {
        this.cliente = cliente;
        txtDNI.setText(cliente.getDni());
        txtNombre.setText(cliente.getNombre());
        txtApellido.setText(cliente.getApellido());
        txtTelefono.setText(cliente.getTelefono());
        txtDireccion.setText(cliente.getDireccion());
        txtCorreo.setText(cliente.getCorreo());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean existeClienteConDni(String dni) {
        Cliente clienteExistente = clienteDAO.findByDni(dni);
        return clienteExistente != null;
    }



}
