package com.proyectoApi.api.service;

import com.proyectoApi.api.dto.EstadoPedidoDto;
import com.proyectoApi.api.dto.PedidoCreadoDto;
import com.proyectoApi.api.dto.PedidoRequest;
import com.proyectoApi.api.exception.ApiException;
import com.proyectoApi.api.exception.ApiException.Kind;
import com.proyectoApi.api.rmi.RmiGateway;
import server.model.mesa.Mesa;
import server.model.pedido.Pedido;
import server.model.producto.Producto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Crea pedidos del cliente web y consulta su estado, usando solo los servicios RMI existentes.
 *
 * Cómo se traduce un pedido web al modelo del servidor:
 *  - Pedido nuevo en estado PENDIENTE (el constructor de Pedido lo deja así; no consume inventario y el personal
 *    lo avanza a EN_COLA / PREPARANDOSE / ... desde la app de escritorio).
 *  - Una fila de producto_pedido por unidad (addProductoPerPedido una vez por unidad).
 *  - El total lo calcula el servidor con los precios de la BD.
 */
public class OrderService {

    private static final Logger LOG = Logger.getLogger(OrderService.class.getName());

    static final int MAX_LINEAS = 30;
    static final int MAX_POR_LINEA = 20;
    static final int MAX_UNIDADES = 60;
    private static final int ID_RETRIES = 5;

    private final RmiGateway rmi;
    private final List<String> paymentMethods;
    private final Object idLock = new Object();

    public OrderService(RmiGateway rmi, List<String> paymentMethods) {
        this.rmi = rmi;
        this.paymentMethods = List.copyOf(paymentMethods);
    }

    /* ------------------------------------------------------------------ crear */

    public PedidoCreadoDto create(PedidoRequest request) {
        Validated order = validate(request);

        Mesa mesa = lookupOrBadRequest("La mesa " + order.mesaId() + " no existe.",
                () -> rmi.mesas(stub -> stub.getMesaById(order.mesaId())));
        // Los ids de producto se resuelven a objetos Producto reales (el servidor los pide en addProductoPerPedido)
        Map<Integer, Producto> productos = new LinkedHashMap<>();
        for (int productoId : order.lineas().keySet()) {
            Producto p = lookupOrBadRequest("El producto " + productoId + " no existe.",
                    () -> rmi.productos(stub -> stub.getProductoById(productoId)));
            if (p == null) throw ApiException.badRequest("El producto " + productoId + " no existe.");
            productos.put(productoId, p);
        }

        Pedido pedido = registrar(mesa, order.metodo());
        int id = pedido.getId();

        Map<Integer, Integer> agregados = new LinkedHashMap<>();
        try {
            for (var linea : order.lineas().entrySet()) {
                Producto p = productos.get(linea.getKey());
                for (int unidad = 0; unidad < linea.getValue(); unidad++) {
                    boolean ok = rmi.pedidos(stub -> stub.addProductoPerPedido(id, p));
                    if (!ok) throw new ApiException(Kind.CONFLICT, "No se pudo añadir '" + p.getNombre() + "' al pedido.");
                    agregados.merge(p.getId(), 1, Integer::sum);
                }
            }
        } catch (RuntimeException e) {
            deshacer(id, agregados, productos);
            throw e;
        }

        Pedido guardado = rmi.pedidos(stub -> stub.getPedidoById(id));
        LOG.info("Pedido " + id + " creado para la mesa " + order.mesaId() + " (" + order.metodo() + ", total "
                + guardado.getPrecioTotal() + ")");
        return new PedidoCreadoDto(id, EstadoWeb.of(guardado.getEstado()), order.mesaId(), Text.money(guardado.getPrecioTotal()));
    }

    /** Un dato inexistente enviado por el cliente es un error 400 (no un 404 del recurso /pedidos). */
    private <T> T lookupOrBadRequest(String message, java.util.function.Supplier<T> lookup) {
        try {
            return lookup.get();
        } catch (ApiException e) {
            if (e.kind() == Kind.NOT_FOUND || e.kind() == Kind.CONFLICT) throw ApiException.badRequest(message);
            throw e;
        }
    }

    /** Reserva un id libre (la tabla no genera ids) e inserta el pedido; reintenta si otro cliente tomó el mismo id. */
    private Pedido registrar(Mesa mesa, String metodo) {
        synchronized (idLock) {
            ApiException last = null;
            for (int intento = 0; intento < ID_RETRIES; intento++) {
                int id = siguienteId() + intento;
                Pedido nuevo = new Pedido(id, LocalDateTime.now(), mesa);
                nuevo.setMetodoPago(metodo);
                try {
                    return rmi.pedidos(stub -> stub.registrarPedido(nuevo));
                } catch (ApiException e) {
                    last = e;
                    if (!esIdDuplicado(e)) throw e;
                }
            }
            throw new ApiException(Kind.CONFLICT, "No se pudo asignar un número de pedido, intenta de nuevo.", last);
        }
    }

    private int siguienteId() {
        int total = rmi.pedidos(stub -> stub.contar());
        if (total == 0) return 1;
        try {
            // ORDER BY id -> el último es el mayor
            List<Pedido> ultimo = rmi.pedidos(stub -> stub.getPedidos(total - 1, total - 1));
            return ultimo.get(0).getId() + 1;
        } catch (ApiException e) {
            if (e.kind() == Kind.UNAVAILABLE) throw e;
            // Si el último pedido no se puede leer (p. ej. un estado antiguo en la BD) no se bloquea la creación:
            // se parte de la cantidad de pedidos y registrar() reintenta con el siguiente id si ya existe.
            LOG.log(Level.WARNING, "No se pudo leer el último pedido; se usa contar()+1 como id inicial", e);
            return total + 1;
        }
    }

    private static boolean esIdDuplicado(ApiException e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            String m = t.getMessage() == null ? "" : t.getMessage().toLowerCase();
            if (m.contains("pedido_pkey") || m.contains("duplicate key") || m.contains("llave duplicada") || m.contains("duplicad")) {
                return true;
            }
        }
        return false;
    }

    /** Compensación best-effort: quita los productos ya añadidos y borra el pedido; si no se puede, lo cancela. */
    private void deshacer(int id, Map<Integer, Integer> agregados, Map<Integer, Producto> productos) {
        try {
            for (Integer productoId : agregados.keySet()) {
                Producto p = productos.get(productoId);
                rmi.pedidos(stub -> stub.removeProductoPerPedido(id, p));
            }
            rmi.pedidos(stub -> stub.removePedido(id));
            LOG.warning("Pedido " + id + " eliminado porque no se pudo completar.");
        } catch (RuntimeException cleanup) {
            LOG.log(Level.SEVERE, "No se pudo eliminar el pedido incompleto " + id + "; se intenta marcar como CANCELADO", cleanup);
            try {
                rmi.pedidos(stub -> stub.setPedidoStatus(id, Pedido.Estado.CANCELADO.getTexto()));
            } catch (RuntimeException ignored) {
                LOG.log(Level.SEVERE, "El pedido incompleto " + id + " quedó en la BD y requiere revisión manual", ignored);
            }
        }
    }

    /* ----------------------------------------------------------------- estado */

    public EstadoPedidoDto status(int id) {
        Pedido pedido = rmi.pedidos(stub -> stub.getPedidoById(id));
        Pedido.Estado actual = pedido.getEstado();

        int enCola = 1;
        if (EstadoWeb.esperando(actual)) {
            // La fila de espera son los pedidos aún sin preparar (PENDIENTE y EN_COLA), por orden de llegada.
            List<Pedido> orden = new ArrayList<>(pedidosPorEstado(Pedido.Estado.PENDIENTE));
            orden.addAll(pedidosPorEstado(Pedido.Estado.EN_COLA));
            orden.sort(Comparator.comparing(Pedido::getFechaPedido).thenComparingInt(Pedido::getId));
            for (int i = 0; i < orden.size(); i++) {
                if (orden.get(i).getId() == id) {
                    enCola = i + 1;
                    break;
                }
            }
        }
        int enPreparacion = pedidosPorEstado(Pedido.Estado.PREPARANDOSE).size();
        return new EstadoPedidoDto(id, EstadoWeb.of(actual), pedido.getMesaAsignada().getId(), enPreparacion, enCola);
    }

    /** El servidor lanza "No se encontraron pedidos..." cuando no hay ninguno: aquí es simplemente una lista vacía. */
    private List<Pedido> pedidosPorEstado(Pedido.Estado estado) {
        try {
            // La interfaz RMI recibe el texto del estado ("En Cola", "Preparándose"...), que el servidor resuelve con Estado.desdeTexto
            return rmi.pedidos(stub -> stub.getPedidosPerEstado(estado.getTexto()));
        } catch (ApiException e) {
            if (e.kind() == Kind.NOT_FOUND) return List.of();
            throw e;
        }
    }

    /* ------------------------------------------------------------- validación */

    record Validated(int mesaId, String metodo, Map<Integer, Integer> lineas) {
    }

    Validated validate(PedidoRequest r) {
        if (r == null) throw ApiException.badRequest("El pedido está vacío.");
        if (r.idMesa() == null || r.idMesa() <= 0) throw ApiException.badRequest("Falta la mesa del pedido.");

        String metodo = paymentMethods.stream()
                .filter(m -> r.metodo() != null && m.equalsIgnoreCase(r.metodo().trim()))
                .findFirst()
                .orElseThrow(() -> ApiException.badRequest("Método de pago no válido. Opciones: " + String.join(", ", paymentMethods) + "."));

        if (r.productos() == null || r.productos().isEmpty()) throw ApiException.badRequest("El pedido no tiene productos.");
        if (r.productos().size() > MAX_LINEAS) throw ApiException.badRequest("El pedido tiene demasiadas líneas.");

        Map<Integer, Integer> lineas = new LinkedHashMap<>();
        int unidades = 0;
        for (PedidoRequest.Linea linea : r.productos()) {
            if (linea == null || linea.idProducto() == null || linea.idProducto() <= 0) {
                throw ApiException.badRequest("Hay un producto sin identificador.");
            }
            int cantidad = linea.cantidad() == null ? 0 : linea.cantidad();
            if (cantidad < 1 || cantidad > MAX_POR_LINEA) {
                throw ApiException.badRequest("La cantidad de cada producto debe estar entre 1 y " + MAX_POR_LINEA + ".");
            }
            lineas.merge(linea.idProducto(), cantidad, Integer::sum);
            unidades += cantidad;
        }
        if (unidades > MAX_UNIDADES) throw ApiException.badRequest("El pedido supera el máximo de " + MAX_UNIDADES + " unidades.");
        for (int total : lineas.values()) {
            if (total > MAX_POR_LINEA) throw ApiException.badRequest("La cantidad de cada producto debe estar entre 1 y " + MAX_POR_LINEA + ".");
        }
        return new Validated(r.idMesa(), metodo, lineas);
    }
}
