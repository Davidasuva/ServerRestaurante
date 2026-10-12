package com.example.controller;

import com.example.Navigator;
import com.example.model.ModeloRmi;
import com.example.util.Async;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** Login: la cédula y la contraseña se validan contra los empleados registrados en el servidor. */
public class LoginController {

    @FXML private TextField campoUsuario;
    @FXML private PasswordField campoClave;
    @FXML private Label mensajeError;
    @FXML private Button btnIngresar;

    @FXML
    private void ingresar() {
        String texto = campoUsuario.getText().trim();
        String clave = campoClave.getText();

        int cedula;
        try {
            cedula = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            mostrarError("La cédula debe ser un número");
            return;
        }

        btnIngresar.setDisable(true);
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);

        Async.ejecutar(
                () -> ModeloRmi.autenticar(cedula, clave),
                empleado -> Navigator.mostrarVistaInicial(),
                mensaje -> {
                    mostrarError(mensaje);
                    btnIngresar.setDisable(false);
                });
    }

    private void mostrarError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setVisible(true);
        mensajeError.setManaged(true);
        campoClave.clear();
    }
}
