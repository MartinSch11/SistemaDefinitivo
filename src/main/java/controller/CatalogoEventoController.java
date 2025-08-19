package controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import model.Producto;
import persistence.dao.ProductoDAO;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.*;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

public class CatalogoEventoController {

    @FXML
    private ScrollPane scrollPaneCatalogo;
    @FXML
    private Pane catalogoContentPane;
    @FXML
    private VBox vboxAcciones;
    @FXML
    private Label lblPrecio;
    @FXML
    private Button btnGuardar;

    private final String imagenProductoPorDefecto = "/productosImag/imagenProductoPorDefecto.png";

    private final ProductoDAO productoDAO = new ProductoDAO();

    private final Map<Long, Integer> contadoresPorId = new HashMap<>();

    private List<Producto> productosEventos = new ArrayList<>();

    private final int tarjetaWidth = 230;
    private final int tarjetaHeight = 280;

    @FXML
    public void initialize() {
        cargarProductosEventos();
        agregarProductosAlCatalogo();
    }

    private void cargarProductosEventos() {
        productosEventos = productoDAO.findAll();
        productosEventos.removeIf(p -> {
            String uso = p.getTipoUso();
            return uso != null && !uso.equals("Eventos") && !uso.equals("Ambos");
        });
    }

    private void agregarProductosAlCatalogo() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setAlignment(Pos.TOP_CENTER);
        gridPane.setPrefWidth(scrollPaneCatalogo.getPrefWidth());

        int column = 0, row = 0;
        for (Producto producto : productosEventos) {
            StackPane stack = crearStackPaneProducto(producto);
            gridPane.add(stack, column++, row);
            if (column == 3) {
                column = 0;
                row++;
            }
        }

        StackPane wrapper = new StackPane(gridPane);
        wrapper.setAlignment(Pos.CENTER);
        catalogoContentPane.getChildren().setAll(wrapper);
        scrollPaneCatalogo.setContent(catalogoContentPane);

        actualizarTotal();
    }

    private StackPane crearStackPaneProducto(Producto producto) {
        StackPane stack = new StackPane();
        stack.setPrefSize(tarjetaWidth, tarjetaHeight);
        stack.getStyleClass().add("stack-pane");

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.BOTTOM_CENTER);
        vbox.setPrefHeight(tarjetaHeight);

        ImageView imageView = crearImageView(producto);
        vbox.getChildren().add(imageView);

        Label nombreProducto = new Label(producto.getNombre());
        nombreProducto.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        nombreProducto.setMaxWidth(180);
        nombreProducto.setWrapText(true);
        nombreProducto.setAlignment(Pos.CENTER);
        vbox.getChildren().add(nombreProducto);

        Label precioProducto = new Label("$" + String.format("%.2f", producto.getPrecio()));
        precioProducto.setStyle("-fx-font-size: 12px; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");
        precioProducto.setAlignment(Pos.CENTER);
        precioProducto.setMaxWidth(180);
        vbox.getChildren().add(precioProducto);

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

        HBox hbox = new HBox(10, menosButton, contadorLabel, masButton);
        hbox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(hbox);
        stack.getChildren().add(vbox);

        int cantidadInicial = contadoresPorId.getOrDefault(producto.getId(), 0);
        contadorLabel.setText(String.valueOf(cantidadInicial));

        menosButton.setDisable(cantidadInicial == 0);

        menosButton.setOnAction(_ -> {
            int count = Integer.parseInt(contadorLabel.getText());
            if (count > 0) {
                count--;
                contadorLabel.setText(String.valueOf(count));
                menosButton.setDisable(count == 0);
                if (count == 0)
                    contadoresPorId.remove(producto.getId());
                else
                    contadoresPorId.put(producto.getId(), count);
                actualizarTotal();
            }
        });

        masButton.setOnAction(_ -> {
            int count = Integer.parseInt(contadorLabel.getText()) + 1;
            contadorLabel.setText(String.valueOf(count));
            menosButton.setDisable(false);
            contadoresPorId.put(producto.getId(), count);
            actualizarTotal();
        });

        return stack;
    }

    private ImageView crearImageView(Producto producto) {
        ImageView imageView = new ImageView();
        Image img = null;
        double rotation = 0;
        if (producto.getImagen() != null && producto.getImagen().length > 0) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(producto.getImagen());
                Metadata metadata = ImageMetadataReader.readMetadata(bais);
                ExifIFD0Directory dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                if (dir != null && dir.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                    switch (dir.getInt(ExifIFD0Directory.TAG_ORIENTATION)) {
                        case 6 -> rotation = 90;
                        case 3 -> rotation = 180;
                        case 8 -> rotation = 270;
                    }
                }
                bais.reset();
                img = new Image(bais);
            } catch (Exception e) {
                img = new Image(new ByteArrayInputStream(producto.getImagen()));
            }
        } else {
            InputStream defaultImg = getClass().getResourceAsStream(imagenProductoPorDefecto);
            img = new Image(defaultImg);
        }

        imageView.setImage(img);
        imageView.setRotate(rotation);
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-effect: dropshadow(gaussian, #00000022, 4, 0, 0, 2);");
        return imageView;
    }

    private void actualizarTotal() {
        double total = productosEventos.stream()
                .mapToDouble(p -> p.getPrecio().doubleValue() *
                        contadoresPorId.getOrDefault(p.getId(), 0))
                .sum();
        lblPrecio.setText("Total: $" + String.format("%.2f", total));
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        if (contadoresPorId.values().stream().allMatch(v -> v == 0)) {
            showAlert(Alert.AlertType.WARNING, "Sin selección", "No se seleccionó ningún producto para el evento.");
            return;
        }
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    public Map<Producto, Integer> getProductosGuardados() {
        Map<Producto, Integer> res = new HashMap<>();
        for (Producto p : productosEventos) {
            int cant = contadoresPorId.getOrDefault(p.getId(), 0);
            if (cant > 0)
                res.put(p, cant);
        }
        return res;
    }

    private void showAlert(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    public void precargarSeleccionDesdeEvento(Map<Long, Integer> cantidadesPorProductoId) {
        contadoresPorId.clear();
        if (cantidadesPorProductoId != null) {
            contadoresPorId.putAll(cantidadesPorProductoId);
        }
        agregarProductosAlCatalogo(); // repinta con los contadores cargados
        actualizarTotal();
    }
}
