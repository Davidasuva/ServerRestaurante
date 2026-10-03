package server.controller.mesas;

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
import javafx.scene.control.TextField;
import server.controller.SeccionBaseController;
import server.model.mesa.Mesa;
import server.model.mesa.MesaInterface;

import java.util.List;

public class MesasController extends SeccionBaseController {

    private final ObservableList<Mesa> datos = FXCollections.observableArrayList();
    private FilteredList<Mesa> filtrados;
    private Mesa seleccionada;

    @FXML private TextField txtBuscar;
    @FXML private TableView<Mesa> tablaMesas;
    @FXML private TableColumn<Mesa, Integer> colId;

    @FXML private Label lblTituloForm;
    @FXML private TextField txtId;
    @FXML private Button btnGuardar;
    @FXML private Button btnEliminar;

    @Override
    protected void alIniciar() {
        configurarTabla();
        limpiarFormulario();
        refrescar();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        tablaMesas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaMesas.setPlaceholder(new Label("No hay mesas para mostrar"));

        filtrados = new FilteredList<>(datos, m -> true);
        SortedList<Mesa> ordenadas = new SortedList<>(filtrados);
        ordenadas.comparatorProperty().bind(tablaMesas.comparatorProperty());
        tablaMesas.setItems(ordenadas);

        txtBuscar.textProperty().addListener((obs, o, n) -> {
            String q = n == null ? "" : n.trim();
            filtrados.setPredicate(m -> q.isEmpty() || String.valueOf(m.getId()).contains(q));
        });

        tablaMesas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nueva) -> {
            if (nueva != null) {
                cargarEnFormulario(nueva);
            }
        });
    }

    private void cargarEnFormulario(Mesa m) {
        abrirForm();
        seleccionada = m;
        lblTituloForm.setText("Editar mesa");
        txtId.setText(String.valueOf(m.getId()));
        btnGuardar.setText("Actualizar");
        btnEliminar.setDisable(false);
        ocultarMensaje();
    }

    private void limpiarFormulario() {
        seleccionada = null;
        tablaMesas.getSelectionModel().clearSelection();
        lblTituloForm.setText("Nueva mesa");
        txtId.clear();
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
        ejecutar(() -> {
            MesaInterface s = servicio(model.getMesaService());
            int total = s.contar();
            return total == 0 ? List.<Mesa>of() : s.getMesa(0, total - 1);
        }, lista -> datos.setAll(lista));
    }

    @FXML
    private void handleGuardar() {
        int id;
        try {
            id = Integer.parseInt(txtId.getText().trim());
            if (id <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarMensaje("El número de mesa debe ser un entero positivo.", true);
            return;
        }

        final Mesa nueva = new Mesa(id);
        final Mesa original = seleccionada;

        ejecutar(() -> {
            MesaInterface s = servicio(model.getMesaService());
            // OJO: modifyMesa recibe (nuevaMesa, idActual) — al revés que los otros servicios.
            Mesa r = original == null ? s.registrar(nueva) : s.modifyMesa(nueva, original.getId());
            if (r == null) {
                throw new IllegalStateException("No se encontró la mesa a actualizar.");
            }
            return r;
        }, r -> {
            boolean eraNueva = original == null;
            limpiarFormulario();
            mostrarMensaje(eraNueva ? "Mesa registrada." : "Mesa actualizada.", false);
            refrescar();
        });
    }

    @FXML
    private void handleEliminar() {
        if (seleccionada == null) {
            return;
        }
        final Mesa objetivo = seleccionada;
        confirmar("Eliminar mesa", "¿Eliminar la mesa " + objetivo.getId() + "?", () ->
                ejecutar(() -> servicio(model.getMesaService()).removeMesa(objetivo.getId()), ok -> {
                    limpiarFormulario();
                    mostrarMensaje(ok ? "Mesa eliminada." : "No se eliminó ninguna mesa.", !ok);
                    refrescar();
                }));
    }
}
