package com.example;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import server.model.producto.Producto;

/** Tarjeta de un producto en Caja: imagen, nombre, precio y botones -/+ (mismos estilos de siempre). */
public final class TarjetaProducto {

    /** La tarjeta y su contador de cantidad (para ponerlo en 0 al terminar un pedido). */
    public record Tarjeta(VBox nodo, Label cantidad) {}

    private TarjetaProducto() {}

    public static Tarjeta crear(Producto producto, String precioTexto, Runnable alSumar, Runnable alRestar) {
        ImageView imagen = new ImageView(cargarImagen(producto));
        imagen.setFitWidth(170);
        imagen.setFitHeight(110);
        imagen.setPreserveRatio(false);
        imagen.setPickOnBounds(true);
        imagen.getStyleClass().add("product-image");

        Label nombre = new Label(producto.getNombre());
        nombre.getStyleClass().add("product-name");
        VBox.setMargin(nombre, new Insets(8, 10, 0, 10));

        Label precio = new Label(precioTexto);
        precio.getStyleClass().add("product-price");

        Button menos = new Button("-");
        menos.getStyleClass().add("qty-button");
        menos.setOnAction(e -> alRestar.run());

        Label cantidad = new Label("0");
        cantidad.getStyleClass().add("qty-value");
        HBox.setMargin(cantidad, new Insets(0, 6, 0, 6));

        Button mas = new Button("+");
        mas.getStyleClass().add("qty-button");
        mas.setOnAction(e -> alSumar.run());

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox fila = new HBox(precio, espacio, menos, cantidad, mas);
        fila.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox.setMargin(fila, new Insets(8, 10, 10, 10));

        VBox card = new VBox(imagen, nombre, fila);
        card.setPrefWidth(170);
        card.getStyleClass().add("product-card");
        return new Tarjeta(card, cantidad);
    }

    /**
     * Busca la imagen: primero lo que diga imagenURL (dirección web/archivo o ruta dentro de
     * /images/), y si no aparece, /images/ + nombre del producto en minúsculas y con guiones.
     * Devuelve null (tarjeta sin imagen) si no encuentra nada.
     */
    private static Image cargarImagen(Producto p) {
        String url = p.getImagenURL() == null ? "" : p.getImagenURL().trim();
        if (url.matches("(?i)^(https?|file):.*")) {
            return new Image(url, 340, 220, false, true, true);
        }

        List<String> rutas = new ArrayList<>();
        if (!url.isEmpty()) {
            rutas.add(url.startsWith("/") ? url : "/" + url);
            rutas.add("/images/" + url);
        }
        rutas.add("/images/" + slug(p.getNombre()) + ".jpg");

        for (String ruta : rutas) {
            var recurso = TarjetaProducto.class.getResource(ruta);
            if (recurso != null) {
                return new Image(recurso.toExternalForm(), 340, 220, false, true, true);
            }
        }
        return null;
    }

    private static String slug(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
    }
}
