package com.example.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/** Pedido tal como lo dibujan Cocina y Listos. Es independiente de las clases del servidor. */
public class PedidoVista {

    private final int id;
    private final String mesa;
    private final String metodoPago;
    private final LocalDateTime fecha;
    private final List<ItemCarrito> items;
    private EstadoPedido estado;

    public PedidoVista(int id, String mesa, String metodoPago, LocalDateTime fecha,
                       List<ItemCarrito> items, EstadoPedido estado) {
        this.id = id;
        this.mesa = mesa;
        this.metodoPago = metodoPago;
        this.fecha = fecha;
        this.items = List.copyOf(items);
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    /** Texto para mostrar: PE-001. */
    public String etiqueta() {
        return String.format("PE-%03d", id);
    }

    public String getMesa() {
        return mesa;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public List<ItemCarrito> getItems() {
        return items;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public int getTotal() {
        return items.stream().mapToInt(ItemCarrito::getSubtotal).sum();
    }

    public long minutosDesdeCreacion() {
        return Math.max(0, Duration.between(fecha, LocalDateTime.now()).toMinutes());
    }
}
