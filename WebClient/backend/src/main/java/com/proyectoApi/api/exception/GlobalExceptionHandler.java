package com.proyectoApi.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * Manejo centralizado de excepciones: si el servidor RMI está caído o
 * falla, aquí se traduce automáticamente a una respuesta HTTP 503 con
 * JSON consistente, sin tener que poner try/catch en cada controller.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RemoteException.class)
    public ResponseEntity<Map<String, String>> handleRemoteException(RemoteException ex) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "El servidor RMI no está disponible", "detalle", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error inesperado", "detalle", ex.getMessage()));
    }
}
