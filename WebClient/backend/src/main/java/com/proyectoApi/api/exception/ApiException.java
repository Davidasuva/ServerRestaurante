package com.proyectoApi.api.exception;

/**
 * Error de negocio/infraestructura con una categoría que el GlobalExceptionHandler
 * traduce a un código HTTP. Es independiente de Spring a propósito.
 */
public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public enum Kind {
        BAD_REQUEST(400), NOT_FOUND(404), CONFLICT(409), INTERNAL(500), UNAVAILABLE(503);

        private final int status;

        Kind(int status) { this.status = status; }

        public int status() { return status; }
    }

    private final Kind kind;

    public ApiException(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    public ApiException(Kind kind, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
    }

    public Kind kind() { return kind; }

    public static ApiException badRequest(String message) { return new ApiException(Kind.BAD_REQUEST, message); }

    public static ApiException notFound(String message) { return new ApiException(Kind.NOT_FOUND, message); }
}
