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
        Async.ejecutar(
                () -> ModeloRmi.listarPedidosPorEstado(EstadoPedido.LISTO),
                this::pintar,
                this::mostrarError);
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
                v -> refrescar(),
                mensaje -> {
                    mostrarError(mensaje);
                    refrescar();
                });
    }

    /** Solo muestra el error si el usuario sigue en esta vista. */
    private void mostrarError(String mensaje) {
        Scene escena = gridListos.getScene();
        if (escena != null && escena.getWindow() != null) {
            Alerta.error(escena.getWindow(), "No se pudo completar", mensaje);
        }
    }
}
