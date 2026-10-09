package com.example.controller;

import com.example.Navigator;

import javafx.fxml.FXML;

/** Controlador de la barra superior (pestañas). Lo usan Caja, Cocina y Listos. */
public class NavController {

    @FXML
    protected void irCaja() {
        Navigator.show("Caja.fxml");
    }

    @FXML
    protected void irCocina() {
        Navigator.show("Cocina.fxml");
    }

    @FXML
    protected void irListos() {
        Navigator.show("Listos.fxml");
    }
}
