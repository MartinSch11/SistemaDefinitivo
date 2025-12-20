package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Ingrediente;
import persistence.dao.IngredienteDAO;
import utilities.ActionLogger;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class TableInsumosController {

    @FXML private TableView<Ingrediente> tableInsumos;
    @FXML private TableColumn<Ingrediente, String> colInsumo;
    @FXML private TableColumn<Ingrediente, String> colProveedor;
    @FXML private Button btnAgregar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;

    private IngredienteDAO catalogoInsumoDAO;
    private ObservableList<Ingrediente> catalogoList;

    public TableInsumosController() {
        catalogoInsumoDAO = new IngredienteDAO();
    }

    @FXML
    public void initialize() {
        configurarTabla();
        cargarInsumos();
        configurarBotones();

        // Permisos del usuario
        java.util.List<String> permisos = model.SessionContext.getInstance().getPermisos();
        boolean puedeCrear = permisos != null && permisos.contains("Insumos-crear");
        boolean puedeModificar = permisos != null && permisos.contains("Insumos-modificar");
        boolean puedeEliminar = permisos != null && permisos.contains("Insumos-eliminar");

        btnAgregar.setDisable(!puedeCrear);
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);

        tableInsumos.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
            btnModificar.setDisable(!(puedeModificar && newSelection != null));
            btnEliminar.setDisable(!(puedeEliminar && newSelection != null));
        });
    }

    private void configurarTabla() {
        colInsumo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colProveedor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProveedor()));

        // Listener para habilitar botones de modificar y eliminar al seleccionar un insumo
        tableInsumos.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            btnModificar.setDisable(newValue == null);
            btnEliminar.setDisable(newValue == null);
        });
    }


    private void configurarBotones() {
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    @FXML
    private void handleAgregar() {
        // Registro de la acción del usuario
        ActionLogger.log("El usuario agregó un nuevo insumo al catálogo.");
        abrirFormularioInsumo(null);
    }

    @FXML
    private void handleModificar() {
        Ingrediente insumoSeleccionado = tableInsumos.getSelectionModel().getSelectedItem();
        if (insumoSeleccionado != null) {
            // Registro de la acción del usuario
            ActionLogger.log("El usuario modificó el insumo del catálogo: " + insumoSeleccionado.getNombre());
            abrirFormularioInsumo(insumoSeleccionado);
        }
    }

    /*@FXML
    private void handleEliminar() {
        CatalogoInsumo insumoSeleccionado = tableInsumos.getSelectionModel().getSelectedItem();
        if (insumoSeleccionado != null) {
            // Registro de la acción del usuario
            ActionLogger.log("El usuario eliminó el insumo del catálogo: " + insumoSeleccionado.getNombre());
            catalogoInsumoDAO.delete(insumoSeleccionado);
            catalogoList.remove(insumoSeleccionado);
        }
    }*/

    @FXML
    private void handleEliminar() {
        Ingrediente insumoSeleccionado = tableInsumos.getSelectionModel().getSelectedItem();

        if (insumoSeleccionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText(null);
            alert.setContentText("¿Desea eliminar el insumo? Esta acción no se puede deshacer.");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // Intentamos borrar
                    catalogoInsumoDAO.delete(insumoSeleccionado);
                    
                    // Si no explotó, actualizamos la tabla y logueamos
                    catalogoList.remove(insumoSeleccionado);
                    ActionLogger.log("El usuario eliminó el insumo del catálogo: " + insumoSeleccionado.getNombre());
                    
                } catch (Exception e) {
                    // ACÁ ESTÁ LA MAGIA: Si falla por integridad referencial, caemos acá.
                    // (Generalmente es RollbackException o PersistenceException)
                    
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("No se puede eliminar");
                    errorAlert.setHeaderText("El insumo está en uso");
                    errorAlert.setContentText("No podés eliminar este insumo porque hay recetas o stock que lo están usando.\n\n" +
                                              "Primero eliminá las recetas/stock asociados o modificalo en lugar de borrarlo.");
                    errorAlert.showAndWait();
                    
                    // Opcional: Imprimir el error real en consola para vos
                    System.err.println("Error al eliminar insumo: " + e.getMessage());
                }
            }
        }
    }

    private void abrirFormularioInsumo(Ingrediente insumo) {
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
            controller.setTableInsumosController(this); // Pasamos la referencia del controlador de la tabla

            stage.showAndWait();
            cargarInsumos(); // Recargar la tabla después de cerrar el formulario
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cargarInsumos() {
        List<Ingrediente> catalogo = catalogoInsumoDAO.findAll();
        catalogoList = FXCollections.observableArrayList(catalogo);
        tableInsumos.setItems(catalogoList);
        tableInsumos.refresh(); // Forzar refresco visual
    }

}
