package com.example.model;

/** Estado de un pedido tal como lo ve la interfaz (Cocina y Listos). */
public enum EstadoPedido {
    EN_COLA, PREPARANDO, LISTO;

    /** Siguiente estado, o null si ya es el último. */
    public EstadoPedido siguiente() {
        return ordinal() + 1 < values().length ? values()[ordinal() + 1] : null;
    }

    /** Estado anterior, o null si es el primero. */
    public EstadoPedido anterior() {
        return ordinal() > 0 ? values()[ordinal() - 1] : null;
    }
}
