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
import model.Categoria;
import model.Producto;
import model.Sabor;
import persistence.dao.ProductoDAO;
import persistence.dao.CategoriaDAO;
import utilities.SceneLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductoFormController {

    @FXML private TextField nombreProductoField;
    @FXML private TextArea descripcionProductoField;
    @FXML private ChoiceBox<Categoria> categoriaChoiceBox;
    @FXML private TextField precioField;

    // Nueva lista para almacenar los sabores seleccionados
    private ObservableList<Sabor> saboresSeleccionados = FXCollections.observableArrayList();

    private Producto productoActual;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    @FXML
    public void initialize() {
        // Cargar categorías
        cargarCategorias();
        ObservableList<Categoria> categorias = FXCollections.observableArrayList(categoriaDAO.findAll());
        categoriaChoiceBox.setItems(categorias);

        // Cargar datos del producto si existe
        if (productoActual != null) {
            cargarDatosProducto(productoActual);
        }
    }

    private void cargarCategorias() {
        List<Categoria> categorias = categoriaDAO.findAll();
        categoriaChoiceBox.getItems().addAll(categorias);
        categoriaChoiceBox.getSelectionModel().selectFirst();
    }

    public void setProductoParaEditar(Producto producto) {
        nombreProductoField.setText(producto.getNombre());
        descripcionProductoField.setText(producto.getDescripcion());
        categoriaChoiceBox.setValue(producto.getCategoria());
        precioField.setText(producto.getPrecio().toString());

        // Cargar sabores seleccionados
        this.saboresSeleccionados.setAll(producto.getSabores()); // Asumiendo que getSabores() devuelve una lista de Sabor
    }

    public void setProducto(Producto producto) {
        this.productoActual = producto;
        if (producto != null) {
            cargarDatosProducto(producto);
        }
    }

    private CatalogoPedidosController catalogoController;
    // Referencia al controlador del catálogo
    public void setCatalogoController(CatalogoPedidosController catalogoController) {
     this.catalogoController = catalogoController;
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

        // Verificar si hay sabores seleccionados
        if (saboresSeleccionados.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "Debe seleccionar al menos un sabor.");
            return;
        }

        try {
            if (productoActual != null) {
                productoActual.setNombre(nombreProducto);
                productoActual.setDescripcion(descripcionProducto);
                productoActual.setCategoria(categoriaSeleccionada);
                productoActual.setPrecio(precio);
                productoActual.setSabores(saboresSeleccionados);  // Guardar los sabores como objetos Sabor
                productoDAO.update(productoActual);
            } else {
                Producto nuevoProducto = new Producto(nombreProducto, descripcionProducto, categoriaSeleccionada, precio, null);
                nuevoProducto.setSabores(saboresSeleccionados);  // Guardar los sabores seleccionados
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

    private boolean validateFields(String nombreProducto, String descripcionProducto, Categoria categoriaSeleccionada, BigDecimal precio) {
        return nombreProducto != null && !nombreProducto.isEmpty() &&
                descripcionProducto != null && !descripcionProducto.isEmpty() &&
                categoriaSeleccionada != null &&
                precio != null && precio.compareTo(BigDecimal.ZERO) > 0;
    }

    public void cargarDatosProducto(Producto producto) {
        nombreProductoField.setText(producto.getNombre());
        descripcionProductoField.setText(producto.getDescripcion());
        categoriaChoiceBox.setValue(producto.getCategoria());
        precioField.setText(producto.getPrecio().toString());

        // Cargar sabores seleccionados
        this.saboresSeleccionados.setAll(producto.getSabores());
    }

    @FXML
    private void handleCargarImagen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Producto");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos de Imagen (*.png, *.jpg)", "*.png", "*.jpg");
        fileChooser.getExtensionFilters().add(extFilter);

        File archivo = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (archivo != null) {
            try {
                byte[] imageBytes = new byte[(int) archivo.length()];
                FileInputStream fis = new FileInputStream(archivo);
                fis.read(imageBytes);
                fis.close();

                if (productoActual != null) {
                    productoActual.setImagen(imageBytes);
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
    private void handleSeleccionarSabores(ActionEvent event) {
        Dialog<List<Sabor>> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar Sabores");

        FXMLLoader loader = SceneLoader.loadDialogPane("/com/example/pasteleria/sabores.fxml", "/css/sabores.css");
        if (loader == null) {
            return;  // Si hay un error al cargar el FXML, salimos del método
        }

        DialogPane dialogPane = loader.getRoot();
        dialog.setDialogPane(dialogPane);

        // Obtener el controlador del archivo FXML cargado
        SaboresController saboresController = loader.getController();
        saboresController.setParentController(this); // Asegúrate de que esto se está llamando

        // Pasar los sabores seleccionados al controlador de sabores
        saboresController.setSaboresSeleccionados(new ArrayList<>(this.saboresSeleccionados)); // Copia de la lista

        // Mostrar el diálogo y esperar a que el usuario haga una selección
        dialog.showAndWait();

        // Obtener los sabores seleccionados del controlador
        List<Sabor> saboresSeleccionados = saboresController.getSaboresSeleccionados();

        // Verificar si hay sabores seleccionados
        if (saboresSeleccionados == null || saboresSeleccionados.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error de selección", "Debe seleccionar al menos un sabor.");
        } else {
            this.saboresSeleccionados.setAll(saboresSeleccionados);
            System.out.println("Sabores seleccionados: " + saboresSeleccionados);
        }
    }


    public void setSaboresSeleccionados(List<Sabor> saboresSeleccionados) {
        System.out.println("Sabores seleccionados: " + saboresSeleccionados); // Depuración
        this.saboresSeleccionados.setAll(saboresSeleccionados);  // Manejar la lista de objetos Sabor
        // No olvides imprimir para depurar
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
