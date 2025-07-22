package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Combo;
import model.ComboProducto;
import model.Producto;
import persistence.dao.ComboDAO;
import persistence.dao.ProductoDAO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ComboFormController {
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private ListView<String> listProductos;
    @FXML private Button btnAgregarProducto;
    @FXML private Button btnQuitarProducto;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private ObservableList<Producto> productosDisponibles = FXCollections.observableArrayList();
    private List<ComboProducto> productosCombo = new ArrayList<>();
    private ComboDAO comboDAO = new ComboDAO();
    private ProductoDAO productoDAO = new ProductoDAO();

    @FXML
    public void initialize() {
        // Solo productos con receta
        productosDisponibles.addAll(productoDAO.findAll().stream()
            .filter(p -> p.getReceta() != null && p.getReceta().getId() != null)
            .toList());
        actualizarListaProductos();
        btnAgregarProducto.setOnAction(e -> agregarProductoAlCombo());
        btnQuitarProducto.setOnAction(e -> quitarProductoDelCombo());
        btnGuardar.setOnAction(e -> guardarCombo());
        btnCancelar.setOnAction(e -> cerrarVentana());
    }

    private void actualizarListaProductos() {
        listProductos.getItems().clear();
        for (ComboProducto cp : productosCombo) {
            listProductos.getItems().add(cp.getProducto().getNombre() + " x" + cp.getCantidad());
        }
    }

    private void agregarProductoAlCombo() {
        ChoiceDialog<Producto> dialog = new ChoiceDialog<>(null, productosDisponibles);
        dialog.setTitle("Agregar producto al combo");
        dialog.setHeaderText("Selecciona un producto para agregar al combo");
        dialog.setContentText("Producto:");
        dialog.showAndWait().ifPresent(producto -> {
            TextInputDialog cantidadDialog = new TextInputDialog("1");
            cantidadDialog.setTitle("Cantidad");
            cantidadDialog.setHeaderText("Cantidad para " + producto.getNombre());
            cantidadDialog.setContentText("Cantidad:");
            cantidadDialog.showAndWait().ifPresent(cantStr -> {
                try {
                    int cantidad = Integer.parseInt(cantStr);
                    if (cantidad > 0) {
                        ComboProducto cp = new ComboProducto();
                        cp.setProducto(producto);
                        cp.setCantidad(cantidad);
                        productosCombo.add(cp);
                        actualizarListaProductos();
                    }
                } catch (NumberFormatException ignored) {}
            });
        });
    }

    private void quitarProductoDelCombo() {
        int idx = listProductos.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            productosCombo.remove(idx);
            actualizarListaProductos();
        }
    }

    private void guardarCombo() {
        String nombre = txtNombre.getText();
        String descripcion = txtDescripcion.getText();
        String precioStr = txtPrecio.getText();
        if (nombre.isEmpty() || precioStr.isEmpty() || productosCombo.isEmpty()) {
            mostrarAlerta("Faltan datos", "Completa todos los campos y agrega al menos un producto.");
            return;
        }
        try {
            BigDecimal precio = new BigDecimal(precioStr);
            Combo combo = new Combo();
            combo.setNombre(nombre);
            combo.setDescripcion(descripcion);
            combo.setPrecio(precio);
            // Asignar el combo padre a cada ComboProducto
            for (ComboProducto cp : productosCombo) {
                cp.setCombo(combo);
            }
            combo.setProductos(new ArrayList<>(productosCombo));
            comboDAO.save(combo);
            cerrarVentana();
        } catch (NumberFormatException e) {
            mostrarAlerta("Precio inválido", "El precio debe ser un número válido.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
