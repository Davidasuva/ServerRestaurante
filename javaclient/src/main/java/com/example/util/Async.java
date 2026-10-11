package com.example.util;

import java.util.function.Consumer;

import com.example.model.ModeloException;

import javafx.concurrent.Task;

/**
 * Ejecuta trabajo lento (llamadas al servidor) fuera del hilo de JavaFX y entrega el
 * resultado de vuelta en el hilo de JavaFX, para que la ventana no se congele.
 */
public final class Async {

    @FunctionalInterface
    public interface Trabajo<T> {
        T ejecutar() throws Exception;
    }

    private Async() {}

    /**
     * @param trabajo   se ejecuta en un hilo aparte
     * @param alTerminar recibe el resultado, en el hilo de JavaFX
     * @param alFallar  recibe el mensaje de error para el usuario, en el hilo de JavaFX
     */
    public static <T> void ejecutar(Trabajo<T> trabajo, Consumer<T> alTerminar, Consumer<String> alFallar) {
        Task<T> tarea = new Task<>() {
            @Override
            protected T call() throws Exception {
                return trabajo.ejecutar();
            }
        };
        tarea.setOnSucceeded(e -> alTerminar.accept(tarea.getValue()));
        tarea.setOnFailed(e -> alFallar.accept(mensaje(tarea.getException())));

        Thread hilo = new Thread(tarea, "rmi-task");
        hilo.setDaemon(true);
        hilo.start();
    }

    private static String mensaje(Throwable error) {
        if (error instanceof ModeloException) {
            return error.getMessage();
        }
        error.printStackTrace();
        return "Error inesperado: " + error;
    }
}
