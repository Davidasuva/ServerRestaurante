package com.example.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Properties;

import server.model.empleado.EmpleadoInterface;
import server.model.mesa.MesaInterface;
import server.model.pedido.PedidoInterface;
import server.model.producto.ProductoInterface;

/**
 * Acceso a los servicios RMI del servidor. Hace el lookup la primera vez que se pide
 * cada servicio y guarda el stub. Tras un {@link RemoteException} llamar a {@link #reset()}
 * para que el siguiente intento vuelva a buscarlos.
 *
 * Valores en client.properties (deben coincidir con el config.properties del servidor).
 */
public final class RmiClient {

    private static final String ARCHIVO = "/client.properties";

    private static final String IP;
    private static final int PUERTO;
    private static final String NOMBRE;

    static {
        Properties p = new Properties();
        try (InputStream in = RmiClient.class.getResourceAsStream(ARCHIVO)) {
            if (in != null) {
                p.load(in);
            }
        } catch (IOException e) {
            System.err.println("[RmiClient] No se pudo leer " + ARCHIVO + ": " + e.getMessage());
        }
        IP = p.getProperty("SERVER_IP", "localhost").trim();
        PUERTO = Integer.parseInt(p.getProperty("SERVER_PORT", "1808").trim());
        NOMBRE = p.getProperty("SERVER_NAME", "Uwu").trim();
    }

    private static PedidoInterface pedidos;
    private static ProductoInterface productos;
    private static MesaInterface mesas;
    private static EmpleadoInterface empleados;

    private RmiClient() {}

    public static synchronized PedidoInterface pedidos() throws RemoteException {
        if (pedidos == null) {
            pedidos = buscar("", PedidoInterface.class);
        }
        return pedidos;
    }

    public static synchronized ProductoInterface productos() throws RemoteException {
        if (productos == null) {
            productos = buscar("-productos", ProductoInterface.class);
        }
        return productos;
    }

    public static synchronized MesaInterface mesas() throws RemoteException {
        if (mesas == null) {
            mesas = buscar("-mesas", MesaInterface.class);
        }
        return mesas;
    }

    public static synchronized EmpleadoInterface empleados() throws RemoteException {
        if (empleados == null) {
            empleados = buscar("-empleados", EmpleadoInterface.class);
        }
        return empleados;
    }

    /** Olvida los stubs guardados para forzar un nuevo lookup (p. ej. tras un RemoteException). */
    public static synchronized void reset() {
        pedidos = null;
        productos = null;
        mesas = null;
        empleados = null;
    }

    private static <T extends Remote> T buscar(String sufijo, Class<T> tipo) throws RemoteException {
        String uri = "//" + IP + ":" + PUERTO + "/" + NOMBRE + sufijo;
        try {
            return tipo.cast(Naming.lookup(uri));
        } catch (NotBoundException | MalformedURLException e) {
            throw new RemoteException("No se encontró el servicio en " + uri, e);
        }
    }
}
