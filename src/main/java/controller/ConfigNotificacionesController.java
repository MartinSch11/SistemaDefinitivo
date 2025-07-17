package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleButton;
import model.NotificacionConfig;
import persistence.dao.NotificacionConfigDAO;

public class ConfigNotificacionesController {
    @FXML private Spinner<Integer> spinnerMinutosNoti;
    @FXML private Spinner<Integer> spinnerDiasAnticipo;
    @FXML private Button btnGuardarNoti;
    @FXML private ToggleButton toggleNotificaciones;
    @FXML private Spinner<Integer> spinnerDuracionNoti;
    @FXML private Spinner<Integer> spinnerDiasAnticipoCaducidad;

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
        spinnerDiasAnticipoCaducidad.setValueFactory(
                new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, configActual.getDiasAnticipacionCaducidad())
        );
        spinnerDuracionNoti.setValueFactory(
                new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, configActual.getDuracionSegundos())
        );
        // Inicializar el ToggleButton según la configuración guardada
        toggleNotificaciones.setSelected(configActual.isNotificacionesActivas());
        toggleNotificaciones.setText(toggleNotificaciones.isSelected() ? "Notificaciones activas" : "Notificaciones desactivadas");
        toggleNotificaciones.selectedProperty().addListener((_, __, newVal) -> {
            toggleNotificaciones.setText(newVal ? "Notificaciones activas" : "Notificaciones desactivadas");
        });
    }

    @FXML
    private void guardarConfigNotificaciones() {
        int minutos = spinnerMinutosNoti.getValue();
        int dias = spinnerDiasAnticipo.getValue();
        int diasCaducidad = spinnerDiasAnticipoCaducidad.getValue();
        int duracion = spinnerDuracionNoti.getValue();
        boolean notificacionesActivas = toggleNotificaciones.isSelected();
        configActual.setMinutos(minutos);
        configActual.setDiasAnticipacion(dias);
        configActual.setDiasAnticipacionCaducidad(diasCaducidad);
        configActual.setDuracionSegundos(duracion);
        configActual.setNotificacionesActivas(notificacionesActivas);
        configDAO.saveOrUpdateGlobal(configActual);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Configuración guardada");
        alert.setHeaderText(null);
        alert.setContentText("Los valores de notificación han sido actualizados y guardados.");
        alert.showAndWait();
    }
}
