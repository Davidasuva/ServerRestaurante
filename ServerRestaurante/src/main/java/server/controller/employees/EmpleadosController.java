package server.controller.employees;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import server.controller.SeccionBaseController;
import server.model.empleado.Empleado;
import server.model.empleado.EmpleadoInterface;

import java.util.List;

public class EmpleadosController extends SeccionBaseController {

    private final ObservableList<Empleado> datos = FXCollections.observableArrayList();
    private FilteredList<Empleado> filtrados;
    private Empleado seleccionado;

    @FXML private TextField txtBuscar;
    @FXML private TableView<Empleado> tablaEmpleados;
    @FXML private TableColumn<Empleado, Integer> colCedula;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colCargo;

    @FXML private Label lblTituloForm;
    @FXML private TextField txtCedula;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cmbCargo;
    @FXML private PasswordField txtContrasena;
    @FXML private Button btnGuardar;
    @FXML private Button btnEliminar;

    @Override
    protected void alIniciar() {
        configurarTabla();
        configurarFormulario();
        refrescar();
    }

    @Override
    public void refrescar() {
        recargar();
    }

    @Override
    protected void alCerrarForm() {
        limpiarFormulario();
    }

    private void configurarTabla() {
        colCedula.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCedula()));
        colNombre.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getNombre()));
        colCargo.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getCargo()));
        tablaEmpleados.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaEmpleados.setPlaceholder(new Label("No hay empleados para mostrar"));

        filtrados = new FilteredList<>(datos, e -> true);
        SortedList<Empleado> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tablaEmpleados.comparatorProperty());
        tablaEmpleados.setItems(ordenados);

        txtBuscar.textProperty().addListener((obs, o, n) -> {
            String q = n == null ? "" : n.trim().toLowerCase();
            filtrados.setPredicate(e -> q.isEmpty()
                    || e.getNombre().toLowerCase().contains(q)
                    || e.getCargo().toLowerCase().contains(q)
                    || String.valueOf(e.getCedula()).contains(q));
        });

        tablaEmpleados.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo != null) {
                cargarEnFormulario(nuevo);
            }
        });
    }

    private void configurarFormulario() {
        cmbCargo.getItems().addAll("Administrador", "Mesero", "Cocinero", "Cajero");
        limpiarFormulario();
    }

    private void cargarEnFormulario(Empleado e) {
        abrirForm();
        seleccionado = e;
        lblTituloForm.setText("Editar empleado");
        txtCedula.setText(String.valueOf(e.getCedula()));
        txtNombre.setText(e.getNombre());
        cmbCargo.setValue(e.getCargo());
        txtContrasena.setText(e.getContrasena());
        btnGuardar.setText("Actualizar");
        btnEliminar.setDisable(false);
        ocultarMensaje();
    }

    private void limpiarFormulario() {
        seleccionado = null;
        tablaEmpleados.getSelectionModel().clearSelection();
        lblTituloForm.setText("Nuevo empleado");
        txtCedula.clear();
        txtNombre.clear();
        cmbCargo.setValue(null);
        cmbCargo.getEditor().clear();
        txtContrasena.clear();
        btnGuardar.setText("Registrar");
        btnEliminar.setDisable(true);
        ocultarMensaje();
    }

    @FXML
    private void handleNuevo() {
        limpiarFormulario();
        abrirForm();
        txtCedula.requestFocus();
    }

    @FXML
    public void handleRecargar() {
        recargar();
    }

    private void recargar() {
        ejecutar(() -> {
            EmpleadoInterface s = model.getEmpleadoService();
            int total = s.contar();
            return total == 0 ? List.<Empleado>of() : s.getEmpleados(0, total - 1);
        }, lista -> datos.setAll(lista));
    }

    @FXML
    private void handleGuardar() {
        String cedulaTxt = txtCedula.getText().trim();
        String nombre = txtNombre.getText().trim();
        String cargo = cmbCargo.getEditor().getText().trim();
        String contrasena = txtContrasena.getText();

        int cedula;
        try {
            cedula = Integer.parseInt(cedulaTxt);
            if (cedula <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarMensaje("La cédula debe ser un número positivo.", true);
            return;
        }
        if (nombre.isEmpty()) { mostrarMensaje("Ingresa el nombre.", true); return; }
        if (cargo.isEmpty()) { mostrarMensaje("Selecciona o escribe un cargo.", true); return; }
        if (contrasena.isEmpty()) { mostrarMensaje("Ingresa la contraseña.", true); return; }

        final Empleado nuevo = new Empleado(cedula, cargo, nombre, contrasena);
        final Empleado original = seleccionado;

        ejecutar(() -> {
            EmpleadoInterface s = model.getEmpleadoService();
            return original == null ? s.registrar(nuevo) : s.modifyEmpleado(original.getCedula(), nuevo);
        }, resultado -> {
            boolean eraNuevo = original == null;
            limpiarFormulario();
            mostrarMensaje(eraNuevo ? "Empleado registrado." : "Empleado actualizado.", false);
            recargar();
        });
    }

    @FXML
    private void handleEliminar() {
        if (seleccionado == null) {
            return;
        }
        final Empleado objetivo = seleccionado;
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar a " + objetivo.getNombre() + " (cédula " + objetivo.getCedula() + ")?",
                ButtonType.OK, ButtonType.CANCEL);
        confirmar.setHeaderText("Eliminar empleado");
        confirmar.showAndWait().ifPresent(respuesta -> {
            if (respuesta != ButtonType.OK) {
                return;
            }
            ejecutar(() -> model.getEmpleadoService().removeEmpleado(objetivo.getCedula()), ok -> {
                limpiarFormulario();
                mostrarMensaje(ok ? "Empleado eliminado." : "No se eliminó ningún empleado.", !ok);
                recargar();
            });
        });
    }
}
