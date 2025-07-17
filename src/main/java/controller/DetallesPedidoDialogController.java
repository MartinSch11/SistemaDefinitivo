package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.Pedido;
import model.PedidoProducto;
import model.Producto;

public class DetallesPedidoDialogController {
    @FXML private Label lblNumeroPedido;
    @FXML private Label lblCliente;
    @FXML private Label lblDni;
    @FXML private Label lblTelefono;
    @FXML private Label lblEmpleado;
    @FXML private Label lblFechaEntrega;
    @FXML private Label lblFormaEntrega;
    @FXML private ListView<String> listProductos;
    @FXML private Button btnCerrar;
    @FXML private Label lblTotal;

    public void setPedido(Pedido pedido) {
        lblNumeroPedido.setText("N° Pedido: " + pedido.getNumeroPedido());
        if (pedido.getCliente() != null) {
            lblCliente.setText("Cliente: " + pedido.getCliente().getNombre() + (pedido.getCliente().getApellido() != null ? " " + pedido.getCliente().getApellido() : ""));
            lblDni.setText("DNI: " + pedido.getCliente().getDni());
            lblTelefono.setText("Teléfono: " + pedido.getCliente().getTelefono());
        } else {
            lblCliente.setText("Cliente: -");
            lblDni.setText("DNI: -");
            lblTelefono.setText("Teléfono: -");
        }
        lblEmpleado.setText("Empleado asignado: " + (pedido.getEmpleadoAsignado() != null ? pedido.getEmpleadoAsignado().getNombre() : "Sin asignar"));
        lblFechaEntrega.setText("Fecha de entrega: " + (pedido.getFechaEntrega() != null ? pedido.getFechaEntrega().toString() : "Sin fecha"));
        lblFormaEntrega.setText("Forma de entrega: " + (pedido.getFormaEntrega() != null ? pedido.getFormaEntrega() : ""));
        listProductos.getItems().clear();
        if (pedido.getPedidoProductos() != null) {
            for (PedidoProducto pp : pedido.getPedidoProductos()) {
                Producto prod = pp.getProducto();
                String nombre = (prod != null ? prod.getNombre() : "Producto desconocido");
                listProductos.getItems().add("- " + nombre + " x " + pp.getCantidad());
            }
        }
        // Mostrar total
        if (pedido.getTotalPedido() != null) {
            lblTotal.setText("Total: $ " + pedido.getTotalPedido().toString());
        } else {
            lblTotal.setText("Total: $ " + pedido.calcularTotalPedido().toString());
        }
    }

    @FXML
    public void initialize() {
        // Permitir cerrar con ESC
        btnCerrar.sceneProperty().addListener((unused1, unused2, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        stage.close();
                    }
                });
            }
        });
    }

    @FXML
    private void handleCerrar() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }
}
