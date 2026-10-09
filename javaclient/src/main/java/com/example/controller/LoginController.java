package com.example.controller;

import java.util.Map;

import com.example.Navigator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    // Usuarios provisionales: usuario -> contraseña
    private static final Map<String, String> USUARIOS = Map.of(
        "admin", "1234",
        "peñagay", "1234",
        "cocinero", "cocina123",
        "PresidenteMilagros", "firmesporlapatria"
    );

    @FXML private TextField campoUsuario;
    @FXML private PasswordField campoClave;
    @FXML private Label mensajeError;

    @FXML
    private void ingresar() {
        String usuario = campoUsuario.getText().trim();
        String clave = campoClave.getText();

        if (clave.equals(USUARIOS.get(usuario))) {
            Navigator.show("Caja.fxml");
        } else {
            mensajeError.setText("Usuario o contraseña incorrectos");
            mensajeError.setVisible(true);
            mensajeError.setManaged(true);
            campoClave.clear();
        }
    }
}