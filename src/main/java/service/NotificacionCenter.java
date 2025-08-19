package service;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class NotificacionCenter {
    private static final NotificacionCenter INSTANCE = new NotificacionCenter();
    private final IntegerProperty unreadCount = new SimpleIntegerProperty(0);

    private NotificacionCenter() {}

    public static NotificacionCenter getInstance() {
        return INSTANCE;
    }

    public IntegerProperty unreadCountProperty() {
        return unreadCount;
    }

    public int getUnreadCount() {
        return unreadCount.get();
    }

    public void setUnreadCount(int value) {
        unreadCount.set(value);
    }
}
