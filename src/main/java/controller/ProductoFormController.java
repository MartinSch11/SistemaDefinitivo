package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Setter;
import model.Categoria;
import model.Producto;
import model.Sabor;
import persistence.dao.CategoriaDAO;
import persistence.dao.ProductoDAO;
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
    @FXML private TextField precioField;

    @Setter
    private ObservableList<Producto> listaProductos;
    private ObservableList<Sabor> saboresSeleccionados = FXCollections.observableArrayList();
    private Producto productoActual;
    @Setter
    private CrudProductosController parentController; // Declarar la variable

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    @FXML
    public void initialize() {
        cargarCategorias();
        if (productoActual != null) {
            cargarDatosProducto(productoActual);
        }
    }

    private void cargarCategorias() {
        List<Categoria> categorias = categoriaDAO.findAll();
        categoriaChoiceBox.setItems(FXCollections.observableArrayList(categorias));
        categoriaChoiceBox.getSelectionModel().selectFirst();
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
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        try {
            Producto producto = crearOActualizarProducto();

            if (productoActual != null) {
                // Modificar producto existente
                productoDAO.update(productoActual);
                modificarProducto(productoActual); // Llama a modificarProducto
            } else {
                // Agregar nuevo producto
                productoDAO.save(producto);
                agregarProducto(producto); // Llama a agregarProducto
            }

            mostrarMensaje(Alert.AlertType.INFORMATION, "Éxito", "Producto guardado exitosamente.");
            cerrarVentana(event);
        } catch (NumberFormatException e) {
            mostrarMensaje(Alert.AlertType.ERROR, "Error de Validación", "El precio debe ser un valor numérico válido.");
        } catch (Exception e) {
            mostrarMensaje(Alert.AlertType.ERROR, "Error", "No se pudo guardar el producto: " + e.getMessage());
        }
    }

    private Producto crearOActualizarProducto() {
        String nombre = nombreProductoField.getText();
        String descripcion = descripcionProductoField.getText();
        Categoria categoria = categoriaChoiceBox.getValue();
        BigDecimal precio = new BigDecimal(precioField.getText());

        if (!validarCampos(nombre, descripcion, categoria, precio)) {
            throw new IllegalArgumentException("Todos los campos deben estar completos y ser válidos.");
        }

        if (saboresSeleccionados.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un sabor.");
        }

        if (productoActual == null) {
            productoActual = new Producto(nombre, descripcion, categoria, precio, null);
        } else {
            productoActual.setNombre(nombre);
            productoActual.setDescripcion(descripcion);
            productoActual.setCategoria(categoria);
            productoActual.setPrecio(precio);
        }

        productoActual.setSabores(saboresSeleccionados);
        return productoActual;
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
            byte[] imageBytes = fis.readAllBytes();
            if (productoActual != null) {
                productoActual.setImagen(imageBytes);
            }
        } catch (IOException e) {
            mostrarMensaje(Alert.AlertType.ERROR, "Error al cargar la imagen", e.getMessage());
        }
    }

    @FXML
    private void handleSeleccionarSabores(ActionEvent event) {
        Dialog<List<Sabor>> dialog = crearDialogoSabores();
        dialog.showAndWait().ifPresent(this::actualizarSaboresSeleccionados);
    }

    private Dialog<List<Sabor>> crearDialogoSabores() {
        Dialog<List<Sabor>> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar Sabores");
        FXMLLoader loader = SceneLoader.loadDialogPane("/com/example/pasteleria/sabores.fxml", "/css/sabores.css");
        if (loader == null) return null;

        dialog.setDialogPane(loader.getRoot());
        SaboresController saboresController = loader.getController();
        saboresController.setParentController(this);
        saboresController.setSaboresSeleccionados(new ArrayList<>(saboresSeleccionados));
        return dialog;
    }

    private void actualizarSaboresSeleccionados(List<Sabor> nuevosSabores) {
        if (nuevosSabores != null && !nuevosSabores.isEmpty()) {
            saboresSeleccionados.setAll(nuevosSabores);
        } else {
            mostrarMensaje(Alert.AlertType.ERROR, "Error de selección", "Debe seleccionar al menos un sabor.");
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
        if (parentController != null) {
            int index = parentController.getListaProductos().indexOf(productoModificado);
            if (index >= 0) {
                parentController.getListaProductos().set(index, productoModificado);
            }
        }
    }

}
