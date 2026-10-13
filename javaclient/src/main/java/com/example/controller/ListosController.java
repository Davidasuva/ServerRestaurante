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

/**
 * Televisión / Listos para reclamar: DESHACER regresa a preparación, Archivar lo quita.
 * Se actualiza sola cada pocos segundos.
 */
public class ListosController {

    private static final int COLUMNAS = 4;
    private static final Duration INTERVALO = Duration.seconds(5);

    @FXML private GridPane gridListos;
    @FXML private Label lblCountListos;

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
                () -> ModeloRmi.listarPedidosPorEstado(EstadoPedido.LISTO),
                lista -> {
                    cargando = false;
                    pintar(lista);
                },
                mensaje -> {
                    cargando = false;
                    if (!silencioso) {
                        mostrarError(mensaje);
                    }
                });
    }

    private void pintar(List<PedidoVista> lista) {
        gridListos.getChildren().clear();
        lblCountListos.setText(String.valueOf(lista.size()));

        for (int i = 0; i < lista.size(); i++) {
            PedidoVista p = lista.get(i);
            Runnable archivar = () -> ejecutarYRefrescar(() -> ModeloRmi.archivarPedido(p.getId()));
            Runnable deshacer = () -> ejecutarYRefrescar(
                    () -> ModeloRmi.cambiarEstado(p.getId(), EstadoPedido.PREPARANDO));
            gridListos.add(TarjetaPedido.crear(p, "Archivar pedido", "archive-button", archivar, deshacer),
                    i % COLUMNAS, i / COLUMNAS);
        }
    }

    private interface Accion {
        void ejecutar() throws Exception;
    }

    private void ejecutarYRefrescar(Accion accion) {
        Async.ejecutar(
                () -> {
                    accion.ejecutar();
                    return null;
                },
                v -> refrescar(false),
                mensaje -> {
                    mostrarError(mensaje);
                    refrescar(false);
                });
    }

    private void mostrarError(String mensaje) {
        Scene escena = gridListos.getScene();
        if (escena != null && escena.getWindow() != null) {
            Alerta.error(escena.getWindow(), "No se pudo completar", mensaje);
        }
    }
}
