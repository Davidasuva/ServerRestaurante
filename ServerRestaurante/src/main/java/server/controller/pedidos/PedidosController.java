package server.controller.pedidos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
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
import server.controller.BarraPaginacion;
import server.controller.SeccionBaseController;
import server.model.empleado.Empleado;
import server.model.empleado.EmpleadoInterface;
import server.model.mesa.Mesa;
import server.model.mesa.MesaInterface;
import server.model.pedido.Pedido;
import server.model.pedido.PedidoInterface;
import server.model.producto.Producto;
import server.model.producto.ProductoInterface;

public class PedidosController
        extends SeccionBaseController {
    private static final List<String> METODOS_PAGO = List.of("Ninguno", "Efectivo", "Tarjeta","Transferencia","Nequi");
    private static final String TODOS = "Todos";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final StringConverter<Mesa> CONVERTIDOR_MESA = new StringConverter<Mesa>(){

        @Override
        public String toString(Mesa m4) {
            return m4 == null ? "" : "Mesa " + m4.getId();
        }

        @Override
        public Mesa fromString(String s2) {
            return null;
        }
    };
    private static final StringConverter<Producto> CONVERTIDOR_PRODUCTO = new StringConverter<Producto>(){

        @Override
        public String toString(Producto p) {
            return p == null ? "" : p.getNombre() + " — " + MONEDA.format(p.getPrecio());
        }

        @Override
        public Producto fromString(String s2) {
            return null;
        }
    };
    private static final StringConverter<Empleado> CONVERTIDOR_EMPLEADO = new StringConverter<Empleado>(){

        @Override
        public String toString(Empleado e) {
            return e == null ? "" : e.getNombre() + " (" + e.getCargo() + ")";
        }

        @Override
        public Empleado fromString(String s2) {
            return null;
        }
    };
    private final ObservableList<Pedido> datos = FXCollections.observableArrayList();
    private final ObservableList<Mesa> mesas = FXCollections.observableArrayList();
    private final ObservableList<Producto> productosDisponibles = FXCollections.observableArrayList();
    private final ObservableList<Producto> productosPedido = FXCollections.observableArrayList();
    private final ObservableList<Empleado> empleadosDisponibles = FXCollections.observableArrayList();
    private final ObservableList<Empleado> encargadosPedido = FXCollections.observableArrayList();
    private FilteredList<Pedido> filtrados;
    private Pedido seleccionado;
    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<String> cmbFiltroEstado;
    @FXML
    private TableView<Pedido> tablaPedidos;
    @FXML
    private TableColumn<Pedido, Integer> colId;
    @FXML
    private TableColumn<Pedido, String> colFecha;
    @FXML
    private TableColumn<Pedido, Integer> colMesa;
    @FXML
    private TableColumn<Pedido, String> colEstado;
    @FXML
    private TableColumn<Pedido, String> colPago;
    @FXML
    private TableColumn<Pedido, Float> colTotal;
    @FXML
    private Label lblTituloForm;
    @FXML
    private TextField txtId;
    @FXML
    private ComboBox<Mesa> cmbMesa;
    @FXML
    private Label lblFecha;
    @FXML
    private Label lblTotal;
    @FXML
    private ComboBox<Pedido.Estado> cmbEstado;
    @FXML
    private ComboBox<String> cmbPago;
    @FXML
    private VBox boxProductos;
    @FXML
    private ListView<Producto> lstProductos;
    @FXML
    private ComboBox<Producto> cmbProducto;
    @FXML
    private VBox boxEncargados;
    @FXML
    private ListView<Empleado> lstEncargados;
    @FXML
    private ComboBox<Empleado> cmbEncargado;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnEliminar;
    private final BarraPaginacion paginacion = new BarraPaginacion(() -> this.cargar(null, null));

    @Override
    protected void alIniciar() {
        this.configurarTabla();
        this.paginacion.instalarDebajoDe(this.tablaPedidos);
        this.configurarFormulario();
        this.limpiarFormulario();
        this.refrescar();
    }

    private void configurarTabla() {
        this.colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<Integer>(((Pedido)c.getValue()).getId()));
        this.colFecha.setCellValueFactory(c -> new ReadOnlyObjectWrapper<String>(PedidosController.formatearFecha(((Pedido)c.getValue()).getFechaPedido())));
        this.colMesa.setCellValueFactory(c -> new ReadOnlyObjectWrapper<Integer>(PedidosController.idMesa((Pedido)c.getValue())));
        this.colEstado.setCellValueFactory(c -> new ReadOnlyObjectWrapper<String>(PedidosController.textoEstado((Pedido)c.getValue())));
        this.colPago.setCellValueFactory(c -> new ReadOnlyObjectWrapper<String>(((Pedido)c.getValue()).getMetodoPago()));
        this.colTotal.setCellValueFactory(c -> new ReadOnlyObjectWrapper<Float>(Float.valueOf(((Pedido)c.getValue()).getPrecioTotal())));
        this.colTotal.setCellFactory(col -> new TableCell<Pedido, Float>(){

            @Override
            protected void updateItem(Float total, boolean vacio) {
                super.updateItem(total, vacio);
                this.setText(vacio || total == null ? null : MONEDA.format(total));
            }
        });
        this.tablaPedidos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        this.tablaPedidos.setPlaceholder(new Label("No hay pedidos para mostrar"));
        this.filtrados = new FilteredList<Pedido>(this.datos, p -> true);
        SortedList<Pedido> ordenados = new SortedList<Pedido>((ObservableList<Pedido>)this.filtrados);
        ordenados.comparatorProperty().bind(this.tablaPedidos.comparatorProperty());
        this.tablaPedidos.setItems(ordenados);
        this.txtBuscar.textProperty().addListener((obs, o, n) -> this.aplicarFiltro());
        this.cmbFiltroEstado.valueProperty().addListener((obs, o, n) -> this.aplicarFiltro());
        this.tablaPedidos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo != null) {
                this.cargarEnFormulario((Pedido)nuevo);
            }
        });
    }

    private void configurarFormulario() {
        this.cmbMesa.setItems(this.mesas);
        this.cmbMesa.setConverter(CONVERTIDOR_MESA);
        this.cmbPago.getItems().setAll((Collection<String>)METODOS_PAGO);
        this.cmbProducto.setItems(this.productosDisponibles);
        this.cmbProducto.setConverter(CONVERTIDOR_PRODUCTO);
        this.lstProductos.setItems(this.productosPedido);
        this.lstProductos.setPlaceholder(new Label("Sin productos"));
        this.lstProductos.setCellFactory(l -> new ListCell<Producto>(){

            @Override
            protected void updateItem(Producto p, boolean vacio) {
                super.updateItem(p, vacio);
                this.setText(vacio || p == null ? null : CONVERTIDOR_PRODUCTO.toString(p));
            }
        });
        this.cmbEncargado.setItems(this.empleadosDisponibles);
        this.cmbEncargado.setConverter(CONVERTIDOR_EMPLEADO);
        this.lstEncargados.setItems(this.encargadosPedido);
        this.lstEncargados.setPlaceholder(new Label("Sin encargados"));
        this.lstEncargados.setCellFactory(l -> new ListCell<Empleado>(){

            @Override
            protected void updateItem(Empleado e, boolean vacio) {
                super.updateItem(e, vacio);
                this.setText(vacio || e == null ? null : CONVERTIDOR_EMPLEADO.toString(e));
            }
        });
        this.cmbEstado.getItems().setAll(Pedido.Estado.values());
        ArrayList<String> filtro = new ArrayList<String>();
        filtro.add(TODOS);
        for (Pedido.Estado e : Pedido.Estado.values()) {
            filtro.add(e.getTexto());
        }
        this.cmbFiltroEstado.getItems().setAll((Collection<String>)filtro);
        this.cmbFiltroEstado.setValue(TODOS);
    }

    private void aplicarFiltro() {
        String q = this.txtBuscar.getText() == null ? "" : this.txtBuscar.getText().trim().toLowerCase();
        String estado = (String)this.cmbFiltroEstado.getValue();
        boolean todos = estado == null || TODOS.equals(estado);
        this.filtrados.setPredicate(p -> !(!todos && !estado.equalsIgnoreCase(PedidosController.textoEstado(p)) || !q.isEmpty() && !String.valueOf(p.getId()).contains(q) && !String.valueOf(PedidosController.idMesa(p)).contains(q) && !PedidosController.textoEstado(p).toLowerCase().contains(q) && !PedidosController.nvl(p.getMetodoPago()).toLowerCase().contains(q)));
    }

    private static String textoEstado(Pedido p) {
        return p.getEstado() == null ? "" : p.getEstado().getTexto();
    }

    private static String nvl(String s2) {
        return s2 == null ? "" : s2;
    }

    private static String formatearFecha(LocalDateTime f) {
        return f == null ? "" : f.format(FORMATO_FECHA);
    }

    private static int idMesa(Pedido p) {
        return p.getMesaAsignada() == null ? 0 : p.getMesaAsignada().getId();
    }

    private void cargarEnFormulario(Pedido p) {
        this.abrirForm();
        this.seleccionado = p;
        this.lblTituloForm.setText("Pedido #" + p.getId());
        this.txtId.setText(String.valueOf(p.getId()));
        this.txtId.setDisable(true);
        this.cmbMesa.setValue(this.mesas.stream().filter(m4 -> m4.getId() == PedidosController.idMesa(p)).findFirst().orElse(p.getMesaAsignada()));
        this.cmbMesa.setDisable(true);
        this.lblFecha.setText(PedidosController.formatearFecha(p.getFechaPedido()));
        this.lblTotal.setText(MONEDA.format(p.getPrecioTotal()));
        this.cmbEstado.setDisable(false);
        this.cmbEstado.setValue(p.getEstado());
        this.cmbPago.setDisable(false);
        this.cmbPago.setValue(p.getMetodoPago());
        this.btnGuardar.setText("Aplicar cambios");
        this.btnEliminar.setDisable(false);
        this.boxProductos.setDisable(false);
        this.boxEncargados.setDisable(false);
        this.ocultarMensaje();
        this.cargarDetalle(p.getId());
    }

    private void limpiarFormulario() {
        this.seleccionado = null;
        this.tablaPedidos.getSelectionModel().clearSelection();
        this.lblTituloForm.setText("Nuevo pedido");
        this.txtId.clear();
        this.txtId.setDisable(false);
        this.cmbMesa.setValue(null);
        this.cmbMesa.setDisable(false);
        this.lblFecha.setText("Se asigna al registrar");
        this.lblTotal.setText(MONEDA.format(0L));
        this.cmbEstado.setValue(Pedido.Estado.PENDIENTE);
        this.cmbEstado.setDisable(true);
        this.cmbPago.setValue("Ninguno");
        this.cmbPago.setDisable(true);
        this.productosPedido.clear();
        this.encargadosPedido.clear();
        this.cmbProducto.setValue(null);
        this.boxProductos.setDisable(true);
        this.cmbEncargado.setValue(null);
        this.boxEncargados.setDisable(true);
        this.btnGuardar.setText("Registrar");
        this.btnEliminar.setDisable(true);
        this.ocultarMensaje();
    }

    @Override
    protected void alCerrarForm() {
        this.limpiarFormulario();
    }

    @FXML
    private void handleNuevo() {
        this.limpiarFormulario();
        this.abrirForm();
        this.txtId.requestFocus();
    }

    @FXML
    public void handleRecargar() {
        this.refrescar();
    }

    @Override
    public void refrescar() {
        this.cargar(null, null, true);
    }

    private void cargar(Integer idASeleccionar, String mensajeOk) {
        this.cargar(idASeleccionar, mensajeOk, false);
    }

    private void cargar(Integer idASeleccionar, String mensajeOk, boolean recargarCombos) {
        int pagina = this.paginacion.getPagina();
        int tamano = this.paginacion.getTamano();
        this.ejecutar(() -> {
            PedidoInterface ps = PedidosController.servicio(this.model.getPedidoService());
            BarraPaginacion.Pagina<Pedido> pedidos = BarraPaginacion.leer(pagina, tamano, ps.contar(), ps::getPedidos);
            List listaMesas = null;
            List listaProductos = null;
            List listaEmpleados = null;
            if (recargarCombos) {
                MesaInterface ms = PedidosController.servicio(this.model.getMesaService());
                ProductoInterface prs = PedidosController.servicio(this.model.getProductoService());
                int totalM = ms.contar();
                int totalPr = prs.contar();
                listaMesas = totalM == 0 ? List.of() : ms.getMesa(0, totalM - 1);
                listaProductos = totalPr == 0 ? List.of() : prs.getProductos(0, totalPr - 1);
                EmpleadoInterface es = PedidosController.servicio(this.model.getEmpleadoService());
                int totalE = es.contar();
                listaEmpleados = totalE == 0 ? List.of() : es.getEmpleados(0, totalE - 1);
            }
            return new Carga(pedidos, listaMesas, listaProductos, listaEmpleados);
        }, carga -> {
            this.paginacion.mostrar(carga.pedidos());
            List<Pedido> lista = carga.pedidos().items();
            this.datos.setAll((Collection<Pedido>)lista);
            if (carga.mesas() != null) {
                this.mesas.setAll((Collection<Mesa>)carga.mesas());
            }
            if (carga.productos() != null) {
                this.productosDisponibles.setAll((Collection<Producto>)carga.productos());
            }
            if (carga.empleados() != null) {
                this.empleadosDisponibles.setAll((Collection<Empleado>)carga.empleados());
            }
            if (idASeleccionar != null) {
                lista.stream().filter(p -> p.getId() == idASeleccionar.intValue()).findFirst().ifPresentOrElse(p -> this.tablaPedidos.getSelectionModel().select(p), () -> {
                    if (this.seleccionado == null) {
                        this.limpiarFormulario();
                    }
                });
            }
            if (mensajeOk != null) {
                this.mostrarMensaje(mensajeOk, false);
            }
        });
    }

    private void cargarDetalle(int idPedido) {
        this.ejecutar(() -> {
            PedidoInterface s2 = PedidosController.servicio(this.model.getPedidoService());
            List<Producto> productos = PedidosController.listaOVacia(() -> s2.getProductosPerPedido(idPedido));
            List<Empleado> encargados = PedidosController.listaOVacia(() -> s2.getEncargadosPerPedido(idPedido));
            return new Detalle(productos, encargados);
        }, detalle -> {
            if (this.seleccionado != null && this.seleccionado.getId() == idPedido) {
                this.productosPedido.setAll((Collection<Producto>)detalle.productos());
                this.encargadosPedido.setAll((Collection<Empleado>)detalle.encargados());
            }
        });
    }

    private static <T> List<T> listaOVacia(Callable<List<T>> consulta) {
        try {
            return consulta.call();
        }
        catch (Exception e) {
            return List.of();
        }
    }

    @FXML
    private void handleGuardar() {
        if (this.seleccionado == null) {
            this.registrarNuevo();
        } else {
            this.aplicarCambios();
        }
    }

    private void registrarNuevo() {
        int id;
        try {
            id = Integer.parseInt(this.txtId.getText().trim());
            if (id <= 0) {
                throw new NumberFormatException();
            }
        }
        catch (NumberFormatException ex) {
            this.mostrarMensaje("El id debe ser un entero positivo.", true);
            return;
        }
        Mesa mesa = (Mesa)this.cmbMesa.getValue();
        if (mesa == null) {
            this.mostrarMensaje("Selecciona la mesa del pedido.", true);
            return;
        }
        Pedido nuevo = new Pedido(id, LocalDateTime.now(), mesa);
        this.ejecutar(() -> PedidosController.servicio(this.model.getPedidoService()).registrarPedido(nuevo), r -> this.cargar(r.getId(), "Pedido registrado."));
    }

    private void aplicarCambios() {
        boolean cambiaPago;
        Pedido original = this.seleccionado;
        Pedido.Estado estado = this.cmbEstado.getValue();
        String pago = this.cmbPago.getEditor().getText().trim();
        if (estado == null) {
            this.mostrarMensaje("Selecciona un estado.", true);
            return;
        }
        if (pago.isEmpty()) {
            this.mostrarMensaje("Selecciona o escribe un método de pago.", true);
            return;
        }
        boolean cambiaEstado = estado != original.getEstado();
        boolean bl = cambiaPago = !pago.equals(original.getMetodoPago());
        if (!cambiaEstado && !cambiaPago) {
            this.mostrarMensaje("No hay cambios que aplicar.", true);
            return;
        }
        this.ejecutar(() -> {
            PedidoInterface s2 = PedidosController.servicio(this.model.getPedidoService());
            if (cambiaEstado && !s2.setPedidoStatus(original.getId(), estado.getTexto())) {
                throw new IllegalStateException("No se pudo cambiar el estado del pedido.");
            }
            if (cambiaPago && !s2.setPaymentMethodPerPedido(original.getId(), pago)) {
                throw new IllegalStateException("No se pudo cambiar el método de pago.");
            }
            return original.getId();
        }, id -> this.cargar((Integer)id, "Pedido actualizado."));
    }

    @FXML
    private void handleAgregarProducto() {
        if (this.seleccionado == null) {
            this.mostrarMensaje("Primero registra o selecciona un pedido.", true);
            return;
        }
        Producto producto = (Producto)this.cmbProducto.getValue();
        if (producto == null) {
            this.mostrarMensaje("Selecciona el producto que quieres agregar.", true);
            return;
        }
        int idPedido = this.seleccionado.getId();
        this.ejecutar(() -> PedidosController.servicio(this.model.getPedidoService()).addProductoPerPedido(idPedido, producto), ok -> {
            if (ok.booleanValue()) {
                this.cargar(idPedido, "Producto agregado al pedido.");
            } else {
                this.mostrarMensaje("No se pudo agregar el producto al pedido.", true);
            }
        });
    }

    @FXML
    private void handleQuitarProducto() {
        if (this.seleccionado == null) {
            return;
        }
        Producto producto = (Producto)this.lstProductos.getSelectionModel().getSelectedItem();
        if (producto == null) {
            this.mostrarMensaje("Selecciona en la lista el producto que quieres quitar.", true);
            return;
        }
        int idPedido = this.seleccionado.getId();
        this.ejecutar(() -> PedidosController.servicio(this.model.getPedidoService()).removeProductoPerPedido(idPedido, producto), ok -> {
            if (ok.booleanValue()) {
                this.cargar(idPedido, "Producto quitado del pedido.");
            } else {
                this.mostrarMensaje("No se pudo quitar el producto del pedido.", true);
            }
        });
    }

    @FXML
    private void handleAgregarEncargado() {
        if (this.seleccionado == null) {
            this.mostrarMensaje("Primero registra o selecciona un pedido.", true);
            return;
        }
        Empleado empleado = (Empleado)this.cmbEncargado.getValue();
        if (empleado == null) {
            this.mostrarMensaje("Selecciona el encargado que quieres agregar.", true);
            return;
        }
        if (this.encargadosPedido.contains(empleado)) {
            this.mostrarMensaje("Ese empleado ya es encargado de este pedido.", true);
            return;
        }
        int idPedido = this.seleccionado.getId();
        this.ejecutar(() -> PedidosController.servicio(this.model.getPedidoService()).addEncargadoPerPedido(idPedido, empleado), ok -> {
            if (ok.booleanValue()) {
                this.cmbEncargado.setValue(null);
                this.cargar(idPedido, "Encargado agregado al pedido.");
            } else {
                this.mostrarMensaje("No se pudo agregar el encargado al pedido.", true);
            }
        });
    }

    @FXML
    private void handleQuitarEncargado() {
        if (this.seleccionado == null) {
            return;
        }
        Empleado empleado = (Empleado)this.lstEncargados.getSelectionModel().getSelectedItem();
        if (empleado == null) {
            this.mostrarMensaje("Selecciona en la lista el encargado que quieres quitar.", true);
            return;
        }
        int idPedido = this.seleccionado.getId();
        this.ejecutar(() -> PedidosController.servicio(this.model.getPedidoService()).removeEncargadoPerPedido(idPedido, empleado), ok -> {
            if (ok.booleanValue()) {
                this.cargar(idPedido, "Encargado quitado del pedido.");
            } else {
                this.mostrarMensaje("No se pudo quitar el encargado del pedido.", true);
            }
        });
    }

    @FXML
    private void handleEliminar() {
        if (this.seleccionado == null) {
            return;
        }
        Pedido objetivo = this.seleccionado;
        this.confirmar("Eliminar pedido", "¿Eliminar el pedido #" + objetivo.getId() + "?", () -> this.ejecutar(() -> PedidosController.servicio(this.model.getPedidoService()).removePedido(objetivo.getId()), ok -> {
            this.limpiarFormulario();
            this.mostrarMensaje(ok ? "Pedido eliminado." : "No se eliminó ningún pedido.", !ok);
            this.refrescar();
        }));
    }

    private record Detalle(List<Producto> productos, List<Empleado> encargados) {
    }

    private record Carga(BarraPaginacion.Pagina<Pedido> pedidos, List<Mesa> mesas, List<Producto> productos, List<Empleado> empleados) {
    }
}