package com.proyectoApi.api.views.ui.panel;

import com.proyectoApi.api.views.mobile.VistaMovilService;
import com.proyectoApi.api.views.mobile.VistaMovilService.Estado;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;

/** Panel con un solo botón: iniciar / apagar la vista móvil. */
public class PanelController {

    private static final String BTN_APAGAR = "btn-apagar";

    private VistaMovilService vista;

    @FXML
    private Circle puntoEstado;
    @FXML
    private Label lblEstado;
    @FXML
    private Button btnToggle;
    @FXML
    private Label lblUrls;
    @FXML
    private Label lblError;

    public void setVistaMovil(VistaMovilService vista) {
        this.vista = vista;
        vista.addListener((estado, urls, error) -> Platform.runLater(() -> pintar(estado, urls, error)));
        refrescar();
    }

    /** Vuelve a dibujar con el estado actual del servicio. */
    public void refrescar() {
        if (vista != null) {
            pintar(vista.getEstado(), vista.getUrls(), null);
        }
    }

    @FXML
    private void handleToggle() {
        if (vista == null) {
            return;
        }
        switch (vista.getEstado()) {
            case APAGADA -> vista.iniciar();
            case ENCENDIDA -> vista.apagar();
            default -> { /* transición en curso: se ignora */ }
        }
    }

    private void pintar(Estado estado, List<String> urls, String error) {
        puntoEstado.getStyleClass().removeAll("punto-apagado", "punto-iniciando", "punto-encendido");
        btnToggle.getStyleClass().remove(BTN_APAGAR);

        switch (estado) {
            case APAGADA -> {
                puntoEstado.getStyleClass().add("punto-apagado");
                lblEstado.setText("Vista móvil apagada");
                btnToggle.setText("Iniciar vista móvil");
                btnToggle.setDisable(false);
            }
            case INICIANDO -> {
                puntoEstado.getStyleClass().add("punto-iniciando");
                lblEstado.setText("Iniciando vista móvil...");
                btnToggle.setText("Iniciando...");
                btnToggle.setDisable(true);
            }
            case ENCENDIDA -> {
                puntoEstado.getStyleClass().add("punto-encendido");
                lblEstado.setText("Vista móvil encendida");
                btnToggle.setText("Apagar vista móvil");
                btnToggle.getStyleClass().add(BTN_APAGAR);
                btnToggle.setDisable(false);
            }
            case APAGANDO -> {
                puntoEstado.getStyleClass().add("punto-iniciando");
                lblEstado.setText("Apagando vista móvil...");
                btnToggle.setText("Apagando...");
                btnToggle.getStyleClass().add(BTN_APAGAR);
                btnToggle.setDisable(true);
            }
        }

        boolean hayUrls = estado == Estado.ENCENDIDA && urls != null && !urls.isEmpty();
        lblUrls.setVisible(hayUrls);
        lblUrls.setManaged(hayUrls);
        lblUrls.setText(hayUrls ? "Abre en el celular (misma red Wi-Fi):\n" + String.join("\n", urls) : "");

        boolean hayError = error != null && !error.isBlank();
        lblError.setVisible(hayError);
        lblError.setManaged(hayError);
        lblError.setText(hayError ? error : "");
    }
}
