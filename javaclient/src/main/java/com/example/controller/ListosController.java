package com.example.controller;

import java.util.List;

import com.example.PedidosStore;
import com.example.TarjetaPedido;
import com.example.model.EstadoPedido;
import com.example.model.PedidoVista;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/** Listos para reclamar: DESHACER regresa a preparación, Archivar lo quita. */
public class ListosController extends NavController {

    private static final int COLUMNAS = 4;

    @FXML private GridPane gridListos;
    @FXML private Label lblCountListos;

    @FXML
    private void initialize() {
        refrescar();
    }

    private void refrescar() {
        gridListos.getChildren().clear();
        List<PedidoVista> lista = PedidosStore.porEstado(EstadoPedido.LISTO);
        lblCountListos.setText(String.valueOf(lista.size()));

        for (int i = 0; i < lista.size(); i++) {
            PedidoVista p = lista.get(i);
            Runnable archivar = () -> {
                PedidosStore.archivar(p);
                refrescar();
            };
            Runnable deshacer = () -> {
                PedidosStore.mover(p, EstadoPedido.PREPARANDO);
                refrescar();
            };
            gridListos.add(TarjetaPedido.crear(p, "Archivar pedido", "archive-button", archivar, deshacer),
                    i % COLUMNAS, i / COLUMNAS);
        }
    }
}
