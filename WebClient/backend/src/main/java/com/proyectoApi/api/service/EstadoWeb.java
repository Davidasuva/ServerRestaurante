package com.proyectoApi.api.service;

import server.model.pedido.Pedido;

/**
 * Traduce el {@link Pedido.Estado} del servidor a los estados que entiende la pantalla del cliente.
 * Es una correspondencia 1 a 1 (el servidor y la pantalla comparten los mismos seis estados):
 *
 * <pre>
 *   Servidor (Pedido.Estado)   Web
 *   PENDIENTE                  PENDIENTE      (recién creado desde la web; aún no consume inventario)
 *   EN_COLA                    EN_COLA
 *   PREPARANDOSE               PREPARANDOSE
 *   PREPARADO                  PREPARADO      (el cliente puede "Confirmar pedido recibido")
 *   ENTREGADO                  ENTREGADO
 *   CANCELADO                  CANCELADO
 * </pre>
 */
public final class EstadoWeb {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String EN_COLA = "EN_COLA";
    public static final String PREPARANDOSE = "PREPARANDOSE";
    public static final String PREPARADO = "PREPARADO";
    public static final String ENTREGADO = "ENTREGADO";
    public static final String CANCELADO = "CANCELADO";

    private EstadoWeb() {
    }

    public static String of(Pedido.Estado estado) {
        if (estado == null) return PENDIENTE;
        return switch (estado) {
            case PENDIENTE -> PENDIENTE;
            case EN_COLA -> EN_COLA;
            case PREPARANDOSE -> PREPARANDOSE;
            case PREPARADO -> PREPARADO;
            case ENTREGADO -> ENTREGADO;
            case CANCELADO -> CANCELADO;
        };
    }

    /** Pedidos que todavía no empezaron a prepararse: los que forman la fila de espera. */
    public static boolean esperando(Pedido.Estado estado) {
        return estado == Pedido.Estado.PENDIENTE || estado == Pedido.Estado.EN_COLA;
    }
}
