package com.proyectoApi.api.views.auth;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.model.empleado.Empleado;
import server.model.empleado.EmpleadoInterface;

/**
 * Valida el login contra el servidor del restaurante por RMI
 * (servicio "&lt;rmi.name&gt;-empleados") y solo deja pasar a los administradores.
 */
public class AdminAuthService {

    public static final String CARGO_ADMIN = "Administrador";
    private static final Logger LOG = Logger.getLogger(AdminAuthService.class.getName());
    private static final int TIMEOUT_CONEXION_MS = 4000;

    private final String host;
    private final int port;
    private final String baseName;

    public AdminAuthService(String host, int port, String baseName) {
        this.host = host;
        this.port = port;
        this.baseName = baseName;
    }

    public Empleado autenticar(int cedula, String password) throws AuthException {
        EmpleadoInterface servicio = conectar();
        Empleado empleado;
        try {
            empleado = servicio.getEmpleadoByCedula(cedula);
        } catch (RemoteException e) {
            if (esNoEncontrado(e)) {
                throw new AuthException("Credenciales incorrectas.");
            }
            LOG.log(Level.WARNING, "Fallo RMI al validar el login", e);
            throw new AuthException("No se pudo validar con el servidor. Revisa la consola.", e);
        }
        if (empleado == null || empleado.getContrasena() == null || !empleado.getContrasena().equals(password)) {
            throw new AuthException("Credenciales incorrectas.");
        }
        // Se revisa el cargo DESPUÉS de la contraseña para no revelar cargos a quien no se autenticó.
        String cargo = empleado.getCargo() == null ? "" : empleado.getCargo().trim();
        if (!CARGO_ADMIN.equalsIgnoreCase(cargo)) {
            throw new AuthException("Solo los administradores pueden ingresar.");
        }
        return empleado;
    }

    private EmpleadoInterface conectar() throws AuthException {
        // Prueba rápida de red para no dejar la ventana colgada si el host no responde.
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, port), TIMEOUT_CONEXION_MS);
        } catch (IOException e) {
            throw new AuthException("No se pudo conectar con el servidor (" + host + ":" + port + ").", e);
        }
        String name = baseName + "-empleados";
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            return (EmpleadoInterface) registry.lookup(name);
        } catch (NotBoundException e) {
            throw new AuthException("El servidor está activo pero no ha publicado '" + name + "'.", e);
        } catch (ConnectException | ConnectIOException | NoSuchObjectException e) {
            throw new AuthException("No se pudo conectar con el servidor (" + host + ":" + port + ").", e);
        } catch (RemoteException e) {
            LOG.log(Level.WARNING, "No se pudo consultar el registro RMI", e);
            throw new AuthException("Error al comunicarse con el servidor. Revisa la consola.", e);
        }
    }

    private static boolean esNoEncontrado(Throwable e) {
        for (Throwable c = e; c != null; c = c.getCause()) {
            String m = c.getMessage();
            if (m != null) {
                String lower = m.toLowerCase();
                if (lower.startsWith("no se encontr") || lower.contains("no encontrad")) {
                    return true;
                }
            }
        }
        return false;
    }
}
