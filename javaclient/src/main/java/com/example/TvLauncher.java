package com.example;

import javafx.application.Application;

/**
 * Punto de entrada del ejecutable de televisión. No extiende Application a propósito:
 * así el JAR único (fat jar) arranca sin pedir JavaFX como módulo.
 */
public final class TvLauncher {

    private TvLauncher() {}

    public static void main(String[] args) {
        Application.launch(TvApp.class, args);
    }
}
