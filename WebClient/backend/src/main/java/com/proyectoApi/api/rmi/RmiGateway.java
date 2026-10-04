package com.proyectoApi.api.rmi;

import com.proyectoApi.api.exception.ApiException;
import com.proyectoApi.api.exception.ApiException.Kind;
import server.model.mesa.MesaInterface;
import server.model.pedido.PedidoInterface;
import server.model.producto.ProductoInterface;

import java.io.InvalidClassException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Único punto de contacto con el servidor RMI.
 *
 * - Busca los stubs de forma perezosa: la API arranca aunque el servidor RMI aún no esté encendido.
 * - Si una llamada falla por conexión, descarta el stub, vuelve a buscarlo en el registro y reintenta una vez
 *   (así sobrevive a reinicios del servidor sin reiniciar Spring).
 * - Traduce las excepciones remotas a {@link ApiException}.
 */
public class RmiGateway {

    private static final Logger LOG = Logger.getLogger(RmiGateway.class.getName());

    /** Nombres de binding tal como los publica ServerModel.deploy(): base, base-productos, base-mesas, ... */
    private enum Binding {
        PEDIDOS(""), PRODUCTOS("-productos"), MESAS("-mesas");

        final String suffix;

        Binding(String suffix) { this.suffix = suffix; }
    }

    private final String host;
    private final int port;
    private final String baseName;
    private final Map<Binding, Remote> stubs = new ConcurrentHashMap<>();

    public RmiGateway(String host, int port, String baseName) {
        this.host = host;
        this.port = port;
        this.baseName = baseName;
    }

    public <R> R pedidos(RemoteCall<PedidoInterface, R> call) { return invoke(Binding.PEDIDOS, PedidoInterface.class, call); }

    public <R> R productos(RemoteCall<ProductoInterface, R> call) { return invoke(Binding.PRODUCTOS, ProductoInterface.class, call); }

    public <R> R mesas(RemoteCall<MesaInterface, R> call) { return invoke(Binding.MESAS, MesaInterface.class, call); }

    public String describe() { return host + ":" + port + "/" + baseName; }

    private <S extends Remote, R> R invoke(Binding binding, Class<S> type, RemoteCall<S, R> call) {
        for (int attempt = 1; ; attempt++) {
            S stub = lookup(binding, type);
            try {
                return call.apply(stub);
            } catch (ServerException e) {
                // La lógica del servidor lanzó una RemoteException (mensaje de negocio para el usuario).
                throw fromServerMessage(rootMessage(e), false, e);
            } catch (RemoteException e) {
                // Fallo de conexión / stub caducado.
                stubs.remove(binding);
                if (isContractMismatch(e)) {
                    LOG.log(Level.SEVERE, "Las clases de server.model de este proyecto no coinciden con las del servidor RMI "
                            + "(revisa serialVersionUID / versión). Ver README.", e);
                    throw new ApiException(Kind.UNAVAILABLE, "El servidor RMI usa una versión incompatible del contrato.", e);
                }
                if (attempt >= 2) {
                    LOG.log(Level.WARNING, "RMI no disponible en " + describe(), e);
                    throw new ApiException(Kind.UNAVAILABLE, "El servidor del restaurante no está disponible.", e);
                }
            } catch (ApiException e) {
                throw e;
            } catch (RuntimeException e) {
                // El servidor lanzó una RuntimeException (p. ej. "No se encontró pedido ..." o un error SQL).
                throw fromServerMessage(e.getMessage(), true, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <S extends Remote> S lookup(Binding binding, Class<S> type) {
        Remote cached = stubs.get(binding);
        if (cached != null) return (S) cached;
        String name = baseName + binding.suffix;
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            Remote stub = registry.lookup(name);
            stubs.put(binding, stub);
            LOG.info("Conectado a //" + host + ":" + port + "/" + name);
            return type.cast(stub);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "No se pudo obtener //" + host + ":" + port + "/" + name + ": " + e);
            throw new ApiException(Kind.UNAVAILABLE, "El servidor del restaurante no está disponible.", e);
        }
    }

    private static boolean isContractMismatch(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c instanceof InvalidClassException || c instanceof ClassNotFoundException) return true;
        }
        return false;
    }

    private static String rootMessage(ServerException e) {
        Throwable detail = e.getCause() != null ? e.getCause() : e;
        return detail.getMessage();
    }

    /**
     * Mensajes de RemoteException = mensajes pensados para el usuario, se muestran tal cual.
     * Mensajes de RuntimeException = suelen traer texto SQL; solo se muestran los "no se encontró".
     */
    static ApiException fromServerMessage(String message, boolean fromRuntime, Throwable cause) {
        String text = message == null ? "" : message;
        String lower = text.toLowerCase();
        if (lower.startsWith("no se encontr") || lower.startsWith("no se encuentra") || lower.contains("no encontrad")) {
            return new ApiException(Kind.NOT_FOUND, text, cause);
        }
        if (fromRuntime) {
            LOG.log(Level.WARNING, "Error interno del servidor RMI: " + text, cause);
            return new ApiException(Kind.INTERNAL, "El servidor no pudo procesar la solicitud.", cause);
        }
        return new ApiException(Kind.CONFLICT, text.isBlank() ? "El servidor rechazó la solicitud." : text, cause);
    }
}
