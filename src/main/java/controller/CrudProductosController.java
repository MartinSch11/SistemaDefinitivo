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
import utilities.ActionLogger;
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
    @FXML private TextField txtBuscar;

    @Getter
    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private ProductoDAO productoDAO;

    @FXML
    public void initialize() {
        productoDAO = new ProductoDAO();
        listaProductos = FXCollections.observableArrayList();
        rellenarColumnas();

        // Listener para búsqueda en tiempo real
        txtBuscar.textProperty().addListener((obs, oldText, newText) -> filtrarProductos(newText));

        // Obtener permisos del usuario
        List<String> permisos = model.SessionContext.getInstance().getPermisos();
        boolean puedeModificar = permisos != null && permisos.contains("Productos-modificar");
        boolean puedeEliminar = permisos != null && permisos.contains("Productos-eliminar");
        boolean puedeCrear = permisos != null && permisos.contains("Productos-crear");

        // Inicialmente deshabilitar según permisos
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);
        btnAgregar.setDisable(!puedeCrear); // Solo puede agregar si tiene permiso crear

        // Listener para habilitar los botones solo si hay selección y permiso
        tableProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnModificar.setDisable(!(puedeModificar && newSelection != null));
            btnEliminar.setDisable(!(puedeEliminar && newSelection != null));
        });
    }

    private void rellenarColumnas() {
        // Configuración de las otras columnas
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colSabor.setCellValueFactory(cellData -> {
            // Convierte la lista de sabores a un string separado por coma
            var sabores = cellData.getValue().getSabores();
            String textoSabores = sabores == null || sabores.isEmpty() ? "" : sabores.stream().map(Object::toString).reduce((a, b) -> a + ", " + b).orElse("");
            return new SimpleStringProperty(textoSabores);
        });

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

            cargarProductos(); // Recargar la tabla de productos después de cerrar el formulario

            // Log de la acción
            ActionLogger.log("Agregar producto: Producto agregado.");

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

                // Log de la acción
                ActionLogger.log("Modificar producto: Producto modificado: " + productoSeleccionado.getNombre());

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

                // Log de la acción
                ActionLogger.log("Eliminar producto: Producto eliminado: " + productoSeleccionado.getNombre());
            }
        } else {
            // Usar showAlert para mostrar un mensaje de error si no hay selección
            showAlert(Alert.AlertType.ERROR, "No se ha seleccionado ningún producto", "Por favor, selecciona un producto para eliminar.");
        }
    }

    @FXML
    void handleBuscar(ActionEvent event) {
        // Lógica para buscar productos (no implementada aquí)
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/loginAdmin.css", false);

        // Log de la acción
        ActionLogger.log("El usuario regresó al menú principal desde la pantalla de Productos.");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void filtrarProductos(String filtro) {
        if (filtro == null || filtro.isEmpty()) {
            tableProductos.setItems(listaProductos);
        } else {
            String filtroLower = filtro.toLowerCase();
            ObservableList<Producto> filtrados = listaProductos.filtered(p -> {
                // Buscar por nombre
                boolean matchNombre = p.getNombre() != null && p.getNombre().toLowerCase().contains(filtroLower);
                // Buscar por descripción
                boolean matchDescripcion = p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(filtroLower);
                // Buscar por categoría
                boolean matchCategoria = p.getCategoria() != null && p.getCategoria().toString().toLowerCase().contains(filtroLower);
                // Buscar por receta
                boolean matchReceta = p.getReceta() != null && p.getReceta().getNombreReceta() != null && p.getReceta().getNombreReceta().toLowerCase().contains(filtroLower);
                // Buscar por sabor
                boolean matchSabor = p.getSabores() != null && p.getSabores().stream().anyMatch(s -> s != null && s.toString().toLowerCase().contains(filtroLower));
                return matchNombre || matchDescripcion || matchCategoria || matchReceta || matchSabor;
            });
            tableProductos.setItems(filtrados);
        }
    }

    public ObservableList<Producto> getListaProductos() {
        return listaProductos;
    }
    public void setListaProductos(ObservableList<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }
}
