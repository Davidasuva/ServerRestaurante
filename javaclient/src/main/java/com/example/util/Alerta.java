package com.example.util;

import javafx.scene.control.Alert;
import javafx.stage.Window;

/** Diálogos con el estilo de la aplicación. */
public final class Alerta {

    private Alerta() {}

    public static void informacion(Window dueno, String titulo, String texto) {
        mostrar(Alert.AlertType.INFORMATION, dueno, titulo, texto);
    }

    public static void error(Window dueno, String titulo, String texto) {
        mostrar(Alert.AlertType.ERROR, dueno, titulo, texto);
    }

    private static void mostrar(Alert.AlertType tipo, Window dueno, String titulo, String texto) {
        Alert alerta = new Alert(tipo);
        if (dueno != null) {
            alerta.initOwner(dueno);
        }
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setGraphic(null);
        alerta.setContentText(texto);
        alerta.getDialogPane().getStylesheets().add(Alerta.class.getResource("/styles.css").toExternalForm());
        alerta.showAndWait();
    }
}
