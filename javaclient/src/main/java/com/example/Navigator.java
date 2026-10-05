package com.example;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Cambia la vista mostrada en la ventana principal. */
public final class Navigator {

    private static Stage stage;

    private Navigator() {}

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    /** @param fxml nombre del archivo en resources, p. ej. "Caja.fxml" */
    public static void show(String fxml) {
        try {
            Parent root = FXMLLoader.load(Navigator.class.getResource("/" + fxml));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Navigator.class.getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar " + fxml, e);
        }
    }
}
