package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;
import model.Categoria;
import model.Producto;
import model.Sabor;

public class AProductoController {
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

    @FXML
    public void initialize() {
        ObservableList<String> categorias = FXCollections.observableArrayList(
                "Tartas", "Galletas", "Cupcakes"
        );
        chbCategoria.setItems(categorias);

        lstSabores.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lstSabores.getItems().addAll("Chocolate", "Fresa", "Vainilla");

        chbCategoria.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleGuardarProd() {
        String nombre = txtNomProducto.getText();
        String descripcion = txtDescProducto.getText();
        String categoriaSeleccionada = chbCategoria.getValue();
        float precio = Float.parseFloat(txtPrecio.getText());

        Categoria categoria = new Categoria();
        categoria.setNombre(categoriaSeleccionada);

        Producto producto = new Producto(nombre, descripcion, categoria, precio);

        ObservableList<String> saboresSeleccionados = lstSabores.getSelectionModel().getSelectedItems();
        for (String saborSeleccionado : saboresSeleccionados) {
            Sabor sabor = new Sabor();
            sabor.setNombre(saborSeleccionado);
            producto.getSabores().add(sabor);
        }

        crudController.agregarProducto(producto);

        Stage stage = (Stage) txtNomProducto.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancelar() {
        Stage stage = (Stage) txtNomProducto.getScene().getWindow();
        stage.close();
    }
}
