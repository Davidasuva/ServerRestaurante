package com.example;

import com.example.PedidosStore.PedidoLocal;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Construye por código la tarjeta de un pedido (misma estructura y estilos que tenían los FXML). */
final class TarjetaPedido {

    private TarjetaPedido() {}

    /**
     * @param alDeshacer si es null, no se muestra el botón DESHACER
     */
    static VBox crear(PedidoLocal p, String textoAccion, String estiloAccion,
                      Runnable alAccion, Runnable alDeshacer) {
        // Fila 1: id + deshacer
        Label id = new Label(p.id);
        id.getStyleClass().add("id-pedido-lg");
        HBox fila1 = new HBox(id, espacio());
        fila1.setAlignment(Pos.CENTER_LEFT);
        if (alDeshacer != null) {
            Button undo = new Button("DESHACER");
            undo.getStyleClass().add("undo-button");
            undo.setGraphicTextGap(5);
            undo.setGraphic(icono("/icons/undo.png"));
            undo.setOnAction(e -> alDeshacer.run());
            fila1.getChildren().add(undo);
        }

        // Fila 2: mesa + hace cuánto
        Label mesa = new Label(p.mesa);
        mesa.getStyleClass().add("badge-mesa");
        long minutos = Math.max(0, (System.currentTimeMillis() - p.creadoMs) / 60000);
        Label hace = new Label("Hace: " + minutos + " min");
        hace.getStyleClass().add("time-label");
        HBox reloj = new HBox(4, icono("/icons/schedule.png"), hace);
        reloj.setAlignment(Pos.CENTER_LEFT);
        HBox fila2 = new HBox(mesa, espacio(), reloj);
        fila2.setAlignment(Pos.CENTER_LEFT);

        // Ítems
        VBox items = new VBox(10);
        items.getStyleClass().add("items-box");
        items.setMinHeight(128);
        VBox.setVgrow(items, Priority.ALWAYS);
        p.items.forEach((nombre, cantidad) -> {
            Label punto = new Label("•");
            punto.getStyleClass().add("bullet");
            Label texto = new Label(cantidad + "x " + nombre);
            texto.getStyleClass().add("order-item");
            texto.setWrapText(true);
            texto.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(texto, Priority.ALWAYS);
            HBox linea = new HBox(8, punto, texto);
            linea.setAlignment(Pos.CENTER_LEFT);
            items.getChildren().add(linea);
        });

        // Botón de acción
        Button accion = new Button(textoAccion);
        accion.getStyleClass().add(estiloAccion);
        accion.setMaxWidth(Double.MAX_VALUE);
        accion.setPrefHeight(36);
        accion.setOnAction(e -> alAccion.run());

        VBox card = new VBox(12, fila1, fila2, items, accion);
        card.getStyleClass().add("tarjeta-pedido");
        card.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private static Region espacio() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    private static ImageView icono(String ruta) {
        ImageView iv = new ImageView(new Image(TarjetaPedido.class.getResourceAsStream(ruta)));
        iv.setFitWidth(12);
        iv.setFitHeight(12);
        iv.setPreserveRatio(true);
        return iv;
    }
}
