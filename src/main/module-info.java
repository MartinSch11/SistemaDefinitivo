module pasteleria {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens controller to javafx.fxml;
    exports controller;
    exports application;
    opens application to javafx.fxml;
}