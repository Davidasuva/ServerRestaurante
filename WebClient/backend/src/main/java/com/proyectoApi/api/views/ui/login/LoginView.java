package com.proyectoApi.api.views.ui.login;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginView {

    private LoginView() {
    }

    public static void show(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(LoginView.class.getResource("/login/LoginView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 720, 480);
        URL cssURL = LoginView.class.getResource("/login/LoginView.css");
        if (cssURL != null) {
            scene.getStylesheets().add(cssURL.toExternalForm());
        }
        stage.setTitle("Restaurante API - Iniciar Sesión");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }
}
