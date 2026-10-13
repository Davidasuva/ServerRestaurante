package com.example;

/** Aplicación de televisión. */
public class TvApp extends AppBase {

    @Override
    protected String titulo() {
        return "Carbón y Sazón - Televisión";
    }

    @Override
    protected String vista() {
        return "Listos.fxml";
    }
}
