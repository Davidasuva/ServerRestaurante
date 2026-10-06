package server.controller.login;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import server.factory.ServerFactory;
import server.model.ServerModel;
import server.model.empleado.Empleado;
import server.model.empleado.EmpleadoInterface;
import server.model.empleado.EmpleadoService;
import server.model.history.History;

import java.net.URL;
import java.rmi.RemoteException;


public class LoginController {

    private static final String CARGO_ADMIN = "Administrador";

    private ServerModel model;
    private EmpleadoInterface empleadoService;
    private boolean passwordVisible=false;

    @FXML private ImageView logo;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private Label lblError;
    @FXML private Button        btnOjo;
    @FXML private Button btnIngresar;

    public void setModel(ServerModel model) {
        this.model = model;
    }

    @FXML
    public void initialize() {

        try{
            empleadoService=new EmpleadoService(new History());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        txtPassword.textProperty().addListener((obs, o, n) -> {
            if (!passwordVisible) txtPasswordVisible.setText(n);
        });
        txtPasswordVisible.textProperty().addListener((obs, o, n) -> {
            if (passwordVisible) txtPassword.setText(n);
        });

        txtUsuario.setOnAction(e->txtPassword.requestFocus());
        txtPassword.setOnAction(e->handleLogin());
        txtPasswordVisible.setOnAction(e->handleLogin());
    }

    @FXML
    public void handleTogglePassword(){
        passwordVisible = !passwordVisible;
        txtPassword.setVisible(!passwordVisible);
        txtPasswordVisible.setVisible(passwordVisible);
        btnOjo.setText(passwordVisible ? "*" : "👁");
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
        String cedula     = txtUsuario.getText().trim();
        String password = passwordVisible
                ? txtPasswordVisible.getText()
                : txtPassword.getText();

        if (cedula.isEmpty()) {
            mostrarError("Ingresa tu usuario.");
            sacudir(txtUsuario);
            return;
        }
        if (password.isEmpty()) {
            mostrarError("Ingresa tu contraseña.");
            sacudir(txtPassword);
            return;
        }

        try {
            Empleado user = empleadoService.getEmpleadoByCedula(Integer.parseInt(cedula));
            if (user.getContrasena().equals(password)) {
                lblError.setVisible(false);
                abrirEmpleadosView();
            } else {
                mostrarError("Credenciales incorrectas.");
                sacudir(btnIngresar);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error interno. Revisa la consola.");
        }
    }


    private void abrirEmpleadosView() {
        try {
            Stage stage = (Stage) btnIngresar.getScene().getWindow();
            ServerFactory.navigateToEmpleados(stage);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo abrir la vista del servidor.");
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