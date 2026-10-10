package com.example.service.local;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.model.EstadoPedido;
import com.example.model.ItemCarrito;
import com.example.model.PedidoVista;
import com.example.service.PedidoService;
import com.example.service.ServicioException;

/**
 * Pedidos en memoria mientras no hay servidor. Devuelve copias, igual que lo haría el
 * servidor, para que lo que ve la interfaz no cambie por detrás.
 */
public class LocalPedidoService implements PedidoService {

    private final List<PedidoVista> pedidos = new ArrayList<>();
    private int contador = 0;

    @Override
    public synchronized PedidoVista crear(int mesaId, String metodoPago, List<ItemCarrito> items)
            throws ServicioException {
        if (items == null || items.isEmpty()) {
            throw new ServicioException("El pedido no tiene productos");
        }
        PedidoVista p = new PedidoVista(++contador, String.format("Mesa %02d", mesaId), metodoPago,
                LocalDateTime.now(), copiarItems(items), EstadoPedido.EN_COLA);
        pedidos.add(p);
        return copia(p);
    }

    @Override
    public synchronized List<PedidoVista> listarPorEstado(EstadoPedido estado) {
        List<PedidoVista> lista = new ArrayList<>();
        for (PedidoVista p : pedidos) {
            if (p.getEstado() == estado) {
                lista.add(copia(p));
            }
        }
        return lista;
    }

    @Override
    public synchronized void cambiarEstado(int pedidoId, EstadoPedido nuevo) throws ServicioException {
        if (nuevo == null) {
            throw new ServicioException("Estado no válido");
        }
        buscar(pedidoId).setEstado(nuevo);
    }

    @Override
    public synchronized void archivar(int pedidoId) throws ServicioException {
        pedidos.remove(buscar(pedidoId));
    }

    private PedidoVista buscar(int pedidoId) throws ServicioException {
        for (PedidoVista p : pedidos) {
            if (p.getId() == pedidoId) {
                return p;
            }
        }
        throw new ServicioException(String.format("No existe el pedido PE-%03d", pedidoId));
    }

    private static PedidoVista copia(PedidoVista p) {
        return new PedidoVista(p.getId(), p.getMesa(), p.getMetodoPago(), p.getFecha(),
                copiarItems(p.getItems()), p.getEstado());
    }

    private static List<ItemCarrito> copiarItems(List<ItemCarrito> items) {
        List<ItemCarrito> copia = new ArrayList<>();
        for (ItemCarrito i : items) {
            copia.add(new ItemCarrito(i.getProductoId(), i.getNombre(), i.getPrecio(), i.getCantidad()));
        }
        return copia;
    }
}
