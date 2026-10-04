package server.controller.pedidos;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import server.controller.SeccionBaseController;
import server.model.mesa.Mesa;
import server.model.mesa.MesaInterface;
import server.model.pedido.Pedido;
import server.model.pedido.PedidoInterface;
import server.model.producto.Producto;
import server.model.producto.ProductoInterface;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class PedidosController extends SeccionBaseController {

    private static final List<String> METODOS_PAGO = List.of("Ninguno", "Efectivo", "Tarjeta");
    private static final String TODOS = "Todos";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private record Carga(List<Pedido> pedidos, List<Mesa> mesas, List<Producto> productos) {}
    private record Detalle(List<Producto> productos, List<String> encargados) {}

    private static final StringConverter<Mesa> CONVERTIDOR_MESA = new StringConverter<>() {
        @Override public String toString(Mesa m) { return m == null ? "" : "Mesa " + m.getId(); }
        @Override public Mesa fromString(String s) { return null; }
    };

    private static final StringConverter<Producto> CONVERTIDOR_PRODUCTO = new StringConverter<>() {
        @Override public String toString(Producto p) {
            return p == null ? "" : p.getNombre() + " — " + MONEDA.format(p.getPrecio());
        }
        @Override public Producto fromString(String s) { return null; }
    };

    private final ObservableList<Pedido> datos = FXCollections.observableArrayList();
    private final ObservableList<Mesa> mesas = FXCollections.observableArrayList();
    private final ObservableList<Producto> productosDisponibles = FXCollections.observableArrayList();
    private final ObservableList<Producto> productosPedido = FXCollections.observableArrayList();
    private final ObservableList<String> encargadosPedido = FXCollections.observableArrayList();
    private FilteredList<Pedido> filtrados;
    private Pedido seleccionado;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TableView<Pedido> tablaPedidos;
    @FXML private TableColumn<Pedido, Integer> colId;
    @FXML private TableColumn<Pedido, String> colFecha;
    @FXML private TableColumn<Pedido, Integer> colMesa;
    @FXML private TableColumn<Pedido, String> colEstado;
    @FXML private TableColumn<Pedido, String> colPago;
    @FXML private TableColumn<Pedido, Float> colTotal;

    @FXML private Label lblTituloForm;
    @FXML private TextField txtId;
    @FXML private ComboBox<Mesa> cmbMesa;
    @FXML private Label lblFecha;
    @FXML private Label lblTotal;
    @FXML private ComboBox<Pedido.Estado> cmbEstado;
    @FXML private ComboBox<String> cmbPago;
    @FXML private VBox boxProductos;
    @FXML private ListView<Producto> lstProductos;
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private ListView<String> lstEncargados;
    @FXML private Button btnGuardar;
    @FXML private Button btnEliminar;

    @Override
    protected void alIniciar() {
        configurarTabla();
        configurarFormulario();
        limpiarFormulario();
        refrescar();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        colFecha.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(formatearFecha(c.getValue().getFechaPedido())));
        colMesa.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(idMesa(c.getValue())));
        colEstado.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(textoEstado(c.getValue())));
        colPago.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getMetodoPago()));
        colTotal.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getPrecioTotal()));
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Float total, boolean vacio) {
                super.updateItem(total, vacio);
                setText(vacio || total == null ? null : MONEDA.format(total));
            }
        });
        tablaPedidos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaPedidos.setPlaceholder(new Label("No hay pedidos para mostrar"));

        filtrados = new FilteredList<>(datos, p -> true);
        SortedList<Pedido> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tablaPedidos.comparatorProperty());
        tablaPedidos.setItems(ordenados);

        txtBuscar.textProperty().addListener((obs, o, n) -> aplicarFiltro());
        cmbFiltroEstado.valueProperty().addListener((obs, o, n) -> aplicarFiltro());

        tablaPedidos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo != null) {
                cargarEnFormulario(nuevo);
            }
        });
    }

    private void configurarFormulario() {
        cmbMesa.setItems(mesas);
        cmbMesa.setConverter(CONVERTIDOR_MESA);
        cmbPago.getItems().setAll(METODOS_PAGO);
        cmbProducto.setItems(productosDisponibles);
        cmbProducto.setConverter(CONVERTIDOR_PRODUCTO);
        lstProductos.setItems(productosPedido);
        lstProductos.setPlaceholder(new Label("Sin productos"));
        lstProductos.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(Producto p, boolean vacio) {
                super.updateItem(p, vacio);
                setText(vacio || p == null ? null : CONVERTIDOR_PRODUCTO.toString(p));
            }
        });
        lstEncargados.setItems(encargadosPedido);
        lstEncargados.setPlaceholder(new Label("Sin encargados"));

        // Los estados salen directamente del enum (su toString muestra el texto, p. ej. "En Cola").
        cmbEstado.getItems().setAll(Pedido.Estado.values());

        List<String> filtro = new ArrayList<>();
        filtro.add(TODOS);
        for (Pedido.Estado e : Pedido.Estado.values()) {
            filtro.add(e.getTexto());
        }
        cmbFiltroEstado.getItems().setAll(filtro);
        cmbFiltroEstado.setValue(TODOS);
    }

    private void aplicarFiltro() {
        String q = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String estado = cmbFiltroEstado.getValue();
        boolean todos = estado == null || TODOS.equals(estado);
        filtrados.setPredicate(p -> (todos || estado.equalsIgnoreCase(textoEstado(p)))
                && (q.isEmpty()
                || String.valueOf(p.getId()).contains(q)
                || String.valueOf(idMesa(p)).contains(q)
                || textoEstado(p).toLowerCase().contains(q)
                || nvl(p.getMetodoPago()).toLowerCase().contains(q)));
    }

    private static String textoEstado(Pedido p) {
        return p.getEstado() == null ? "" : p.getEstado().getTexto();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String formatearFecha(LocalDateTime f) {
        return f == null ? "" : f.format(FORMATO_FECHA);
    }

    private static int idMesa(Pedido p) {
        return p.getMesaAsignada() == null ? 0 : p.getMesaAsignada().getId();
    }

    // ───────────────────────── Formulario ─────────────────────────

    private void cargarEnFormulario(Pedido p) {
        abrirForm();
        seleccionado = p;
        lblTituloForm.setText("Pedido #" + p.getId());
        txtId.setText(String.valueOf(p.getId()));
        txtId.setDisable(true);
        cmbMesa.setValue(mesas.stream().filter(m -> m.getId() == idMesa(p)).findFirst().orElse(p.getMesaAsignada()));
        cmbMesa.setDisable(true); // la mesa no se cambia desde aquí
        lblFecha.setText(formatearFecha(p.getFechaPedido()));
        lblTotal.setText(MONEDA.format(p.getPrecioTotal()));
        cmbEstado.setDisable(false);
        cmbEstado.setValue(p.getEstado());
        cmbPago.setDisable(false);
        cmbPago.setValue(p.getMetodoPago());
        btnGuardar.setText("Aplicar cambios");
        btnEliminar.setDisable(false);
        boxProductos.setDisable(false);
        ocultarMensaje();
        cargarDetalle(p.getId());
    }

    private void limpiarFormulario() {
        seleccionado = null;
        tablaPedidos.getSelectionModel().clearSelection();
        lblTituloForm.setText("Nuevo pedido");
        txtId.clear();
        txtId.setDisable(false);
        cmbMesa.setValue(null);
        cmbMesa.setDisable(false);
        lblFecha.setText("Se asigna al registrar");
        lblTotal.setText(MONEDA.format(0));
        // Un pedido nuevo siempre nace Pendiente y sin pago (así no consume inventario).
        cmbEstado.setValue(Pedido.Estado.PENDIENTE);
        cmbEstado.setDisable(true);
        cmbPago.setValue("Ninguno");
        cmbPago.setDisable(true);
        productosPedido.clear();
        encargadosPedido.clear();
        cmbProducto.setValue(null);
        boxProductos.setDisable(true); // primero hay que registrar el pedido
        btnGuardar.setText("Registrar");
        btnEliminar.setDisable(true);
        ocultarMensaje();
    }

    @Override
    protected void alCerrarForm() {
        limpiarFormulario();
    }

    @FXML
    private void handleNuevo() {
        limpiarFormulario();
        abrirForm();
        txtId.requestFocus();
    }

    @FXML
    public void handleRecargar() {
        refrescar();
    }

    // ───────────────────────── Datos ─────────────────────────

    @Override
    public void refrescar() {
        cargar(null, null);
    }

    /** Recarga pedidos y mesas; si se indica, reselecciona ese pedido y muestra el mensaje de éxito. */
    private void cargar(Integer idASeleccionar, String mensajeOk) {
        ejecutar(() -> {
            PedidoInterface ps = servicio(model.getPedidoService());
            MesaInterface ms = servicio(model.getMesaService());
            int totalP = ps.contar();
            int totalM = ms.contar();
            ProductoInterface prs = servicio(model.getProductoService());
            int totalPr = prs.contar();
            return new Carga(
                    totalP == 0 ? List.<Pedido>of() : ps.getPedidos(0, totalP - 1),
                    totalM == 0 ? List.<Mesa>of() : ms.getMesa(0, totalM - 1),
                    totalPr == 0 ? List.<Producto>of() : prs.getProductos(0, totalPr - 1));
        }, carga -> {
            datos.setAll(carga.pedidos());
            mesas.setAll(carga.mesas());
            productosDisponibles.setAll(carga.productos());
            if (idASeleccionar != null) {
                carga.pedidos().stream().filter(p -> p.getId() == idASeleccionar).findFirst()
                        .ifPresent(p -> tablaPedidos.getSelectionModel().select(p));
            }
            if (mensajeOk != null) {
                mostrarMensaje(mensajeOk, false); // después de seleccionar, porque cargarEnFormulario oculta el mensaje
            }
        });
    }

    /** Productos y encargados del pedido (solo lectura). */
    private void cargarDetalle(int idPedido) {
        ejecutar(() -> {
            PedidoInterface s = servicio(model.getPedidoService());
            List<Producto> productos = listaOVacia(() -> s.getProductosPerPedido(idPedido));
            List<String> encargados = listaOVacia(() -> s.getEncargadosPerPedido(idPedido).stream()
                    .map(e -> e.getNombre() + " (" + e.getCargo() + ")")
                    .toList());
            return new Detalle(productos, encargados);
        }, detalle -> {
            if (seleccionado != null && seleccionado.getId() == idPedido) {
                productosPedido.setAll(detalle.productos());
                encargadosPedido.setAll(detalle.encargados());
            }
        });
    }

    /**
     * PedidoService lanza excepción cuando un pedido no tiene productos o encargados,
     * y aquí eso es un caso normal (lista vacía), no un error para el usuario.
     */
    private static <T> List<T> listaOVacia(Callable<List<T>> consulta) {
        try {
            return consulta.call();
        } catch (Exception e) {
            return List.of();
        }
    }

    // ───────────────────────── Acciones ─────────────────────────

    @FXML
    private void handleGuardar() {
        if (seleccionado == null) {
            registrarNuevo();
        } else {
            aplicarCambios();
        }
    }

    private void registrarNuevo() {
        int id;
        try {
            id = Integer.parseInt(txtId.getText().trim());
            if (id <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarMensaje("El id debe ser un entero positivo.", true);
            return;
        }
        Mesa mesa = cmbMesa.getValue();
        if (mesa == null) {
            mostrarMensaje("Selecciona la mesa del pedido.", true);
            return;
        }

        final Pedido nuevo = new Pedido(id, LocalDateTime.now(), mesa);
        ejecutar(() -> servicio(model.getPedidoService()).registrarPedido(nuevo),
                r -> cargar(r.getId(), "Pedido registrado."));
    }

    private void aplicarCambios() {
        final Pedido original = seleccionado;
        final Pedido.Estado estado = cmbEstado.getValue();
        final String pago = cmbPago.getEditor().getText().trim();

        if (estado == null) { mostrarMensaje("Selecciona un estado.", true); return; }
        if (pago.isEmpty()) { mostrarMensaje("Selecciona o escribe un método de pago.", true); return; }

        final boolean cambiaEstado = estado != original.getEstado();
        final boolean cambiaPago = !pago.equals(original.getMetodoPago());
        if (!cambiaEstado && !cambiaPago) {
            mostrarMensaje("No hay cambios que aplicar.", true);
            return;
        }

        ejecutar(() -> {
            PedidoInterface s = servicio(model.getPedidoService());
            // Cambiar el estado pasa por setPedidoStatus para que se ajuste el inventario.
            // La interfaz sigue recibiendo texto; el servicio lo convierte con Pedido.Estado.desdeTexto(...).
            if (cambiaEstado && !s.setPedidoStatus(original.getId(), estado.getTexto())) {
                throw new IllegalStateException("No se pudo cambiar el estado del pedido.");
            }
            if (cambiaPago && !s.setPaymentMethodPerPedido(original.getId(), pago)) {
                throw new IllegalStateException("No se pudo cambiar el método de pago.");
            }
            return original.getId();
        }, id -> cargar(id, "Pedido actualizado."));
    }

    @FXML
    private void handleAgregarProducto() {
        if (seleccionado == null) {
            mostrarMensaje("Primero registra o selecciona un pedido.", true);
            return;
        }
        final Producto producto = cmbProducto.getValue();
        if (producto == null) {
            mostrarMensaje("Selecciona el producto que quieres agregar.", true);
            return;
        }
        final int idPedido = seleccionado.getId();
        ejecutar(() -> servicio(model.getPedidoService()).addProductoPerPedido(idPedido, producto), ok -> {
            if (ok) {
                cargar(idPedido, "Producto agregado al pedido.");
            } else {
                mostrarMensaje("No se pudo agregar el producto al pedido.", true);
            }
        });
    }

    @FXML
    private void handleQuitarProducto() {
        if (seleccionado == null) {
            return;
        }
        final Producto producto = lstProductos.getSelectionModel().getSelectedItem();
        if (producto == null) {
            mostrarMensaje("Selecciona en la lista el producto que quieres quitar.", true);
            return;
        }
        final int idPedido = seleccionado.getId();
        ejecutar(() -> servicio(model.getPedidoService()).removeProductoPerPedido(idPedido, producto), ok -> {
            if (ok) {
                cargar(idPedido, "Producto quitado del pedido.");
            } else {
                mostrarMensaje("No se pudo quitar el producto del pedido.", true);
            }
        });
    }

    @FXML
    private void handleEliminar() {
        if (seleccionado == null) {
            return;
        }
        final Pedido objetivo = seleccionado;
        confirmar("Eliminar pedido", "¿Eliminar el pedido #" + objetivo.getId() + "?", () ->
                ejecutar(() -> servicio(model.getPedidoService()).removePedido(objetivo.getId()), ok -> {
                    limpiarFormulario();
                    mostrarMensaje(ok ? "Pedido eliminado." : "No se eliminó ningún pedido.", !ok);
                    refrescar();
                }));
    }
}