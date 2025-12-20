package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Proveedor;
import persistence.dao.ProveedorDAO;
import utilities.ActionLogger;
import utilities.Paths;
import utilities.SceneLoader;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ProveedoresController {
    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnModificar;
    @FXML
    private TableView<Proveedor> tableViewProveedores;
    @FXML
    private TableColumn<Proveedor, String> colCuit;
    @FXML
    private TableColumn<Proveedor, String> colNombre;
    @FXML
    private TableColumn<Proveedor, String> colInsumo;
    @FXML
    private TableColumn<Proveedor, String> colTelefono;
    @FXML
    private TableColumn<Proveedor, String> colUbicacion;
    @FXML
    private TableColumn<Proveedor, String> colCorreo;
    @FXML
    private TextField txtFiltrar;

    private ObservableList<Proveedor> proveedoresList = FXCollections.observableArrayList();
    private ProveedorDAO proveedorDAO;

    public ProveedoresController() {
        proveedorDAO = new ProveedorDAO();
    }

    @FXML
    public void initialize() {
        // Configuración de las columnas
        colCuit.setCellValueFactory(new PropertyValueFactory<>("cuit"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        /// 1. EL DATO: En vez de confiar en lo que tiene el objeto memoria,
        // le preguntamos a la base de datos "Che, ¿qué insumos tiene este proveedor?".
        colInsumo.setCellValueFactory(cellData -> {
            Proveedor p = cellData.getValue();
            // Usamos tu DAO para buscar los insumos por nombre EN ESTE MOMENTO
            List<String> insumos = proveedorDAO.findInsumosByProveedor(p.getNombre());
            
            String texto;
            if (insumos == null || insumos.isEmpty()) {
                texto = "No tiene insumos";
            } else {
                // Unimos la lista con comas (Ej: "Harina, Huevo, Azucar")
                texto = String.join(", ", insumos);
            }
            
            return new javafx.beans.property.SimpleStringProperty(texto);
        });

        // 2. EL DISEÑO (WRAP): Esto lo tenías bien, dejalo así que es lo que hace que baje el renglón.
        colInsumo.setCellFactory(columna -> {
            return new TableCell<Proveedor, String>() {
                private final Text text = new Text();
                {
                    text.wrappingWidthProperty().bind(columna.widthProperty().subtract(10));
                }
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null); // Limpiamos texto plano por las dudas
                    } else {
                        text.setText(item);
                        setGraphic(text);
                    }
                }
            };
        });
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));

        // Cargar los datos en la tabla
        cargarDatos();

        // Permisos del usuario
        java.util.List<String> permisos = model.SessionContext.getInstance().getPermisos();
        boolean puedeCrear = permisos != null && permisos.contains("Proveedores-crear");
        boolean puedeModificar = permisos != null && permisos.contains("Proveedores-modificar");
        boolean puedeEliminar = permisos != null && permisos.contains("Proveedores-eliminar");

        btnAgregar.setDisable(!puedeCrear);
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);
        // btnAgregar puede que no exista, si existe agregar lógica similar

        // Listener para habilitar los botones solo si hay selección y permiso
        tableViewProveedores.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
            btnModificar.setDisable(!(puedeModificar && newSelection != null));
            btnEliminar.setDisable(!(puedeEliminar && newSelection != null));
        });

        // Búsqueda en tiempo real
        txtFiltrar.textProperty().addListener((_, _, newText) -> filtrarProveedores(newText));
    }

    private void cargarDatos() {
        List<Proveedor> proveedores = proveedorDAO.findAll();
        proveedoresList.setAll(proveedores);
        tableViewProveedores.setItems(proveedoresList);
    }

    private void filtrarProveedores(String filtro) {
        if (filtro == null || filtro.isEmpty()) {
            tableViewProveedores.setItems(proveedoresList);
        } else {
            String filtroLower = filtro.toLowerCase();
            ObservableList<Proveedor> filtrados = proveedoresList.filtered(p -> {
                boolean matchNombre = p.getNombre() != null && p.getNombre().toLowerCase().contains(filtroLower);
                boolean matchInsumo = p.getInsumosString() != null
                        && p.getInsumosString().toLowerCase().contains(filtroLower);
                boolean matchTelefono = p.getTelefono() != null && p.getTelefono().toLowerCase().contains(filtroLower);
                return matchNombre || matchInsumo || matchTelefono;
            });
            tableViewProveedores.setItems(filtrados);
        }
    }

    @FXML
    void handleEliminar(ActionEvent event) {
        Proveedor proveedorSeleccionado = tableViewProveedores.getSelectionModel().getSelectedItem();

        if (proveedorSeleccionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("Eliminar Proveedor");
            alert.setContentText("¿Estás seguro de que deseas eliminar a: " + proveedorSeleccionado.getNombre()
                    + "?\nEsta acción no se puede deshacer.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // 1. INTENTO DE BORRADO
                    proveedorDAO.delete(proveedorSeleccionado);

                    // 2. ACTUALIZAR TABLA
                    proveedoresList.remove(proveedorSeleccionado);
                    tableViewProveedores.refresh();

                    mostrarAlerta("Éxito", "El proveedor ha sido eliminado correctamente.",
                            Alert.AlertType.INFORMATION);
                    ActionLogger.log("El usuario eliminó el proveedor: " + proveedorSeleccionado.getNombre());

                } catch (Exception e) {
                    // 3. ATAJAMOS EL ERROR DE INTEGRIDAD
                    Alert errorAlert = new Alert(Alert.AlertType.WARNING);
                    errorAlert.setTitle("No se puede eliminar");
                    errorAlert.setHeaderText("Proveedor con historial");
                    errorAlert.setContentText("No podés eliminar a '" + proveedorSeleccionado.getNombre() +
                            "' porque ya tenés INSUMOS cargados o un HISTORIAL DE COMPRAS con él.\n\n" +
                            "El sistema protege esos registros contables.");
                    errorAlert.showAndWait();

                    System.err.println("Error al eliminar proveedor: " + e.getMessage());
                }
            }
        } else {
            mostrarAlerta("Selección requerida", "Por favor, selecciona un proveedor para eliminar.",
                    Alert.AlertType.WARNING);
        }
    }

    @FXML
    void handleModificar(ActionEvent event) {
        Proveedor proveedorSeleccionado = tableViewProveedores.getSelectionModel().getSelectedItem();

        if (proveedorSeleccionado != null) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/pasteleria/NuevoProveedor.fxml"));
                AnchorPane root = loader.load();

                NuevoProveedorController nuevoProveedorController = loader.getController();
                nuevoProveedorController.cargarProveedor(proveedorSeleccionado);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Modificar Proveedor");

                // 🔁 Recargar la tabla al cerrar la ventana de modificación
                stage.setOnHidden(_ -> cargarDatos());

                stage.show();

                ActionLogger.log("El usuario accedió al formulario para modificar el proveedor: "
                        + proveedorSeleccionado.getNombre());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Selección requerida", "Por favor, selecciona un proveedor para modificar.",
                    Alert.AlertType.WARNING);
        }
    }

    @FXML
    void handleAgregar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/NuevoProveedor.fxml"));
            AnchorPane root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Agregar Proveedor");

            // Al cerrar la ventana, recargar la tabla
            stage.setOnHidden(_ -> cargarDatos());

            stage.show();

            ActionLogger.log("El usuario accedió al formulario para crear un proveedor.");
        } catch (IOException e) {
            e.printStackTrace();
            ActionLogger.log("Error al intentar abrir el formulario de agregado.");
        }
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/mainMenu.css", false);
        ActionLogger.log("El usuario volvió al menú principal.");
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}