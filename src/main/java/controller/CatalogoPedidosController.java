package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import model.Producto;
import model.Categoria;
import model.Producto;
import model.Sabor;
import persistence.dao.CategoriaDAO;
import persistence.dao.ProductoDAO;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.*;

public class CatalogoPedidosController {
    @FXML private Button guardarPedidoButton;
    @FXML
    private ScrollPane scrollPaneCatalogo;

    @FXML
    private GridPane gridPane;
    private final String imagenProductoPorDefecto = "/productosImag/imagenProductoPorDefecto.png";
    private Map<Producto, Integer> contadoresProductos = new HashMap<>();

    private List<Producto> productos = new ArrayList<>(); // Lista de productos a mostrar
    private ProductoDAO ProductoDAO = new ProductoDAO();
    private EventObject event;

    private DialogNuevoPedidoController dialogNuevoPedidoController;
    public void setDialogNuevoPedidoController(DialogNuevoPedidoController controller) {
        this.dialogNuevoPedidoController = controller;
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
        menosButton.setOnAction(event -> {
            int count = Integer.parseInt(contadorLabel.getText());
            if (count > 0) contadorLabel.setText(String.valueOf(--count));
            contadoresProductos.put(producto, Integer.parseInt(contadorLabel.getText()));
        });
        masButton.setOnAction(event -> { int count = Integer.parseInt(contadorLabel.getText());
            contadorLabel.setText(String.valueOf(++count));
            contadoresProductos.put(producto, Integer.parseInt(contadorLabel.getText()));
        });


        return stackPane;
    }

    private void guardarPedido(ActionEvent actionEvent) {
        for (Map.Entry<Producto, Integer> entry : contadoresProductos.entrySet()) {
            if (entry.getValue() > 0) {
                System.out.println("Producto: " + entry.getKey().getNombre() + ", Cantidad: " + entry.getValue());
                productosGuardadados.agregarProducto(entry.getKey(), entry.getValue());
            }
        }

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }

}




/*// Inicialmente cargar los productos de la base de datos
        productos = ProductoDAO.findAll();

        agregarProductosAlGrid();

        Button guardarPedidoButton = new Button("Guardar Pedido");
        guardarPedidoButton.getStyleClass().add("buttons");
        guardarPedidoButton.setOnAction(event -> guardarPedido());
        gridPane.add(guardarPedidoButton, 0, productos.size() + 1, 3, 1);

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(guardarPedidoButton);
        gridPane.add(vbox, 0, productos.size() + 1, 3, 1);*/