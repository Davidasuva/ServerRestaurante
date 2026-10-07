package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pedidos en memoria, compartidos entre Caja, Cocina y Listos.
 * Provisional: se reemplaza por el servidor cuando se conecte.
 */
public final class PedidosStore {

    public enum Estado {
        EN_COLA, PREPARANDO, LISTO;

        public Estado siguiente() {
            return ordinal() + 1 < values().length ? values()[ordinal() + 1] : null;
        }

        public Estado anterior() {
            return ordinal() > 0 ? values()[ordinal() - 1] : null;
        }
    }

    public static class PedidoLocal {
        public final String id;
        public final String mesa;
        public final Map<String, Integer> items;
        public final long creadoMs = System.currentTimeMillis();
        public Estado estado = Estado.EN_COLA;

        PedidoLocal(String id, String mesa, Map<String, Integer> items) {
            this.id = id;
            this.mesa = mesa;
            this.items = items;
        }
    }

    private static final List<PedidoLocal> PEDIDOS = new ArrayList<>();
    private static int contador = 0;

    private PedidosStore() {}

    /** Crea un pedido nuevo; entra directo a "En cola". */
    public static PedidoLocal crear(String mesa, Map<String, Integer> items) {
        PedidoLocal p = new PedidoLocal(String.format("PE-%03d", ++contador), mesa, items);
        PEDIDOS.add(p);
        return p;
    }

    public static List<PedidoLocal> porEstado(Estado estado) {
        List<PedidoLocal> lista = new ArrayList<>();
        for (PedidoLocal p : PEDIDOS) {
            if (p.estado == estado) {
                lista.add(p);
            }
        }
        return lista;
    }

    public static void mover(PedidoLocal p, Estado nuevo) {
        p.estado = nuevo;
    }

    public static void archivar(PedidoLocal p) {
        PEDIDOS.remove(p);
    }
}
