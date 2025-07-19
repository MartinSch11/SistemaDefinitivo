package controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
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
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.control.CustomMenuItem;

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

        // Registrar un único log de inicio de sesión with valores válidos
        ActionLogger.log("El usuario ha iniciado sesión.");

        // Actualizar la interfaz con los valores validados
        setUserNameAndRole(userName, roleName, null);
        configurarPermisos(session.getPermisos());

        // Cargar configuración de notificaciones desde la base de datos usando el método uniforme
        configNotificaciones = configDAO.findOrDefault();
        int minutos = configNotificaciones.getMinutos();
        int diasAnticipo = configNotificaciones.getDiasAnticipacion();
        boolean notificacionesActivas = configNotificaciones.isNotificacionesActivas();
        // Mostrar notificación solo una vez al inicio de sesión
        if (notificacionesActivas && !notificacionMostradaAlInicio) {
            mostrarNotificacionesEventos(
                    configNotificaciones.getDiasAnticipacion(),
                    configNotificaciones.getDiasAnticipacionCaducidad()
            );
            notificacionMostradaAlInicio = true;
        }
        // Iniciar timer para mostrar notificaciones según configuración
        if (notificacionTimer == null) {
            notificacionTimer = new Timeline(
                    new KeyFrame(Duration.minutes(minutos), e -> {
                        // Recargar la configuración por si fue modificada
                        configNotificaciones = configDAO.findOrDefault();
                        if (configNotificaciones.isNotificacionesActivas()) {
                            mostrarNotificacionesEventos(
                                    configNotificaciones.getDiasAnticipacion(),
                                    configNotificaciones.getDiasAnticipacionCaducidad()
                            );
                        }
                    })
            );
            notificacionTimer.setCycleCount(Timeline.INDEFINITE);
            notificacionTimer.play();
        }

        // Reiniciar el timer de notificaciones con la configuración más reciente
        reiniciarTimerNotificaciones();

        // Crear el ContextMenu de ajustes
        ajustesMenu = new ContextMenu();
        ajustesMenu.getStyleClass().add("context-menu-ajustes");
        // Crear CustomMenuItem para cada opción con ícono que cambia color en hover/focus
        CustomMenuItem itemConfig = crearCustomMenuItem("Configuración", "/com.example.image/settings-negro.png", "/com.example.image/settings.png", this::handleSettings);
        CustomMenuItem itemAyuda = crearCustomMenuItem("Ayuda", "/com.example.image/help-negro.png", "/com.example.image/help-blanco.png", this::handleAyuda); // Usa el mismo ícono si no hay versión blanca
        CustomMenuItem itemCerrarSesion = crearCustomMenuItem("Cerrar Sesión", "/com.example.image/logout-negro.png", "/com.example.image/logout-blanco.png", this::changeUser); // Usa el mismo ícono si no hay versión blanca
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
        int diasEventos = configNotificaciones != null ? configNotificaciones.getDiasAnticipacion() : 3;
        int diasCaducidad = configNotificaciones != null ? configNotificaciones.getDiasAnticipacionCaducidad() : 3;
        mostrarNotificacionesEventos(diasEventos, diasCaducidad);
    }

    private int getToastDurationMs() {
        // Siempre obtener la configuración más reciente
        NotificacionConfig config = configDAO.findOrDefault();
        int duracionSegundos = config.getDuracionSegundos();
        return duracionSegundos > 0 ? duracionSegundos * 1000 : 5000;
    }

    private void mostrarNotificacionesEventos(int diasEventos, int diasCaducidad) {
        if (toastVisible) return; // No superponer toasts
        NotificacionService notiService = new NotificacionService();
        List<Notificacion> notificaciones = notiService.obtenerNotificacionesEventosYCaducidad(diasEventos, diasCaducidad);
        if (!notificaciones.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Notificacion n : notificaciones) {
                sb.append("● ").append(n.getMensaje());
                if (n.getFechaEvento() != null) {
                    sb.append(" (")
                            .append(n.getFechaEvento().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                            .append(")");
                }
                sb.append("\n\n"); // Espacio extra entre notificaciones
            }
            String hora = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            System.out.println("[NOTIFICACIÓN] Toast mostrado a las " + hora);
            mostrarToast(sb.toString().trim(), getToastDurationMs());
        }
    }

    public void setUserNameAndRole(String userName, String roleName, String sexo) {
        lblRol.setText(roleName); // Muestra el rol en el Label
        String saludo;
        // Si no se pasa sexo, tomarlo de SessionContext
        if (sexo == null) {
            sexo = SessionContext.getInstance().getSexo();
        }
        if (sexo != null) {
            if (sexo.equalsIgnoreCase("Femenino")) {
                saludo = "¡Bienvenida, ";
            } else if (sexo.equalsIgnoreCase("Masculino")) {
                saludo = "¡Bienvenido, ";
            } else {
                saludo = "¡Bienvenid@, ";
            }
        } else {
            saludo = "¡Bienvenid@, ";
        }
        lblBienvenida.setText(saludo + userName + "!"); // Mensaje personalizado
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
        toastProgressBar.setPrefWidth(300); // la barra es de 300px
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
        Button btn = (Button) event.getSource();
        if (ajustesMenu.isShowing()) {
            ajustesMenu.hide();
        } else {
            ajustesMenu.show(btn, javafx.geometry.Side.BOTTOM, 0, 0);
        }
    }

    // Oculta el MenuButton tras seleccionar una opción
    @FXML
    private void handleSettings(ActionEvent event) {
        // Verificar permisos antes de abrir la pantalla de configuración
        List<String> permisos = SessionContext.getInstance().getPermisos();
        String roleName = SessionContext.getInstance().getRoleName();
        boolean esAdmin = roleName != null && roleName.trim().equalsIgnoreCase("Administrador");
        if ((permisos != null && permisos.contains("Settings-ver")) || esAdmin) {
            ActionLogger.log("El usuario accedió a la sección de Configuración");
            ajustesMenu.hide();
            SceneLoader.loadScene(new NodeSceneStrategy(btnSettings), Paths.SETTINGS, "/css/components.css", false);
        } else {
            ajustesMenu.hide();
            mostrarToast("No tienes permiso para acceder a Configuración.", 3500);
        }
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
        if (notificacionTimer != null) notificacionTimer.stop();
        ActionLogger.log("Accedió a la sección de Eventos");
        SceneLoader.loadScene(new NodeSceneStrategy(btnEventos), Paths.EVENTOS, "/css/eventos.css", false);
    }

    @FXML
    void handleProductos(ActionEvent event) {
        if (notificacionTimer != null) notificacionTimer.stop();
        ActionLogger.log("Accedió a la sección de Productos");
        SceneLoader.loadScene(new NodeSceneStrategy(btnProductos), Paths.PRODUCTOS, "/css/productos.css", false);
    }


    @FXML
    void handleProveedores(ActionEvent event)  {
        if (notificacionTimer != null) notificacionTimer.stop();
        ActionLogger.log("Accedió a la sección de Proveedores");
        SceneLoader.loadScene(new NodeSceneStrategy(btnProveedores), Paths.PROVEEDORES, "/css/components.css", false);
    }

    @FXML
    void handleEstadistica(ActionEvent event) {
        if (notificacionTimer != null) notificacionTimer.stop();
        ActionLogger.log("Accedió a la sección de Estadísticas");
        SceneLoader.loadScene(new NodeSceneStrategy(btnEstadistica), Paths.ESTADISTICA, "/css/components.css", false);
    }

    @FXML
    void handleStock(ActionEvent event) {
        if (notificacionTimer != null) notificacionTimer.stop();
        ActionLogger.log("Accedió a la sección de Stock");
        SceneLoader.loadScene(new NodeSceneStrategy(btnStock), Paths.STOCK, "/css/components.css", false);
    }

    @FXML
    void handleRecetas(ActionEvent event){
        if (notificacionTimer != null) notificacionTimer.stop();
        ActionLogger.log("Accedió a la sección de Recetas");
        SceneLoader.loadScene(new NodeSceneStrategy(btnRecetas), Paths.RECETAS, "/css/components.css", false);
    }

    @FXML
    void viewAgenda(ActionEvent event) {
        if (notificacionTimer != null) notificacionTimer.stop();
        ActionLogger.log("Accedió a la sección de Agenda");
        SceneLoader.loadScene(new NodeSceneStrategy(btnAgenda), Paths.AGENDA, "/css/components.css", false);
    }

    @FXML
    void handleTablero(ActionEvent event){
        if (notificacionTimer != null) notificacionTimer.stop();
        ActionLogger.log("Accedió a la sección de Pedidos");
        SceneLoader.loadScene(new NodeSceneStrategy(btnPedidos), Paths.PEDIDOS, "/css/components.css", false);
    }

    /**
     * Crea un CustomMenuItem con ícono que cambia color en hover/focus.
     */
    private CustomMenuItem crearCustomMenuItem(String texto, String iconoNormal, String iconoHover, javafx.event.EventHandler<ActionEvent> handler) {
        ImageView iconView = new ImageView(new Image(getClass().getResource(iconoNormal).toExternalForm(), 18, 18, true, true));
        Label label = new Label(texto);
        HBox hbox = new HBox(8, iconView, label);
        hbox.setStyle("-fx-padding: 4 12 4 8; -fx-alignment: center-left;");
        hbox.setMinWidth(180);
        hbox.setPrefWidth(180);
        CustomMenuItem item = new CustomMenuItem(hbox);
        item.setOnAction(handler);
        hbox.setOnMouseEntered(e -> {
            iconView.setImage(new Image(getClass().getResource(iconoHover).toExternalForm(), 18, 18, true, true));
        });
        hbox.setOnMouseExited(e -> {
            iconView.setImage(new Image(getClass().getResource(iconoNormal).toExternalForm(), 18, 18, true, true));
        });
        return item;
    }

    /**
     * Reinicia el timer de notificaciones con la configuración más reciente.
     */
    public void reiniciarTimerNotificaciones() {
        if (notificacionTimer != null) {
            notificacionTimer.stop();
            notificacionTimer = null;
        }
        configNotificaciones = configDAO.findOrDefault();
        int minutos = configNotificaciones.getMinutos();
        notificacionTimer = new Timeline(
                new KeyFrame(Duration.minutes(minutos), e -> {
                    configNotificaciones = configDAO.findOrDefault();
                    if (configNotificaciones.isNotificacionesActivas()) {
                        mostrarNotificacionesEventos(
                                configNotificaciones.getDiasAnticipacion(),
                                configNotificaciones.getDiasAnticipacionCaducidad()
                        );
                    }
                })
        );
        notificacionTimer.setCycleCount(Timeline.INDEFINITE);
        notificacionTimer.play();
    }

}
