package server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import server.database.Database;
import server.factory.ServerFactory;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        ServerFactory.start(stage);
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Aplicación cerrada — liberando puertos RMI.");
        Database.closeConnection();
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}