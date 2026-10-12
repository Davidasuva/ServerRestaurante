package com.proyectoApi.api;

import com.proyectoApi.api.views.ui.ApiMain;

/**
 * Punto de entrada del jar. Debe ser una clase que NO extienda Application
 * (igual que server.Launcher) para que JavaFX arranque bien desde un jar.
 */
public class Launcher {

    public static void main(String[] args) {
        ApiMain.main(args);
    }
}
