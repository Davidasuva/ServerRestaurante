package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

@Override
public void start(Stage stage) throws Exception {
    Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Regular.ttf"), 14);
    Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Bold.ttf"), 14);

    Parent root = FXMLLoader.load(getClass().getResource("/Caja.fxml"));
    Scene scene = new Scene(root);
    scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    stage.setScene(scene);
    stage.show();
}
    public static void main(String[] args) {
        launch(args);
    }
}