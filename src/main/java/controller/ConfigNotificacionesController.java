package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import model.NotificacionConfig;
import persistence.dao.NotificacionConfigDAO;

public class ConfigNotificacionesController {
    @FXML private Spinner<Integer> spinnerMinutosNoti;
    @FXML private Spinner<Integer> spinnerDiasAnticipo;
    @FXML private Button btnGuardarNoti;

    private NotificacionConfigDAO configDAO = new NotificacionConfigDAO();
    private NotificacionConfig configActual;

    @FXML
    private void initialize() {
        configActual = configDAO.findOrDefault();
        spinnerMinutosNoti.setValueFactory(
                new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, configActual.getMinutos())
        );
        spinnerDiasAnticipo.setValueFactory(
                new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, configActual.getDiasAnticipacion())
        );
    }

    @FXML
    private void guardarConfigNotificaciones() {
        int minutos = spinnerMinutosNoti.getValue();
        int dias = spinnerDiasAnticipo.getValue();
        configActual.setMinutos(minutos);
        configActual.setDiasAnticipacion(dias);
        configDAO.saveOrUpdateGlobal(configActual);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Configuración guardada");
        alert.setHeaderText(null);
        alert.setContentText("Los valores de notificación han sido actualizados y guardados.");
        alert.showAndWait();
    }
}
