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
import utilities.ActionLogger;

import java.util.ArrayList;
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
        colIngrediente.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getInsumo().getNombre()));
        colCantidad.setCellValueFactory(cellData -> {
            InsumoReceta insumoReceta = cellData.getValue();
            return new SimpleStringProperty(
                    insumoReceta.getCantidadUtilizada() + " " + insumoReceta.getUnidad()
            );
        });

        cmbIngredientes.setItems(FXCollections.observableArrayList(new InsumoDAO().findAll()));
        cmbUnidad.setItems(FXCollections.observableArrayList("GR", "KG", "ML", "L", "UNIDAD", "UNIDADES"));

        tableIngredientes.setItems(listaInsumosReceta);
        btnEliminar.setDisable(true);

        tableIngredientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnEliminar.setDisable(newSelection == null);
            btnEditar.setDisable(newSelection == null);
        });
    }


    @FXML
    private void handleAgregar(ActionEvent event) {
        // Validación de campos obligatorios
        if (cmbIngredientes.getValue() == null || txtCantIngrediente.getText().isEmpty() || cmbUnidad.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Debe completar todos los campos para agregar un ingrediente.");
            return;
        }

        try {
            // Parsear la cantidad ingresada
            int cantidad = Integer.parseInt(txtCantIngrediente.getText());
            Insumo insumoSeleccionado = cmbIngredientes.getValue();
            String unidadSeleccionada = cmbUnidad.getValue();

            // Verificar duplicados
            boolean ingredienteYaExiste = listaInsumosReceta.stream()
                    .anyMatch(insumoReceta -> insumoReceta.getInsumo().equals(insumoSeleccionado));

            if (ingredienteYaExiste) {
                showAlert(Alert.AlertType.ERROR, "Error", "Este ingrediente ya está en la receta.");
                return; // No agregar duplicados
            }

            // Crear nuevo objeto InsumoReceta y agregar a la lista
            InsumoReceta nuevoInsumoReceta = new InsumoReceta(null, insumoSeleccionado, cantidad, unidadSeleccionada);
            listaInsumosReceta.add(nuevoInsumoReceta);

            // Log de la acción
            ActionLogger.log("Ingrediente agregado: " + insumoSeleccionado.getNombre() + ", Cantidad: " + cantidad + " " + unidadSeleccionada);

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
        // Limpiar los campos y restaurar los botones
        limpiarFormulario();

        // Volver los botones a su estado inicial
        btnAgregar.setDisable(false);
        btnEliminar.setDisable(false);
        btnEditar.setText("Editar");

        // Log de la acción de cancelar
        ActionLogger.log("Operación cancelada en la pantalla de nueva receta.");
    }

    @FXML
    private void handleEditar(ActionEvent event) {
        InsumoReceta insumoRecetaSeleccionado = tableIngredientes.getSelectionModel().getSelectedItem();

        if (insumoRecetaSeleccionado != null) {
            if (btnEditar.getText().equals("Editar")) {
                cargarDatosParaEdicion(insumoRecetaSeleccionado);
                ActionLogger.log("Insumo de receta seleccionado para editar: " + insumoRecetaSeleccionado.getInsumo().getNombre());
            } else {
                actualizarIngrediente(insumoRecetaSeleccionado);
                ActionLogger.log("Cambios guardados para ingrediente: " + insumoRecetaSeleccionado.getInsumo().getNombre());
            }
        }
    }

    private void cargarDatosParaEdicion(InsumoReceta insumoReceta) {
        // Aseguramos que el insumo esté en el ComboBox (por si la lista fue recargada)
        if (!cmbIngredientes.getItems().contains(insumoReceta.getInsumo())) {
            cmbIngredientes.getItems().add(insumoReceta.getInsumo());
        }
        cmbIngredientes.setValue(insumoReceta.getInsumo());
        txtCantIngrediente.setText(String.valueOf(insumoReceta.getCantidadUtilizada()));
        // Aseguramos que la unidad esté en el ComboBox (por si hay variantes)
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

    private void aplicarFiltroNumeros(TextField textField) {

    }

    public void cargarRecetaParaModificar(Receta receta) {
        this.recetaModificada = receta;
        if (receta != null) {
            txtNomReceta.setText(receta.getNombreReceta());
            listaInsumosReceta.setAll(receta.getInsumosReceta());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
