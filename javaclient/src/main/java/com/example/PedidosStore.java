package com.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.model.EstadoPedido;
import com.example.model.ItemCarrito;
import com.example.model.PedidoVista;

/**
 * Pedidos en memoria, compartidos entre Caja, Cocina y Listos.
 * Provisional: se reemplaza por el servidor cuando se conecte.
 */
public final class PedidosStore {

    private static final List<PedidoVista> PEDIDOS = new ArrayList<>();
    private static int contador = 0;

    private PedidosStore() {}

    /** Crea un pedido nuevo; entra directo a "En cola". */
    public static PedidoVista crear(String mesa, String metodoPago, List<ItemCarrito> items) {
        PedidoVista p = new PedidoVista(++contador, mesa, metodoPago, LocalDateTime.now(), items, EstadoPedido.EN_COLA);
        PEDIDOS.add(p);
        return p;
    }

    public static List<PedidoVista> porEstado(EstadoPedido estado) {
        List<PedidoVista> lista = new ArrayList<>();
        for (PedidoVista p : PEDIDOS) {
            if (p.getEstado() == estado) {
                lista.add(p);
            }
        }
        return lista;
    }

    public static void mover(PedidoVista p, EstadoPedido nuevo) {
        p.setEstado(nuevo);
    }

    public static void archivar(PedidoVista p) {
        PEDIDOS.remove(p);
    }
}
