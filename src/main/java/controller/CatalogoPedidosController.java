package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import model.Producto;
import persistence.dao.ProductoDAO;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.*;

public class CatalogoPedidosController {
    @FXML private Button guardarPedidoButton;
    @FXML private ScrollPane scrollPaneCatalogo;
    @FXML private GridPane gridPane;
    private final String imagenProductoPorDefecto = "/productosImag/imagenProductoPorDefecto.png";
    private Map<Producto, Integer> contadoresProductos = new HashMap<>();
    private List<Producto> productos = new ArrayList<>(); // Lista de productos a mostrar
    private ProductoDAO ProductoDAO = new ProductoDAO();

    private Map<Producto, Integer> productosGuardados = new HashMap<>(); // Mapa para productos guardados

    private NuevoPedidoController nuevoPedidoController;

    public void setDialogNuevoPedidoController(NuevoPedidoController controller) {
        if (controller != null) {
            this.nuevoPedidoController = controller;
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "El controlador NuevoPedidoController no está disponible.");
        }
    }

    public void initialize() {
        // Inicialmente cargar los productos de la base de datos
        productos = ProductoDAO.findAll();
        agregarProductosAlGrid();

        Button guardarPedidoButton = new Button("Guardar Pedido");
        guardarPedidoButton.getStyleClass().add("buttons");
        guardarPedidoButton.setOnAction(this::guardarPedido); // Asegúrate de que se pase correctamente el evento al metodo
        gridPane.add(guardarPedidoButton, 0, productos.size() + 1, 3, 1);
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(guardarPedidoButton);
        gridPane.add(vbox, 0, productos.size() + 1, 3, 1);
    }

    public void agregarProducto(Producto producto) {
        productos.add(producto);
        agregarProductosAlGrid();
    }

    public void modificarProducto(Producto producto) {
        // Encuentra y actualiza el producto existente
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getId() == producto.getId()) {
                productos.set(i, producto);
                break;
            }
        }
        agregarProductosAlGrid();
    }

    private void agregarProductosAlGrid() {
        gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPrefWidth(scrollPaneCatalogo.getPrefWidth());
        gridPane.setPrefHeight(scrollPaneCatalogo.getPrefHeight());

        int column = 0;
        int row = 0;
        for (Producto producto : productos) {
            StackPane stackPane = crearStackPaneProducto(producto);
            gridPane.add(stackPane, column++, row);
            if (column == 3) { // Tres columnas por fila
                column = 0;
                row++;
            }
        }
        scrollPaneCatalogo.setContent(gridPane);
    }

    private StackPane crearStackPaneProducto(Producto producto) {
        StackPane stackPane = new StackPane();
        stackPane.setPrefWidth(260);
        stackPane.setPrefHeight(170);
        stackPane.getStyleClass().add("stack-pane");

        VBox vbox = new VBox(5);
        vbox.setAlignment(Pos.CENTER);

        ImageView imageView = new ImageView();
        if (producto.getImagen() != null) {
            imageView.setImage(new Image(new ByteArrayInputStream(producto.getImagen())));
        } else {
            InputStream defaultImage = getClass().getResourceAsStream(imagenProductoPorDefecto);
            if (defaultImage != null) {
                imageView.setImage(new Image(defaultImage));
            } else {
                System.out.println("Imagen por defecto no encontrada");
            }
        }
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        vbox.getChildren().add(imageView);

        // Nombre del producto
        Label nombreProducto = new Label(producto.getNombre());
        nombreProducto.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        vbox.getChildren().add(nombreProducto);

        // ComboBox vacío por el momento
        ComboBox<String> comboBox = new ComboBox<>();
        vbox.getChildren().add(comboBox);

        // HBox para los botones y el contador
        HBox hBox = new HBox(5);
        hBox.setAlignment(Pos.CENTER);

        // Botón "-"
        Button menosButton = new Button("-");
        menosButton.getStyleClass().add("buttons");
        hBox.getChildren().add(menosButton);

        // Label contador
        Label contadorLabel = new Label("0");
        hBox.getChildren().add(contadorLabel);

        // Botón "+"
        Button masButton = new Button("+");
        masButton.getStyleClass().add("buttons");
        hBox.getChildren().add(masButton);

        vbox.getChildren().add(hBox);

        stackPane.getChildren().add(vbox);

        // Lógica de los botones
        menosButton.setDisable(true); // Inicialmente deshabilitado

        menosButton.setOnAction(event -> {
            int count = Integer.parseInt(contadorLabel.getText());
            if (count > 0) {
                contadorLabel.setText(String.valueOf(--count));
                menosButton.setDisable(count - 1 <= 0); // Deshabilita si llega a 0
                contadoresProductos.put(producto, count);
            }
        });

        masButton.setOnAction(event -> {
            int count = Integer.parseInt(contadorLabel.getText());
            contadorLabel.setText(String.valueOf(++count));
            menosButton.setDisable(false); // Habilita el botón "-"
            contadoresProductos.put(producto, count);
        });
        return stackPane;
    }

    private void guardarPedido(ActionEvent actionEvent) {
        for (Map.Entry<Producto, Integer> entry : contadoresProductos.entrySet()) {
            if (entry.getValue() > 0) {
                productosGuardados.put(entry.getKey(), entry.getValue());
            }
        }

        if (!productosGuardados.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Pedido guardado", "El pedido ha sido guardado exitosamente.");
        } else {
            showAlert(Alert.AlertType.WARNING, "Sin selección", "No se seleccionó ningún producto para el pedido.");
        }

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }

    public Map<Producto, Integer> getProductosGuardados() {
        return productosGuardados;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}