package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Producto;
import persistence.dao.ProductoDAO;
import utilities.SceneLoader;
import utilities.Paths;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class CrudProductosController {

    @FXML private Button btnAgregar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;
    @FXML private Button btnVolver;
    @FXML private TableView<Producto> tableProductos;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colDescripcion;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Float> colPrecio;
    @FXML private TableColumn<Producto, String> colSabor;

    private ObservableList<Producto> listaProductos;
    private ProductoDAO productoDAO;

    @FXML
    public void initialize() {
        productoDAO = new ProductoDAO();
        listaProductos = FXCollections.observableArrayList();

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colSabor.setCellValueFactory(new PropertyValueFactory<>("sabores"));  // Mostrar sabores como String

        cargarProductos();

        // Agregar Listener para habilitar los botones cuando se seleccione un producto
        tableProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean productoSeleccionado = newSelection != null;
            btnModificar.setDisable(!productoSeleccionado);
            btnEliminar.setDisable(!productoSeleccionado);
        });
    }

    private void cargarProductos() {
        List<Producto> productos = productoDAO.findAll();
        listaProductos.setAll(productos);
        tableProductos.setItems(listaProductos);
    }

    @FXML
    public void handleAgregar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/productos_form.fxml"));
            DialogPane dialogPane = loader.load();

            // Crear un diálogo
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Agregar Producto");

            ProductoFormController controller = loader.getController();

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                cargarProductos(); // Actualiza la tabla de productos si se agrega uno nuevo
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleModificar(ActionEvent event) {
        Producto productoSeleccionado = tableProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/productos_form.fxml"));
                DialogPane dialogPane = loader.load();

                // Crear un diálogo
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(dialogPane);
                dialog.setTitle("Modificar Producto");

                // Pasar el producto seleccionado al formulario
                ProductoFormController controller = loader.getController();
                controller.setProductoParaEditar(productoSeleccionado);  // Aquí pasamos el producto seleccionado

                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    cargarProductos();  // Recargar lista de productos después de modificar
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "No se ha seleccionado ningún producto", "Por favor, selecciona un producto para modificar.");
        }
    }

    @FXML
    void handleEliminar(ActionEvent event) {
        Producto productoSeleccionado = tableProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            // Crear un cuadro de diálogo de confirmación
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("Eliminar Producto");
            alert.setContentText("¿Estás seguro de que deseas eliminar el producto: " + productoSeleccionado.getNombre() + "?");

            // Mostrar el cuadro de diálogo y esperar la respuesta
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                productoDAO.delete(productoSeleccionado);
                listaProductos.remove(productoSeleccionado);  // Eliminar el producto de la lista observable
                tableProductos.setItems(listaProductos); // Asegurarse de que la tabla se actualice
            }
        } else {
            // Usar showAlert para mostrar un mensaje de error si no hay selección
            showAlert(Alert.AlertType.ERROR, "No se ha seleccionado ningún producto", "Por favor, selecciona un producto para eliminar.");
        }
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.ADMIN_MAINMENU, "/css/loginAdmin.css", true);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
