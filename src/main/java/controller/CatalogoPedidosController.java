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
    @FXML
    private Button guardarPedidoButton;
    @FXML
    private ScrollPane scrollPaneCatalogo;
    @FXML
    private GridPane gridPane;
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
        // Filtrar solo productos con receta asignada
        productos = productos.stream().filter(p -> p.getReceta() != null).toList();
        agregarProductosAlGrid();
        // Limpiar estado al abrir el catálogo
        contadoresProductos.clear();
        productosGuardados.clear();
        Button guardarPedidoButton = new Button("Guardar Pedido");
        guardarPedidoButton.getStyleClass().add("buttons");
        guardarPedidoButton.setOnAction(this::guardarPedido);
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(guardarPedidoButton);
        gridPane.add(vbox, 0, productos.size() + 1, 3, 1);

        System.out.println("[DEBUG INICIO] contadoresProductos tras ingresar: ");
        for (Map.Entry<Producto, Integer> entry : contadoresProductos.entrySet()) {
            System.out.println("  - " + entry.getKey().getId() + " | " + entry.getKey().getNombre() + " | cantidad: "
                    + entry.getValue());
        }
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
            }
        }
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        vbox.getChildren().add(imageView);

        Label nombreProducto = new Label(producto.getNombre());
        nombreProducto.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        vbox.getChildren().add(nombreProducto);

        // Eliminar ComboBox si no se usa

        HBox hBox = new HBox(5);
        hBox.setAlignment(Pos.CENTER);

        Button menosButton = new Button("-");
        menosButton.getStyleClass().add("buttons");
        menosButton.setPrefWidth(24);
        menosButton.setPrefHeight(24);
        menosButton.setStyle("-fx-font-size: 14px; -fx-background-radius: 12px; -fx-background-color: #B70505; -fx-border-radius: 12px;");

        Label contadorLabel = new Label("0");
        contadorLabel.setStyle("-fx-font-size: 14px; -fx-min-width: 24px; -fx-alignment: center;");

        Button masButton = new Button("+");
        masButton.getStyleClass().add("buttons");
        masButton.setPrefWidth(24);
        masButton.setPrefHeight(24);
        masButton.setStyle("-fx-font-size: 14px; -fx-background-radius: 12px; -fx-background-color: #B70505; -fx-border-radius: 12px;");

        hBox.getChildren().addAll(menosButton, contadorLabel, masButton);
        vbox.getChildren().add(hBox);
        stackPane.getChildren().add(vbox);

        // 🔥 Cargar cantidad si ya fue seleccionada
        int cantidadActual = contadoresProductos.getOrDefault(producto, 0);
        contadorLabel.setText(String.valueOf(cantidadActual));
        menosButton.setDisable(cantidadActual == 0);

        // ➖ Acción del botón menos
        menosButton.setOnAction(event -> {
            int count = Integer.parseInt(contadorLabel.getText());
            if (count > 0) {
                count--;
                contadorLabel.setText(String.valueOf(count));
                menosButton.setDisable(count == 0);

                if (count == 0) {
                    contadoresProductos.remove(producto);
                } else {
                    contadoresProductos.put(producto, count);
                }

                // LOG
                System.out.println("[DEBUG Catalogo] contadoresProductos tras -:");
                for (Map.Entry<Producto, Integer> entry : contadoresProductos.entrySet()) {
                    System.out.println("  - " + entry.getKey().getId() + " | " + entry.getKey().getNombre() + " | cantidad: " + entry.getValue());
                }
            }
        });

        // ➕ Acción del botón más
        masButton.setOnAction(event -> {
            int count = Integer.parseInt(contadorLabel.getText());
            count++;
            contadorLabel.setText(String.valueOf(count));
            menosButton.setDisable(false);
            contadoresProductos.put(producto, count);

            System.out.println("[DEBUG Catalogo] contadoresProductos tras +:");
            for (Map.Entry<Producto, Integer> entry : contadoresProductos.entrySet()) {
                System.out.println("  - " + entry.getKey().getId() + " | " + entry.getKey().getNombre() + " | cantidad: " + entry.getValue());
            }
        });

        return stackPane;
    }


    private void guardarPedido(ActionEvent actionEvent) {
        productosGuardados.clear();

        // Elimina todos los productos con cantidad <= 0 de contadoresProductos
        contadoresProductos.entrySet().removeIf(entry -> entry.getValue() <= 0);

        // Ahora solo guarda los productos con cantidad > 0
        for (Map.Entry<Producto, Integer> entry : contadoresProductos.entrySet()) {
            productosGuardados.put(entry.getKey(), entry.getValue());
        }

        if (productosGuardados.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Sin selección", "No se seleccionó ningún producto para el pedido.");
        }

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }

    public Map<Producto, Integer> getProductosGuardados() {
        // LOG: Mostrar productos y cantidades que se van a guardar
        System.out.println("[DEBUG Catalogo] Productos guardados que se envían:");
        for (Map.Entry<Producto, Integer> entry : productosGuardados.entrySet()) {
            System.out.println("  - " + entry.getKey().getId() + " | " + entry.getKey().getNombre() + " | cantidad: "
                    + entry.getValue());
        }
        // Devolver copia directa para evitar acumulación
        return new HashMap<>(productosGuardados);
    }

    /**
     * Carga productos y cantidades en el catálogo para edición de pedido.
     */
    public void cargarProductosGuardados(Map<Producto, Integer> productos) {
        if (productos == null)
            return;

        contadoresProductos.clear(); // Limpiar cualquier selección anterior
        contadoresProductos.putAll(productos);

        // Actualizar visualmente los contadores en el grid
        for (Node node : gridPane.getChildren()) {
            if (node instanceof StackPane) {
                StackPane stack = (StackPane) node;
                for (Node child : stack.getChildren()) {
                    if (child instanceof VBox) {
                        VBox vbox = (VBox) child;
                        Producto producto = null;
                        Label nombreLabel = null;
                        Label contadorLabel = null;
                        Button menosButton = null;

                        // Obtener el nombre del producto
                        for (Node vboxChild : vbox.getChildren()) {
                            if (vboxChild instanceof Label && nombreLabel == null) {
                                nombreLabel = (Label) vboxChild;
                            }
                        }

                        // Buscar el producto asociado por nombre
                        if (nombreLabel != null) {
                            for (Producto p : productos.keySet()) {
                                if (p.getNombre().equals(nombreLabel.getText())) {
                                    producto = p;
                                    break;
                                }
                            }
                        }

                        // Encontrar el contador y el botón "-"
                        for (Node vboxChild : vbox.getChildren()) {
                            if (vboxChild instanceof HBox) {
                                for (Node hboxChild : ((HBox) vboxChild).getChildren()) {
                                    if (hboxChild instanceof Label) {
                                        contadorLabel = (Label) hboxChild;
                                    } else if (hboxChild instanceof Button btn) {
                                        if (btn.getText().equals("-")) {
                                            menosButton = btn;
                                        }
                                    }
                                }
                            }
                        }

                        // Establecer el valor del contador y habilitar botón si es necesario
                        if (producto != null && contadorLabel != null) {
                            int cantidad = contadoresProductos.getOrDefault(producto, 0);
                            contadorLabel.setText(String.valueOf(cantidad));
                            if (menosButton != null) {
                                // Cambiar: solo deshabilitar el botón menos si la cantidad es 0
                                menosButton.setDisable(cantidad == 0);
                            }
                        }
                    }
                }
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}