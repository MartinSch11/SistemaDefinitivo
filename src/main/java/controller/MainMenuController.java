package controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import model.SessionContext;
import model.Notificacion;
import model.NotificacionConfig;
import service.NotificacionService;
import utilities.*;
import persistence.dao.NotificacionConfigDAO;
import persistence.dao.EventoDAO;
import model.Evento;
import java.time.LocalDate;

import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class MainMenuController {

    @FXML private Button btnProductos;
    @FXML private Button btnEventos;
    @FXML private Button btnPedidos;
    @FXML private Button btnProveedores;
    @FXML private Button btnEstadistica;
    @FXML private Button btnStock;
    @FXML private Button btnRecetas;
    @FXML private Button btnAgenda;
    @FXML private Label lblRol;
    @FXML private Label lblBienvenida; // Nueva etiqueta para el mensaje de bienvenida
    @FXML private AnchorPane toastNotification;
    @FXML private Label toastMessage;
    @FXML private Pane toastProgressBar;
    @FXML private Button btnSettings;

    private boolean toastVisible = false;
    private final int TOAST_DURATION_MS = 5000;
    private static boolean notificacionMostradaAlInicio = false;
    private static Timeline notificacionTimer;

    private NotificacionConfigDAO configDAO = new NotificacionConfigDAO();
    private NotificacionConfig configNotificaciones;
    private ContextMenu ajustesMenu;

    public void initialize() {
        // Asegurarse de que el nombre de usuario y el rol estén establecidos
        SessionContext session = SessionContext.getInstance();

        // Verificar que los valores no sean null
        String userName = session.getUserName() != null ? session.getUserName() : "Desconocido";
        String roleName = session.getRoleName() != null ? session.getRoleName() : "Sin rol";

        // Registrar un único log de inicio de sesión con valores válidos
        ActionLogger.log("El usuario ha iniciado sesión.");

        // Actualizar la interfaz con los valores validados
        setUserNameAndRole(userName, roleName);
        configurarPermisos(session.getPermisos());

        // Cargar configuración de notificaciones desde la base de datos usando el método uniforme
        configNotificaciones = configDAO.findOrDefault();
        int minutos = configNotificaciones.getMinutos();
        int diasAnticipo = configNotificaciones.getDiasAnticipacion();
        // Mostrar notificación solo una vez al inicio de sesión
        if (!notificacionMostradaAlInicio) {
            mostrarNotificacionesEventos(diasAnticipo);
            notificacionMostradaAlInicio = true;
        }
        // Iniciar timer para mostrar notificaciones según configuración
        if (notificacionTimer == null) {
            notificacionTimer = new Timeline(
                    new KeyFrame(Duration.minutes(minutos), e -> {
                        // Recargar la configuración por si fue modificada
                        configNotificaciones = configDAO.findOrDefault();
                        mostrarNotificacionesEventos(configNotificaciones.getDiasAnticipacion());
                    })
            );
            notificacionTimer.setCycleCount(Timeline.INDEFINITE);
            notificacionTimer.play();
        }

        // Crear el ContextMenu de ajustes
        ajustesMenu = new ContextMenu();
        ajustesMenu.getStyleClass().add("context-menu-ajustes");
        MenuItem itemConfig = new MenuItem("Configuración");
        itemConfig.setOnAction(this::handleSettings);
        itemConfig.setGraphic(new javafx.scene.image.ImageView(new javafx.scene.image.Image(getClass().getResource("/com.example.image/settings-negro.png").toExternalForm(), 18, 18, true, true)));
        MenuItem itemAyuda = new MenuItem("Ayuda");
        itemAyuda.setOnAction(this::handleAyuda);
        itemAyuda.setGraphic(new javafx.scene.image.ImageView(new javafx.scene.image.Image(getClass().getResource("/com.example.image/help-negro.png").toExternalForm(), 18, 18, true, true)));
        MenuItem itemCerrarSesion = new MenuItem("Cerrar Sesión");
        itemCerrarSesion.setOnAction(this::changeUser);
        itemCerrarSesion.setGraphic(new javafx.scene.image.ImageView(new javafx.scene.image.Image(getClass().getResource("/com.example.image/logout-negro.png").toExternalForm(), 18, 18, true, true)));
        ajustesMenu.getItems().addAll(itemConfig, itemAyuda, itemCerrarSesion);

        // Actualizar estado de eventos pasados a 'Realizado' al iniciar la app
        EventoDAO eventoDAO = new EventoDAO();
        LocalDate hoy = LocalDate.now();
        for (Evento evento : eventoDAO.findAll()) {
            if (evento.getFecha_evento().isBefore(hoy) && !"Realizado".equals(evento.getEstado())) {
                evento.setEstado("Realizado");
                eventoDAO.update(evento);
            }
        }
        eventoDAO.close();
    }

    private void mostrarNotificacionesEventos() {
        mostrarNotificacionesEventos(configNotificaciones != null ? configNotificaciones.getDiasAnticipacion() : 3);
    }

    private void mostrarNotificacionesEventos(int diasAnticipo) {
        if (toastVisible) return; // No superponer toasts
        NotificacionService notiService = new NotificacionService();
        List<Notificacion> notificaciones = notiService.obtenerNotificacionesEventosProximos(diasAnticipo);
        if (!notificaciones.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Notificacion n : notificaciones) {
                sb.append("● ").append(n.getMensaje());
                if (n.getFechaEvento() != null) {
                    sb.append(" (")
                            .append(n.getFechaEvento().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                            .append(")");
                }
                sb.append("\n");
            }
            String hora = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            System.out.println("[NOTIFICACIÓN] Toast mostrado a las " + hora);
            mostrarToast(sb.toString().trim(), TOAST_DURATION_MS);
        }
    }

    public void setUserNameAndRole(String userName, String roleName) {
        lblRol.setText(roleName);     // Muestra el rol en el Label
        lblBienvenida.setText("¡Bienvenid@, " + userName + "!"); // Mensaje personalizado
    }

    public void configurarPermisos(List<String> permisos) {
        if (permisos == null) {
            permisos = new ArrayList<>();
        }
        btnEstadistica.setDisable(!permisos.contains("Estadísticas-ver"));
        btnEventos.setDisable(!permisos.contains("Eventos-ver"));
        btnPedidos.setDisable(!permisos.contains("Pedidos-ver"));
        btnRecetas.setDisable(!permisos.contains("Recetas-ver"));
        btnProductos.setDisable(!permisos.contains("Productos-ver"));
        btnProveedores.setDisable(!permisos.contains("Proveedores-ver"));
        btnStock.setDisable(!permisos.contains("Stock-ver"));
        btnAgenda.setDisable(!permisos.contains("Agenda-ver"));
        // Ya no se controla btnSettingsMenu ni btnAyuda aquí
    }

    /**
     * Muestra un toast animado en la esquina inferior derecha con barra de tiempo.
     * @param mensaje Mensaje a mostrar
     * @param duracionMs Duración en milisegundos
     */
    public void mostrarToast(String mensaje, int duracionMs) {
        if (toastVisible) return; // Evita superponer toasts
        toastVisible = true;
        toastMessage.setText(mensaje);
        toastNotification.setVisible(true);
        toastProgressBar.setPrefWidth(300); // Ahora la barra es de 300px
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(toastProgressBar.prefWidthProperty(), 300)),
                new KeyFrame(Duration.millis(duracionMs), new KeyValue(toastProgressBar.prefWidthProperty(), 0))
        );
        timeline.setOnFinished(e -> {
            toastNotification.setVisible(false);
            toastVisible = false;
        });
        timeline.play();
    }

    /**
     * Muestra el MenuButton de ajustes (adminLogin) al hacer clic en la tuerca.
     * Sincroniza la visibilidad y permisos según el usuario.
     */
    @FXML
    public void mostrarMenuAjustes(ActionEvent event) {
        // Verifica permisos antes de mostrar el menú
        List<String> permisos = SessionContext.getInstance().getPermisos();
        boolean puedeVerAjustes = permisos != null && permisos.contains("Settings-ver");
        if (puedeVerAjustes) {
            Button btn = (Button) event.getSource();
            ajustesMenu.show(btn, javafx.geometry.Side.BOTTOM, 0, 0);
        }
    }

    // Oculta el MenuButton tras seleccionar una opción
    @FXML
    private void handleSettings(ActionEvent event) {
        ActionLogger.log("El usuario accedió a la sección de Configuración");
        ajustesMenu.hide();
        SceneLoader.loadScene(new NodeSceneStrategy(btnSettings), Paths.SETTINGS, "/css/components.css", false);
    }
    @FXML
    private void handleAyuda(ActionEvent event) {
        ActionLogger.log("El usuario accedió a la sección de Ayuda");
        ajustesMenu.hide();
        SceneLoader.loadScene(new NodeSceneStrategy(btnSettings), Paths.AYUDA, "/css/components.css", false);
    }
    @FXML
    private void changeUser(ActionEvent event) {
        ActionLogger.log("El usuario cerró sesión");
        ajustesMenu.hide();
        SceneLoader.loadScene(new NodeSceneStrategy(btnSettings), Paths.LOGIN, "/css/components.css", false);
    }

    @FXML
    void handleEventos(ActionEvent event) {
        ActionLogger.log("Accedió a la sección de Eventos");
        SceneLoader.loadScene(new NodeSceneStrategy(btnEventos), Paths.EVENTOS, "/css/eventos.css", false);
    }

    @FXML
    void handleProductos(ActionEvent event) {
        ActionLogger.log("Accedió a la sección de Productos");
        SceneLoader.loadScene(new NodeSceneStrategy(btnProductos), Paths.PRODUCTOS, "/css/productos.css", false);
    }


    @FXML
    void handleProveedores(ActionEvent event)  {
        ActionLogger.log("Accedió a la sección de Proveedores");
        SceneLoader.loadScene(new NodeSceneStrategy(btnProveedores), Paths.PROVEEDORES, "/css/components.css", false);
    }

    @FXML
    void handleEstadistica(ActionEvent event) {
        ActionLogger.log("Accedió a la sección de Estadísticas");
        SceneLoader.loadScene(new NodeSceneStrategy(btnEstadistica), Paths.ESTADISTICA, "/css/components.css", false);
    }

    @FXML
    void handleStock(ActionEvent event) {
        ActionLogger.log("Accedió a la sección de Stock");
        SceneLoader.loadScene(new NodeSceneStrategy(btnStock), Paths.STOCK, "/css/components.css", false);
    }

    @FXML
    void handleRecetas(ActionEvent event){
        ActionLogger.log("Accedió a la sección de Recetas");
        SceneLoader.loadScene(new NodeSceneStrategy(btnRecetas), Paths.RECETAS, "/css/components.css", false);
    }

    @FXML
    void viewAgenda(ActionEvent event) {
        ActionLogger.log("Accedió a la sección de Agenda");
        SceneLoader.loadScene(new NodeSceneStrategy(btnAgenda), Paths.AGENDA, "/css/components.css", false);
    }

    @FXML
    void handleTablero(ActionEvent event){
        ActionLogger.log("Accedió a la sección de Pedidos");
        SceneLoader.loadScene(new NodeSceneStrategy(btnPedidos), Paths.PEDIDOS, "/css/components.css", false);
    }

}
