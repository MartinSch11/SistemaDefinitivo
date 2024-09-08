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

    private ProductoDAO productoDAO = new ProductoDAO();
    private CategoriaDAO categoriaDAO = new CategoriaDAO();
    private SaborDAO saborDAO = new SaborDAO();

    @FXML
    public void initialize() {
        cargarCategorias();
        cargarSabores();
    }

    private void cargarCategorias() {
        ObservableList<String> categorias = FXCollections.observableArrayList();
        for (Categoria categoria : categoriaDAO.findAll()) {
            categorias.add(categoria.getNombre());
        }
        chbCategoria.setItems(categorias);
        chbCategoria.getSelectionModel().selectFirst();
    }

    private void cargarSabores() {
        ObservableList<String> sabores = FXCollections.observableArrayList();
        for (Sabor sabor : saborDAO.findAll()) {
            sabores.add(sabor.getSabor());  // Usa getSabor en lugar de getNombre
        }
        lstSabores.getItems().addAll(sabores);
        lstSabores.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    private void handleGuardarProd() {
        Producto producto = crearProductoDesdeFormulario();
        if (producto != null) {
            productoDAO.save(producto); // Guardar el producto en la base de datos
            crudController.agregarProducto(producto);
            cerrarVentana();
        }
    }

    private Producto crearProductoDesdeFormulario() {
        String nombre = txtNomProducto.getText();
        String descripcion = txtDescProducto.getText();
        String categoriaSeleccionada = chbCategoria.getValue();

        float precio;
        try {
            precio = Float.parseFloat(txtPrecio.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Precio inválido", "Por favor ingresa un valor numérico para el precio.");
            return null;
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(categoriaSeleccionada);

        Producto producto = new Producto(nombre, descripcion, categoria, precio);

        ObservableList<String> saboresSeleccionados = lstSabores.getSelectionModel().getSelectedItems();
        for (String saborSeleccionado : saboresSeleccionados) {
            Sabor sabor = new Sabor();
            sabor.setSabor(saborSeleccionado);  // Usa setSabor en lugar de setNombre
            producto.getSabores().add(sabor);
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
        Stage stage = (Stage) txtNomProducto.getScene().getWindow();
        stage.close();
    }
}
