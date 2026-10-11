package com.example.model;

/** Error del modelo con un mensaje listo para mostrarle al usuario. */
public class ModeloException extends Exception {

    public ModeloException(String mensaje) {
        super(mensaje);
    }

    public ModeloException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
