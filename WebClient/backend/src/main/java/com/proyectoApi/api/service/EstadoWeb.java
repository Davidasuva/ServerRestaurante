package com.proyectoApi.api.service;

/**
 * Traduce el estado de texto libre de la BD ("Pendiente", "En preparación", "Listo", "Entregado", "Pagado", "Cancelado")
 * a los estados que entiende la pantalla del cliente.
 */
public final class EstadoWeb {

    public static final String EN_COLA = "EN_COLA";
    public static final String EN_PREPARACION = "EN_PREPARACION";
    public static final String LISTO = "LISTO";
    public static final String CANCELADO = "CANCELADO";

    private EstadoWeb() {
    }

    public static String of(String estadoBd) {
        return switch (Text.key(estadoBd)) {
            case "pendiente" -> EN_COLA;
            case "listo", "entregado", "pagado" -> LISTO;
            case "cancelado" -> CANCELADO;
            // "En preparación" y cualquier estado personalizado que el personal haya puesto después de Pendiente
            default -> EN_PREPARACION;
        };
    }
}
