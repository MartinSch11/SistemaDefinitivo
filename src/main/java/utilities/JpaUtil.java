package utilities;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.fxml.FXML;
import javafx.scene.control.TextFormatter;
import lombok.Getter;

public class JpaUtil {

    private static final String PERSISTENCE_UNIT_NAME = "pasteleriaPU";
    @Getter
    private static EntityManagerFactory entityManagerFactory;

    static {
        try {
            entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
}