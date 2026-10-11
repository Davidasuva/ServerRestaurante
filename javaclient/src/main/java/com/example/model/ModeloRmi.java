package com.example.model;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.rmi.ConnectIOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.net.RmiClient;

import server.model.mesa.Mesa;
import server.model.pedido.Pedido;
import server.model.pedido.PedidoInterface;
import server.model.producto.Producto;

/**
 * Modelo RMI: único punto del cliente que habla con el servidor. Cada método llama a las
 * interfaces remotas y traduce la respuesta a lo que entienden las vistas
 * ({@link PedidoVista}, {@link ItemCarrito}, {@link EstadoPedido}).
 *
 * Todos los métodos bloquean (red): llamarlos fuera del hilo de JavaFX.
 */
public final class ModeloRmi {

    private static final int INTENTOS_ID = 5;

    private ModeloRmi() {}

    // ------------------------------------------------------------------ mesas y productos

    /** Todas las mesas, por id. */
    public static List<Mesa> listarMesas() throws ModeloException {
        try {
            int total = RmiClient.mesas().contar();
            return total == 0 ? List.of() : RmiClient.mesas().getMesa(0, total - 1);
        } catch (RemoteException e) {
            throw fallo(e);
        }
    }

    /** Todo el catálogo, por id. */
    public static List<Producto> listarProductos() throws ModeloException {
        try {
            int total = RmiClient.productos().contar();
            return total == 0 ? List.of() : RmiClient.productos().getProductos(0, total - 1);
        } catch (RemoteException e) {
            throw fallo(e);
        }
    }

    // ------------------------------------------------------------------ pedidos

    /**
     * Crea un pedido y lo deja "En cola". El servidor no asigna el id ni guarda los productos
     * al registrar, así que son 3 pasos: registrar, agregar cada unidad y poner en cola
     * (este último valida y descuenta el inventario). Si algo falla después de registrar, el
     * pedido a medias se borra para no dejar basura.
     */
    public static PedidoVista crearPedido(int mesaId, String metodoPago, List<ItemCarrito> items)
            throws ModeloException {
        if (items == null || items.isEmpty()) {
            throw new ModeloException("El pedido no tiene productos");
        }
        PedidoInterface servidor;
        try {
            servidor = RmiClient.pedidos();
        } catch (RemoteException e) {
            throw fallo(e);
        }

        LocalDateTime fecha = LocalDateTime.now();
        int id = registrar(servidor, mesaId, metodoPago, fecha);
        try {
            for (ItemCarrito item : items) {
                Producto referencia = new Producto(item.getProductoId(), item.getPrecio(), "", "", item.getNombre());
                for (int i = 0; i < item.getCantidad(); i++) {
                    if (!servidor.addProductoPerPedido(id, referencia)) {
                        throw new ModeloException("No se pudo agregar '" + item.getNombre() + "' al pedido");
                    }
                }
            }
            if (!servidor.setPedidoStatus(id, Pedido.Estado.EN_COLA.getTexto())) {
                throw new ModeloException("No se pudo poner el pedido en cola");
            }
        } catch (RemoteException e) {
            descartar(servidor, id);
            throw fallo(e);
        } catch (ModeloException e) {
            descartar(servidor, id);
            throw e;
        }

        List<ItemCarrito> copia = new ArrayList<>();
        for (ItemCarrito i : items) {
            copia.add(new ItemCarrito(i.getProductoId(), i.getNombre(), i.getPrecio(), i.getCantidad()));
        }
        return new PedidoVista(id, etiquetaMesa(mesaId), metodoPago, fecha, copia, EstadoPedido.EN_COLA);
    }

    /** Pedidos en ese estado, del más antiguo al más nuevo, con sus productos agrupados. */
    public static List<PedidoVista> listarPedidosPorEstado(EstadoPedido estado) throws ModeloException {
        try {
            PedidoInterface servidor = RmiClient.pedidos();
            List<Pedido> pedidos;
            try {
                pedidos = servidor.getPedidosPerEstado(estadoServidor(estado).getTexto());
            } catch (RemoteException e) {
                if (esListaVacia(e)) {
                    return List.of();
                }
                throw e;
            }

            List<PedidoVista> vistas = new ArrayList<>();
            for (Pedido p : pedidos) {
                List<Producto> productos;
                try {
                    productos = servidor.getProductosPerPedido(p.getId());
                } catch (RemoteException e) {
                    if (!esListaVacia(e)) {
                        throw e;
                    }
                    productos = List.of();
                }
                vistas.add(new PedidoVista(p.getId(), etiquetaMesa(p.getMesaAsignada().getId()), p.getMetodoPago(),
                        p.getFechaPedido(), agrupar(productos), estado));
            }
            vistas.sort(Comparator.comparingInt(PedidoVista::getId));
            return vistas;
        } catch (RemoteException e) {
            throw fallo(e);
        }
    }

    /** Mueve un pedido a otro estado (avanzar o deshacer). */
    public static void cambiarEstado(int pedidoId, EstadoPedido nuevo) throws ModeloException {
        cambiar(pedidoId, estadoServidor(nuevo));
    }

    /** El pedido ya fue entregado: sale de Listos. */
    public static void archivarPedido(int pedidoId) throws ModeloException {
        cambiar(pedidoId, Pedido.Estado.ENTREGADO);
    }

    // ------------------------------------------------------------------ internos

    private static void cambiar(int pedidoId, Pedido.Estado estado) throws ModeloException {
        try {
            if (!RmiClient.pedidos().setPedidoStatus(pedidoId, estado.getTexto())) {
                throw new ModeloException(String.format("No se pudo cambiar el estado del pedido PE-%03d", pedidoId));
            }
        } catch (RemoteException e) {
            throw fallo(e);
        }
    }

    /** Registra el pedido con el siguiente id libre; reintenta con el siguiente si el id ya existe. */
    private static int registrar(PedidoInterface servidor, int mesaId, String metodoPago, LocalDateTime fecha)
            throws ModeloException {
        RemoteException ultimo = null;
        try {
            int id = siguienteId(servidor);
            for (int intento = 0; intento < INTENTOS_ID; intento++, id++) {
                Pedido nuevo = new Pedido(id, fecha, new Mesa(mesaId));
                nuevo.setMetodoPago(metodoPago);
                try {
                    servidor.registrarPedido(nuevo);
                    return id;
                } catch (RemoteException e) {
                    ultimo = e;
                }
            }
        } catch (RemoteException e) {
            ultimo = e;
        }
        throw fallo(ultimo);
    }

    /** Último id + 1 (los pedidos vienen ordenados por id, así que basta mirar el último). */
    private static int siguienteId(PedidoInterface servidor) throws RemoteException {
        int total = servidor.contar();
        if (total == 0) {
            return 1;
        }
        List<Pedido> ultimo = servidor.getPedidos(total - 1, total - 1);
        return ultimo.get(0).getId() + 1;
    }

    /** Borra un pedido que quedó a medias; si no se puede, no pasa nada. */
    private static void descartar(PedidoInterface servidor, int id) {
        try {
            servidor.removePedido(id);
        } catch (RemoteException ignorado) {
            // ya hay un error que informar; esto es solo limpieza
        }
    }

    /** El servidor devuelve productos repetidos (uno por unidad): se agrupan con su cantidad. */
    private static List<ItemCarrito> agrupar(List<Producto> productos) {
        Map<Integer, ItemCarrito> porProducto = new LinkedHashMap<>();
        for (Producto p : productos) {
            ItemCarrito item = porProducto.computeIfAbsent(p.getId(),
                    k -> new ItemCarrito(p.getId(), p.getNombre(), Math.round(p.getPrecio()), 0));
            item.setCantidad(item.getCantidad() + 1);
        }
        return new ArrayList<>(porProducto.values());
    }

    private static String etiquetaMesa(int mesaId) {
        return String.format("Mesa %02d", mesaId);
    }

    private static Pedido.Estado estadoServidor(EstadoPedido estado) {
        return switch (estado) {
            case EN_COLA -> Pedido.Estado.EN_COLA;
            case PREPARANDO -> Pedido.Estado.PREPARANDOSE;
            case LISTO -> Pedido.Estado.PREPARADO;
        };
    }

    // ------------------------------------------------------------------ errores

    /** El servidor avisa "No se encontraron ..." con una excepción cuando una lista está vacía. */
    private static boolean esListaVacia(Throwable e) {
        String mensaje = raiz(e).getMessage();
        return mensaje != null && mensaje.startsWith("No se encontraron");
    }

    private static Throwable raiz(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    /** Convierte un error de RMI en un mensaje para el usuario y fuerza un nuevo lookup la próxima vez. */
    private static ModeloException fallo(RemoteException e) {
        RmiClient.reset();
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof NotBoundException) {
                return new ModeloException("El servidor no tiene publicado el servicio '" + t.getMessage()
                        + "'. Revise SERVER_NAME en client.properties.", e);
            }
            if (t instanceof ConnectException || t instanceof java.rmi.ConnectException
                    || t instanceof ConnectIOException || t instanceof UnknownHostException
                    || t instanceof NoRouteToHostException || t instanceof SocketTimeoutException) {
                return new ModeloException("No se pudo conectar con el servidor. Verifique que esté encendido y "
                        + "que SERVER_IP y SERVER_PORT de client.properties sean correctos.", e);
            }
        }
        Throwable raiz = raiz(e);
        String mensaje = raiz.getMessage() != null ? raiz.getMessage() : raiz.toString();
        return new ModeloException(mensaje, e);
    }
}
