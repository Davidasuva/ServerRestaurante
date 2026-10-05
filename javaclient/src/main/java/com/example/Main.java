package com.example;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Regular.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Bold.ttf"), 12);

        Navigator.init(stage);
        stage.setTitle("Carbón y Sazón");
        Navigator.show("Login.fxml");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
