package com.example;

import javafx.application.Application;

/**
 * Punto de entrada del ejecutable de Caja. No extiende Application a propósito:
 * así el JAR único (fat jar) arranca sin pedir JavaFX como módulo.
 */
public final class CajaLauncher {

    private CajaLauncher() {}

    public static void main(String[] args) {
        Application.launch(CajaApp.class, args);
    }
}
