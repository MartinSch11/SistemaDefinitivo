package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.CatalogoInsumo;
import model.Insumo;
import model.InsumoReceta;
import model.Receta;
import persistence.dao.CatalogoInsumoDAO;
import persistence.dao.InsumoDAO;
import persistence.dao.RecetaDAO;
import utilities.ActionLogger;

import java.io.IOException;

public class NuevaRecetaController {

    @FXML private TextField txtNomReceta;
    @FXML private ComboBox<CatalogoInsumo> cmbIngredientes;
    @FXML private TextField txtCantIngrediente;
    @FXML private ComboBox<String> cmbUnidad;
    @FXML private Button btnAgregar;
    @FXML private Button btnEditar;
    @FXML private TableView<InsumoReceta> tableIngredientes;
    @FXML private TableColumn<InsumoReceta, String> colIngrediente;
    @FXML private TableColumn<InsumoReceta, String> colCantidad;
    @FXML private Button btnEliminar;
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;
    @FXML private GridPane gridAcciones; // Grid de Agregar/Editar/Eliminar
    @FXML private GridPane gridEdicion;  // Grid de Guardar cambios/Cancelar edición
    @FXML private Button btnGuardarCambios;
    @FXML private Button btnCancelarEdicion;
    @FXML private Label lblTitulo;

    private ObservableList<InsumoReceta> listaInsumosReceta = FXCollections.observableArrayList();
    private RecetaDAO recetaDAO = new RecetaDAO();
    private Receta recetaModificada;

    @FXML
    public void initialize() {
        colIngrediente
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInsumo().getNombre()));
        colCantidad.setCellValueFactory(cellData -> {
            InsumoReceta insumoReceta = cellData.getValue();
            double cantidad = insumoReceta.getCantidadUtilizada();
            int cantidadInt = (int) cantidad;
            String cantidadStr = (cantidad == cantidadInt) ? String.valueOf(cantidadInt) : String.valueOf(cantidad);
            return new SimpleStringProperty(cantidadStr + " " + insumoReceta.getUnidad());
        });

        cmbIngredientes.setItems(FXCollections.observableArrayList(new CatalogoInsumoDAO().findAll()));
        cmbUnidad.setItems(FXCollections.observableArrayList("GR", "KG", "ML", "L", "UNIDAD", "UNIDADES"));

        // Mostrar solo el nombre en el ComboBox de ingredientes
        cmbIngredientes.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(CatalogoInsumo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });
        cmbIngredientes.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(CatalogoInsumo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        // Listener para filtrar unidades según el estado del insumo seleccionado
        cmbIngredientes.valueProperty().addListener((obs, oldCatalogo, newCatalogo) -> {
            filtrarUnidadesPorEstado(newCatalogo);
        });

        tableIngredientes.setItems(listaInsumosReceta);
        btnEliminar.setDisable(true);

        tableIngredientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnEliminar.setDisable(newSelection == null);
            btnEditar.setDisable(newSelection == null);
        });

        // Filtro para que txtCantIngrediente solo acepte números enteros
        txtCantIngrediente.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        }));

        gridEdicion.setVisible(false);
        gridAcciones.setVisible(true);
    }

    /**
     * Filtra las unidades de medida disponibles según el estado del insumo
     * seleccionado.
     * LÍQUIDO: ML, L
     * SÓLIDO: GR, KG
     * UNIDAD: UNIDAD, UNIDADES
     */
    private void filtrarUnidadesPorEstado(CatalogoInsumo catalogoInsumo) {
        cmbUnidad.getItems().clear();
        String estado = (catalogoInsumo != null) ? catalogoInsumo.getEstado() : null;
        if (estado == null) {
            cmbUnidad.getItems().addAll("GR", "KG", "ML", "L", "UNIDAD", "UNIDADES");
            return;
        }
        switch (estado) {
            case "LÍQUIDO":
                cmbUnidad.getItems().addAll("ML", "L");
                break;
            case "SÓLIDO":
                cmbUnidad.getItems().addAll("GR", "KG");
                break;
            case "UNIDAD":
                cmbUnidad.getItems().addAll("UNIDAD", "UNIDADES");
                break;
            default:
                cmbUnidad.getItems().addAll("GR", "KG", "ML", "L", "UNIDAD", "UNIDADES");
        }
        if (!cmbUnidad.getItems().isEmpty()) {
            cmbUnidad.setValue(cmbUnidad.getItems().get(0));
        }
    }

    @FXML
    private void handleAgregar(ActionEvent event) {
        // Validación de campos obligatorios
        if (cmbIngredientes.getValue() == null || txtCantIngrediente.getText().isEmpty()
                || cmbUnidad.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Debe completar todos los campos para agregar un ingrediente.");
            return;
        }

        try {
            int cantidad = Integer.parseInt(txtCantIngrediente.getText());
            CatalogoInsumo catalogoSeleccionado = cmbIngredientes.getValue();
            String unidadSeleccionada = cmbUnidad.getValue();

            // Verificar duplicados
            boolean ingredienteYaExiste = listaInsumosReceta.stream()
                    .anyMatch(insumoReceta -> insumoReceta.getInsumo().getCatalogoInsumo().equals(catalogoSeleccionado));

            if (ingredienteYaExiste) {
                showAlert(Alert.AlertType.ERROR, "Error", "Este ingrediente ya está en la receta.");
                return;
            }

            // Buscar el Insumo persistente asociado al CatalogoInsumo seleccionado
            InsumoDAO insumoDAO = new InsumoDAO();
            Insumo insumoPersistente = insumoDAO.findByCatalogoInsumoId(catalogoSeleccionado.getId());
            if (insumoPersistente == null) {
                // Crear insumo nuevo con cantidad 0 y datos mínimos
                insumoPersistente = new Insumo();
                insumoPersistente.setCatalogoInsumo(catalogoSeleccionado);
                insumoPersistente.setNombre(catalogoSeleccionado.getNombre());
                insumoPersistente.setCantidad(0.0);
                insumoPersistente.setMedida(unidadSeleccionada);
                insumoDAO.save(insumoPersistente);
            }

            // Crear nuevo objeto InsumoReceta y agregar a la lista
            InsumoReceta nuevoInsumoReceta = new InsumoReceta(null, insumoPersistente, cantidad, unidadSeleccionada);
            listaInsumosReceta.add(nuevoInsumoReceta);

            // Log de la acción
            ActionLogger.log("Ingrediente agregado: " + catalogoSeleccionado.getNombre() + ", Cantidad: " + cantidad + " " + unidadSeleccionada);

            // Limpiar campos después de agregar
            cmbIngredientes.setValue(null);
            txtCantIngrediente.clear();
            cmbUnidad.setValue(null);

            // Refrescar la tabla
            tableIngredientes.refresh();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "La cantidad debe ser un número entero válido.");
        }
    }

    @FXML
    private void handleEliminar(ActionEvent event) {
        InsumoReceta selectedInsumoReceta = tableIngredientes.getSelectionModel().getSelectedItem();
        if (selectedInsumoReceta != null) {
            if (recetaModificada != null && selectedInsumoReceta.getId() != null) {
                // Eliminar de la base de datos si el insumo ya estaba almacenado
                recetaDAO.eliminarInsumoDeReceta(selectedInsumoReceta.getId());
                ActionLogger.log("Ingrediente eliminado de la receta: " + selectedInsumoReceta.getInsumo().getNombre());
            }
            // Eliminar de la lista en memoria
            listaInsumosReceta.remove(selectedInsumoReceta);
            tableIngredientes.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        if (txtNomReceta.getText().isEmpty() || tableIngredientes.getItems().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Debe completar todos los campos.");
            return;
        }

        if (recetaModificada == null) {
            recetaModificada = new Receta(txtNomReceta.getText());
        } else {
            recetaModificada.setNombreReceta(txtNomReceta.getText());
            recetaModificada.getInsumosReceta().clear();
        }

        for (InsumoReceta insumoReceta : listaInsumosReceta) {
            insumoReceta.setReceta(recetaModificada);
            recetaModificada.addInsumo(insumoReceta);
        }

        // Log de la acción de guardar receta
        ActionLogger.log("Receta guardada: " + recetaModificada.getNombreReceta());

        // Cerrar ventana
        ((Stage) btnGuardar.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar cancelación");
        confirmAlert.setHeaderText("¿Estás seguro que quieres cancelar?");
        confirmAlert.setContentText("Los cambios no guardados se perderán.");
        ButtonType btnSi = new ButtonType("Sí", ButtonBar.ButtonData.YES);
        ButtonType btnNo = new ButtonType("No", ButtonBar.ButtonData.NO);
        confirmAlert.getButtonTypes().setAll(btnSi, btnNo);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == btnSi) {
                limpiarFormulario();
                btnAgregar.setDisable(false);
                btnEliminar.setDisable(false);
                btnEditar.setText("Editar");
                ActionLogger.log("Operación cancelada en la pantalla de nueva receta.");
                // Cerrar ventana
                Stage stage = (Stage) btnCancelar.getScene().getWindow();
                stage.close();
            }
        });
    }

    @FXML
    private void handleEditar(ActionEvent event) {
        InsumoReceta insumoRecetaSeleccionado = tableIngredientes.getSelectionModel().getSelectedItem();
        if (insumoRecetaSeleccionado != null) {
            if (btnEditar.getText().equals("Editar")) {
                cargarDatosParaEdicion(insumoRecetaSeleccionado);
                ActionLogger.log("Insumo de receta seleccionado para editar: "
                        + insumoRecetaSeleccionado.getInsumo().getNombre());
                // Cambios para modo edición
                gridAcciones.setVisible(false);
                gridEdicion.setVisible(true);
                tableIngredientes.setDisable(true);
                cmbIngredientes.setDisable(true);
            } else {
                actualizarIngrediente(insumoRecetaSeleccionado);
                ActionLogger.log("Cambios guardados para ingrediente: " + insumoRecetaSeleccionado.getInsumo().getNombre());
            }
        }
    }

    @FXML
    private void handleGuardarCambios(ActionEvent event) {
        InsumoReceta insumoRecetaSeleccionado = tableIngredientes.getSelectionModel().getSelectedItem();
        if (insumoRecetaSeleccionado != null) {
            actualizarIngrediente(insumoRecetaSeleccionado);
            // Restaurar estado
            gridAcciones.setVisible(true);
            gridEdicion.setVisible(false);
            tableIngredientes.setDisable(false);
            cmbIngredientes.setDisable(false);
        }
    }

    @FXML
    private void handleCancelarEdicion(ActionEvent event) {
        // Restaurar estado
        limpiarFormulario();
        gridAcciones.setVisible(true);
        gridEdicion.setVisible(false);
        tableIngredientes.setDisable(false);
        cmbIngredientes.setDisable(false);
        btnAgregar.setDisable(false);
        btnEliminar.setDisable(false);
        btnEditar.setText("Editar");
    }

    private void cargarDatosParaEdicion(InsumoReceta insumoReceta) {
        CatalogoInsumo catalogo = insumoReceta.getInsumo().getCatalogoInsumo();
        if (!cmbIngredientes.getItems().contains(catalogo)) {
            cmbIngredientes.getItems().add(catalogo);
        }
        cmbIngredientes.setValue(catalogo);
        // Mostrar la cantidad como entero si es posible
        int cantidadInt = (int) insumoReceta.getCantidadUtilizada();
        if (insumoReceta.getCantidadUtilizada() == cantidadInt) {
            txtCantIngrediente.setText(String.valueOf(cantidadInt));
        } else {
            txtCantIngrediente.setText(String.valueOf(insumoReceta.getCantidadUtilizada()));
        }
        if (!cmbUnidad.getItems().contains(insumoReceta.getUnidad())) {
            cmbUnidad.getItems().add(insumoReceta.getUnidad());
        }
        cmbUnidad.setValue(insumoReceta.getUnidad());

        btnAgregar.setDisable(true);
        btnEliminar.setDisable(true);
        btnEditar.setText("Guardar cambios");
    }

    private void actualizarIngrediente(InsumoReceta insumoRecetaSeleccionado) {
        if (validarCamposIngrediente()) {
            CatalogoInsumo catalogoSeleccionado = cmbIngredientes.getValue();
            Insumo insumo = insumoRecetaSeleccionado.getInsumo();
            insumo.setCatalogoInsumo(catalogoSeleccionado);
            insumo.setNombre(catalogoSeleccionado.getNombre());
            insumoRecetaSeleccionado.setCantidadUtilizada(Integer.parseInt(txtCantIngrediente.getText()));
            insumoRecetaSeleccionado.setUnidad(cmbUnidad.getValue());

            tableIngredientes.refresh();
            btnAgregar.setDisable(false);
            btnEliminar.setDisable(false);
            btnEditar.setText("Editar");

            limpiarFormulario();
        }
    }

    private boolean validarCamposIngrediente() {
        if (cmbIngredientes.getValue() == null || txtCantIngrediente.getText().isEmpty()
                || cmbUnidad.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Debe completar todos los campos.");
            return false;
        }
        try {
            Integer.parseInt(txtCantIngrediente.getText());
            return true;
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "La cantidad debe ser un número válido.");
            return false;
        }
    }

    private void limpiarFormulario() {
        cmbIngredientes.setValue(null);
        txtCantIngrediente.clear();
        cmbUnidad.setValue(null);
    }

    public Receta getRecetaModificada() {
        return recetaModificada;
    }

    public void cargarRecetaParaModificar(Receta receta) {
        this.recetaModificada = receta;
        if (receta != null) {
            txtNomReceta.setText(receta.getNombreReceta());
            listaInsumosReceta.setAll(receta.getInsumosReceta());
        }
    }

    private void abrirFormularioInsumo(CatalogoInsumo insumo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/NuevoInsumo.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(insumo == null ? "Agregar Insumo al Catálogo" : "Modificar Insumo del Catálogo");
            stage.initModality(Modality.WINDOW_MODAL);

            NuevoInsumoController controller = loader.getController();
            if (insumo != null) {
                controller.setInsumo(insumo);
            }

            stage.showAndWait();
            recargarComboInsumos(); // Recargar la lista del ComboBox después de cerrar el formulario
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirFormularioInsumo() {
        abrirFormularioInsumo(null);
        recargarComboInsumos(); // Recargar ComboBox de insumos después de cerrar el formulario
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void recargarComboInsumos() {
        cmbIngredientes.setItems(FXCollections.observableArrayList(new CatalogoInsumoDAO().findAll()));
    }

    public void setTitulo(String titulo) {
        if (lblTitulo != null) {
            lblTitulo.setText(titulo);
        }
    }
}
