package com.example.controller;

import java.util.List;

import com.example.TarjetaPedido;
import com.example.model.EstadoPedido;
import com.example.model.ModeloRmi;
import com.example.model.PedidoVista;
import com.example.util.Alerta;
import com.example.util.Async;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/** Cocina: "En cola" -> "En preparación" -> Listos. */
public class CocinaController extends NavController {

    private record Tablero(List<PedidoVista> cola, List<PedidoVista> preparando) {}

    @FXML private GridPane gridCola;
    @FXML private GridPane gridPrep;
    @FXML private Label lblCountCola;
    @FXML private Label lblCountPrep;

    @FXML
    private void initialize() {
        refrescar();
    }

    private void refrescar() {
        Async.ejecutar(
                () -> new Tablero(
                        ModeloRmi.listarPedidosPorEstado(EstadoPedido.EN_COLA),
                        ModeloRmi.listarPedidosPorEstado(EstadoPedido.PREPARANDO)),
                this::pintar,
                this::errorAlCargar);
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
                v -> refrescar(),
                mensaje -> {
                    mostrarError(mensaje);
                    refrescar();
                });
    }

    private void errorAlCargar(String mensaje) {
        mostrarError(mensaje);
    }

    /** Solo muestra el error si el usuario sigue en esta vista. */
    private void mostrarError(String mensaje) {
        Scene escena = gridCola.getScene();
        if (escena != null && escena.getWindow() != null) {
            Alerta.error(escena.getWindow(), "No se pudo completar", mensaje);
        }
    }
}
