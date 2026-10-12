package com.example;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Cambia la vista mostrada en la ventana principal. Cada aplicación tiene una sola vista tras el login. */
public final class Navigator {

    private static Stage stage;
    private static String vistaInicial;

    private Navigator() {}

    /**
     * @param primaryStage ventana principal
     * @param vistaPostLogin FXML que se muestra después de iniciar sesión, p. ej. "Caja.fxml"
     */
    public static void init(Stage primaryStage, String vistaPostLogin) {
        stage = primaryStage;
        vistaInicial = vistaPostLogin;
    }

    public static void mostrarLogin() {
        show("Login.fxml");
    }

    /** Pasa de la pantalla de login a la vista de esta aplicación. */
    public static void mostrarVistaInicial() {
        show(vistaInicial);
    }

    /** @param fxml nombre del archivo en resources, p. ej. "Caja.fxml" */
    private static void show(String fxml) {
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
