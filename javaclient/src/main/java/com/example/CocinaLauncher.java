package com.example;

import javafx.application.Application;

/**
 * Punto de entrada del ejecutable de Cocina. No extiende Application a propósito:
 * así el JAR único (fat jar) arranca sin pedir JavaFX como módulo.
 */
public final class CocinaLauncher {

    private CocinaLauncher() {}

    public static void main(String[] args) {
        Application.launch(CocinaApp.class, args);
    }
}
