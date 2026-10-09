package com.example.controller;

import java.util.List;

import com.example.PedidosStore;
import com.example.TarjetaPedido;
import com.example.model.EstadoPedido;
import com.example.model.PedidoVista;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/** Cocina: "En cola" -> "En preparación" -> Listos. */
public class CocinaController extends NavController {

    @FXML private GridPane gridCola;
    @FXML private GridPane gridPrep;
    @FXML private Label lblCountCola;
    @FXML private Label lblCountPrep;

    @FXML
    private void initialize() {
        refrescar();
    }

    private void refrescar() {
        llenar(gridCola, lblCountCola, EstadoPedido.EN_COLA, "Empezar a Preparar");
        llenar(gridPrep, lblCountPrep, EstadoPedido.PREPARANDO, "Marcar como Listo");
    }

    private void llenar(GridPane grid, Label contador, EstadoPedido estado, String textoAccion) {
        grid.getChildren().clear();
        List<PedidoVista> lista = PedidosStore.porEstado(estado);
        contador.setText(String.valueOf(lista.size()));

        for (int i = 0; i < lista.size(); i++) {
            PedidoVista p = lista.get(i);
            Runnable avanzar = () -> {
                PedidosStore.mover(p, estado.siguiente());
                refrescar();
            };
            Runnable deshacer = estado.anterior() == null ? null : () -> {
                PedidosStore.mover(p, estado.anterior());
                refrescar();
            };
            grid.add(TarjetaPedido.crear(p, textoAccion, "action-button", avanzar, deshacer), i % 2, i / 2);
        }
    }
}
