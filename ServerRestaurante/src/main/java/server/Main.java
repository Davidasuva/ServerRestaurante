package server;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import server.factory.ServerFactory;
import server.model.ServerModel;
import server.model.empleado.Empleado;

public class Main extends Application {

    private ServerModel server;

    @Override
    public void start(Stage primaryStage) {
        server = ServerFactory.crearServer();

        Parent login = ServerFactory.crearLoginView(
                server,
                empleado -> abrirVistaPrincipal(primaryStage, empleado));

        primaryStage.setTitle("Servidor Restaurante");
        primaryStage.setScene(new Scene(login));
        primaryStage.show();
    }

    // TEMPORAL: reemplázalo por la vista real (pedir a la factory la vista que quieras)
    private void abrirVistaPrincipal(Stage stage, Empleado empleado) {
        Label mensaje = new Label("Sesión iniciada como " + empleado.getNombre());
        stage.setScene(new Scene(new StackPane(mensaje), 500, 400));
    }

    @Override
    public void stop() {
        // Cuando tengas el botón de apagar, aquí llamas a server.stop().
        // System.exit es necesario porque EmpleadoService queda exportado por RMI
        // y eso mantiene viva la JVM aunque cierres la ventana.
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
