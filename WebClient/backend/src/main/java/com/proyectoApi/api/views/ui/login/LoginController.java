package com.proyectoApi.api.views.ui.login;

import com.proyectoApi.api.views.auth.AdminAuthService;
import com.proyectoApi.api.views.auth.AuthException;
import com.proyectoApi.api.environment.ApiEnvironment;
import com.proyectoApi.api.views.ui.factory.ApiFactory;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Login copiado del server. Valida por RMI y solo deja entrar a administradores. */
public class LoginController {

    private AdminAuthService authService;
    private boolean passwordVisible = false;

    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtPasswordVisible;
    @FXML
    private Label lblError;
    @FXML
    private Button btnOjo;
    @FXML
    private Button btnIngresar;

    @FXML
    public void initialize() {
        ApiEnvironment env = ApiEnvironment.getInstance();
        this.authService = new AdminAuthService(env.getRmiHost(), env.getRmiPort(), env.getRmiName());

        txtPassword.textProperty().addListener((obs, o, n) -> {
            if (!passwordVisible) {
                txtPasswordVisible.setText(n);
            }
        });
        txtPasswordVisible.textProperty().addListener((obs, o, n) -> {
            if (passwordVisible) {
                txtPassword.setText(n);
            }
        });
        txtUsuario.setOnAction(e -> txtPassword.requestFocus());
        txtPassword.setOnAction(e -> handleLogin());
        txtPasswordVisible.setOnAction(e -> handleLogin());
    }

    @FXML
    public void handleTogglePassword() {
        passwordVisible = !passwordVisible;
        txtPassword.setVisible(!passwordVisible);
        txtPasswordVisible.setVisible(passwordVisible);
        btnOjo.setText(passwordVisible ? "*" : "\uD83D\uDC41");
        if (passwordVisible) {
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.requestFocus();
            txtPasswordVisible.positionCaret(txtPasswordVisible.getText().length());
        } else {
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.requestFocus();
        }
    }

    @FXML
    public void handleLogin() {
        if (btnIngresar.isDisabled()) {
            return;
        }
        String usuario = txtUsuario.getText().trim();
        String password = passwordVisible ? txtPasswordVisible.getText() : txtPassword.getText();

        if (usuario.isEmpty()) {
            mostrarError("Ingresa tu usuario.");
            sacudir(txtUsuario);
            return;
        }
        if (password.isEmpty()) {
            mostrarError("Ingresa tu contraseña.");
            sacudir(txtPassword);
            return;
        }
        final int cedula;
        try {
            cedula = Integer.parseInt(usuario);
        } catch (NumberFormatException e) {
            mostrarError("El usuario es tu número de cédula.");
            sacudir(txtUsuario);
            return;
        }

        // La consulta es por red: se hace en segundo plano para no congelar la ventana.
        lblError.setVisible(false);
        btnIngresar.setDisable(true);
        btnIngresar.setText("Verificando...");
        Thread hilo = new Thread(() -> {
            try {
                authService.autenticar(cedula, password);
                Platform.runLater(this::abrirPanel);
            } catch (AuthException e) {
                Platform.runLater(() -> {
                    liberarBoton();
                    mostrarError(e.getMessage());
                    sacudir(btnIngresar);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    liberarBoton();
                    mostrarError("Error interno. Revisa la consola.");
                });
            }
        }, "login-api");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void liberarBoton() {
        btnIngresar.setDisable(false);
        btnIngresar.setText("Iniciar sesión");
    }

    private void abrirPanel() {
        try {
            Stage stage = (Stage) btnIngresar.getScene().getWindow();
            ApiFactory.navigateToPanel(stage);
        } catch (Exception e) {
            e.printStackTrace();
            liberarBoton();
            mostrarError("No se pudo abrir el panel del API.");
        }
    }

    private void mostrarError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(200), lblError);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void sacudir(Node n) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(55), n);
        tt.setFromX(0);
        tt.setByX(6);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }
}
