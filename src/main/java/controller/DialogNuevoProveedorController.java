package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert.AlertType;

import java.util.Optional;

public class DialogNuevoProveedorController {
    @FXML private ComboBox<String> cmbTipoDeInsumo;
    @FXML private TextField fieldCantidadInsumo;
    @FXML private TextField proveedorField;
    @FXML private ComboBox<String> insumo;
    @FXML private TextField telefonoField;
    @FXML private TextField ubicacionField;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    boolean camposCompletos = false;

    @FXML
    private void initialize() {
        /*btnGuardar.setOnAction(event -> guardarDatos());*/

        /*---------momentaneo------------*/
        insumo.setItems(FXCollections.observableArrayList(
                "Harina",
                "Azúcar",
                "Leche",
                "Huevos",
                "Manteca"
        ));

        proveedorField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z]*")) {
                proveedorField.setText(newValue.replaceAll("[^a-zA-Z]", ""));
            }
        });
        telefonoField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                telefonoField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

    }

    private void alertaCamposObligatorios(){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText("Campos obligatorios sin completar.");
        alert.setContentText("Solución: completar los campos vacíos.");

        ButtonType botonAceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(botonAceptar);
        alert.showAndWait();

    }

    private boolean validarCamposObligatorios() {
        boolean valid = true;

        if ((proveedorField.getText() == null || proveedorField.getText().trim().isEmpty()) ||
                insumo.getValue() == null || cmbTipoDeInsumo.getValue() == null || (fieldCantidadInsumo.getText() == null) || fieldCantidadInsumo.getText().trim().isEmpty() ||
                (telefonoField.getText() == null || telefonoField.getText().trim().isEmpty()) ||
                (ubicacionField.getText() == null || ubicacionField.getText().trim().isEmpty())) {

            valid = false;
            alertaCamposObligatorios();
        }else{
            camposCompletos = true;
        }
        return valid;
    }

    @FXML
    void cancelarNuevoProveedor(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("Se perderán los cambios no guardados. ¿Desea salir?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    void guardarNuevoProveedor(ActionEvent event) {
        validarCamposObligatorios();
        if (camposCompletos == true){
            System.out.println("Nombre guardado: " + proveedorField.getText());
            System.out.println("Insumo guardado: " + insumo.getValue());
            System.out.println("Tipo de cantidad: " + cmbTipoDeInsumo.getValue());
            System.out.println("Cantidad: " + fieldCantidadInsumo.getText());
            System.out.println("Telefono guardado: " + telefonoField.getText());
            System.out.println("Ubicacion guardado: " + ubicacionField.getText());
            Stage stage = (Stage) btnGuardar.getScene().getWindow();
            stage.close();
        }

    }




}
