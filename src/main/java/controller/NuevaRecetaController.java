package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Insumo;
import model.InsumoReceta;
import model.Receta;
import persistence.dao.InsumoDAO;
import persistence.dao.RecetaDAO;

import java.util.List;

public class NuevaRecetaController {

    @FXML private TextField txtNomReceta;
    @FXML private ComboBox<Insumo> cmbIngredientes;
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

    private ObservableList<InsumoReceta> listaInsumosReceta = FXCollections.observableArrayList();
    private RecetaDAO recetaDAO = new RecetaDAO();
    private Receta recetaModificada;

    @FXML
    public void initialize() {
        // Configurar columnas de la tabla
        colIngrediente.setCellValueFactory(cellData -> cellData.getValue().getInsumo().nombreProperty());

        // Mostramos la cantidad seguida de la unidad
        colCantidad.setCellValueFactory(cellData -> {
            InsumoReceta insumoReceta = cellData.getValue();
            return new SimpleStringProperty(insumoReceta.getCantidadUtilizada() + " " + insumoReceta.getUnidad());
        });

        // Cargar insumos disponibles y unidades en los ComboBox
        cmbIngredientes.setItems(FXCollections.observableArrayList(new InsumoDAO().findAll()));
        cmbUnidad.setItems(FXCollections.observableArrayList("GR", "KG", "ML", "L", "CUCHARADITA", "CUCHARADA", "UNIDAD", "UNIDADES"));

        // Configurar la tabla
        tableIngredientes.setItems(listaInsumosReceta);

        // Deshabilitar el botón de eliminar al inicio
        btnEliminar.setDisable(true);

        // Configurar selección en la tabla
        tableIngredientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnEliminar.setDisable(newSelection == null);
            btnEditar.setDisable(newSelection == null);
        });
        aplicarFiltroNumeros(txtCantIngrediente);
    }

    @FXML
    private void handleAgregar(ActionEvent event) {
        if (cmbIngredientes.getValue() == null || txtCantIngrediente.getText().isEmpty() || cmbUnidad.getValue() == null) {
            mostrarAlerta("Error", "Debe completar todos los campos para agregar un ingrediente.");
            return;
        }

        try {
            int cantidad = Integer.parseInt(txtCantIngrediente.getText());
            Insumo insumoSeleccionado = cmbIngredientes.getValue();
            String unidadSeleccionada = cmbUnidad.getValue();
            InsumoReceta insumoReceta = new InsumoReceta(null, insumoSeleccionado, cantidad, unidadSeleccionada);

            listaInsumosReceta.add(insumoReceta);
            // Limpiar campos
            cmbIngredientes.setValue(null);
            txtCantIngrediente.clear();
            cmbUnidad.setValue(null);
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "La cantidad debe ser un número entero válido.");
        }
    }

    @FXML
    private void handleEliminar(ActionEvent event) {
        InsumoReceta selectedInsumoReceta = tableIngredientes.getSelectionModel().getSelectedItem();
        if (selectedInsumoReceta != null) {
            listaInsumosReceta.remove(selectedInsumoReceta);
            tableIngredientes.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        if (txtNomReceta.getText().isEmpty() || tableIngredientes.getItems().isEmpty()) {
            mostrarAlerta("Error", "Debe completar todos los campos.");
            return;
        }

        Receta receta;

        // Verificar si estamos editando una receta existente
        if (recetaModificada != null && recetaModificada.getId() != -1) {
            receta = recetaModificada;
            receta.setNombreReceta(txtNomReceta.getText());

            // Limpiar ingredientes previos
            receta.getInsumosReceta().clear();
            recetaDAO.update(receta);  // Guardar cambios previos antes de actualizar los ingredientes

            // Agregar ingredientes actuales
            for (InsumoReceta insumoReceta : tableIngredientes.getItems()) {
                receta.addInsumo(insumoReceta);  // Usar addInsumo() para asegurar la relación bidireccional
            }

            // Guardar cambios finales
            recetaDAO.update(receta);

        } else {
            // Guardar una nueva receta
            receta = new Receta(txtNomReceta.getText());

            // Agregar los ingredientes de la tabla a la nueva receta
            for (InsumoReceta insumoReceta : tableIngredientes.getItems()) {
                receta.addInsumo(insumoReceta);
            }

            // Guardar la nueva receta en la base de datos
            recetaDAO.save(receta);
        }

        // Cerrar la ventana de edición
        ((Stage) btnGuardar.getScene().getWindow()).close();
    }

    public void cargarRecetaParaModificar(Receta receta) {
        this.recetaModificada = receta;
        // Establecer el nombre de la receta
        txtNomReceta.setText(receta.getNombreReceta());

        // Limpiar la tabla de ingredientes
        listaInsumosReceta.clear();

        // Cargar los ingredientes y cantidades de la receta
        for (InsumoReceta insumoReceta : receta.getInsumosReceta()) {
            listaInsumosReceta.add(insumoReceta);
        }

        // Actualizar la tabla de ingredientes
        tableIngredientes.setItems(listaInsumosReceta);
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        // Limpiar los campos y restaurar los botones
        limpiarFormulario();

        // Volver los botones a su estado inicial
        btnAgregar.setDisable(false);
        btnEliminar.setDisable(false);
        btnEditar.setText("Editar");
    }

    @FXML
    private void handleEditar(ActionEvent event) {
        InsumoReceta insumoRecetaSeleccionado = tableIngredientes.getSelectionModel().getSelectedItem();

        if (insumoRecetaSeleccionado != null) {
            if (btnEditar.getText().equals("Editar")) {
                cargarDatosParaEdicion(insumoRecetaSeleccionado);
            } else {
                actualizarIngrediente(insumoRecetaSeleccionado);
            }
        }
    }

    private void cargarDatosParaEdicion(InsumoReceta insumoReceta) {
        cmbIngredientes.setValue(insumoReceta.getInsumo());
        txtCantIngrediente.setText(String.valueOf(insumoReceta.getCantidadUtilizada()));
        cmbUnidad.setValue(insumoReceta.getUnidad());

        btnAgregar.setDisable(true);
        btnEliminar.setDisable(true);
        btnEditar.setText("Guardar cambios");
    }

    private void actualizarIngrediente(InsumoReceta insumoRecetaSeleccionado) {
        if (validarCamposIngrediente()) {
            insumoRecetaSeleccionado.setInsumo(cmbIngredientes.getValue());
            insumoRecetaSeleccionado.setCantidadUtilizada(Integer.parseInt(txtCantIngrediente.getText()));
            insumoRecetaSeleccionado.setUnidad(cmbUnidad.getValue());

            tableIngredientes.refresh(); // Refrescar la tabla
            btnAgregar.setDisable(false);
            btnEliminar.setDisable(false);
            btnEditar.setText("Editar");

            limpiarFormulario();
        }
    }

    private boolean validarCamposIngrediente() {
        if (cmbIngredientes.getValue() == null || txtCantIngrediente.getText().isEmpty() || cmbUnidad.getValue() == null) {
            mostrarAlerta("Error", "Debe completar todos los campos.");
            return false;
        }
        try {
            Integer.parseInt(txtCantIngrediente.getText());
            return true;
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "La cantidad debe ser un número válido.");
            return false;
        }
    }

    private void limpiarFormulario() {
        cmbIngredientes.setValue(null);
        txtCantIngrediente.clear();
        cmbUnidad.setValue(null);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public Receta getRecetaModificada() {
        return recetaModificada;
    }

    private void aplicarFiltroNumeros(TextField textField) {
        textField.setTextFormatter(new TextFormatter<String>(change -> {
            // Permitir solo caracteres numéricos (0-9)
            if (change.getControlNewText().matches("[0-9]*")) {
                return change; // Permite el cambio si es un número
            }
            return null; // Rechaza el cambio si no es un número
        }));
    }
}
