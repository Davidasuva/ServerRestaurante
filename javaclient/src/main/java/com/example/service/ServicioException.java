package com.example.service;

/** Error de un service con un mensaje listo para mostrarle al usuario (p. ej. "Inventario insuficiente de 'Pan'"). */
public class ServicioException extends Exception {

    public ServicioException(String mensaje) {
        super(mensaje);
    }

    public ServicioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
