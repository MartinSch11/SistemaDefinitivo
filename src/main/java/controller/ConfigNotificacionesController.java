package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import model.NotificacionConfig;
import persistence.dao.NotificacionConfigDAO;

public class ConfigNotificacionesController {
    @FXML
    private Spinner<Integer> spinnerDiasAnticipo; // Eventos
    @FXML
    private Spinner<Integer> spinnerDiasAnticipoCaducidad; // Insumos
    @FXML
    private Spinner<Integer> spinnerDiasAnticipoPedidos; // NUEVO: Pedidos
    @FXML
    private Button btnGuardarNoti;

    private final NotificacionConfigDAO configDAO = new NotificacionConfigDAO();
    private NotificacionConfig configActual;

    @FXML
    private void initialize() {
        configActual = configDAO.findOrDefault();

        // Eventos
        spinnerDiasAnticipo.setValueFactory(
                new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1, 30, configActual.getDiasAnticipacion()));

        // Caducidad de insumos
        spinnerDiasAnticipoCaducidad.setValueFactory(
                new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1, 120, configActual.getDiasAnticipacionCaducidad()));

        // Pedidos (permitimos 0 = solo hoy)
        spinnerDiasAnticipoPedidos.setValueFactory(
                new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(
                        0, 30, configActual.getDiasAnticipacionPedidos()));
    }

    @FXML
    private void guardarConfigNotificaciones() {
        int diasEventos = spinnerDiasAnticipo.getValue();
        int diasCaducidad = spinnerDiasAnticipoCaducidad.getValue();
        int diasPedidos = spinnerDiasAnticipoPedidos.getValue();

        configActual.setDiasAnticipacion(diasEventos);
        configActual.setDiasAnticipacionCaducidad(diasCaducidad);
        configActual.setDiasAnticipacionPedidos(diasPedidos);

        configDAO.saveOrUpdateGlobal(configActual);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Configuración guardada");
        alert.setHeaderText(null);
        alert.setContentText("Los valores de notificación han sido actualizados y guardados.");
        alert.showAndWait();
    }
}
