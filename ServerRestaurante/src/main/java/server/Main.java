package server;

import javafx.application.Application;
import javafx.stage.Stage;
import server.view.console.ConsoleView;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        new ConsoleView(primaryStage).mostrar();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

