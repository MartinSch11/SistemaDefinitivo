package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Pedido;
import persistence.dao.PedidoDAO;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.control.TextField;

public class HistorialPedidosDialogController {
    @FXML
    private TableView<Pedido> tablaPedidos;
    @FXML
    private TableColumn<Pedido, Long> colNumero;
    @FXML
    private TableColumn<Pedido, String> colCliente;
    @FXML
    private TableColumn<Pedido, String> colProductos;
    @FXML
    private TableColumn<Pedido, String> colFechaEntrega;
    @FXML
    private TableColumn<Pedido, String> colEmpleado;
    @FXML
    private Button cerrarDialogo;
    @FXML private TextField txtBuscar;

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @FXML
    public void initialize() {
        colNumero.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("numeroPedido"));
        colCliente.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (cellData.getValue().getCliente() != null) {
                String nombre = cellData.getValue().getCliente().getNombre();
                String apellido = cellData.getValue().getCliente().getApellido();
                return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
            } else {
                return "-";
            }
        }));
        colProductos.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            return cellData.getValue().generarDetalle();
        }));
        colFechaEntrega.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (cellData.getValue().getFechaEntregado() != null) {
                return cellData.getValue().getFechaEntregado().format(dateFormatter);
            } else {
                return "-";
            }
        }));
        colEmpleado.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            if (cellData.getValue().getEmpleadoAsignado() != null) {
                return cellData.getValue().getEmpleadoAsignado().getNombre();
            } else {
                return "-";
            }
        }));
        cargarPedidosEntregados();
        // Listener para búsqueda en tiempo real
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {
            filtrarPedidos(newVal);
        });
    }

    public void cargarPedidosEntregados() {
        List<Pedido> pedidosEntregados = pedidoDAO.findByEstado("Entregado");
        ObservableList<Pedido> data = FXCollections.observableArrayList(pedidosEntregados);
        tablaPedidos.setItems(data);
    }

    private void filtrarPedidos(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            cargarPedidosEntregados();
            return;
        }
        String filtroLower = filtro.toLowerCase();
        List<Pedido> pedidos = pedidoDAO.findByEstado("Entregado");
        List<Pedido> filtrados = pedidos.stream()
                .filter(p -> (p.getCliente() != null && ((p.getCliente().getNombre() + " " + p.getCliente().getApellido()).toLowerCase().contains(filtroLower)))
                        || p.generarDetalle().toLowerCase().contains(filtroLower)
                        || (p.getEmpleadoAsignado() != null && p.getEmpleadoAsignado().getNombre().toLowerCase().contains(filtroLower))
                        || (p.getNumeroPedido() != null && String.valueOf(p.getNumeroPedido()).contains(filtroLower)))
                .collect(java.util.stream.Collectors.toList());
        tablaPedidos.setItems(javafx.collections.FXCollections.observableArrayList(filtrados));
    }

    @FXML
    private void cerrarDialogo() {
        Stage stage = (Stage) tablaPedidos.getScene().getWindow();
        stage.close();
    }
}
