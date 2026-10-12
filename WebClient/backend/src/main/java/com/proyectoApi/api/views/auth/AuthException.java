package com.proyectoApi.api.views.auth;

/** Error de autenticación con un mensaje listo para mostrar al usuario. */
public class AuthException extends Exception {

    private static final long serialVersionUID = 1L;

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
