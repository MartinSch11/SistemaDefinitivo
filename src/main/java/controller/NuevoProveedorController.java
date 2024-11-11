package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Proveedor;
import persistence.dao.ProveedorDAO;

public class NuevoProveedorController {
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtUbicacion;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtPrefijo;
    @FXML private TextField txtDNI;
    @FXML private TextField txtPosfijo;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private ProveedorDAO proveedorDAO;
    private Proveedor proveedorActual;

    public NuevoProveedorController() {
        proveedorDAO = new ProveedorDAO();
    }

    @FXML
    public void initialize() {
        // Configurar TextFormatter para limitar el input en los campos CUIT
        txtPrefijo.setTextFormatter(new TextFormatter<>(change ->
                (change.getControlNewText().matches("\\d{0,2}")) ? change : null));
        txtDNI.setTextFormatter(new TextFormatter<>(change ->
                (change.getControlNewText().matches("\\d{0,8}")) ? change : null));
        txtPosfijo.setTextFormatter(new TextFormatter<>(change ->
                (change.getControlNewText().matches("\\d{0,1}")) ? change : null));
        txtTelefono.setTextFormatter(new TextFormatter<>(change ->
                (change.getControlNewText().matches("\\d{0,15}")) ? change : null)); // Se permiten hasta 15 dígitos
        txtNombre.setTextFormatter(new TextFormatter<>(change ->
                (change.getControlNewText().matches("[a-zA-ZáéíóúÁÉÍÓÚ\\s]{0,50}")) ? change : null)); // Solo letras y espacios, hasta 50 caracteres
        btnCancelar.setOnAction(event -> cerrarVentana());
    }

    // Método para cargar los datos del proveedor en el formulario
    public void cargarProveedor(Proveedor proveedor) {
        this.proveedorActual = proveedor;

        // Cargar los datos del proveedor en los campos del formulario
        txtNombre.setText(proveedor.getNombre());
        txtTelefono.setText(proveedor.getTelefono());
        txtUbicacion.setText(proveedor.getUbicacion());
        txtCorreo.setText(proveedor.getCorreo());
        txtPrefijo.setText(proveedor.getPrefijo());
        txtDNI.setText(proveedor.getDni());
        txtPosfijo.setText(proveedor.getPostfijo());
    }

    @FXML
    public void handleGuardar(ActionEvent event) {
        // Validar los campos
        if (camposValidos()) {
            // Actualizar el proveedor con los nuevos datos
            proveedorActual.setNombre(txtNombre.getText());
            proveedorActual.setTelefono(txtTelefono.getText());
            proveedorActual.setUbicacion(txtUbicacion.getText());
            proveedorActual.setCorreo(txtCorreo.getText());
            proveedorActual.setPrefijo(txtPrefijo.getText());
            proveedorActual.setDni(txtDNI.getText());
            proveedorActual.setPostfijo(txtPosfijo.getText());

            // Guardar los cambios en la base de datos
            proveedorDAO.update(proveedorActual);

            // Mostrar un mensaje de éxito
            mostrarAlerta("Proveedor modificado", "Los datos del proveedor han sido modificados exitosamente.", Alert.AlertType.INFORMATION);

            // Cerrar la ventana
            cerrarVentana();
        } else {
            // Mostrar un mensaje de advertencia si los campos no son válidos
            mostrarAlerta("Campos incompletos", "Por favor, completa todos los campos obligatorios.", Alert.AlertType.WARNING);
        }
    }

    // Método para verificar que los campos no estén vacíos
    private boolean camposValidos() {
        return !txtNombre.getText().isEmpty() && !txtTelefono.getText().isEmpty() &&
                !txtUbicacion.getText().isEmpty() && !txtCorreo.getText().isEmpty();
    }

    // Método para mostrar una alerta
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Método para cerrar la ventana actual
    private void cerrarVentana() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }
}
