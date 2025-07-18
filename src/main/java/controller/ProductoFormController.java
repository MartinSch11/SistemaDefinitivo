package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Setter;
import model.Categoria;
import model.Producto;
import model.Receta;
import model.Sabor;
import persistence.dao.CategoriaDAO;
import persistence.dao.ProductoDAO;
import persistence.dao.RecetaDAO;
import utilities.ActionLogger;
import utilities.SceneLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductoFormController {

    @FXML private TextField nombreProductoField;
    @FXML private TextArea descripcionProductoField;
    @FXML private ChoiceBox<Categoria> categoriaChoiceBox;
    @FXML private ComboBox<Receta> cmbReceta;  // El ComboBox de Recetas
    @FXML private TextField precioField;

    @Setter
    private ObservableList<Producto> listaProductos;
    private ObservableList<Sabor> saboresSeleccionados = FXCollections.observableArrayList();
    private Producto productoActual;
    @Setter
    private CrudProductosController parentController;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final RecetaDAO recetaDAO = new RecetaDAO();  // DAO para Recetas

    private byte[] imagen; //variable para almacenar la imagen por el usuario
    private boolean imagenCargada = false; // Variable de control para verificar si la imagen es cargada por el usuario

    @FXML
    public void initialize() {
        cargarCategorias();
        cargarRecetas();  // Cargar las recetas en el ComboBox
        if (productoActual != null) {
            cargarDatosProducto(productoActual);
        }
    }

    private void cargarCategorias() {
        List<Categoria> categorias = categoriaDAO.findAll();
        categoriaChoiceBox.setItems(FXCollections.observableArrayList(categorias));
        categoriaChoiceBox.getSelectionModel().selectFirst();
    }

    private void cargarRecetas() {
        List<Receta> recetas = recetaDAO.findAll();
        ObservableList<Receta> recetasList = FXCollections.observableArrayList(recetas);

        // Establecer las recetas en el ComboBox
        cmbReceta.setItems(recetasList);
    }

    public void setSaboresSeleccionados(ObservableList<Sabor> saboresSeleccionados) {
        this.saboresSeleccionados = saboresSeleccionados;
    }

    public void setProducto(Producto producto) {
        this.productoActual = producto;
        if (producto != null) {
            cargarDatosProducto(producto);
        }
    }

    private void cargarDatosProducto(Producto producto) {
        nombreProductoField.setText(producto.getNombre());
        descripcionProductoField.setText(producto.getDescripcion());
        categoriaChoiceBox.setValue(producto.getCategoria());
        precioField.setText(producto.getPrecio().toString());
        saboresSeleccionados.setAll(producto.getSabores());
        cmbReceta.setValue(producto.getReceta());  // Esto seleccionará la receta asociada al producto
    }

    private CatalogoPedidosController catalogoController;
    public void setCatalogoController(CatalogoPedidosController catalogoController) {
        this.catalogoController = catalogoController;
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        try {
            // Inicializar el CatalogoPedidosController si es nulo
            if (catalogoController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/CatalogoPedidos.fxml"));
                Parent root = loader.load();
                catalogoController = loader.getController();
            }

            Producto producto = crearOActualizarProducto();

            if (productoActual != null) {
                // Modificar producto existente
                actualizarProductoExistente(productoActual);
                // Registro de la acción
                ActionLogger.log("Producto modificado: " + producto.getNombre() + " (ID: " + producto.getId() + ")");
            } else {
                // Agregar nuevo producto
                agregarNuevoProducto(producto);
                // Registro de la acción
                ActionLogger.log("Nuevo producto creado: " + producto.getNombre());
            }

            cerrarVentana(event);

        } catch (NumberFormatException e) {
            mostrarMensaje(Alert.AlertType.ERROR, "Error de Validación", "El precio debe ser un valor numérico válido.");
            // Registro de la acción
            ActionLogger.log("Error al guardar producto: El precio no es válido.");
        } catch (Exception e) {
            // Registro de la acción
            ActionLogger.log("Error al guardar producto: " + e.getMessage());
        }
    }

    private void actualizarProductoExistente(Producto producto) {
        productoDAO.update(producto);
        if (catalogoController != null) {
            catalogoController.modificarProducto(producto);
        } else {
            mostrarMensaje(Alert.AlertType.ERROR, "Error", "catalogoController es null.");
        }
    }

    private void agregarNuevoProducto(Producto producto) {
        producto.setImagen(imagen);
        productoDAO.save(producto);
        if (catalogoController != null) {
            catalogoController.agregarProducto(producto);
        } else {
            mostrarMensaje(Alert.AlertType.ERROR, "Error", "catalogoController es null.");
        }
    }

    private Producto crearOActualizarProducto() {
        String nombre = nombreProductoField.getText();
        String descripcion = descripcionProductoField.getText();
        Categoria categoria = categoriaChoiceBox.getValue();
        BigDecimal precio = new BigDecimal(precioField.getText());
        Receta receta = cmbReceta.getValue();  // Obtener la receta seleccionada
        try{
            if (!validarCampos(nombre, descripcion, categoria, precio)) {
                throw new IllegalArgumentException("Todos los campos deben estar completos y ser válidos.");
            }

            if (saboresSeleccionados == null || saboresSeleccionados.isEmpty()) {
                mostrarMensaje(Alert.AlertType.ERROR, "Error de Validación", "Debe seleccionar al menos un sabor.");
                return null;
            }

            if (productoActual == null) {
                productoActual = new Producto(nombre, descripcion, categoria, precio, imagen);
            } else {
                productoActual.setNombre(nombre);
                productoActual.setDescripcion(descripcion);
                productoActual.setCategoria(categoria);
                productoActual.setPrecio(precio);
                productoActual.setImagen(imagen);
            }

            productoActual.setSabores(saboresSeleccionados);
            // Asignar la receta seleccionada al producto
            productoActual.setReceta(receta);


            return productoActual;
        } catch (Exception e) {
            mostrarMensaje(Alert.AlertType.ERROR, "Error", "Error al crear/actualizar el producto: " + e.getMessage());
            return null;
        }
    }

    private boolean validarCampos(String nombre, String descripcion, Categoria categoria, BigDecimal precio) {
        return nombre != null && !nombre.isEmpty() &&
                descripcion != null && !descripcion.isEmpty() &&
                categoria != null &&
                precio != null && precio.compareTo(BigDecimal.ZERO) > 0;
    }

    @FXML
    private void handleCargarImagen(ActionEvent event) {
        File archivo = seleccionarArchivo();
        if (archivo != null) {
            cargarImagen(archivo);
            imagenCargada = true; // Marcar que la imagen ha sido cargada por el usuario
            // Registro de la acción
            ActionLogger.log("Imagen cargada para el producto: " + nombreProductoField.getText());
        } else {
            // Registro de la acción en caso de que no se seleccione ninguna imagen
            ActionLogger.log("Intento fallido de cargar imagen para el producto: " + nombreProductoField.getText());
        }
    }

    private File seleccionarArchivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Producto");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de Imagen (*.png, *.jpg)", "*.png", "*.jpg"));
        return fileChooser.showOpenDialog(nombreProductoField.getScene().getWindow());
    }

    private void cargarImagen(File archivo) {
        try (FileInputStream fis = new FileInputStream(archivo)) {
            imagen = fis.readAllBytes();
            // Guardar los bytes de la imagen en la nueva variable imagenUsuario
            mostrarMensaje(Alert.AlertType.INFORMATION, "Imagen Cargada", "La imagen ha sido cargada exitosamente.");
        } catch (IOException e) {
            mostrarMensaje(Alert.AlertType.ERROR, "Error al cargar la imagen", e.getMessage());
        }
    }

    @FXML
    private void handleSeleccionarSabores(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/sabores.fxml"));
            AnchorPane root = loader.load();
            SaboresController saboresController = loader.getController();
            saboresController.setParentController(this);
            saboresController.setSaboresSeleccionados(new ArrayList<>(saboresSeleccionados));

            Stage stage = new Stage();
            stage.setTitle("Seleccionar Sabores");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initOwner(nombreProductoField.getScene().getWindow());
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.showAndWait();

            // Al cerrar la ventana, actualiza los sabores seleccionados
            this.saboresSeleccionados.setAll(saboresController.getSaboresSeleccionados());
            ActionLogger.log("Sabores seleccionados para el producto: " + nombreProductoField.getText());
        } catch (IOException e) {
            mostrarMensaje(Alert.AlertType.ERROR, "Error", "No se pudo abrir la ventana de selección de sabores.");
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        cerrarVentana(event);
    }

    private void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void mostrarMensaje(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    public void agregarProducto(Producto nuevoProducto) {
        if (parentController != null) {
            parentController.getListaProductos().add(nuevoProducto);
        }
    }

    public void modificarProducto(Producto productoModificado) {
        if (parentController != null && !parentController.getListaProductos().contains(productoModificado)) {
            int index = parentController.getListaProductos().indexOf(productoModificado);
            if (index >= 0) {
                parentController.getListaProductos().set(index, productoModificado);
            }
        }
    }

    public CrudProductosController getParentController() {
        return parentController;
    }
    public void setParentController(CrudProductosController parentController) {
        this.parentController = parentController;
    }
    public ObservableList<Producto> getListaProductos() {
        return listaProductos;
    }
    public void setListaProductos(ObservableList<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }
}