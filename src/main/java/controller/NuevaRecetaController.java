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
import model.Ingrediente;
import model.RecetaDetalle;
import model.Receta;
import persistence.dao.IngredienteDAO; // Asumo que este DAO ahora maneja la entidad Ingrediente
import persistence.dao.RecetaDAO;
import utilities.ActionLogger;

import java.io.IOException;

public class NuevaRecetaController {

    @FXML private TextField txtNomReceta;
    @FXML private ComboBox<Ingrediente> cmbIngredientes;
    @FXML private TextField txtCantIngrediente;
    @FXML private ComboBox<String> cmbUnidad;
    @FXML private Button btnAgregar;
    @FXML private Button btnEditar;
    @FXML private TableView<RecetaDetalle> tableIngredientes;
    @FXML private TableColumn<RecetaDetalle, String> colIngrediente;
    @FXML private TableColumn<RecetaDetalle, String> colCantidad;
    @FXML private Button btnEliminar;
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;
    @FXML private GridPane gridAcciones; 
    @FXML private GridPane gridEdicion;  
    @FXML private Button btnGuardarCambios;
    @FXML private Button btnCancelarEdicion;
    @FXML private Label lblTitulo;

    private ObservableList<RecetaDetalle> listaInsumosReceta = FXCollections.observableArrayList();
    private RecetaDAO recetaDAO = new RecetaDAO();
    private Receta recetaModificada;

    private IngredienteDAO ingredienteDAO = new IngredienteDAO(); 

    @FXML
    public void initialize() {
        // 1. Configurar Columna Nombre (Ahora navegamos getIngrediente())
        colIngrediente.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getIngrediente().getNombre()));

        // 2. Configurar Columna Cantidad (Ahora es getCantidad() y es double)
        colCantidad.setCellValueFactory(cellData -> {
            RecetaDetalle detalle = cellData.getValue();
            double cantidad = detalle.getCantidad();
            // Formateo visual para quitar decimales .0
            String cantidadStr = (cantidad == Math.floor(cantidad)) 
                    ? String.format("%.0f", cantidad) 
                    : String.format(java.util.Locale.ROOT, "%.2f", cantidad);
            
            return new SimpleStringProperty(cantidadStr + " " + detalle.getUnidad());
        });

        // 3. Cargar Combo de Ingredientes
        cmbIngredientes.setItems(FXCollections.observableArrayList(ingredienteDAO.findAll()));
        cmbUnidad.setItems(FXCollections.observableArrayList("GR", "KG", "ML", "L", "UNIDAD", "UNIDADES"));

        // Renderizado del ComboBox (Nombre del ingrediente)
        cmbIngredientes.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Ingrediente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });
        cmbIngredientes.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Ingrediente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        // Listener para filtrar unidades
        cmbIngredientes.valueProperty().addListener((obs, oldVal, newVal) -> filtrarUnidadesPorEstado(newVal));

        tableIngredientes.setItems(listaInsumosReceta);
        btnEliminar.setDisable(true);

        tableIngredientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean haySeleccion = (newSelection != null);
            btnEliminar.setDisable(!haySeleccion);
            btnEditar.setDisable(!haySeleccion);
        });

        // Filtro para input numérico (permite decimales ahora con \\d*\\.?\\d*)
        txtCantIngrediente.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*\\.?\\d*")) { 
                return change;
            }
            return null;
        }));

        gridEdicion.setVisible(false);
        gridAcciones.setVisible(true);
    }

    private void filtrarUnidadesPorEstado(Ingrediente ingrediente) {
        cmbUnidad.getItems().clear();
        String estado = (ingrediente != null) ? ingrediente.getEstado() : null;
        
        if (estado == null) {
            cmbUnidad.getItems().addAll("GR", "KG", "ML", "L", "UNIDAD", "UNIDADES");
            return;
        }
        switch (estado) {
            case "LÍQUIDO": cmbUnidad.getItems().addAll("ML", "L"); break;
            case "SÓLIDO": cmbUnidad.getItems().addAll("GR", "KG"); break;
            case "UNIDAD": cmbUnidad.getItems().addAll("UNIDAD", "UNIDADES"); break;
            default: cmbUnidad.getItems().addAll("GR", "KG", "ML", "L", "UNIDAD", "UNIDADES");
        }
        if (!cmbUnidad.getItems().isEmpty()) {
            cmbUnidad.setValue(cmbUnidad.getItems().get(0));
        }
    }

    @FXML
    private void handleAgregar(ActionEvent event) {
        if (cmbIngredientes.getValue() == null || txtCantIngrediente.getText().isEmpty() || cmbUnidad.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Debe completar todos los campos.");
            return;
        }

        try {
            double cantidad = Double.parseDouble(txtCantIngrediente.getText());
            Ingrediente ingredienteSeleccionado = cmbIngredientes.getValue();
            String unidadSeleccionada = cmbUnidad.getValue();

            // Verificar duplicados
            boolean yaExiste = listaInsumosReceta.stream()
                    .anyMatch(d -> d.getIngrediente().getId().equals(ingredienteSeleccionado.getId()));

            if (yaExiste) {
                showAlert(Alert.AlertType.ERROR, "Error", "Este ingrediente ya está en la receta.");
                return;
            }

            // CAMBIO IMPORTANTE: Creamos RecetaDetalle apuntando al Ingrediente, no al Lote
            // (El primer parámetro es la receta, que se asigna al guardar)
            RecetaDetalle nuevoDetalle = new RecetaDetalle(null, ingredienteSeleccionado, cantidad, unidadSeleccionada);
            
            listaInsumosReceta.add(nuevoDetalle);

            ActionLogger.log("Ingrediente agregado: " + ingredienteSeleccionado.getNombre() + ", Cant: " + cantidad);

            limpiarFormulario();
            tableIngredientes.refresh();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "La cantidad debe ser un número válido.");
        }
    }

    @FXML
    private void handleEliminar(ActionEvent event) {
        RecetaDetalle seleccionado = tableIngredientes.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            // Si estamos editando una receta existente y el detalle ya tiene ID, lo borramos de la DB
            if (recetaModificada != null && seleccionado.getId() != null) {
                recetaDAO.eliminarInsumoDeReceta(seleccionado.getId());
                ActionLogger.log("Ingrediente eliminado de receta: " + seleccionado.getIngrediente().getNombre());
            }
            listaInsumosReceta.remove(seleccionado);
            tableIngredientes.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        if (txtNomReceta.getText().isEmpty() || tableIngredientes.getItems().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Debe completar el nombre y agregar ingredientes.");
            return;
        }

        if (recetaModificada == null) {
            // NUEVA RECETA
            recetaModificada = new Receta(txtNomReceta.getText());
            // Vinculamos los detalles a la receta padre
            for (RecetaDetalle detalle : listaInsumosReceta) {
                detalle.setReceta(recetaModificada);
                recetaModificada.getIngredientes().add(detalle); // CAMBIO: getIngredientes()
            }
            recetaDAO.save(recetaModificada);
            ActionLogger.log("Receta creada: " + recetaModificada.getNombreReceta());
        } else {
            // EDICIÓN DE RECETA EXISTENTE
            recetaModificada.setNombreReceta(txtNomReceta.getText());
            
            // Limpiamos la lista actual de la entidad para reemplazarla o actualizarla
            recetaModificada.getIngredientes().clear();
            
            for (RecetaDetalle detalle : listaInsumosReceta) {
                detalle.setReceta(recetaModificada);
                recetaModificada.getIngredientes().add(detalle);
            }
            recetaDAO.update(recetaModificada);
            ActionLogger.log("Receta modificada: " + recetaModificada.getNombreReceta());
        }
        
        ((Stage) btnGuardar.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Cancelar? Se perderán los cambios.", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                ((Stage) btnCancelar.getScene().getWindow()).close();
            }
        });
    }

    @FXML
    private void handleEditar(ActionEvent event) {
        RecetaDetalle seleccionado = tableIngredientes.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            cargarDatosParaEdicion(seleccionado);
            gridAcciones.setVisible(false);
            gridEdicion.setVisible(true);
            tableIngredientes.setDisable(true);
            cmbIngredientes.setDisable(true); // No dejamos cambiar el ingrediente, solo cantidad
        }
    }

    @FXML
    private void handleGuardarCambios(ActionEvent event) {
        RecetaDetalle seleccionado = tableIngredientes.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            try {
                double nuevaCant = Double.parseDouble(txtCantIngrediente.getText());
                String nuevaUnidad = cmbUnidad.getValue();
                
                seleccionado.setCantidad(nuevaCant); // CAMBIO: setCantidad (double)
                seleccionado.setUnidad(nuevaUnidad);
                
                tableIngredientes.refresh();
                restaurarModoNormal();
                
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Cantidad inválida.");
            }
        }
    }

    @FXML
    private void handleCancelarEdicion(ActionEvent event) {
        restaurarModoNormal();
    }

    private void restaurarModoNormal() {
        limpiarFormulario();
        gridAcciones.setVisible(true);
        gridEdicion.setVisible(false);
        tableIngredientes.setDisable(false);
        cmbIngredientes.setDisable(false);
        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);
        tableIngredientes.getSelectionModel().clearSelection();
    }

    private void cargarDatosParaEdicion(RecetaDetalle detalle) {
        Ingrediente ingrediente = detalle.getIngrediente();
        cmbIngredientes.setValue(ingrediente);
        
        // Formateo visual para editar
        double cant = detalle.getCantidad();
        if (cant == Math.floor(cant)) {
            txtCantIngrediente.setText(String.format("%.0f", cant));
        } else {
            txtCantIngrediente.setText(String.valueOf(cant));
        }
        
        cmbUnidad.setValue(detalle.getUnidad());
    }

    private void limpiarFormulario() {
        cmbIngredientes.setValue(null);
        txtCantIngrediente.clear();
        cmbUnidad.setValue(null);
    }

    public void cargarRecetaParaModificar(Receta receta) {
        this.recetaModificada = receta;
        if (receta != null) {
            txtNomReceta.setText(receta.getNombreReceta());
            listaInsumosReceta.setAll(receta.getIngredientes()); // CAMBIO: getIngredientes()
        }
    }

    // Método para abrir el ABM de Ingredientes (Nuevo Insumo)
    @FXML
    private void abrirFormularioInsumo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/NuevoInsumo.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Gestión de Ingredientes");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showAndWait();
            
            // Recargar combo al volver
            cmbIngredientes.setItems(FXCollections.observableArrayList(ingredienteDAO.findAll()));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setTitulo(String titulo) {
        if (lblTitulo != null) lblTitulo.setText(titulo);
    }
}