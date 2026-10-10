package com.example.service;

import java.util.List;

import com.example.model.EstadoPedido;
import com.example.model.ItemCarrito;
import com.example.model.PedidoVista;

/** Operaciones sobre pedidos que usan Caja, Cocina y Listos. */
public interface PedidoService {

    /**
     * Crea un pedido y lo deja en "En cola".
     *
     * @param mesaId     id de la mesa
     * @param metodoPago Nequi, Transferencia, Efectivo...
     * @param items      productos con su cantidad (cada {@link ItemCarrito} trae el id del producto)
     * @return el pedido creado, con su id definitivo
     * @throws ServicioException si no se puede crear (p. ej. falta inventario)
     */
    PedidoVista crear(int mesaId, String metodoPago, List<ItemCarrito> items) throws ServicioException;

    /** Pedidos en ese estado, con sus ítems. Lista vacía si no hay ninguno. */
    List<PedidoVista> listarPorEstado(EstadoPedido estado) throws ServicioException;

    /** Mueve un pedido a otro estado (avanzar o deshacer). */
    void cambiarEstado(int pedidoId, EstadoPedido nuevo) throws ServicioException;

    /** Saca un pedido de Listos porque ya fue entregado. */
    void archivar(int pedidoId) throws ServicioException;
}
