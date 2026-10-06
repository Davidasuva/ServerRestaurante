package server.controller;

import environment.Environment;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import server.factory.ServerFactory;
import server.model.ServerModel;
import server.model.history.Action;
import server.model.observer.Observer;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Lógica común a las secciones del servidor (Mesas, Ingredientes, Productos, Pedidos):
 * navegación, control del servidor, consola desplegable, mensajes y tareas en segundo plano.
 *
 * Cada FXML de sección debe declarar los mismos fx:id de la cabecera:
 * btnConsola, btnServidor, indicador, lblEstado, panelConsola, txtConsola, lblMensaje.
 *
 * Da igual en qué orden se llamen initialize() y setModel(): la sección arranca
 * (alIniciar) cuando ya se cumplieron las dos cosas.
 */
public abstract class SeccionBaseController {

    protected static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
    private static final double ALTO_CONSOLA = 260;

    protected ServerModel model;

    private Observer observadorHistorial;
    private int lineasMostradas = 0;
    private boolean consolaAbierta = false;
    private boolean servidorOcupado = false;
    private boolean fxmlListo = false;
    private boolean iniciado = false;

    @FXML protected Button btnConsola;
    @FXML protected Button btnServidor;
    @FXML protected Region indicador;
    @FXML protected Label lblEstado;
    @FXML protected VBox panelConsola;
    @FXML protected TextArea txtConsola;
    @FXML protected Label lblMensaje;
    /** Panel lateral de añadir/editar. Arranca oculto (visible=false, managed=false en el FXML). */
    @FXML protected VBox panelForm;

    /** Se ejecuta una sola vez, con el modelo ya inyectado y los @FXML ya cargados. */
    protected abstract void alIniciar();

    /** Vuelve a consultar los datos de la sección (la usa la factory al navegar). */
    public abstract void refrescar();

    // ───────────────────────── Ciclo de vida ─────────────────────────

    @FXML
    public void initialize() {
        fxmlListo = true;
        iniciarSiListo();
    }

    public void setModel(ServerModel model) {
        this.model = model;
        iniciarSiListo();
    }

    private void iniciarSiListo() {
        if (iniciado || !fxmlListo || model == null) {
            return;
        }
        iniciado = true;
        configurarConsola();
        refrescarEstadoServidor(null);
        alIniciar();
    }

    /** Devuelve el servicio o avisa de que el servidor aún no se inició (los servicios nacen en deploy()). */
    protected static <S> S servicio(S servicio) {
        if (servicio == null) {
            throw new IllegalStateException("Inicia el servidor para cargar los datos.");
        }
        return servicio;
    }

    // ───────────────────────── Navegación ─────────────────────────

    @FXML protected void handleIrMesas(ActionEvent e)        { ServerFactory.navigateToMesas(stageDe(e)); }
    @FXML protected void handleIrPedidos(ActionEvent e)      { ServerFactory.navigateToPedidos(stageDe(e)); }
    @FXML protected void handleIrProductos(ActionEvent e)    { ServerFactory.navigateToProductos(stageDe(e)); }
    @FXML protected void handleIrIngredientes(ActionEvent e) { ServerFactory.navigateToIngredientes(stageDe(e)); }
    @FXML protected void handleIrEmpleados(ActionEvent e)    { ServerFactory.navigateToEmpleados(stageDe(e)); }

    private Stage stageDe(ActionEvent e) {
        return (Stage) ((Node) e.getSource()).getScene().getWindow();
    }

    // ───────────────────────── Panel de añadir / editar ─────────────────────────

    /** Muestra el panel lateral del formulario. */
    protected void abrirForm() {
        if (panelForm != null) {
            panelForm.setManaged(true);
            panelForm.setVisible(true);
        }
    }

    /** Oculta el panel lateral del formulario. */
    protected void cerrarForm() {
        if (panelForm != null) {
            panelForm.setVisible(false);
            panelForm.setManaged(false);
        }
    }

    /** Hook: cada sección limpia aquí su formulario al cerrarlo. */
    protected void alCerrarForm() {
    }

    @FXML
    protected void handleCerrarForm() {
        alCerrarForm();
        cerrarForm();
    }

    // ───────────────────────── Consola ─────────────────────────

    private void configurarConsola() {
        panelConsola.setMinHeight(0);
        panelConsola.setPrefHeight(0);
        panelConsola.setMaxHeight(0);
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(panelConsola.widthProperty());
        clip.heightProperty().bind(panelConsola.heightProperty());
        panelConsola.setClip(clip);

        if (observadorHistorial != null) {
            model.getHistory().detach(observadorHistorial);
        }
        observadorHistorial = new Observer(model.getHistory()) {
            @Override
            public void update() {
                Platform.runLater(SeccionBaseController.this::volcarHistorial);
            }
        };
        volcarHistorial();
    }

    private void volcarHistorial() {
        List<Action> acciones = model.getHistory().getActions();
        for (int i = lineasMostradas; i < acciones.size(); i++) {
            Action a = acciones.get(i);
            txtConsola.appendText("[" + a.getTimestamp() + "] " + a.getDescription() + "\n");
        }
        lineasMostradas = acciones.size();
        txtConsola.setScrollTop(Double.MAX_VALUE);
    }

    @FXML
    protected void handleToggleConsola() {
        consolaAbierta = !consolaAbierta;
        double destino = consolaAbierta ? ALTO_CONSOLA : 0;

        btnConsola.setText(consolaAbierta ? "Consola ▴" : "Consola ▾");
        if (consolaAbierta) {
            if (!btnConsola.getStyleClass().contains("activo")) {
                btnConsola.getStyleClass().add("activo");
            }
        } else {
            btnConsola.getStyleClass().remove("activo");
        }

        new Timeline(new KeyFrame(Duration.millis(220),
                new KeyValue(panelConsola.prefHeightProperty(), destino, Interpolator.EASE_BOTH),
                new KeyValue(panelConsola.maxHeightProperty(), destino, Interpolator.EASE_BOTH)
        )).play();

        if (consolaAbierta) {
            Platform.runLater(() -> txtConsola.setScrollTop(Double.MAX_VALUE));
        }
    }

    @FXML
    protected void handleLimpiarConsola() {
        txtConsola.clear();
    }

    // ───────────────────────── Servidor ─────────────────────────

    @FXML
    protected void handleToggleServidor() {
        if (servidorOcupado) {
            return;
        }
        final boolean iniciar = !model.isRunning();
        servidorOcupado = true;
        btnServidor.setDisable(true);
        lblEstado.setText(iniciar ? "Iniciando…" : "Apagando…");

        Task<Boolean> tarea = new Task<>() {
            @Override
            protected Boolean call() {
                if (iniciar) {
                    return model.deploy();
                }
                model.stop();
                return true;
            }
        };
        tarea.setOnSucceeded(e -> {
            servidorOcupado = false;
            btnServidor.setDisable(false);
            boolean fallo = iniciar && !Boolean.TRUE.equals(tarea.getValue());
            refrescarEstadoServidor(fallo ? "No se pudo iniciar (revisa la consola)" : null);
            if (iniciar && !fallo) {
                refrescar();
            }
        });
        tarea.setOnFailed(e -> {
            servidorOcupado = false;
            btnServidor.setDisable(false);
            Throwable ex = tarea.getException();
            model.getHistory().addAction("Error inesperado: " + (ex != null ? ex.getMessage() : "desconocido"));
            refrescarEstadoServidor("Error inesperado (revisa la consola)");
        });

        Thread hilo = new Thread(tarea, "server-control-thread");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void refrescarEstadoServidor(String textoEstado) {
        boolean activo = model.isRunning();
        indicador.getStyleClass().setAll("indicador", activo ? "indicador-activo" : "indicador-apagado");
        btnServidor.getStyleClass().setAll("button", activo ? "btn-apagar" : "btn-iniciar");
        btnServidor.setText(activo ? "■  Apagar servidor" : "▶  Iniciar servidor");
        if (textoEstado != null) {
            lblEstado.setText(textoEstado);
        } else {
            Environment env = Environment.getInstance();
            lblEstado.setText(activo
                    ? "Servidor activo · " + env.getIp() + ":" + env.getPort()
                    : "Servidor apagado");
        }
    }

    // ───────────────────────── Utilidades ─────────────────────────

    /** Ejecuta el trabajo fuera del hilo de JavaFX y entrega resultado o error de vuelta en el hilo de JavaFX. */
    protected <T> void ejecutar(Callable<T> trabajo, Consumer<T> alExito) {
        Task<T> tarea = new Task<>() {
            @Override
            protected T call() throws Exception {
                return trabajo.call();
            }
        };
        tarea.setOnSucceeded(e -> alExito.accept(tarea.getValue()));
        tarea.setOnFailed(e -> {
            Throwable ex = tarea.getException();
            mostrarMensaje(ex != null && ex.getMessage() != null ? ex.getMessage() : "Ocurrió un error.", true);
        });
        Thread hilo = new Thread(tarea, "seccion-db-thread");
        hilo.setDaemon(true);
        hilo.start();
    }

    protected void confirmar(String encabezado, String texto, Runnable siAcepta) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, texto, ButtonType.OK, ButtonType.CANCEL);
        alerta.setHeaderText(encabezado);
        alerta.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                siAcepta.run();
            }
        });
    }

    protected void mostrarMensaje(String texto, boolean error) {
        lblMensaje.setText(texto);
        lblMensaje.getStyleClass().setAll("label", error ? "mensaje-error" : "mensaje-ok");
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }

    protected void ocultarMensaje() {
        lblMensaje.setVisible(false);
        lblMensaje.setManaged(false);
    }

    public void sincronizarEstadoServidor() {
        if (iniciado) {
            refrescarEstadoServidor(null);
        }
    }
}
