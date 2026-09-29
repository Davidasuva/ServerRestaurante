package server.controller.login;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import server.model.empleado.Empleado;
import server.model.empleado.EmpleadoInterface;

import java.net.URL;
import java.util.function.Consumer;

public class LoginController {

    private static final String CARGO_ADMIN = "Administrador";

    private final EmpleadoInterface empleadoService;
    private final Consumer<Empleado> onLoginExitoso;

    @FXML private ImageView logo;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btLogin;

    public LoginController(EmpleadoInterface empleadoService, Consumer<Empleado> onLoginExitoso) {
        this.empleadoService = empleadoService;
        this.onLoginExitoso = onLoginExitoso;
    }

    // Se ejecuta sola después de cargar el FXML
    @FXML
    private void initialize() {
        lblError.setText("");

        // La ruta del FXML sirve en Scene Builder pero no al ejecutar, así que se carga aquí
        URL url = getClass().getResource("/images/Logo.png");
        if (url != null) {
            logo.setImage(new Image(url.toExternalForm()));
        }
    }

    @FXML
    private void onLogin() {
        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText();

        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarError("Ingresa un usuario y contraseña");
            return;
        }

        int cedula;
        try {
            cedula = Integer.parseInt(usuario);
        } catch (NumberFormatException e) {
            mostrarError("El usuario debe ser un número de cédula");
            return;
        }

        lblError.setText("");
        btLogin.setDisable(true);

        Task<Empleado> tarea = new Task<>() {
            @Override
            protected Empleado call() throws Exception {
                Empleado empleado = empleadoService.getEmpleadoByCedula(cedula);
                if (!CARGO_ADMIN.equals(empleado.getCargo())) {
                    throw new Exception("Solo los administradores pueden iniciar sesión en el servidor");
                }
                if (!password.equals(empleado.getContrasena())) {
                    throw new Exception("Contraseña incorrecta");
                }
                return empleado;
            }
        };

        tarea.setOnSucceeded(e -> {
            btLogin.setDisable(false);
            onLoginExitoso.accept(tarea.getValue());
        });

        tarea.setOnFailed(e -> {
            btLogin.setDisable(false);
            Throwable error = tarea.getException();
            String mensaje = error.getMessage();
            mostrarError(mensaje != null ? mensaje : "Error inesperado: " + error);
        });

        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
    }
}