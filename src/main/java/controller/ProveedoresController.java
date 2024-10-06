package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import utilities.Paths;
import utilities.SceneLoader;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProveedoresController {
    /*
    private ObservableList<Proveedor> proveedoresList = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colInsumo.setCellValueFactory(new PropertyValueFactory<>("insumo"));
        colTipoDeInsumo.setCellValueFactory(new PropertyValueFactory<>("tipoDeInsumo"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        // Setear los datos iniciales (si tienes)
        tableViewProveedores.setItems(proveedoresList);
    }*/

    @FXML
    void handleVolver(ActionEvent event) {
        SceneLoader.handleVolver(event, Paths.ADMIN_MAINMENU, "/css/loginAdmin.css", true);
    }

    @FXML
    void handleNuevoProveedor(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/DialogNuevoProveedor.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Añadir proveedor");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void InsertarDatosTabla(){

    }


}
