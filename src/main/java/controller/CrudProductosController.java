package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Producto;
import persistence.dao.ProductoDAO;
import utilities.NodeSceneStrategy;
import utilities.Paths;
import utilities.SceneLoader;

import java.util.List;

public class CrudProductosController {

    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnModificar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnVolver;
    @FXML
    private TableView<Producto> tableProductos;
    @FXML
    private TableColumn<Producto, String> colNombre;
    @FXML
    private TableColumn<Producto, String> colDescripcion;
    @FXML
    private TableColumn<Producto, String> colCategoria;
    @FXML
    private TableColumn<Producto, Float> colPrecio;
    @FXML
    private TableColumn<Producto, String> colSabor;

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
        modalProductosController controller = SceneLoader.handleModalWithController(new NodeSceneStrategy(btnAgregar), Paths.APRODUCTOS, "/css/components.css", this);
        // Si es necesario, puedes interactuar con el controlador aquí
    }

    @FXML
    void handleModificar(ActionEvent event) {
        Producto productoSeleccionado = tableProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            // Llamar al método que retorna el controlador
            modalProductosController controller = SceneLoader.handleModalWithController(new NodeSceneStrategy(btnModificar), Paths.APRODUCTOS, "/css/components.css", this);
            if (controller != null) {
                controller.setProductoParaEditar(productoSeleccionado);  // Pasa el producto al modal para editarlo
            }
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
}
