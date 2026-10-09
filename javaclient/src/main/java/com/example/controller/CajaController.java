package com.example.controller;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.PedidosStore;
import com.example.model.ItemCarrito;
import com.example.model.PedidoVista;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Caja: elegir mesa, agregar/quitar productos y ver la factura en vivo.
 * Por ahora trabaja en local (sin servidor). Hereda de NavController para conservar las pestañas.
 */
public class CajaController extends NavController {

    private static final int NUM_MESAS = 10;
    private static final String[] METODOS_PAGO = {"Nequi", "Transferencia", "Efectivo"};
    private static final DecimalFormat PESOS = crearFormato();

    private static DecimalFormat crearFormato() {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", simbolos);
    }

    @FXML private ChoiceBox<String> choiceMesa;
    @FXML private ChoiceBox<String> choiceMetodoPago;
    @FXML private Label lblMesa;
    @FXML private FlowPane gridProductos;
    @FXML private VBox listaItems;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTotal;
    @FXML private Button btnPagar;

    /** Líneas de la factura, en el orden en que se agregaron. */
    private final Map<String, ItemCarrito> carrito = new LinkedHashMap<>();

    /** Contadores de las tarjetas, para ponerlos en 0 al terminar un pedido. */
    private final List<Label> contadores = new ArrayList<>();

    @FXML
    private void initialize() {
        for (int i = 1; i <= NUM_MESAS; i++) {
            choiceMesa.getItems().add(String.format("Mesa %02d", i));
        }
        choiceMesa.valueProperty().addListener((obs, anterior, nueva) -> actualizarSubtitulo());

        choiceMetodoPago.getItems().addAll(METODOS_PAGO);
        choiceMetodoPago.valueProperty().addListener((obs, anterior, nueva) -> actualizarSubtitulo());
        actualizarSubtitulo();

        for (Node n : gridProductos.getChildren()) {
            if (n instanceof VBox card) {
                conectarTarjeta(card);
            }
        }
        refrescarFactura();
    }

    /** Subtítulo de la factura: "Mesa 04 · Nequi". */
    private void actualizarSubtitulo() {
        String mesa = choiceMesa.getValue() == null ? "Seleccione una mesa" : choiceMesa.getValue();
        String pago = choiceMetodoPago.getValue();
        lblMesa.setText(pago == null ? mesa : mesa + " · " + pago);
        actualizarBotonPagar();
    }

    /** Pagar solo se habilita con mesa, método de pago y al menos un producto. */
    private void actualizarBotonPagar() {
        btnPagar.setDisable(choiceMesa.getValue() == null
                || choiceMetodoPago.getValue() == null
                || carrito.isEmpty());
    }

    @FXML
    private void pagar() {
        String resumen = choiceMesa.getValue() + " · " + choiceMetodoPago.getValue();
        String total = lblTotal.getText();

        PedidoVista pedido = PedidosStore.crear(choiceMesa.getValue(), choiceMetodoPago.getValue(),
                new ArrayList<>(carrito.values()));

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.initOwner(btnPagar.getScene().getWindow());
        alerta.setTitle("Pedido creado");
        alerta.setHeaderText(null);
        alerta.setGraphic(null);
        alerta.setContentText("El pedido " + pedido.etiqueta() + " se creó correctamente.\n\n" + resumen + "\nTotal: " + total);
        alerta.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        alerta.showAndWait();

        limpiarPedido();
    }

    /** Deja Caja lista para el siguiente pedido. */
    private void limpiarPedido() {
        carrito.clear();
        contadores.forEach(c -> c.setText("0"));
        choiceMesa.setValue(null);
        choiceMetodoPago.setValue(null);
        refrescarFactura();
    }

    /**
     * Estructura de cada tarjeta (ver Caja.fxml):
     * VBox[ ImageView, Label nombre, HBox[ Label precio, Region, Button -, Label cantidad, Button + ] ]
     */
    private void conectarTarjeta(VBox card) {
        String nombre = ((Label) card.getChildren().get(1)).getText().trim();
        HBox fila = (HBox) card.getChildren().get(2);
        int precio = Integer.parseInt(((Label) fila.getChildren().get(0)).getText().replaceAll("\\D", ""));
        Button menos = (Button) fila.getChildren().get(2);
        Label cantidad = (Label) fila.getChildren().get(3);
        Button mas = (Button) fila.getChildren().get(4);

        cantidad.setText("0");
        contadores.add(cantidad);
        mas.setOnAction(e -> cambiar(nombre, precio, +1, cantidad));
        menos.setOnAction(e -> cambiar(nombre, precio, -1, cantidad));
    }

    private void cambiar(String nombre, int precio, int delta, Label cantidadLabel) {
        ItemCarrito linea = carrito.computeIfAbsent(nombre, k -> new ItemCarrito(0, nombre, precio, 0));
        linea.setCantidad(Math.max(0, linea.getCantidad() + delta));
        if (linea.getCantidad() == 0) {
            carrito.remove(nombre);
        }
        cantidadLabel.setText(String.valueOf(linea.getCantidad()));
        refrescarFactura();
    }

    private void refrescarFactura() {
        listaItems.getChildren().clear();
        int total = 0;

        if (carrito.isEmpty()) {
            Label vacio = new Label("Aún no hay productos");
            vacio.getStyleClass().add("texto-secundario");
            listaItems.getChildren().add(vacio);
        }

        for (ItemCarrito l : carrito.values()) {
            int subtotalLinea = l.getSubtotal();
            total += subtotalLinea;

            Label nombre = new Label(l.getCantidad() + " × " + l.getNombre());
            nombre.getStyleClass().add("factura-item-name");
            Region espacio = new Region();
            HBox.setHgrow(espacio, Priority.ALWAYS);
            Label valor = new Label(pesos(subtotalLinea));
            valor.getStyleClass().add("factura-item-value");

            HBox fila = new HBox(nombre, espacio, valor);
            fila.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            listaItems.getChildren().add(fila);
        }

        lblSubtotal.setText(pesos(total));
        lblTotal.setText(pesos(total));
        actualizarBotonPagar();
    }

    private static String pesos(int valor) {
        return "$" + PESOS.format(valor);
    }
}
