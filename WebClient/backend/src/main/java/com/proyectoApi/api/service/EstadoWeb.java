package com.proyectoApi.api.service;

/**
 * Traduce el estado de texto libre de la BD ("Pendiente", "En preparación", "Listo", "Entregado", "Pagado", "Cancelado")
 * a los estados que entiende la pantalla del cliente:
 *
 * <pre>
 *   BD                         Web
 *   Pendiente                  EN_COLA
 *   En preparación (u otro)    PREPARANDOSE
 *   Listo                      PREPARADO     (el cliente puede "Confirmar pedido recibido")
 *   Entregado / Pagado         ENTREGADO
 *   Cancelado                  CANCELADO
 * </pre>
 * (PENDIENTE también existe en la pantalla, pero la API no lo usa: un pedido nuevo ya entra a la cola.)
 */
public final class EstadoWeb {

    public static final String EN_COLA = "EN_COLA";
    public static final String PREPARANDOSE = "PREPARANDOSE";
    public static final String PREPARADO = "PREPARADO";
    public static final String ENTREGADO = "ENTREGADO";
    public static final String CANCELADO = "CANCELADO";

    private EstadoWeb() {
    }

    public static String of(String estadoBd) {
        return switch (Text.key(estadoBd)) {
            case "pendiente" -> EN_COLA;
            case "listo" -> PREPARADO;
            case "entregado", "pagado" -> ENTREGADO;
            case "cancelado" -> CANCELADO;
            // "En preparación" y cualquier estado personalizado que el personal haya puesto después de Pendiente
            default -> PREPARANDOSE;
        };
    }
}
