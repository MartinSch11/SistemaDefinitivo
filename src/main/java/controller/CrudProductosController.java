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
        colSabor.setCellValueFactory(new PropertyValueFactory<>("sabores"));

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

    public void agregarProducto(Producto producto) {
        productoDAO.save(producto);
        listaProductos.add(producto);
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

            ProductoFormController controller = loader.getController();  // Cambia EventoFormController a ProductosFormController

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
                // Cargar el FXML del formulario para modificar
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/producto_form.fxml"));
                DialogPane dialogPane = loader.load();

                // Crear un diálogo
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(dialogPane);
                dialog.setTitle("Modificar Producto");

                // Obtener el controlador y pasar el producto seleccionado
                ProductoFormController controller = loader.getController();
                controller.setProductoParaEditar(productoSeleccionado);

                // Mostrar el diálogo y esperar la respuesta
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Recargar la lista de productos o realizar otras acciones necesarias
                    cargarProductos();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
           mostrarAlerta("No se ha seleccionado ningún producto", "Por favor, selecciona un producto para modificar.");
        }
    }

    @FXML
    void handleEliminar(ActionEvent event) {
        Producto productoSeleccionado = tableProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            productoDAO.delete(productoSeleccionado);
            listaProductos.remove(productoSeleccionado);
        }
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.ADMIN_MAINMENU, "/css/loginAdmin.css", true);
    }

    public void close() {
        productoDAO.close();
    }


    public void actualizarProducto(Producto producto) {
        int index = listaProductos.indexOf(producto);  // Reemplaza 'productos' por 'listaProductos'
        if (index >= 0) {
            listaProductos.set(index, producto); // Actualiza el producto en la lista observable
        }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
