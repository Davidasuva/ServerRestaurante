package com.example.controller;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.TarjetaProducto;
import com.example.model.ItemCarrito;
import com.example.model.ModeloRmi;
import com.example.model.PedidoVista;
import com.example.util.Alerta;
import com.example.util.Async;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import server.model.mesa.Mesa;
import server.model.producto.Producto;

/**
 * Caja: elegir mesa, agregar/quitar productos y ver la factura en vivo.
 * Mesas y productos vienen del servidor (ModeloRmi).
 */
public class CajaController {

    private static final String[] METODOS_PAGO = {"Nequi", "Transferencia", "Efectivo"};
    private static final DecimalFormat PESOS = crearFormato();

    private static DecimalFormat crearFormato() {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", simbolos);
    }

    @FXML private ChoiceBox<Mesa> choiceMesa;
    @FXML private ChoiceBox<String> choiceMetodoPago;
    @FXML private Label lblMesa;
    @FXML private FlowPane gridProductos;
    @FXML private VBox listaItems;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTotal;
    @FXML private Button btnPagar;

    /** Líneas de la factura por id de producto, en el orden en que se agregaron. */
    private final Map<Integer, ItemCarrito> carrito = new LinkedHashMap<>();

    /** Contadores de las tarjetas por id de producto, para ponerlos en 0 al terminar un pedido. */
    private final Map<Integer, Label> contadores = new HashMap<>();

    @FXML
    private void initialize() {
        choiceMesa.setConverter(new StringConverter<>() {
            @Override
            public String toString(Mesa mesa) {
                return mesa == null ? "" : etiqueta(mesa);
            }

            @Override
            public Mesa fromString(String texto) {
                return null;
            }
        });
        choiceMesa.valueProperty().addListener((obs, anterior, nueva) -> actualizarSubtitulo());

        choiceMetodoPago.getItems().addAll(METODOS_PAGO);
        choiceMetodoPago.valueProperty().addListener((obs, anterior, nueva) -> actualizarSubtitulo());
        actualizarSubtitulo();
        refrescarFactura();

        cargarMesas();
        cargarProductos();
    }

    private void cargarMesas() {
        Async.ejecutar(
                ModeloRmi::listarMesas,
                mesas -> choiceMesa.getItems().setAll(mesas),
                mensaje -> mostrarError("No se pudieron cargar las mesas", mensaje));
    }

    private void cargarProductos() {
        Async.ejecutar(
                ModeloRmi::listarProductos,
                this::pintarProductos,
                mensaje -> mostrarError("No se pudo cargar el catálogo", mensaje));
    }

    private void pintarProductos(List<Producto> productos) {
        gridProductos.getChildren().clear();
        contadores.clear();

        if (productos.isEmpty()) {
            Label vacio = new Label("No hay productos en el catálogo");
            vacio.getStyleClass().add("texto-secundario");
            gridProductos.getChildren().add(vacio);
            return;
        }

        for (Producto p : productos) {
            TarjetaProducto.Tarjeta tarjeta = TarjetaProducto.crear(p, pesos(precio(p)),
                    () -> cambiar(p, +1), () -> cambiar(p, -1));
            contadores.put(p.getId(), tarjeta.cantidad());
            gridProductos.getChildren().add(tarjeta.nodo());
        }
    }

    /** Subtítulo de la factura: "Mesa 04 · Nequi". */
    private void actualizarSubtitulo() {
        String mesa = choiceMesa.getValue() == null ? "Seleccione una mesa" : etiqueta(choiceMesa.getValue());
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
        Mesa mesa = choiceMesa.getValue();
        String metodoPago = choiceMetodoPago.getValue();
        String resumen = etiqueta(mesa) + " · " + metodoPago;
        String total = lblTotal.getText();

        // Copia de lo que hay en la factura ahora mismo (el usuario puede seguir tocando las tarjetas)
        List<ItemCarrito> items = new ArrayList<>();
        for (ItemCarrito l : carrito.values()) {
            items.add(new ItemCarrito(l.getProductoId(), l.getNombre(), l.getPrecio(), l.getCantidad()));
        }

        btnPagar.setDisable(true);
        Async.ejecutar(
                () -> ModeloRmi.crearPedido(mesa.getId(), metodoPago, items),
                (PedidoVista pedido) -> {
                    Scene escena = btnPagar.getScene();
                    Alerta.informacion(escena == null ? null : escena.getWindow(), "Pedido creado",
                            "El pedido " + pedido.etiqueta() + " se creó correctamente.\n\n" + resumen + "\nTotal: " + total);
                    limpiarPedido();
                },
                mensaje -> {
                    mostrarError("No se pudo crear el pedido", mensaje);
                    actualizarBotonPagar();
                });
    }

    /** Deja Caja lista para el siguiente pedido. */
    private void limpiarPedido() {
        carrito.clear();
        contadores.values().forEach(c -> c.setText("0"));
        choiceMesa.setValue(null);
        choiceMetodoPago.setValue(null);
        refrescarFactura();
    }

    private void cambiar(Producto producto, int delta) {
        ItemCarrito linea = carrito.computeIfAbsent(producto.getId(),
                k -> new ItemCarrito(producto.getId(), producto.getNombre(), precio(producto), 0));
        linea.setCantidad(Math.max(0, linea.getCantidad() + delta));
        if (linea.getCantidad() == 0) {
            carrito.remove(producto.getId());
        }
        contadores.get(producto.getId()).setText(String.valueOf(linea.getCantidad()));
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

    /** Solo muestra el error si el usuario sigue en esta vista. */
    private void mostrarError(String titulo, String mensaje) {
        Scene escena = btnPagar.getScene();
        if (escena != null && escena.getWindow() != null) {
            Alerta.error(escena.getWindow(), titulo, mensaje);
        }
    }

    private static String etiqueta(Mesa mesa) {
        return String.format("Mesa %02d", mesa.getId());
    }

    private static int precio(Producto producto) {
        return Math.round(producto.getPrecio());
    }

    private static String pesos(int valor) {
        return "$" + PESOS.format(valor);
    }
}
