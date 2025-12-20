package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Categoria;
import model.Combo;
import model.Producto;
import model.Receta;
import persistence.dao.ProductoDAO;
import utilities.SceneLoader;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import utilities.Paths;
import utilities.ActionLogger;
import java.math.RoundingMode;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.scene.text.Text;

public class CrudProductosController {

    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnModificar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnVolver;
    @FXML
    private TableView<Producto> tableProductos;
    @FXML
    private TableColumn<Producto, String> colNombre;
    @FXML
    private TableColumn<Producto, String> colDescripcion;
    @FXML
    private TableColumn<Producto, String> colReceta;
    @FXML
    private TableColumn<Producto, String> colCategoria;
    @FXML
    private TableColumn<Producto, String> colUso;
    @FXML
    private TableColumn<Producto, Float> colPrecio;
    @FXML
    private TableColumn<Producto, String> colSabor;
    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<String> comboFiltro;
    @FXML
    private ScrollPane scrollPaneCombos;
    @FXML
    private ScrollPane scrollPaneProductos;
    @FXML
    private TableView<Combo> tableCombos;
    @FXML
    private TableColumn<Combo, String> colComboNombre;
    @FXML
    private TableColumn<Combo, String> colComboDescripcion;
    @FXML
    private TableColumn<Combo, String> colComboProductos;
    @FXML
    private TableColumn<Combo, String> colComboPrecio;
    @FXML
    private Button btnAjusteMasivo;

    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private ObservableList<Combo> listaCombos = FXCollections.observableArrayList();
    private ProductoDAO productoDAO;
    private List<Combo> combos = new ArrayList<>();

    @FXML
    public void initialize() {
        productoDAO = new ProductoDAO();
        listaProductos = FXCollections.observableArrayList();
        rellenarColumnas();
        rellenarColumnasCombos();

        // Listener para búsqueda en tiempo real
        txtBuscar.textProperty().addListener((_, _, newText) -> filtrarProductos(newText));

        // Obtener permisos del usuario
        List<String> permisos = model.SessionContext.getInstance().getPermisos();
        boolean puedeModificar = permisos != null && permisos.contains("Productos-modificar");
        boolean puedeEliminar = permisos != null && permisos.contains("Productos-eliminar");
        boolean puedeCrear = permisos != null && permisos.contains("Productos-crear");

        // Inicialmente deshabilitar según permisos
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);
        btnAgregar.setDisable(!puedeCrear); // Solo puede agregar si tiene permiso crear

        // Listener para habilitar los botones solo si hay selección y permiso
        tableProductos.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
            btnModificar.setDisable(!(puedeModificar && newSelection != null));
            btnEliminar.setDisable(!(puedeEliminar && newSelection != null));
        });

        // --- Filtro de productos/combos ---
        persistence.dao.ComboDAO comboDAO = new persistence.dao.ComboDAO();
        combos = comboDAO.findAll();
        comboFiltro.getItems().addAll("Productos", "Combos");
        comboFiltro.getSelectionModel().selectFirst();
        comboFiltro.setOnAction(_ -> filtrarTablaPorTipo());
        filtrarTablaPorTipo();
    }

    private void rellenarColumnas() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colUso.setCellValueFactory(new PropertyValueFactory<>("tipoUso"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colSabor.setCellValueFactory(cellData -> {
            var sabores = cellData.getValue().getSabores();
            String textoSabores = (sabores == null || sabores.isEmpty()) ? ""
                    : sabores.stream().map(Object::toString).reduce((a, b) -> a + ", " + b).orElse("");
            return new SimpleStringProperty(textoSabores);
        });

        // nombre de receta
        colReceta.setCellValueFactory(cellData -> {
            Receta receta = cellData.getValue().getReceta();
            return new SimpleStringProperty(receta != null ? receta.getNombreReceta() : "");
        });

        // ⬇️ wrap + tooltip para la descripción
        colDescripcion.setCellFactory(_ -> new TableCell<Producto, String>() {
            private final Text text = new Text();
            {
                text.wrappingWidthProperty().bind(colDescripcion.widthProperty().subtract(10));
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                    setTooltip(new Tooltip(item));
                }
            }
        });

        cargarProductos();
    }

    private void rellenarColumnasCombos() {
        colComboNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colComboDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colComboPrecio.setCellValueFactory(cellData -> {
            var precio = cellData.getValue().getPrecio();
            return new SimpleStringProperty(precio != null ? precio.toString() : "");
        });
        colComboProductos.setCellValueFactory(cellData -> {
            var productos = cellData.getValue().getProductos();
            String texto = (productos == null || productos.isEmpty()) ? ""
                    : productos.stream()
                            .map(cp -> cp.getProducto().getNombre() + " x" + cp.getCantidad())
                            .reduce((a, b) -> a + ", " + b).orElse("");
            return new SimpleStringProperty(texto);
        });

        // ⬇️ wrap + tooltip para descripción de combos también
        colComboDescripcion.setCellFactory(_ -> new TableCell<Combo, String>() {
            private final Text text = new Text();
            {
                text.wrappingWidthProperty().bind(colComboDescripcion.widthProperty().subtract(10));
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                    setTooltip(new Tooltip(item));
                }
            }
        });

        cargarCombos();
    }

    private void cargarProductos() {
        listaProductos.clear(); // Limpiar la lista antes de cargar los nuevos productos
        List<Producto> productos = productoDAO.findAll(); // Obtener productos de la base de datos
        listaProductos.addAll(productos); // Agregar los productos a la lista observable
        tableProductos.setItems(listaProductos); // Establecer la lista en la TableView
    }

    private void cargarCombos() {
        listaCombos.clear();
        listaCombos.addAll(combos);
        tableCombos.setItems(listaCombos);
    }

    @FXML
    public void handleAgregar(ActionEvent event) {
        String filtro = comboFiltro.getValue();
        if ("Combos".equals(filtro)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/combo_form.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Agregar Combo");
                stage.showAndWait();
                // Recargar combos desde la base de datos para reflejar los nuevos
                persistence.dao.ComboDAO comboDAO = new persistence.dao.ComboDAO();
                combos = comboDAO.findAll();
                cargarCombos();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/pasteleria/productos_form.fxml"));
                Parent root = loader.load();
                ProductoFormController controller = loader.getController();
                controller.setParentController(this);
                controller.setListaProductos(listaProductos);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Agregar Producto");
                stage.showAndWait(); // Esperar a que se cierre la ventana antes de recargar
                cargarProductos();
                ActionLogger.log("Producto agregado: " + (controller.getListaProductos().isEmpty() ? ""
                        : controller.getListaProductos().get(controller.getListaProductos().size() - 1).getNombre()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleModificar(ActionEvent event) {
        Producto productoSeleccionado = tableProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/pasteleria/productos_form.fxml"));
                Parent root = loader.load();

                // Crear un nuevo Stage (ventana)
                Stage stage = new Stage();
                stage.setTitle("Modificar Producto");
                stage.setScene(new Scene(root));

                ProductoFormController controller = loader.getController();
                controller.setProducto(productoSeleccionado);
                controller.setListaProductos(listaProductos);

                // Mostrar la ventana
                stage.showAndWait();

                cargarProductos(); // Recargar la lista tras modificar

                // Log de la acción
                ActionLogger.log("Producto modificado: " + productoSeleccionado.getNombre());

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "No se ha seleccionado ningún producto",
                    "Por favor, selecciona un producto para modificar.");
        }
    }

    @FXML
    void handleEliminar(ActionEvent event) {
        Producto productoSeleccionado = tableProductos.getSelectionModel().getSelectedItem();

        if (productoSeleccionado != null) {
            // Confirmación
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("Eliminar Producto");
            alert.setContentText("¿Estás seguro de que deseas eliminar: " + productoSeleccionado.getNombre() + "?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // INTENTO DE BORRADO
                    productoDAO.delete(productoSeleccionado);

                    // Si llegamos acá, se borró bien
                    listaProductos.remove(productoSeleccionado);
                    tableProductos.refresh();

                    ActionLogger.log("Producto eliminado: " + productoSeleccionado.getNombre());

                    // Aviso de éxito (opcional, pero queda bien)
                    showAlert(Alert.AlertType.INFORMATION, "Éxito", "El producto se eliminó correctamente.");

                } catch (Exception e) {
                    // ATAJADA DE PENAL (Integridad referencial)
                    // Si el producto está en un Pedido o un Combo, cae acá.

                    Alert errorAlert = new Alert(Alert.AlertType.WARNING);
                    errorAlert.setTitle("No se puede eliminar");
                    errorAlert.setHeaderText("Producto en uso");
                    errorAlert.setContentText(
                            "No podés eliminar este producto porque forma parte de un COMBO o ya tiene VENTAS registradas.\n\n"
                                    +
                                    "El sistema protege el historial de ventas.");
                    errorAlert.showAndWait();

                    System.err.println("Error al eliminar producto: " + e.getMessage());
                }
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Selección requerida",
                    "Por favor, selecciona un producto de la lista para eliminar.");
        }
    }

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.MAINMENU, "/css/mainMenu.css", false);

        // Log de la acción
        ActionLogger.log("El usuario regresó al menú principal desde la pantalla de Productos.");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // <--- ESTO ES LA CLAVE
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void filtrarProductos(String filtro) {
        if (filtro == null || filtro.isEmpty()) {
            tableProductos.setItems(listaProductos);
        } else {
            String filtroLower = filtro.toLowerCase();
            ObservableList<Producto> filtrados = listaProductos.filtered(p -> {
                // Buscar por nombre
                boolean matchNombre = p.getNombre() != null && p.getNombre().toLowerCase().contains(filtroLower);
                // Buscar por descripción
                boolean matchDescripcion = p.getDescripcion() != null
                        && p.getDescripcion().toLowerCase().contains(filtroLower);
                // Buscar por categoría
                boolean matchCategoria = p.getCategoria() != null
                        && p.getCategoria().toString().toLowerCase().contains(filtroLower);
                // Buscar por receta
                boolean matchReceta = p.getReceta() != null && p.getReceta().getNombreReceta() != null
                        && p.getReceta().getNombreReceta().toLowerCase().contains(filtroLower);
                // Buscar por sabor
                boolean matchSabor = p.getSabores() != null && p.getSabores().stream()
                        .anyMatch(s -> s != null && s.toString().toLowerCase().contains(filtroLower));
                return matchNombre || matchDescripcion || matchCategoria || matchReceta || matchSabor;
            });
            tableProductos.setItems(filtrados);
        }
    }

    private void filtrarTablaPorTipo() {
        String filtro = comboFiltro.getValue();
        if (filtro == null || filtro.equals("Productos")) {
            scrollPaneCombos.setVisible(false);
            scrollPaneCombos.setManaged(false);
            scrollPaneProductos.setVisible(true);
            scrollPaneProductos.setManaged(true);
            cargarProductos();
        } else if (filtro.equals("Combos")) {
            scrollPaneCombos.setVisible(true);
            scrollPaneCombos.setManaged(true);
            scrollPaneProductos.setVisible(false);
            scrollPaneProductos.setManaged(false);
            cargarCombos();
        }
    }

    public ObservableList<Producto> getListaProductos() {
        return listaProductos;
    }

    public void setListaProductos(ObservableList<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    @FXML
    void handleAjusteMasivo(ActionEvent event) {
        if ("Combos".equals(comboFiltro.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Atención",
                    "El ajuste masivo por ahora solo aplica a Productos individuales.");
            return;
        }

        if (listaProductos.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Lista vacía", "No hay productos para ajustar.");
            return;
        }

        // 1. Crear el Diálogo
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajuste Masivo de Precios");

        // --- CABECERA CENTRADA ---
        Label titleLabel = new Label("Ajuste de Precios");
        titleLabel.setStyle(
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; -fx-font-family: 'Inter';");
        StackPane headerPane = new StackPane(titleLabel);
        headerPane.setStyle("-fx-background-color: #B70505; -fx-padding: 15px;");
        headerPane.setAlignment(Pos.CENTER);
        headerPane.setPrefWidth(400);
        dialog.getDialogPane().setHeader(headerPane);

        // --- ESTILOS Y BOTONES ---
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/productos_form.css").toExternalForm());
        dialogPane.getStyleClass().add("my-dialog");
        dialogPane.setMinWidth(420);

        ButtonType btnAplicarType = new ButtonType("Aplicar", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(btnAplicarType, ButtonType.CANCEL);

        Button btnAplicarNode = (Button) dialogPane.lookupButton(btnAplicarType);
        btnAplicarNode.getStyleClass().add("form-producto-btn");
        Button btnCancelarNode = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        btnCancelarNode.getStyleClass().add("form-producto-btn-cancelar");

        // 2. Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 10, 20));
        grid.setStyle("-fx-background-color: #f7ede3;");

        // --- Controles ---
        ToggleGroup groupOperacion = new ToggleGroup();
        RadioButton rbAumento = new RadioButton("Aumento (Inflación)");
        rbAumento.setToggleGroup(groupOperacion);
        rbAumento.setSelected(true);
        rbAumento.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-font-family: 'Inter';");

        RadioButton rbDescuento = new RadioButton("Disminución / Oferta");
        rbDescuento.setToggleGroup(groupOperacion);
        rbDescuento.setStyle("-fx-text-fill: #B70505; -fx-font-weight: bold; -fx-font-family: 'Inter';");

        ToggleGroup groupModo = new ToggleGroup();
        RadioButton rbPorcentaje = new RadioButton("Porcentaje (%)");
        rbPorcentaje.setToggleGroup(groupModo);
        rbPorcentaje.setSelected(true);

        RadioButton rbFijo = new RadioButton("Monto Fijo ($)");
        rbFijo.setToggleGroup(groupModo);

        TextField txtValor = new TextField();
        txtValor.setPromptText("Ej: 10.5");
        txtValor.getStyleClass().add("form-producto-input");

        // --- CAMBIO IMPORTANTE AQUÍ: EL COMBOBOX ---
        ComboBox<Categoria> comboCategoriaAjuste = new ComboBox<>();
        comboCategoriaAjuste.getStyleClass().add("form-producto-combo");
        comboCategoriaAjuste.setMaxWidth(Double.MAX_VALUE); // Que ocupe todo el ancho posible

        // A. Crear opción ficticia "Todas"
        Categoria catTodas = new Categoria();
        catTodas.setId(null); // ID nulo será nuestra señal de "Todas"
        catTodas.setNombre("Todas las categorías");

        // B. Cargar desde BD
        persistence.dao.CategoriaDAO categoriaDAO = new persistence.dao.CategoriaDAO();
        List<Categoria> categoriasDB = categoriaDAO.findAll();

        // C. Llenar combo: Primero "Todas", luego las demás
        comboCategoriaAjuste.getItems().add(catTodas);
        comboCategoriaAjuste.getItems().addAll(categoriasDB);

        // D. Seleccionar "Todas" por defecto
        comboCategoriaAjuste.getSelectionModel().selectFirst();

        // Etiquetas
        Label lblCat = new Label("Categoría:");
        lblCat.getStyleClass().add("form-producto-label");
        Label lblTipo = new Label("Tipo:");
        lblTipo.getStyleClass().add("form-producto-label");
        Label lblMetodo = new Label("Método:");
        lblMetodo.getStyleClass().add("form-producto-label");
        Label lblValor = new Label("Valor:");
        lblValor.getStyleClass().add("form-producto-label");

        // Armado del Grid (SIN EL BOTÓN X)
        grid.add(lblCat, 0, 0);
        grid.add(comboCategoriaAjuste, 1, 0); // Ponemos el combo directo, sin HBox

        grid.add(lblTipo, 0, 1);
        grid.add(rbAumento, 1, 1);
        grid.add(rbDescuento, 1, 2);

        grid.add(lblMetodo, 0, 3);
        grid.add(rbPorcentaje, 1, 3);
        grid.add(rbFijo, 1, 4);

        grid.add(lblValor, 0, 5);
        grid.add(txtValor, 1, 5);

        dialogPane.setContent(grid);

        // Validación (Igual)
        btnAplicarNode.addEventFilter(ActionEvent.ACTION, ae -> {
            String input = txtValor.getText();
            if (!input.matches("^\\d*\\.?\\d+$")) {
                showAlert(Alert.AlertType.ERROR, "Valor inválido", "Por favor ingresa un número válido.");
                ae.consume();
            }
        });

        // 3. Procesar
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == btnAplicarType) {
            try {
                BigDecimal valor = new BigDecimal(txtValor.getText());
                boolean esAumento = rbAumento.isSelected();
                boolean esPorcentaje = rbPorcentaje.isSelected();
                Categoria categoriaSeleccionada = comboCategoriaAjuste.getValue();

                aplicarCambiosPrecios(valor, esAumento, esPorcentaje, categoriaSeleccionada);

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error al procesar el valor: " + e.getMessage());
            }
        }
    }

    private void aplicarCambiosPrecios(BigDecimal valor, boolean esAumento, boolean esPorcentaje,
            Categoria categoriaFiltro) {
        int contador = 0;

        for (Producto p : listaProductos) {
            // 1. Filtro de Categoría
            // MODIFICADO: Ahora verificamos si categoriaFiltro tiene ID. Si es null, es
            // "Todas".
            if (categoriaFiltro != null && categoriaFiltro.getId() != null) {
                // Si el producto no tiene categoría o su ID es distinto al seleccionado, saltar
                if (p.getCategoria() == null || !p.getCategoria().getId().equals(categoriaFiltro.getId())) {
                    continue;
                }
            }

            // ... (El resto de la lógica matemática SIGUE IGUAL) ...
            BigDecimal precioActual = p.getPrecio();
            if (precioActual == null)
                precioActual = BigDecimal.ZERO;

            BigDecimal nuevoPrecio;

            if (esPorcentaje) {
                BigDecimal factor = valor.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                if (esAumento) {
                    nuevoPrecio = precioActual.multiply(BigDecimal.ONE.add(factor));
                } else {
                    nuevoPrecio = precioActual.multiply(BigDecimal.ONE.subtract(factor));
                }
            } else {
                if (esAumento) {
                    nuevoPrecio = precioActual.add(valor);
                } else {
                    nuevoPrecio = precioActual.subtract(valor);
                }
            }

            if (nuevoPrecio.compareTo(BigDecimal.ZERO) < 0) {
                nuevoPrecio = BigDecimal.ZERO;
            }

            nuevoPrecio = nuevoPrecio.setScale(2, RoundingMode.HALF_UP);

            if (precioActual.compareTo(nuevoPrecio) != 0) {
                p.setPrecio(nuevoPrecio);
                productoDAO.update(p);
                contador++;
            }
        }

        // ... (Refresco de tabla y alertas igual) ...
        tableProductos.refresh();

        String tipo = esPorcentaje ? "%" : "$";
        String operacion = esAumento ? "Aumento" : "Descuento";

        // Mensaje personalizado según si se eligió una categoría o todas
        String catMsg = (categoriaFiltro == null || categoriaFiltro.getId() == null)
                ? "todas las categorías"
                : "categoría " + categoriaFiltro.getNombre();

        ActionLogger.log("Ajuste masivo: " + operacion + " de " + valor + tipo + " en " + catMsg);

        showAlert(Alert.AlertType.INFORMATION, "Éxito",
                "Se actualizaron " + contador + " productos.\n" +
                        "Operación: " + operacion + " del " + valor + tipo + "\n" +
                        "Alcance: " + catMsg);
    }
}
