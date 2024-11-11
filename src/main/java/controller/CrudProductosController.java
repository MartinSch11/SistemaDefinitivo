package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import lombok.Getter;
import model.Producto;
import model.Receta;
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
    @FXML private TableColumn<Producto, String> colReceta;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Float> colPrecio;
    @FXML private TableColumn<Producto, String> colSabor;

    @Getter
    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private ProductoDAO productoDAO;

    @FXML
    public void initialize() {
        productoDAO = new ProductoDAO();

        // Inicializar listaProductos directamente en la declaración
        listaProductos = FXCollections.observableArrayList();

        // Llamar primero a rellenarColumnas para asegurar que las columnas se configuren
        rellenarColumnas();

        // Agregar Listener para habilitar los botones cuando se seleccione un producto
        tableProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean productoSeleccionado = newSelection != null;
            btnModificar.setDisable(!productoSeleccionado);
            btnEliminar.setDisable(!productoSeleccionado);
        });
    }

    private void rellenarColumnas() {
        // Configuración de las otras columnas
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colSabor.setCellValueFactory(new PropertyValueFactory<>("sabores"));

        // Para la columna colReceta, usamos un CellFactory para obtener el nombre de la receta
        colReceta.setCellValueFactory(cellData -> {
            // Obtener la receta del producto y, si existe, su nombre
            Receta receta = cellData.getValue().getReceta();
            return receta != null ? new SimpleStringProperty(receta.getNombreReceta()) : new SimpleStringProperty("");  // Devuelve el nombre de la receta o vacío
        });

        // Rellenamos la tabla con los productos
        cargarProductos();
    }



    private void cargarProductos() {
        listaProductos.clear(); // Limpiar la lista antes de cargar los nuevos productos
        List<Producto> productos = productoDAO.findAll(); // Obtener productos de la base de datos
        listaProductos.addAll(productos); // Agregar los productos a la lista observable
        tableProductos.setItems(listaProductos); // Establecer la lista en la TableView
    }

    @FXML
    public void handleAgregar(ActionEvent event) {
        try {
            // Cargar el FXML del formulario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/productos_form.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del formulario
            ProductoFormController controller = loader.getController();
            controller.setParentController(this); // Establecer el controlador padre
            controller.setListaProductos(listaProductos); // Pasar la lista observable

            // Crear una nueva ventana para el formulario
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Agregar Producto");
            stage.show();

            cargarProductos();// Recargar la tabla de productos después de cerrar el formulario

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
                Parent root = loader.load(); // Cargar el FXML como un contenedor normal

                // Crear un nuevo Stage (ventana)
                Stage stage = new Stage();
                stage.setTitle("Modificar Producto");
                stage.setScene(new Scene(root));

                ProductoFormController controller = loader.getController();
                controller.setProducto(productoSeleccionado);
                controller.setListaProductos(listaProductos);

                // Mostrar la ventana
                stage.showAndWait();

                cargarProductos(); // Recargar la lista tras modificar
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
    void handleBuscar(ActionEvent event) {
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