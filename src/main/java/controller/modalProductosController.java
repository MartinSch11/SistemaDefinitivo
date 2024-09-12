package controller;

import persistence.dao.CategoriaDAO;
import persistence.dao.SaborDAO;
import persistence.dao.ProductoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import model.Categoria;
import model.Producto;
import model.Sabor;

import java.util.List;
import java.util.stream.Collectors;

public class modalProductosController {
    @FXML
    private ListView<String> lstSabores;
    @FXML
    private TextField txtNomProducto;
    @FXML
    private TextArea txtDescProducto;
    @FXML
    private ChoiceBox<String> chbCategoria;
    @FXML
    private TextField txtPrecio;

    @Setter
    private CrudProductosController crudController;
    @Setter
    private Producto productoParaEditar;

    private ProductoDAO productoDAO = new ProductoDAO();
    private CategoriaDAO categoriaDAO = new CategoriaDAO();
    private SaborDAO saborDAO = new SaborDAO();

    @FXML
    public void initialize() {
        cargarCategorias();
        cargarSabores();

        if (productoParaEditar != null) {
            cargarDatosProducto(productoParaEditar);
        }
    }

    private void cargarCategorias() {
        ObservableList<String> categorias = FXCollections.observableArrayList(
                categoriaDAO.findAll().stream()
                        .map(Categoria::getNombre)
                        .collect(Collectors.toList())
        );
        chbCategoria.setItems(categorias);
        chbCategoria.getSelectionModel().selectFirst();
    }

    private void cargarSabores() {
        ObservableList<String> sabores = FXCollections.observableArrayList(
                saborDAO.findAll().stream()
                        .map(Sabor::getSabor)
                        .collect(Collectors.toList())
        );
        lstSabores.getItems().addAll(sabores);
        lstSabores.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void cargarDatosProducto(Producto producto) {
        txtNomProducto.setText(producto.getNombre());
        txtDescProducto.setText(producto.getDescripcion());
        chbCategoria.setValue(producto.getCategoria().getNombre());
        txtPrecio.setText(String.valueOf(producto.getPrecio()));

        List<String> saboresProducto = producto.getSabores().stream()
                .map(Sabor::getSabor)
                .collect(Collectors.toList());

        lstSabores.getSelectionModel().clearSelection(); // Limpiar selección previa

        for (String sabor : saboresProducto) {
            int index = lstSabores.getItems().indexOf(sabor);
            if (index >= 0) {
                lstSabores.getSelectionModel().select(index);
            }
        }
    }

    @FXML
    private void handleGuardarProd() {
        Producto producto = crearProductoDesdeFormulario();
        if (producto != null) {
            if (productoParaEditar == null) {
                productoDAO.save(producto); // Guardar nuevo producto
                crudController.agregarProducto(producto);
            } else {
                productoDAO.update(producto); // Actualizar producto existente
                crudController.actualizarProducto(producto);
            }
            cerrarVentana();
        }
    }

    private Producto crearProductoDesdeFormulario() {
        String nombre = txtNomProducto.getText();
        String descripcion = txtDescProducto.getText();
        String categoriaSeleccionada = chbCategoria.getValue();

        if (nombre.isEmpty() || descripcion.isEmpty() || categoriaSeleccionada == null || txtPrecio.getText().isEmpty()) {
            mostrarAlerta("Error", "Datos incompletos", "Por favor, completa todos los campos.");
            return null;
        }

        float precio;
        try {
            precio = Float.parseFloat(txtPrecio.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Precio inválido", "Por favor ingresa un valor numérico para el precio.");
            return null;
        }

        Categoria categoria = categoriaDAO.findByName(categoriaSeleccionada);
        if (categoria == null) {
            mostrarAlerta("Error", "Categoría no encontrada", "La categoría seleccionada no es válida.");
            return null;
        }

        Producto producto = (productoParaEditar == null) ?
                new Producto(nombre, descripcion, categoria, precio) : productoParaEditar;

        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setCategoria(categoria);
        producto.setPrecio(precio);

        producto.getSabores().clear();
        ObservableList<String> saboresSeleccionados = lstSabores.getSelectionModel().getSelectedItems();
        for (String saborSeleccionado : saboresSeleccionados) {
            Sabor sabor = saborDAO.findByName(saborSeleccionado);
            if (sabor != null) {
                producto.getSabores().add(sabor);
            }
        }

        return producto;
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtNomProducto.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String cabecera, String contenido) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecera);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }

    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }
}
