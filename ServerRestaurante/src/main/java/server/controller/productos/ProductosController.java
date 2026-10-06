package server.controller.productos;

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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import server.controller.BarraPaginacion;
import server.controller.SeccionBaseController;
import server.model.ingrediente.Ingrediente;
import server.model.ingrediente.IngredienteInterface;
import server.model.producto.Producto;
import server.model.producto.ProductoInterface;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ProductosController extends SeccionBaseController {

    /** ingredientes es null cuando no se recargaron (solo se recargan al refrescar, no al cambiar de página). */
    private record Carga(BarraPaginacion.Pagina<Producto> productos, List<Ingrediente> ingredientes) {}

    private static final StringConverter<Ingrediente> CONVERTIDOR_INGREDIENTE = new StringConverter<>() {
        @Override public String toString(Ingrediente i) { return i == null ? "" : i.getNombre(); }
        @Override public Ingrediente fromString(String s) { return null; }
    };

    private final ObservableList<Producto> datos = FXCollections.observableArrayList();
    private final ObservableList<Ingrediente> ingredientesDisponibles = FXCollections.observableArrayList();
    private final ObservableList<Ingrediente> ingredientesDelProducto = FXCollections.observableArrayList();
    private FilteredList<Producto> filtrados;
    private Producto seleccionado;

    @FXML private TextField txtBuscar;
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Float> colPrecio;

    @FXML private Label lblTituloForm;
    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField txtPrecio;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtImagen;

    @FXML private VBox boxIngredientes;
    @FXML private ListView<Ingrediente> lstIngredientes;
    @FXML private ComboBox<Ingrediente> cmbIngrediente;

    @FXML private Button btnGuardar;
    @FXML private Button btnEliminar;

    /** Categorías vistas en las páginas cargadas, para que el combo no se limite a la página actual. */
    private final Set<String> categoriasConocidas = new TreeSet<>();

    private final BarraPaginacion paginacion = new BarraPaginacion(() -> cargar(null, null));

    @Override
    protected void alIniciar() {
        configurarTabla();
        paginacion.instalarDebajoDe(tablaProductos);
        configurarIngredientes();
        limpiarFormulario();
        refrescar();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        colNombre.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getNombre()));
        colCategoria.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCategoria()));
        colPrecio.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getPrecio()));
        colPrecio.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Float precio, boolean vacio) {
                super.updateItem(precio, vacio);
                setText(vacio || precio == null ? null : MONEDA.format(precio));
            }
        });
        tablaProductos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaProductos.setPlaceholder(new Label("No hay productos para mostrar"));

        filtrados = new FilteredList<>(datos, p -> true);
        SortedList<Producto> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tablaProductos.comparatorProperty());
        tablaProductos.setItems(ordenados);

        txtBuscar.textProperty().addListener((obs, o, n) -> {
            String q = n == null ? "" : n.trim().toLowerCase();
            filtrados.setPredicate(p -> q.isEmpty()
                    || nvl(p.getNombre()).toLowerCase().contains(q)
                    || nvl(p.getCategoria()).toLowerCase().contains(q)
                    || String.valueOf(p.getId()).contains(q));
        });

        tablaProductos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo != null) {
                cargarEnFormulario(nuevo);
            }
        });
    }

    private void configurarIngredientes() {
        cmbIngrediente.setItems(ingredientesDisponibles);
        cmbIngrediente.setConverter(CONVERTIDOR_INGREDIENTE);

        lstIngredientes.setItems(ingredientesDelProducto);
        lstIngredientes.setPlaceholder(new Label("Sin ingredientes asociados"));
        lstIngredientes.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(Ingrediente i, boolean vacio) {
                super.updateItem(i, vacio);
                setText(vacio || i == null ? null : i.getNombre() + "  (stock: " + i.getCantidad() + ")");
            }
        });
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private void cargarEnFormulario(Producto p) {
        abrirForm();
        seleccionado = p;
        lblTituloForm.setText("Editar producto");
        txtId.setText(String.valueOf(p.getId()));
        txtId.setDisable(true);
        txtNombre.setText(p.getNombre());
        cmbCategoria.setValue(p.getCategoria());
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        txtDescripcion.setText(p.getDescripcion());
        txtImagen.setText(nvl(p.getImagenURL()));
        btnGuardar.setText("Actualizar");
        boxIngredientes.setDisable(false);
        btnEliminar.setDisable(false);
        ocultarMensaje();
        cargarIngredientesDe(p.getId());
    }

    private void limpiarFormulario() {
        seleccionado = null;
        tablaProductos.getSelectionModel().clearSelection();
        lblTituloForm.setText("Nuevo producto");
        txtId.clear();
        txtId.setDisable(false);
        txtNombre.clear();
        cmbCategoria.setValue(null);
        cmbCategoria.getEditor().clear();
        txtPrecio.clear();
        txtDescripcion.clear();
        txtImagen.clear();
        ingredientesDelProducto.clear();
        cmbIngrediente.setValue(null);
        boxIngredientes.setDisable(true); // primero hay que registrar el producto
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

    @Override
    public void refrescar() {
        cargar(null, null, true);
    }

    /** Recarga productos e ingredientes; si se indica, reselecciona ese producto y muestra el mensaje de éxito. */
    private void cargar(Integer idASeleccionar, String mensajeOk) {
        cargar(idASeleccionar, mensajeOk, false);
    }

    private void cargar(Integer idASeleccionar, String mensajeOk, boolean recargarIngredientes) {
        final int pagina = paginacion.getPagina();
        final int tamano = paginacion.getTamano();
        ejecutar(() -> {
            ProductoInterface ps = servicio(model.getProductoService());
            BarraPaginacion.Pagina<Producto> productos =
                    BarraPaginacion.leer(pagina, tamano, ps.contar(), ps::getProductos);
            List<Ingrediente> ingredientes = null;
            if (recargarIngredientes) {
                IngredienteInterface is = servicio(model.getIngredienteService());
                int totalI = is.contar();
                ingredientes = totalI == 0 ? List.<Ingrediente>of() : is.getIngredientes(0, totalI - 1);
            }
            return new Carga(productos, ingredientes);
        }, carga -> {
            paginacion.mostrar(carga.productos());
            List<Producto> lista = carga.productos().items();
            datos.setAll(lista);
            if (carga.ingredientes() != null) {
                ingredientesDisponibles.setAll(carga.ingredientes());
            }
            String categoriaActual = cmbCategoria.getEditor().getText(); // setAll puede borrar el valor de un combo editable
            lista.stream()
                    .map(Producto::getCategoria)
                    .filter(c -> c != null && !c.isBlank())
                    .forEach(categoriasConocidas::add);
            cmbCategoria.getItems().setAll(categoriasConocidas);
            if (categoriaActual != null && !categoriaActual.isBlank()) {
                cmbCategoria.setValue(categoriaActual);
            }
            if (idASeleccionar != null) {
                lista.stream().filter(p -> p.getId() == idASeleccionar).findFirst()
                        .ifPresentOrElse(
                                p -> tablaProductos.getSelectionModel().select(p),
                                () -> { if (seleccionado == null) limpiarFormulario(); }); // quedó en otra página
            }
            if (mensajeOk != null) {
                mostrarMensaje(mensajeOk, false); // después de seleccionar, porque cargarEnFormulario oculta el mensaje
            }
        });
    }

    private void cargarIngredientesDe(int idProducto) {
        ejecutar(() -> servicio(model.getProductoService()).getIngredientesPerProduct(idProducto), lista -> {
            if (seleccionado != null && seleccionado.getId() == idProducto) {
                ingredientesDelProducto.setAll(lista);
            }
        });
    }

    @FXML
    private void handleGuardar() {
        int id;
        float precio;
        String nombre = txtNombre.getText().trim();
        String categoria = cmbCategoria.getEditor().getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        String imagen = txtImagen.getText().trim();

        try {
            id = Integer.parseInt(txtId.getText().trim());
            if (id <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarMensaje("El id debe ser un entero positivo.", true);
            return;
        }
        if (nombre.isEmpty()) { mostrarMensaje("Ingresa el nombre.", true); return; }
        if (categoria.isEmpty()) { mostrarMensaje("Selecciona o escribe una categoría.", true); return; }
        try {
            precio = Float.parseFloat(txtPrecio.getText().trim().replace(',', '.'));
            if (precio < 0 || Float.isNaN(precio) || Float.isInfinite(precio)) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarMensaje("El precio debe ser un número mayor o igual a 0.", true);
            return;
        }

        final Producto nuevo = new Producto(id, precio, descripcion, categoria, nombre);
        if (!imagen.isEmpty()) {
            nuevo.setImagenURL(imagen);
        }
        final Producto original = seleccionado;

        ejecutar(() -> {
            ProductoInterface s = servicio(model.getProductoService());
            Producto r = original == null ? s.registrar(nuevo) : s.modifyProducto(original.getId(), nuevo);
            if (r == null) {
                throw new IllegalStateException("No se encontró el producto a actualizar.");
            }
            return r;
        }, r -> {
            if (original == null) {
                // Se deja seleccionado el producto nuevo para poder asociarle ingredientes enseguida.
                cargar(r.getId(), "Producto registrado. Ya puedes asociarle ingredientes.");
            } else {
                cargar(r.getId(), "Producto actualizado.");
            }
        });
    }

    @FXML
    private void handleEliminar() {
        if (seleccionado == null) {
            return;
        }
        final Producto objetivo = seleccionado;
        confirmar("Eliminar producto",
                "¿Eliminar \"" + objetivo.getNombre() + "\" (id " + objetivo.getId() + ")?", () ->
                        ejecutar(() -> servicio(model.getProductoService()).removeProducto(objetivo.getId()), ok -> {
                            limpiarFormulario();
                            mostrarMensaje(ok ? "Producto eliminado." : "No se eliminó ningún producto.", !ok);
                            refrescar();
                        }));
    }

    @FXML
    private void handleAgregarIngrediente() {
        if (seleccionado == null) {
            return;
        }
        Ingrediente ing = cmbIngrediente.getValue();
        if (ing == null) {
            mostrarMensaje("Selecciona el ingrediente que quieres agregar.", true);
            return;
        }
        final int idProducto = seleccionado.getId();
        ejecutar(() -> servicio(model.getProductoService()).addIngredienteToProducto(idProducto, ing.getId()), ok -> {
            if (ok) {
                mostrarMensaje("Ingrediente agregado al producto.", false);
                cargarIngredientesDe(idProducto);
            } else {
                mostrarMensaje("No se pudo agregar (¿ya estaba asociado?).", true);
            }
        });
    }

    @FXML
    private void handleQuitarIngrediente() {
        if (seleccionado == null) {
            return;
        }
        Ingrediente ing = lstIngredientes.getSelectionModel().getSelectedItem();
        if (ing == null) {
            mostrarMensaje("Selecciona en la lista el ingrediente que quieres quitar.", true);
            return;
        }
        final int idProducto = seleccionado.getId();
        ejecutar(() -> servicio(model.getProductoService()).removeIngredienteFromProducto(idProducto, ing.getId()), ok -> {
            mostrarMensaje(ok ? "Ingrediente quitado del producto." : "No se pudo quitar el ingrediente.", !ok);
            cargarIngredientesDe(idProducto);
        });
    }
}