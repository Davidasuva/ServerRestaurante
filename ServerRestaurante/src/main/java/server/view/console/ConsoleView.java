package server.view.console;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import server.factory.ServerFactory;
import server.model.ServerModel;
import server.model.history.Action;
import server.model.observer.Observer;

import java.util.List;

/**
 * Vista de consola sencilla para el servidor:
 * un botón para iniciar el servidor y, debajo, un log en vivo
 * del History (usando el Observer que ya tiene el proyecto).
 */
public class ConsoleView {

    private final Stage stage;
    private ServerModel serverModel;

    private Button btnIniciar;
    private Label lblEstado;
    private TextArea txtConsola;

    public ConsoleView(Stage stage) {
        this.stage = stage;
    }

    public void mostrar() {
        btnIniciar = new Button("Iniciar servidor");
        btnIniciar.setOnAction(e -> iniciarServidor());

        lblEstado = new Label("Detenido");
        lblEstado.setTextFill(Color.FIREBRICK);

        HBox top = new HBox(15, btnIniciar, lblEstado);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(10));

        txtConsola = new TextArea();
        txtConsola.setEditable(false);
        txtConsola.setWrapText(true);
        txtConsola.setStyle(
                "-fx-control-inner-background:#1e1e1e;" +
                        "-fx-text-fill:#d4d4d4;" +
                        "-fx-font-family:'Consolas','Monospaced';" +
                        "-fx-font-size:12px;"
        );

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(txtConsola);
        BorderPane.setMargin(txtConsola, new Insets(0, 10, 10, 10));

        stage.setTitle("ServerRestaurante - Consola");
        stage.setScene(new Scene(root, 640, 420));
        stage.setOnCloseRequest(e -> detenerServidor());
        stage.show();
    }

    private void iniciarServidor() {
        if (serverModel != null) {
            return; // ya está corriendo, evita doble despliegue
        }
        btnIniciar.setDisable(true);

        serverModel = ServerFactory.crearServer();

        // Nos suscribimos al History: cada addAction() dispara notifyObservers()
        // y aquí refrescamos la consola (siempre en el hilo de JavaFX).
        new Observer(serverModel.getHistory()) {
            @Override
            public void update() {
                Platform.runLater(ConsoleView.this::refrescarConsola);
            }
        };
        refrescarConsola(); // muestra el mensaje inicial del ServerModel

        // deploy() hace I/O de red (RMI), así que no puede ir en el hilo de UI.
        new Thread(() -> {
            boolean ok = serverModel.deploy();
            Platform.runLater(() -> {
                lblEstado.setText(ok ? "Corriendo" : "Error al iniciar");
                lblEstado.setTextFill(ok ? Color.SEAGREEN : Color.FIREBRICK);
                if (!ok) {
                    btnIniciar.setDisable(false);
                    serverModel = null;
                }
            });
        }, "server-deploy-thread").start();
    }

    private void detenerServidor() {
        if (serverModel != null) {
            serverModel.stop();
        }
    }

    private void refrescarConsola() {
        List<Action> acciones = serverModel.getHistory().getActions();
        StringBuilder sb = new StringBuilder();
        for (Action a : acciones) {
            sb.append(a.getTimestamp()).append("  ").append(a.getDescription()).append('\n');
        }
        txtConsola.setText(sb.toString());
        txtConsola.positionCaret(txtConsola.getText().length());
    }
}
