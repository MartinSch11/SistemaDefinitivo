package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import model.Combo;
import model.Producto;
import persistence.dao.ProductoDAO;
import persistence.dao.ComboDAO;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.*;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.Directory;
import com.drew.metadata.exif.ExifIFD0Directory;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class CatalogoPedidosController {
    @FXML private Button guardarPedidoButton;
    @FXML private ScrollPane scrollPaneCatalogo;
    @FXML private GridPane gridPane;
    @FXML private ComboBox<String> comboFiltro;
    @FXML private Pane catalogoContentPane;
    
    private final String imagenProductoPorDefecto = "/productosImag/imagenProductoPorDefecto.png";
    private Map<Producto, Integer> contadoresProductos = new HashMap<>();
    private List<Producto> productos = new ArrayList<>(); // Lista de productos a mostrar
    private ProductoDAO ProductoDAO = new ProductoDAO();

    private Map<Producto, Integer> productosGuardados = new HashMap<>(); // Mapa para productos guardados

    private NuevoPedidoController nuevoPedidoController;

    private List<Combo> combos = new ArrayList<>(); // Lista de combos a mostrar
    private Map<Combo, Integer> contadoresCombos = new HashMap<>();
    private Map<Combo, Integer> combosGuardados = new HashMap<>();

    public void setDialogNuevoPedidoController(NuevoPedidoController controller) {
        if (controller != null) {
            this.nuevoPedidoController = controller;
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "El controlador NuevoPedidoController no está disponible.");
        }
    }

    public void initialize() {
        // Inicialmente cargar los productos y combos de la base de datos
        productos = ProductoDAO.findAll();
        // Filtrar productos que tengan receta distinta de null
        productos.removeIf(p -> p.getReceta() == null);
        ComboDAO comboDAO = new ComboDAO();
        combos = comboDAO.findAll();
        // Configurar ComboBox de filtro
        comboFiltro.getItems().addAll("Todos", "Productos", "Combos");
        comboFiltro.getSelectionModel().selectFirst(); // 'Todos' por defecto
        comboFiltro.setOnAction(e -> filtrarCatalogo());
        filtrarCatalogo();
        // Limpiar estado al abrir el catálogo
        contadoresProductos.clear();
        productosGuardados.clear();
    }

    private void filtrarCatalogo() {
        String filtro = comboFiltro.getValue();
        if (filtro == null || filtro.equals("Todos") || filtro.equals("Productos") || filtro.equals("Combos")) {
            agregarProductosAlGrid();
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

    private int tarjetaWidth = 230; // Ancho fijo
    private int tarjetaHeight = 280; // Alto fijo, suficientemente grande para combos largos

    private void agregarProductosAlGrid() {
        gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setAlignment(Pos.TOP_CENTER);
        gridPane.setPrefWidth(scrollPaneCatalogo.getPrefWidth());
        gridPane.setPrefHeight(scrollPaneCatalogo.getPrefHeight());

        int column = 0;
        int row = 0;
        String filtro = comboFiltro.getValue();
        boolean mostrarProductos = filtro == null || filtro.equals("Todos") || filtro.equals("Productos");
        boolean mostrarCombos = filtro == null || filtro.equals("Todos") || filtro.equals("Combos");

        if (mostrarProductos && (!filtro.equals("Combos"))) {
            for (Producto producto : productos) {
                StackPane stackPane = crearStackPaneProducto(producto);
                stackPane.setPrefWidth(tarjetaWidth);
                stackPane.setPrefHeight(tarjetaHeight);
                stackPane.setMinWidth(tarjetaWidth);
                stackPane.setMinHeight(tarjetaHeight);
                gridPane.add(stackPane, column++, row);
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }
        }
        if (mostrarCombos && (!filtro.equals("Productos"))) {
            for (Combo combo : combos) {
                StackPane stackPane = crearStackPaneCombo(combo);
                stackPane.setPrefWidth(tarjetaWidth);
                stackPane.setPrefHeight(tarjetaHeight);
                stackPane.setMinWidth(tarjetaWidth);
                stackPane.setMinHeight(tarjetaHeight);
                gridPane.add(stackPane, column++, row);
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }
        }
        // --- Agregar el botón Guardar Pedido al final, centrado ---
        Button guardarPedidoButton = new Button("Guardar Pedido");
        guardarPedidoButton.getStyleClass().add("buttons");
        guardarPedidoButton.setOnAction(this::guardarPedido);
        VBox vbox = new VBox(guardarPedidoButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.setPadding(new javafx.geometry.Insets(0, 0, 10, 0)); // Padding abajo de 10px
        int totalRows = row + (column > 0 ? 1 : 0);
        gridPane.add(vbox, 0, totalRows, 3, 1); // Ocupa las 3 columnas

        VBox wrapper = new VBox(gridPane);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPrefWidth(scrollPaneCatalogo.getPrefWidth());
        wrapper.setPrefHeight(scrollPaneCatalogo.getPrefHeight());
        catalogoContentPane.getChildren().setAll(wrapper);
        scrollPaneCatalogo.setContent(catalogoContentPane);
    }

    private StackPane crearStackPaneProducto(Producto producto) {
        StackPane stackPane = new StackPane();
        stackPane.setPrefWidth(tarjetaWidth); // Más angosto
        stackPane.setPrefHeight(tarjetaHeight);
        stackPane.getStyleClass().add("stack-pane");

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        ImageView imageView = new ImageView();
        Image img = null;
        double rotation = 0;
        if (producto.getImagen() != null && producto.getImagen().length > 0) {
            // Detectar orientación EXIF
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(producto.getImagen());
                Metadata metadata = ImageMetadataReader.readMetadata(bais);
                ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                    int orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                    switch (orientation) {
                        case 6: rotation = 90; break;
                        case 3: rotation = 180; break;
                        case 8: rotation = 270; break;
                        default: rotation = 0;
                    }
                }
                bais.reset();
                img = new Image(bais);
            } catch (Exception ex) {
                img = new Image(new ByteArrayInputStream(producto.getImagen()));
            }
            imageView.setImage(img);
            imageView.setRotate(rotation);
            // --- CROP CUADRADO centrado para cualquier orientación ---
            double imgWidth = img.getWidth();
            double imgHeight = img.getHeight();
            double side = Math.min(imgWidth, imgHeight);
            double x = (imgWidth - side) / 2;
            double y = (imgHeight - side) / 2;
            imageView.setViewport(new javafx.geometry.Rectangle2D(x, y, side, side));
            imageView.setFitWidth(150);
            imageView.setFitHeight(150);
        } else {
            InputStream defaultImage = getClass().getResourceAsStream(imagenProductoPorDefecto);
            if (defaultImage != null) {
                img = new Image(defaultImage);
                imageView.setImage(img);
            }
            imageView.setFitWidth(120);
            imageView.setFitHeight(100);
        }
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setStyle("-fx-alignment: center; -fx-effect: dropshadow(gaussian, #00000022, 4, 0, 0, 2);");
        VBox.setMargin(imageView, new javafx.geometry.Insets(5,0,5,0));
        vbox.getChildren().add(imageView);

        Label nombreProducto = new Label(producto.getNombre());
        nombreProducto.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        nombreProducto.setMaxWidth(180);
        nombreProducto.setWrapText(true);
        nombreProducto.setAlignment(Pos.CENTER);
        vbox.getChildren().add(nombreProducto);

        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER);

        Button menosButton = new Button("-");
        menosButton.getStyleClass().add("buttons");
        menosButton.setPrefWidth(24);
        menosButton.setPrefHeight(24);
        menosButton.setStyle(
                "-fx-font-size: 14px; -fx-background-radius: 12px; -fx-background-color: #B70505; -fx-border-radius: 12px;");

        Label contadorLabel = new Label("0");
        contadorLabel.setStyle("-fx-font-size: 14px; -fx-min-width: 24px; -fx-alignment: center;");

        Button masButton = new Button("+");
        masButton.getStyleClass().add("buttons");
        masButton.setPrefWidth(24);
        masButton.setPrefHeight(24);
        masButton.setStyle(
                "-fx-font-size: 14px; -fx-background-radius: 12px; -fx-background-color: #B70505; -fx-border-radius: 12px;");

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
                    System.out.println("  - " + entry.getKey().getId() + " | " + entry.getKey().getNombre()
                            + " | cantidad: " + entry.getValue());
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
                System.out.println("  - " + entry.getKey().getId() + " | " + entry.getKey().getNombre()
                        + " | cantidad: " + entry.getValue());
            }
        });

        return stackPane;
    }

    private StackPane crearStackPaneCombo(Combo combo) {
        StackPane stackPane = new StackPane();
        stackPane.setPrefWidth(tarjetaWidth);
        stackPane.setPrefHeight(tarjetaHeight);
        stackPane.getStyleClass().add("stack-pane");

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        ImageView imageView = new ImageView();
        Image img = null;
        if (combo.getImagen() != null && combo.getImagen().length > 0) {
            img = new Image(new ByteArrayInputStream(combo.getImagen()));
            imageView.setImage(img);
            double imgWidth = img.getWidth();
            double imgHeight = img.getHeight();
            double side = Math.min(imgWidth, imgHeight);
            double x = (imgWidth - side) / 2;
            double y = (imgHeight - side) / 2;
            imageView.setViewport(new javafx.geometry.Rectangle2D(x, y, side, side));
            imageView.setFitWidth(150);
            imageView.setFitHeight(150);
        } else {
            InputStream defaultImage = getClass().getResourceAsStream(imagenProductoPorDefecto);
            if (defaultImage != null) {
                img = new Image(defaultImage);
                imageView.setImage(img);
            }
            imageView.setFitWidth(120);
            imageView.setFitHeight(100);
        }
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setStyle("-fx-alignment: center; -fx-effect: dropshadow(gaussian, #00000022, 4, 0, 0, 2);");
        VBox.setMargin(imageView, new javafx.geometry.Insets(5,0,5,0));
        vbox.getChildren().add(imageView);

        Label nombreCombo = new Label(combo.getNombre());
        nombreCombo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        nombreCombo.setMaxWidth(200); // Más ancho para evitar corte
        nombreCombo.setWrapText(true);
        nombreCombo.setAlignment(Pos.CENTER);
        vbox.getChildren().add(nombreCombo);

        // Mostrar productos incluidos en el combo
        StringBuilder productosIncluidos = new StringBuilder("Incluye: ");
        combo.getProductos().forEach(cp -> productosIncluidos.append(cp.getProducto().getNombre()).append(" x").append(cp.getCantidad()).append(", "));
        if (productosIncluidos.length() > 9) productosIncluidos.setLength(productosIncluidos.length() - 2);
        Label productosLabel = new Label(productosIncluidos.toString());
        productosLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        productosLabel.setMaxWidth(200); // Más ancho
        productosLabel.setWrapText(true);
        productosLabel.setAlignment(Pos.CENTER);
        vbox.getChildren().add(productosLabel);

        // --- Contador visual para combos (igual que productos) ---
        HBox hBox = new HBox(10);
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

        // --- Lógica de contador para combos ---
        int cantidadActual = contadoresCombos.getOrDefault(combo, 0);
        contadorLabel.setText(String.valueOf(cantidadActual));
        menosButton.setDisable(cantidadActual == 0);

        menosButton.setOnAction(event -> {
            int count = Integer.parseInt(contadorLabel.getText());
            if (count > 0) {
                count--;
                contadorLabel.setText(String.valueOf(count));
                menosButton.setDisable(count == 0);
                if (count == 0) {
                    contadoresCombos.remove(combo);
                } else {
                    contadoresCombos.put(combo, count);
                }
                // LOG
                System.out.println("[DEBUG Catalogo] contadoresCombos tras -:");
                for (Map.Entry<Combo, Integer> entry : contadoresCombos.entrySet()) {
                    System.out.println("  - " + entry.getKey().getId() + " | " + entry.getKey().getNombre()
                            + " | cantidad: " + entry.getValue());
                }
            }
        });
        masButton.setOnAction(event -> {
            int count = Integer.parseInt(contadorLabel.getText());
            count++;
            contadorLabel.setText(String.valueOf(count));
            menosButton.setDisable(false);
            contadoresCombos.put(combo, count);
            System.out.println("[DEBUG Catalogo] contadoresCombos tras +:");
            for (Map.Entry<Combo, Integer> entry : contadoresCombos.entrySet()) {
                System.out.println("  - " + entry.getKey().getId() + " | " + entry.getKey().getNombre()
                        + " | cantidad: " + entry.getValue());
            }
        });
        return stackPane;
    }

    private void guardarPedido(ActionEvent actionEvent) {
        productosGuardados.clear();
        combosGuardados.clear();
        // Elimina todos los productos con cantidad <= 0 de contadoresProductos
        contadoresProductos.entrySet().removeIf(entry -> entry.getValue() <= 0);
        contadoresCombos.entrySet().removeIf(entry -> entry.getValue() <= 0);
        // Ahora solo guarda los productos con cantidad > 0
        for (Map.Entry<Producto, Integer> entry : contadoresProductos.entrySet()) {
            productosGuardados.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Combo, Integer> entry : contadoresCombos.entrySet()) {
            combosGuardados.put(entry.getKey(), entry.getValue());
        }
        if (productosGuardados.isEmpty() && combosGuardados.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Sin selección", "No se seleccionó ningún producto ni combo para el pedido.");
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
        return new HashMap<>(productosGuardados);
    }

    public Map<Combo, Integer> getCombosGuardados() {
        System.out.println("[DEBUG Catalogo] Combos guardados que se envían:");
        for (Map.Entry<Combo, Integer> entry : combosGuardados.entrySet()) {
            System.out.println("  - " + entry.getKey().getId() + " | " + entry.getKey().getNombre() + " | cantidad: "
                    + entry.getValue());
        }
        return new HashMap<>(combosGuardados);
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

    /**
     * Carga combos y cantidades en el catálogo para edición de pedido.
     */
    public void cargarCombosGuardados(Map<Combo, Integer> combos) {
        if (combos == null)
            return;

        contadoresCombos.clear(); // Limpiar cualquier selección anterior
        contadoresCombos.putAll(combos);

        // Actualizar visualmente los contadores en el grid
        for (Node node : gridPane.getChildren()) {
            if (node instanceof StackPane) {
                StackPane stack = (StackPane) node;
                for (Node child : stack.getChildren()) {
                    if (child instanceof VBox) {
                        VBox vbox = (VBox) child;
                        Combo combo = null;
                        Label nombreLabel = null;
                        Label contadorLabel = null;
                        Button menosButton = null;

                        // Obtener el nombre del combo
                        for (Node vboxChild : vbox.getChildren()) {
                            if (vboxChild instanceof Label && nombreLabel == null) {
                                nombreLabel = (Label) vboxChild;
                            }
                        }

                        // Buscar el combo asociado por nombre
                        if (nombreLabel != null) {
                            for (Combo c : combos.keySet()) {
                                if (c.getNombre().equals(nombreLabel.getText())) {
                                    combo = c;
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
                        if (combo != null && contadorLabel != null) {
                            int cantidad = contadoresCombos.getOrDefault(combo, 0);
                            contadorLabel.setText(String.valueOf(cantidad));
                            if (menosButton != null) {
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