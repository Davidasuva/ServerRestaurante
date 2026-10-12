package com.example;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/** Base de las tres aplicaciones: carga fuentes, muestra el login y luego la vista propia de cada una. */
public abstract class AppBase extends Application {

    /** Título de la ventana. */
    protected abstract String titulo();

    /** FXML que se muestra tras el login, p. ej. "Caja.fxml". */
    protected abstract String vista();

    @Override
    public void start(Stage stage) {
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Regular.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Bold.ttf"), 12);

        Navigator.init(stage, vista());
        stage.setTitle(titulo());
        Navigator.mostrarLogin();
        stage.show();
    }
}
