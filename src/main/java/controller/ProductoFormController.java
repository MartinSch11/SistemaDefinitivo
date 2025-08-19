package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Categoria;
import model.Producto;
import model.Receta;
import model.Sabor;
import persistence.dao.CategoriaDAO;
import persistence.dao.ProductoDAO;
import persistence.dao.RecetaDAO;
import utilities.ActionLogger;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

public class ProductoFormController {

    @FXML
    private TextField nombreProductoField;
    @FXML
    private TextArea descripcionProductoField;
    @FXML
    private ChoiceBox<Categoria> categoriaChoiceBox;
    @FXML
    private ComboBox<Receta> cmbReceta;
    @FXML
    private TextField precioField;
    @FXML
    private ImageView imagenProductoView;
    @FXML
    private ComboBox<String> cmbTipoUso;

    private ObservableList<Producto> listaProductos;
    private ObservableList<Sabor> saboresSeleccionados = FXCollections.observableArrayList();
    private Producto productoActual;
    private CrudProductosController parentController;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final RecetaDAO recetaDAO = new RecetaDAO();

    private byte[] imagen; // variable para almacenar la imagen por el usuario

    @FXML
    public void initialize() {
        cargarCategorias();
        cargarRecetas();
        cargarOpcionesTipoUso(); // <-- nuevo
        if (productoActual != null) {
            cargarDatosProducto(productoActual);
        }
    }

    private void cargarOpcionesTipoUso() {
        cmbTipoUso.setItems(FXCollections.observableArrayList("Pedidos", "Eventos", "Ambos"));
        cmbTipoUso.getSelectionModel().select("Ambos"); // valor por defecto
    }

    private void cargarCategorias() {
        List<Categoria> categorias = categoriaDAO.findAll();
        categoriaChoiceBox.setItems(FXCollections.observableArrayList(categorias));
        categoriaChoiceBox.getSelectionModel().selectFirst();
    }

    private void cargarRecetas() {
        List<Receta> recetas = recetaDAO.findAll();
        ObservableList<Receta> recetasList = FXCollections.observableArrayList(recetas);

        // Establecer las recetas en el ComboBox
        cmbReceta.setItems(recetasList);
    }

    public void setSaboresSeleccionados(ObservableList<Sabor> saboresSeleccionados) {
        this.saboresSeleccionados.setAll(saboresSeleccionados);
        // Si se está editando un producto, actualizar sus sabores
        if (productoActual != null) {
            productoActual.setSabores(new ArrayList<>(saboresSeleccionados));
        }
    }

    public void setProducto(Producto producto) {
        this.productoActual = producto;
        if (producto != null) {
            cargarDatosProducto(producto);
        }
    }

    private void cargarDatosProducto(Producto producto) {
        nombreProductoField.setText(producto.getNombre());
        descripcionProductoField.setText(producto.getDescripcion());
        categoriaChoiceBox.setValue(producto.getCategoria());
        precioField.setText(producto.getPrecio().toString());
        saboresSeleccionados.setAll(producto.getSabores());
        cmbReceta.setValue(producto.getReceta());
        // Mostrar imagen si existe
        if (producto.getImagen() != null && producto.getImagen().length > 0) {
            javafx.scene.image.Image img = new javafx.scene.image.Image(
                    new java.io.ByteArrayInputStream(producto.getImagen()));
            imagenProductoView.setImage(img);
            imagenProductoView.setRotate(0); // No rotar, ya está bien
            // --- CROP CUADRADO centrado para cualquier orientación ---
            double imgWidth = img.getWidth();
            double imgHeight = img.getHeight();
            double side = Math.min(imgWidth, imgHeight);
            double x = (imgWidth - side) / 2;
            double y = (imgHeight - side) / 2;
            imagenProductoView.setViewport(new javafx.geometry.Rectangle2D(x, y, side, side));
            imagenProductoView.setFitWidth(100);
            imagenProductoView.setFitHeight(100);
            imagen = producto.getImagen(); // Para conservar la imagen si no se carga una nueva
        } else {
            imagenProductoView.setImage(null);
        }
        // Cargar tipo de uso
        if (producto.getTipoUso() != null) {
            cmbTipoUso.setValue(producto.getTipoUso());
        } else {
            cmbTipoUso.getSelectionModel().clearSelection(); // sin selección = "Ambos"
        }
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        try {

            Producto producto = crearOActualizarProducto();

            if (producto == null) {
                // Si hubo error de validación, no continuar ni cerrar
                return;
            }

            if (productoActual != null) {
                // Modificar producto existente
                actualizarProductoExistente(productoActual);
                // Registro de la acción
                ActionLogger.log("Producto modificado: " + producto.getNombre() + " (ID: " + producto.getId() + ")");
            } else {
                // Agregar nuevo producto
                agregarNuevoProducto(producto);
                // Registro de la acción
                ActionLogger.log("Nuevo producto creado: " + producto.getNombre());
            }

            cerrarVentana(event);

        } catch (NumberFormatException e) {
            mostrarMensaje(Alert.AlertType.ERROR, "Error de Validación",
                    "El precio debe ser un valor numérico válido.");
            // Registro de la acción
            ActionLogger.log("Error al guardar producto: El precio no es válido.");
        } catch (Exception e) {
            // Registro de la acción
            ActionLogger.log("Error al guardar producto: " + e.getMessage());
        }
    }

    private void actualizarProductoExistente(Producto producto) {
        productoDAO.update(producto);
    }

    private void agregarNuevoProducto(Producto producto) {
        producto.setImagen(imagen);
        productoDAO.save(producto);
    }

    private Producto crearOActualizarProducto() {
        String nombre = nombreProductoField.getText();
        String descripcion = descripcionProductoField.getText();
        Categoria categoria = categoriaChoiceBox.getValue();
        BigDecimal precio = new BigDecimal(precioField.getText());
        Receta receta = cmbReceta.getValue();
        String tipoUso = cmbTipoUso.getValue(); // puede ser null

        try {
            if (!validarCampos(nombre, descripcion, categoria, precio)) {
                throw new IllegalArgumentException("Todos los campos deben estar completos y ser válidos.");
            }

            if (saboresSeleccionados == null || saboresSeleccionados.isEmpty()) {
                mostrarMensaje(Alert.AlertType.ERROR, "Error de Validación", "Debe seleccionar al menos un sabor.");
                return null;
            }

            if (productoActual == null) {
                productoActual = new Producto(nombre, descripcion, categoria, precio, imagen);
            } else {
                productoActual.setNombre(nombre);
                productoActual.setDescripcion(descripcion);
                productoActual.setCategoria(categoria);
                productoActual.setPrecio(precio);
                productoActual.setImagen(imagen);
            }

            productoActual.setSabores(saboresSeleccionados);
            productoActual.setReceta(receta);
            productoActual.setTipoUso(tipoUso == null ? "Ambos" : tipoUso); // <- Aquí se guarda el uso

            return productoActual;
        } catch (Exception e) {
            mostrarMensaje(Alert.AlertType.ERROR, "Error", "Error al crear/actualizar el producto: " + e.getMessage());
            return null;
        }
    }

    private boolean validarCampos(String nombre, String descripcion, Categoria categoria, BigDecimal precio) {
        return nombre != null && !nombre.isEmpty() &&
                descripcion != null && !descripcion.isEmpty() &&
                categoria != null &&
                precio != null && precio.compareTo(BigDecimal.ZERO) > 0;
    }

    @FXML
    private void handleCargarImagen(ActionEvent event) {
        File archivo = seleccionarArchivo();
        if (archivo != null) {
            cargarImagen(archivo);
            // Registro de la acción
            ActionLogger.log("Imagen cargada para el producto: " + nombreProductoField.getText());
        } else {
            // Registro de la acción en caso de que no se seleccione ninguna imagen
            ActionLogger.log("Intento fallido de cargar imagen para el producto: " + nombreProductoField.getText());
        }
    }

    private File seleccionarArchivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Producto");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Archivos de Imagen (*.png, *.jpg)", "*.png", "*.jpg"));
        return fileChooser.showOpenDialog(nombreProductoField.getScene().getWindow());
    }

    private void cargarImagen(File archivo) {
        try {
            BufferedImage original = ImageIO.read(archivo);
            if (original == null)
                throw new IOException("Formato de imagen no soportado");

            // Leer orientación EXIF
            int orientation = 1;
            try {
                Metadata metadata = ImageMetadataReader.readMetadata(archivo);
                ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                    orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                }
            } catch (Exception ex) {
                // Si falla la lectura EXIF, continuar sin rotar
            }

            // Rotar imagen si es necesario
            BufferedImage rotated = original;
            if (orientation != 1) {
                AffineTransform tx = new AffineTransform();
                switch (orientation) {
                    case 6: // 90°
                        tx.translate(original.getHeight(), 0);
                        tx.rotate(Math.toRadians(90));
                        rotated = new BufferedImage(original.getHeight(), original.getWidth(),
                                BufferedImage.TYPE_INT_RGB);
                        break;
                    case 3: // 180°
                        tx.translate(original.getWidth(), original.getHeight());
                        tx.rotate(Math.toRadians(180));
                        rotated = new BufferedImage(original.getWidth(), original.getHeight(),
                                BufferedImage.TYPE_INT_RGB);
                        break;
                    case 8: // 270°
                        tx.translate(0, original.getWidth());
                        tx.rotate(Math.toRadians(270));
                        rotated = new BufferedImage(original.getHeight(), original.getWidth(),
                                BufferedImage.TYPE_INT_RGB);
                        break;
                    default:
                        break;
                }
                if (orientation == 6 || orientation == 3 || orientation == 8) {
                    Graphics2D g2d = rotated.createGraphics();
                    g2d.drawImage(original, tx, null);
                    g2d.dispose();
                }
            }

            // Redimensionar si es necesario
            int maxDim = 400;
            int width = rotated.getWidth();
            int height = rotated.getHeight();
            if (width > maxDim || height > maxDim) {
                float scale = Math.min((float) maxDim / width, (float) maxDim / height);
                width = Math.round(width * scale);
                height = Math.round(height * scale);
                Image tmp = rotated.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = resized.createGraphics();
                g2d.drawImage(tmp, 0, 0, null);
                g2d.dispose();
                rotated = resized;
            }

            // Comprimir a JPEG con calidad 0.85
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.85f); // Calidad alta
            writer.write(null, new javax.imageio.IIOImage(rotated, null, null), param);
            writer.dispose();
            ios.close();

            imagen = baos.toByteArray();
            javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(imagen));
            imagenProductoView.setImage(img);
            imagenProductoView.setRotate(0);
            // --- CROP CUADRADO centrado para cualquier orientación ---
            double imgWidth = img.getWidth();
            double imgHeight = img.getHeight();
            double side = Math.min(imgWidth, imgHeight);
            double x = (imgWidth - side) / 2;
            double y = (imgHeight - side) / 2;
            imagenProductoView.setViewport(new javafx.geometry.Rectangle2D(x, y, side, side));
            imagenProductoView.setFitWidth(100);
            imagenProductoView.setFitHeight(100);
        } catch (IOException e) {
            mostrarMensaje(Alert.AlertType.ERROR, "Error al cargar la imagen", e.getMessage());
        }
    }

    @FXML
    private void handleSeleccionarSabores(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/sabores.fxml"));
            AnchorPane root = loader.load();
            SaboresController saboresController = loader.getController();
            saboresController.setParentController(this);
            saboresController.setSaboresSeleccionados(new ArrayList<>(saboresSeleccionados));

            Stage stage = new Stage();
            stage.setTitle("Seleccionar Sabores");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initOwner(nombreProductoField.getScene().getWindow());
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.showAndWait();

            // Al cerrar la ventana, actualiza los sabores seleccionados
            List<Sabor> seleccionados = saboresController.getSaboresSeleccionados();
            if (seleccionados != null && !seleccionados.isEmpty()) {
                this.saboresSeleccionados.setAll(seleccionados);
                ActionLogger.log("Sabores seleccionados para el producto: " + seleccionados);
                System.out.println("DEBUG: Sabores seleccionados: " + seleccionados);
            } else {
                this.saboresSeleccionados.clear();
                ActionLogger.log("No se seleccionaron sabores para el producto.");
                System.out.println("DEBUG: No se seleccionaron sabores.");
            }
            // Forzar refresco visual del ChoiceBox si tienes uno para sabores
            // Si usas un campo visual para mostrar los sabores, actualízalo aquí
            // Ejemplo:
            // saboresChoiceBox.setItems(FXCollections.observableArrayList(this.saboresSeleccionados));
        } catch (IOException e) {
            mostrarMensaje(Alert.AlertType.ERROR, "Error", "No se pudo abrir la ventana de selección de sabores.");
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        cerrarVentana(event);
    }

    private void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void mostrarMensaje(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    public void agregarProducto(Producto nuevoProducto) {
        if (parentController != null) {
            parentController.getListaProductos().add(nuevoProducto);
        }
    }

    public void modificarProducto(Producto productoModificado) {
        if (parentController != null && !parentController.getListaProductos().contains(productoModificado)) {
            int index = parentController.getListaProductos().indexOf(productoModificado);
            if (index >= 0) {
                parentController.getListaProductos().set(index, productoModificado);
            }
        }
    }

    public CrudProductosController getParentController() {
        return parentController;
    }

    public void setParentController(CrudProductosController parentController) {
        this.parentController = parentController;
    }

    public ObservableList<Producto> getListaProductos() {
        return listaProductos;
    }

    public void setListaProductos(ObservableList<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    @FXML
    private void handleNuevaCategoria() {
        abrirVentanaModal("/com/example/pasteleria/Categoria.fxml", "Nueva Categoría");
        cargarCategorias(); // Refresca el ChoiceBox después de cerrar la ventana
    }

    @FXML
    private void handleNuevaReceta() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/NuevaReceta.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Nueva Receta");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            cargarRecetas();

        } catch (Exception e) {
            mostrarMensaje(Alert.AlertType.ERROR, "Error", "No se pudo abrir el formulario de receta.");
        }
    }

    private void abrirVentanaModal(String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(loader.load())); // Usar loader.load() directamente
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
