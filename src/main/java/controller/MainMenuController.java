package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import model.SessionContext;
import model.NotificacionEntity;
import utilities.*;
import persistence.dao.NotificacionEntityDAO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.CustomMenuItem;
import javafx.stage.Popup;
import javafx.geometry.Bounds;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

public class MainMenuController {

    @FXML
    private Button btnProductos;
    @FXML
    private Button btnEventos;
    @FXML
    private Button btnPedidos;
    @FXML
    private Button btnProveedores;
    @FXML
    private Button btnEstadistica;
    @FXML
    private Button btnStock;
    @FXML
    private Button btnRecetas;
    @FXML
    private Button btnAgenda;
    @FXML
    private Label lblRol;
    @FXML
    private Label lblBienvenida;
    @FXML
    private Button btnSettings;
    @FXML
    private Button btnNotificaciones;
    @FXML
    private Label lblContadorNotificaciones;

    private ScheduledService<Integer> badgeRefresher; // refresca el contador del ícono
    private Popup popupNotificaciones;
    private final NotificacionEntityDAO notiDAO = new NotificacionEntityDAO();
    private ContextMenu ajustesMenu;

    public void initialize() {
        // Asegurarse de que el nombre de usuario y el rol estén establecidos
        SessionContext session = SessionContext.getInstance();

        String userName = session.getUserName() != null ? session.getUserName() : "Desconocido";
        String roleName = session.getRoleName() != null ? session.getRoleName() : "Sin rol";

        ActionLogger.log("El usuario ha iniciado sesión.");

        setUserNameAndRole(userName, roleName, null);
        configurarPermisos(session.getPermisos());

        // ContextMenu ajustes
        ajustesMenu = new ContextMenu();
        ajustesMenu.getStyleClass().add("context-menu-ajustes");
        CustomMenuItem itemConfig = crearCustomMenuItem("Configuración", "/com.example.image/settings-negro.png",
                "/com.example.image/settings.png", this::handleSettings);
        CustomMenuItem itemAyuda = crearCustomMenuItem("Ayuda", "/com.example.image/help-negro.png",
                "/com.example.image/help-blanco.png", this::handleAyuda);
        CustomMenuItem itemCerrarSesion = crearCustomMenuItem("Cerrar Sesión", "/com.example.image/logout-negro.png",
                "/com.example.image/logout-blanco.png", this::changeUser);
        ajustesMenu.getItems().addAll(itemConfig, itemAyuda, itemCerrarSesion);

        // 👉 Suscribirse al bus para actualizar el badge al instante
        service.NotificacionCenter.getInstance().unreadCountProperty().addListener((_, _, newV) -> {
            int cant = (newV == null) ? 0 : newV.intValue();
            actualizarBadge(cant);
        });
        // set inicial por si el scheduler ya corrió antes de abrir esta vista
        actualizarBadge(service.NotificacionCenter.getInstance().getUnreadCount());

        // además, actualizamos una vez y arrancamos refresco ligero del contador
        // (respaldo)
        actualizarContadorNotificaciones();
        startBadgeRefresher();

        // opcional: que no ocupe espacio cuando esté oculto
        lblContadorNotificaciones.managedProperty().bind(lblContadorNotificaciones.visibleProperty());
    }

    private void actualizarContadorNotificaciones() {
        List<NotificacionEntity> notificaciones = notiDAO.findNoLeidas(); // CAMBIO AQUÍ
        int cantidad = notificaciones.size();

        if (cantidad > 0) {
            lblContadorNotificaciones.setText(String.valueOf(cantidad));
            lblContadorNotificaciones.setVisible(true);
        } else {
            lblContadorNotificaciones.setVisible(false);
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

        // Detener refrescos
        stopBadgeRefresher();
        service.NotificacionScheduler.getInstance().stop();

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
    void handleProveedores(ActionEvent event) {
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
    void handleRecetas(ActionEvent event) {
        ActionLogger.log("Accedió a la sección de Recetas");
        SceneLoader.loadScene(new NodeSceneStrategy(btnRecetas), Paths.RECETAS, "/css/components.css", false);
    }

    @FXML
    void viewAgenda(ActionEvent event) {
        ActionLogger.log("Accedió a la sección de Agenda");
        SceneLoader.loadScene(new NodeSceneStrategy(btnAgenda), Paths.AGENDA, "/css/components.css", false);
    }

    @FXML
    void handleTablero(ActionEvent event) {
        ActionLogger.log("Accedió a la sección de Pedidos");
        SceneLoader.loadScene(new NodeSceneStrategy(btnPedidos), Paths.PEDIDOS, "/css/components.css", false);
    }

    /**
     * Crea un CustomMenuItem con ícono que cambia color en hover/focus.
     */
    private CustomMenuItem crearCustomMenuItem(String texto, String iconoNormal, String iconoHover,
            javafx.event.EventHandler<ActionEvent> handler) {
        ImageView iconView = new ImageView(
                new Image(getClass().getResource(iconoNormal).toExternalForm(), 18, 18, true, true));
        Label label = new Label(texto);
        HBox hbox = new HBox(8, iconView, label);
        hbox.setStyle("-fx-padding: 4 12 4 8; -fx-alignment: center-left;");
        hbox.setMinWidth(180);
        hbox.setPrefWidth(180);
        CustomMenuItem item = new CustomMenuItem(hbox);
        item.setOnAction(handler);
        hbox.setOnMouseEntered(_ -> {
            iconView.setImage(new Image(getClass().getResource(iconoHover).toExternalForm(), 18, 18, true, true));
        });
        hbox.setOnMouseExited(_ -> {
            iconView.setImage(new Image(getClass().getResource(iconoNormal).toExternalForm(), 18, 18, true, true));
        });
        return item;
    }

    @FXML
    private void handleNotificationClick(ActionEvent event) {
        if (popupNotificaciones != null && popupNotificaciones.isShowing()) {
            popupNotificaciones.hide();
            return;
        }

        List<NotificacionEntity> todas = notiDAO.findAll();

        VBox contenedorNotis = new VBox(10);
        contenedorNotis.setPadding(new Insets(10));
        contenedorNotis.setPrefWidth(340);
        contenedorNotis.getStyleClass().add("popup-notificaciones");
        contenedorNotis.setStyle("-fx-background-color: white;");

        // Título con botón "more"
        Label titulo = new Label("Notificaciones");
        titulo.getStyleClass().add("titulo");

        ImageView iconoMore = new ImageView(new Image(
                getClass().getResource("/com.example.image/more-black.png").toExternalForm(), 16, 16, true, true));
        Button btnMoreTitulo = new Button();
        btnMoreTitulo.setGraphic(iconoMore);
        btnMoreTitulo.setPrefSize(24, 24);
        btnMoreTitulo.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        btnMoreTitulo.setOnMouseEntered(_ -> btnMoreTitulo.setStyle(
                "-fx-background-color: #eee; -fx-cursor: hand; -fx-background-radius: 8px;"));
        btnMoreTitulo.setOnMouseExited(_ -> btnMoreTitulo.setStyle(
                "-fx-background-color: transparent; -fx-cursor: hand;"));

        ContextMenu menuTitulo = new ContextMenu();
        MenuItem marcarTodas = new MenuItem("Marcar todas como leídas");
        marcarTodas.setOnAction(_ -> {
            notiDAO.marcarTodasComoLeidas();
            actualizarContadorNotificaciones();
            handleNotificationClick(null); // recargar
        });

        MenuItem eliminarLeidas = new MenuItem("Eliminar leídas");
        eliminarLeidas.setOnAction(_ -> {
            notiDAO.eliminarLeidas();
            actualizarContadorNotificaciones();
            handleNotificationClick(null); // recargar
        });

        menuTitulo.getItems().addAll(marcarTodas, eliminarLeidas);
        btnMoreTitulo.setOnAction(_ -> menuTitulo.show(btnMoreTitulo, javafx.geometry.Side.BOTTOM, 0, 0));

        HBox header = new HBox();
        header.setSpacing(10);
        header.setPadding(new Insets(0, 0, 5, 0));
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(titulo, Priority.ALWAYS);
        titulo.setMaxWidth(Double.MAX_VALUE);
        header.getChildren().addAll(titulo, btnMoreTitulo);

        // Botones de filtro
        Button btnTodas = new Button("Todas");
        Button btnNoLeidas = new Button("No leídas");

        btnTodas.getStyleClass().addAll("boton-filtro", "boton-filtro-activo");
        btnNoLeidas.getStyleClass().add("boton-filtro");

        HBox filtros = new HBox(10, btnTodas, btnNoLeidas);
        filtros.setPadding(new Insets(5, 0, 5, 0));

        contenedorNotis.getChildren().addAll(header, filtros);

        ScrollPane scroll = new ScrollPane(contenedorNotis);
        scroll.setPrefHeight(340);
        scroll.setFitToWidth(true);
        scroll.setStyle(
                "-fx-background: white; -fx-background-color: white; -fx-background-insets: 0; -fx-padding: 0;");

        VBox contenido = new VBox(scroll);
        contenido.setPadding(new Insets(10));
        contenido.setPrefWidth(340);
        contenido.setStyle(
                "-fx-background-color: white;-fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-color: #ccc; -fx-effect: dropshadow(one-pass-box, rgba(0, 0, 0, 0.15), 10, 0.1, 0, 4);");

        btnTodas.setOnAction(_ -> {
            actualizarEstilosFiltro(btnTodas, btnNoLeidas);
            mostrarNotificaciones(contenedorNotis, todas, 2);
        });

        btnNoLeidas.setOnAction(_ -> {
            actualizarEstilosFiltro(btnNoLeidas, btnTodas);
            mostrarNotificaciones(contenedorNotis, todas.stream().filter(n -> !n.isLeida()).toList(), 2);
        });

        mostrarNotificaciones(contenedorNotis, todas, 2);

        if (popupNotificaciones == null) {
            popupNotificaciones = new Popup();
            popupNotificaciones.setAutoHide(true);
        } else {
            popupNotificaciones.getContent().clear();
        }

        popupNotificaciones.getContent().add(contenido);
        Bounds bounds = btnNotificaciones.localToScreen(btnNotificaciones.getBoundsInLocal());
        double x = bounds.getMaxX() - 340;
        double y = bounds.getMaxY() + 5;

        popupNotificaciones.show(btnNotificaciones, x, y);
    }

    private void mostrarNotificaciones(VBox contenedor, List<NotificacionEntity> notificaciones, int desdeIndex) {
        contenedor.getChildren().remove(desdeIndex, contenedor.getChildren().size());

        LocalDate hoy = LocalDate.now();

        List<NotificacionEntity> hoyList = new ArrayList<>();
        List<NotificacionEntity> anterioresList = new ArrayList<>();
        List<NotificacionEntity> futurasList = new ArrayList<>();

        for (NotificacionEntity n : notificaciones) {
            if (n.getFecha() == null)
                continue;

            if (n.getFecha().isEqual(hoy)) {
                hoyList.add(n);
            } else if (n.getFecha().isBefore(hoy)) {
                anterioresList.add(n);
            } else if (n.getFecha().isAfter(hoy)) {
                futurasList.add(n);
            }
        }

        if (hoyList.isEmpty() && anterioresList.isEmpty() && futurasList.isEmpty()) {
            Label vacio = new Label("No hay notificaciones.");
            vacio.getStyleClass().add("mensaje");
            contenedor.getChildren().add(vacio);
            return;
        }

        if (!hoyList.isEmpty()) {
            contenedor.getChildren().add(separadorSeccion("Hoy"));
            hoyList.forEach(n -> contenedor.getChildren().add(crearNotificacionVisual(n)));
        }

        if (!futurasList.isEmpty()) {
            contenedor.getChildren().add(separadorSeccion("Próximas"));
            futurasList.forEach(n -> contenedor.getChildren().add(crearNotificacionVisual(n)));
        }

        if (!anterioresList.isEmpty()) {
            contenedor.getChildren().add(separadorSeccion("Anteriores"));

            int maxInicial = 3;
            List<NotificacionEntity> primeras = anterioresList.stream().limit(maxInicial).toList();
            List<NotificacionEntity> restantes = anterioresList.stream().skip(maxInicial).toList();

            primeras.forEach(n -> contenedor.getChildren().add(crearNotificacionVisual(n)));

            if (!restantes.isEmpty()) {
                Button verMas = new Button("Ver notificaciones anteriores");
                verMas.getStyleClass().add("btn-ver-mas-notis");

                VBox.setMargin(verMas, new Insets(10, 0, 0, 0));
                verMas.setMaxWidth(Double.MAX_VALUE);
                verMas.setAlignment(javafx.geometry.Pos.CENTER);

                verMas.setOnAction(_ -> {
                    restantes.forEach(n -> contenedor.getChildren().add(crearNotificacionVisual(n)));
                    contenedor.getChildren().remove(verMas);
                });

                contenedor.getChildren().add(verMas);
            }
        }
    }

    private Label separadorSeccion(String texto) {
        Label label = new Label(texto);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;");
        return label;
    }

    private HBox crearNotificacionVisual(NotificacionEntity n) {
        HBox fila = new HBox(10);
        fila.setPadding(new Insets(5));
        fila.setStyle("-fx-alignment: center-left;");

        VBox textoBox = new VBox(2);
        Label mensaje = new Label(n.getMensaje());
        mensaje.getStyleClass().addAll("mensaje");
        mensaje.setWrapText(true);

        Label fecha = new Label(n.getFecha() != null
                ? n.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "");
        fecha.getStyleClass().add("fecha");

        textoBox.getChildren().addAll(mensaje, fecha);
        textoBox.setPrefWidth(220);

        // ⋯ botón "more"
        ImageView icono = new ImageView(new Image(
                getClass().getResource("/com.example.image/more-black.png").toExternalForm(), 16, 16, true, true));
        Button btnMore = new Button();
        btnMore.setGraphic(icono);
        btnMore.setPrefSize(24, 24);
        btnMore.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        btnMore.setOnMouseEntered(
                _ -> btnMore.setStyle("-fx-background-color: #eee; -fx-cursor: hand; -fx-background-radius: 8px;"));
        btnMore.setOnMouseExited(
                _ -> btnMore.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"));

        // 🔴 punto rojo si no fue leída
        Circle punto = new Circle(5);
        punto.setFill(!n.isLeida() ? Color.web("#B70505") : Color.TRANSPARENT);

        ContextMenu menu = new ContextMenu();
        javafx.scene.control.MenuItem marcarLeida = new javafx.scene.control.MenuItem("Marcar como leída");
        marcarLeida.setDisable(n.isLeida());
        marcarLeida.setOnAction(_ -> {
            notiDAO.marcarComoLeida(n.getId());
            actualizarContadorNotificaciones();
            handleNotificationClick(null); // recargar
        });

        javafx.scene.control.MenuItem eliminar = new javafx.scene.control.MenuItem("Eliminar");
        eliminar.setOnAction(_ -> {
            notiDAO.delete(n.getId());
            actualizarContadorNotificaciones();
            handleNotificationClick(null); // recargar
        });

        menu.getItems().addAll(marcarLeida, eliminar);
        btnMore.setOnAction(_ -> menu.show(btnMore, javafx.geometry.Side.BOTTOM, 0, 0));

        HBox contenedorOpciones = new HBox(5, btnMore, punto);
        contenedorOpciones.setStyle("-fx-alignment: center;");

        fila.getChildren().addAll(textoBox, contenedorOpciones);
        return fila;
    }

    private void actualizarEstilosFiltro(Button activo, Button inactivo) {
        activo.getStyleClass().remove("boton-filtro");
        activo.getStyleClass().add("boton-filtro-activo");

        inactivo.getStyleClass().remove("boton-filtro-activo");
        if (!inactivo.getStyleClass().contains("boton-filtro")) {
            inactivo.getStyleClass().add("boton-filtro");
        }
    }

    private void startBadgeRefresher() {
        if (badgeRefresher != null)
            return;

        badgeRefresher = new ScheduledService<>() {
            @Override
            protected Task<Integer> createTask() {
                return new Task<>() {
                    @Override
                    protected Integer call() {
                        // consulta rápida del contador (DB liviana)
                        return notiDAO.findNoLeidas().size();
                    }
                };
            }
        };
        badgeRefresher.setPeriod(Duration.seconds(20)); // ⏱️ ajustá el intervalo
        badgeRefresher.setRestartOnFailure(true);
        badgeRefresher.setOnSucceeded(ev -> {
            Integer cant = (Integer) ev.getSource().getValue();
            if (cant != null)
                actualizarBadge(cant);
        });
        badgeRefresher.start();
    }

    private void stopBadgeRefresher() {
        if (badgeRefresher != null) {
            badgeRefresher.cancel();
            badgeRefresher = null;
        }
    }

    private void actualizarBadge(int cantidad) {
        if (cantidad > 0) {
            lblContadorNotificaciones.setText(String.valueOf(cantidad));
            lblContadorNotificaciones.setVisible(true);
        } else {
            lblContadorNotificaciones.setVisible(false);
        }
    }

}
