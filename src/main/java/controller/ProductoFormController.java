package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Categoria;
import model.Producto;
import model.Sabor;
import persistence.dao.ProductoDAO;
import persistence.dao.CategoriaDAO;
import persistence.dao.SaborDAO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class ProductoFormController{

    @FXML private TextField nombreProductoField;
    @FXML private TextArea descripcionProductoField;
    @FXML private ChoiceBox<Categoria> categoriaChoiceBox;
    @FXML private TextField precioField;
    @FXML private ListView<String> saboresListView;

    private Producto productoActual;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final SaborDAO saborDAO = new SaborDAO();

    @FXML
    public void initialize() {
        // Cargar categorías y asegurarte de que el ChoiceBox es de tipo Categoria
        cargarCategorias();
        ObservableList<Categoria> categorias = FXCollections.observableArrayList(categoriaDAO.findAll());
        categoriaChoiceBox.setItems(categorias);

        // Cargar sabores
        cargarSabores();

        // Cargar datos del producto si existe
        if (productoActual != null) {
            cargarDatosProducto(productoActual);
        }
    }

    private void cargarCategorias() {
        List<Categoria> categorias = categoriaDAO.findAll();
        categoriaChoiceBox.getItems().addAll(categorias);  // Asegúrate de que esto sea correcto
        categoriaChoiceBox.getSelectionModel().selectFirst();  // Seleccionar la primera categoría
    }

    private void cargarSabores() {
        List<String> sabores = saborDAO.findAll()
                .stream()
                .map(Sabor::getSabor)
                .toList();
        saboresListView.getItems().addAll(sabores);
        saboresListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void setProductoParaEditar(Producto producto) {
        nombreProductoField.setText(producto.getNombre());
        descripcionProductoField.setText(producto.getDescripcion());

        // Asignar la categoría del producto
        categoriaChoiceBox.setValue(producto.getCategoria());

        // Asignar el precio del producto
        precioField.setText(String.valueOf(producto.getPrecio()));

        // Limpiar la lista de sabores antes de agregar los nuevos
        saboresListView.getItems().clear();

        // Obtener la lista de sabores del producto
        List<String> saboresProducto = producto.getSabores()
                .stream()
                .map(Sabor::getSabor)
                .toList();

        // Cargar todos los sabores disponibles y seleccionar los que el producto ya tiene
        List<String> todosLosSabores = saborDAO.findAll()
                .stream()
                .map(Sabor::getSabor)
                .toList();

        // Agregar todos los sabores al ListView
        saboresListView.getItems().addAll(todosLosSabores);

        // Seleccionar los sabores que ya tiene el producto
        for (String sabor : saboresProducto) {
            int index = saboresListView.getItems().indexOf(sabor);
            if (index >= 0) {
                saboresListView.getSelectionModel().select(index);
            }
        }

        // Configurar la selección múltiple
        saboresListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void setProducto(Producto producto) {
        this.productoActual = producto;
        if (producto != null) {
            cargarDatosProducto(producto);
        }
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        String nombreProducto = nombreProductoField.getText();
        String descripcionProducto = descripcionProductoField.getText();
        Categoria categoriaSeleccionada = categoriaChoiceBox.getValue();

        BigDecimal precio;
        try {
            precio = new BigDecimal(precioField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "Por favor, ingresa un valor válido para el precio.");
            return;
        }

        if (!validateFields(nombreProducto, descripcionProducto, categoriaSeleccionada, precio)) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "Todos los campos deben ser llenados.");
            return;
        }

        try {
            if (categoriaSeleccionada == null) {
                showAlert(Alert.AlertType.ERROR, "Error de Validación", "La categoría seleccionada no es válida.");
                return;
            }

            if (productoActual != null) {
                // Actualizar producto existente
                productoActual.setNombre(nombreProducto);
                productoActual.setDescripcion(descripcionProducto);
                productoActual.setCategoria(categoriaSeleccionada);
                productoActual.setPrecio(precio);
                actualizarSaboresSeleccionados(productoActual);
                productoDAO.update(productoActual);
            } else {
                // Crear un nuevo producto
                Producto nuevoProducto = new Producto(nombreProducto, descripcionProducto, categoriaSeleccionada, precio, productoActual.getImagen()); // Guardar imagen
                actualizarSaboresSeleccionados(nuevoProducto);
                productoDAO.save(nuevoProducto);
            }

            // Cerrar el formulario
            Stage stage = (Stage) nombreProductoField.getScene().getWindow();
            stage.close();
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Producto guardado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error al guardar el producto: " + e.getMessage());
        }
    }

    private void actualizarSaboresSeleccionados(Producto producto) {
        producto.getSabores().clear();
        List<String> saboresSeleccionados = saboresListView.getSelectionModel().getSelectedItems();
        for (String saborSeleccionado : saboresSeleccionados) {
            Sabor sabor = saborDAO.findByName(saborSeleccionado);
            if (sabor != null) {
                producto.getSabores().add(sabor);
            }
        }
    }

    private boolean validateFields(String nombreProducto, String descripcionProducto, Categoria categoriaSeleccionada, BigDecimal precio) {
        return nombreProducto != null && !nombreProducto.isEmpty() &&
                descripcionProducto != null && !descripcionProducto.isEmpty() &&
                categoriaSeleccionada != null && // Validar el objeto Categoria
                precio != null && precio.compareTo(BigDecimal.ZERO) > 0;
    }

    public void cargarDatosProducto(Producto producto) {
        nombreProductoField.setText(producto.getNombre());
        descripcionProductoField.setText(producto.getDescripcion());
        categoriaChoiceBox.setValue(producto.getCategoria());  // Esto debería ser correcto ahora
        precioField.setText(producto.getPrecio().toString());

        // Cargar sabores
        List<String> saboresProducto = producto.getSabores()
                .stream()
                .map(Sabor::getSabor)
                .toList();

        saboresListView.getSelectionModel().clearSelection();
        for (String sabor : saboresProducto) {
            int index = saboresListView.getItems().indexOf(sabor);
            if (index >= 0) {
                saboresListView.getSelectionModel().select(index);
            }
        }
    }
    @FXML
    private void handleCargarImagen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Producto");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos de Imagen (*.png, *.jpg, *.gif)", "*.png", "*.jpg", "*.gif");
        fileChooser.getExtensionFilters().add(extFilter);

        // Mostrar el diálogo para seleccionar la imagen
        File archivo = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (archivo != null) {
            try {
                // Convertir la imagen a byte[]
                byte[] imageBytes = new byte[(int) archivo.length()];
                FileInputStream fis = new FileInputStream(archivo);
                fis.read(imageBytes);
                fis.close();

                // Asignar la imagen en bytes al producto actual o al nuevo producto
                if (productoActual != null) {
                    productoActual.setImagen(imageBytes); // Actualiza el producto existente
                } else {
                    // Aquí puedes guardar la imagen para un nuevo producto más tarde
                    // Esto se hace al momento de crear el nuevo producto en handleGuardar
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar la imagen: " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Error al leer la imagen: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
