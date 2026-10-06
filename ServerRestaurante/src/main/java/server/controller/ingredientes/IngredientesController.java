package server.controller.ingredientes;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import server.controller.BarraPaginacion;
import server.controller.SeccionBaseController;
import server.model.ingrediente.Ingrediente;
import server.model.ingrediente.IngredienteInterface;

import java.util.List;

public class IngredientesController extends SeccionBaseController {

    private final ObservableList<Ingrediente> datos = FXCollections.observableArrayList();
    private FilteredList<Ingrediente> filtrados;
    private Ingrediente seleccionado;

    @FXML private TextField txtBuscar;
    @FXML private TableView<Ingrediente> tablaIngredientes;
    @FXML private TableColumn<Ingrediente, Integer> colId;
    @FXML private TableColumn<Ingrediente, String> colNombre;
    @FXML private TableColumn<Ingrediente, Integer> colCantidad;
    @FXML private TableColumn<Ingrediente, String> colDescripcion;

    @FXML private Label lblTituloForm;
    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCantidad;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtAjuste;
    @FXML private Button btnAjustar;
    @FXML private Button btnGuardar;
    @FXML private Button btnEliminar;

    private final BarraPaginacion paginacion = new BarraPaginacion(() -> cargar(null, null));

    @Override
    protected void alIniciar() {
        configurarTabla();
        paginacion.instalarDebajoDe(tablaIngredientes);
        limpiarFormulario();
        refrescar();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        colNombre.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getNombre()));
        colCantidad.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCantidad()));
        colDescripcion.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getDescripcion()));
        tablaIngredientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaIngredientes.setPlaceholder(new Label("No hay ingredientes para mostrar"));

        filtrados = new FilteredList<>(datos, i -> true);
        SortedList<Ingrediente> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tablaIngredientes.comparatorProperty());
        tablaIngredientes.setItems(ordenados);

        txtBuscar.textProperty().addListener((obs, o, n) -> {
            String q = n == null ? "" : n.trim().toLowerCase();
            filtrados.setPredicate(i -> q.isEmpty()
                    || nvl(i.getNombre()).toLowerCase().contains(q)
                    || nvl(i.getDescripcion()).toLowerCase().contains(q)
                    || String.valueOf(i.getId()).contains(q));
        });

        tablaIngredientes.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo != null) {
                cargarEnFormulario(nuevo);
            }
        });
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private void cargarEnFormulario(Ingrediente i) {
        abrirForm();
        seleccionado = i;
        lblTituloForm.setText("Editar ingrediente");
        txtId.setText(String.valueOf(i.getId()));
        txtId.setDisable(true); // el servicio no permite cambiar el id de un ingrediente
        txtNombre.setText(i.getNombre());
        txtCantidad.setText(String.valueOf(i.getCantidad()));
        txtDescripcion.setText(i.getDescripcion());
        txtAjuste.clear();
        txtAjuste.setDisable(false);
        btnAjustar.setDisable(false);
        btnGuardar.setText("Actualizar");
        btnEliminar.setDisable(false);
        ocultarMensaje();
    }

    private void limpiarFormulario() {
        seleccionado = null;
        tablaIngredientes.getSelectionModel().clearSelection();
        lblTituloForm.setText("Nuevo ingrediente");
        txtId.clear();
        txtId.setDisable(false);
        txtNombre.clear();
        txtCantidad.clear();
        txtDescripcion.clear();
        txtAjuste.clear();
        txtAjuste.setDisable(true);
        btnAjustar.setDisable(true);
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
        cargar(null, null);
    }

    /** Recarga la tabla; si se indica, reselecciona ese ingrediente y muestra el mensaje de éxito. */
    private void cargar(Integer idASeleccionar, String mensajeOk) {
        final int pagina = paginacion.getPagina();
        final int tamano = paginacion.getTamano();
        ejecutar(() -> {
            IngredienteInterface s = servicio(model.getIngredienteService());
            return BarraPaginacion.leer(pagina, tamano, s.contar(), s::getIngredientes);
        }, resultado -> {
            paginacion.mostrar(resultado);
            List<Ingrediente> lista = resultado.items();
            datos.setAll(lista);
            if (idASeleccionar != null) {
                lista.stream().filter(i -> i.getId() == idASeleccionar).findFirst()
                        .ifPresentOrElse(
                                i -> tablaIngredientes.getSelectionModel().select(i),
                                () -> { if (seleccionado == null) limpiarFormulario(); }); // quedó en otra página
            }
            if (mensajeOk != null) {
                mostrarMensaje(mensajeOk, false); // después de seleccionar, porque cargarEnFormulario oculta el mensaje
            }
        });
    }

    @FXML
    private void handleGuardar() {
        int id;
        int cantidad;
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        try {
            id = Integer.parseInt(txtId.getText().trim());
            if (id <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarMensaje("El id debe ser un entero positivo.", true);
            return;
        }
        if (nombre.isEmpty()) { mostrarMensaje("Ingresa el nombre.", true); return; }
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
            if (cantidad < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarMensaje("La cantidad debe ser un entero mayor o igual a 0.", true);
            return;
        }

        final Ingrediente nuevo = new Ingrediente(id, descripcion, cantidad, nombre);
        final Ingrediente original = seleccionado;

        ejecutar(() -> {
            IngredienteInterface s = servicio(model.getIngredienteService());
            Ingrediente r = original == null ? s.registrar(nuevo) : s.modifyIngrediente(original.getId(), nuevo);
            if (r == null) {
                throw new IllegalStateException("No se encontró el ingrediente a actualizar.");
            }
            return r;
        }, r -> {
            boolean eraNuevo = original == null;
            limpiarFormulario();
            mostrarMensaje(eraNuevo ? "Ingrediente registrado." : "Ingrediente actualizado.", false);
            refrescar();
        });
    }

    /** Suma o resta stock sin tocar los demás campos (ej: 10 o -3). */
    @FXML
    private void handleAjustar() {
        if (seleccionado == null) {
            return;
        }
        int delta;
        try {
            delta = Integer.parseInt(txtAjuste.getText().trim()); // acepta "+10" y "-3"
            if (delta == 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarMensaje("Ingresa un número distinto de 0 (ej: 10 o -3).", true);
            return;
        }

        final int id = seleccionado.getId();
        ejecutar(() -> servicio(model.getIngredienteService()).ajustarCantidad(id, delta), ok -> {
            if (ok) {
                cargar(id, "Inventario ajustado en " + delta + ".");
            } else {
                mostrarMensaje("No se pudo ajustar el inventario.", true);
            }
        });
    }

    @FXML
    private void handleEliminar() {
        if (seleccionado == null) {
            return;
        }
        final Ingrediente objetivo = seleccionado;
        confirmar("Eliminar ingrediente",
                "¿Eliminar \"" + objetivo.getNombre() + "\" (id " + objetivo.getId() + ")?", () ->
                        ejecutar(() -> servicio(model.getIngredienteService()).removeIngrediente(objetivo.getId()), ok -> {
                            limpiarFormulario();
                            mostrarMensaje(ok ? "Ingrediente eliminado." : "No se eliminó ningún ingrediente.", !ok);
                            refrescar();
                        }));
    }
}