package com.example.controller;

import java.util.List;

import com.example.TarjetaPedido;
import com.example.model.EstadoPedido;
import com.example.model.ModeloRmi;
import com.example.model.PedidoVista;
import com.example.util.Alerta;
import com.example.util.Async;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

/** Cocina: "En cola" -> "En preparación" -> Listos. Se actualiza sola cada pocos segundos. */
public class CocinaController {

    private static final Duration INTERVALO = Duration.seconds(5);

    private record Tablero(List<PedidoVista> cola, List<PedidoVista> preparando) {}

    @FXML private GridPane gridCola;
    @FXML private GridPane gridPrep;
    @FXML private Label lblCountCola;
    @FXML private Label lblCountPrep;

    private boolean cargando;

    @FXML
    private void initialize() {
        refrescar(false);

        Timeline automatico = new Timeline(new KeyFrame(INTERVALO, e -> refrescar(true)));
        automatico.setCycleCount(Animation.INDEFINITE);
        automatico.play();
    }

    /** @param silencioso true en la actualización automática: no molesta con alertas si el servidor no responde */
    private void refrescar(boolean silencioso) {
        if (silencioso && cargando) {
            return;
        }
        cargando = true;
        Async.ejecutar(
                () -> new Tablero(
                        ModeloRmi.listarPedidosPorEstado(EstadoPedido.EN_COLA),
                        ModeloRmi.listarPedidosPorEstado(EstadoPedido.PREPARANDO)),
                tablero -> {
                    cargando = false;
                    pintar(tablero);
                },
                mensaje -> {
                    cargando = false;
                    if (!silencioso) {
                        mostrarError(mensaje);
                    }
                });
    }

    private void pintar(Tablero t) {
        llenar(gridCola, lblCountCola, t.cola(), EstadoPedido.EN_COLA, "Empezar a Preparar");
        llenar(gridPrep, lblCountPrep, t.preparando(), EstadoPedido.PREPARANDO, "Marcar como Listo");
    }

    private void llenar(GridPane grid, Label contador, List<PedidoVista> lista, EstadoPedido estado, String textoAccion) {
        grid.getChildren().clear();
        contador.setText(String.valueOf(lista.size()));

        for (int i = 0; i < lista.size(); i++) {
            PedidoVista p = lista.get(i);
            Runnable avanzar = () -> mover(p, estado.siguiente());
            Runnable deshacer = estado.anterior() == null ? null : () -> mover(p, estado.anterior());
            grid.add(TarjetaPedido.crear(p, textoAccion, "action-button", avanzar, deshacer), i % 2, i / 2);
        }
    }

    private void mover(PedidoVista p, EstadoPedido nuevo) {
        Async.ejecutar(
                () -> {
                    ModeloRmi.cambiarEstado(p.getId(), nuevo);
                    return null;
                },
                v -> refrescar(false),
                mensaje -> {
                    mostrarError(mensaje);
                    refrescar(false);
                });
    }

    private void mostrarError(String mensaje) {
        Scene escena = gridCola.getScene();
        if (escena != null && escena.getWindow() != null) {
            Alerta.error(escena.getWindow(), "No se pudo completar", mensaje);
        }
    }
}
