package com.bridgeRmi.bridge.service;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Capa de "servicio": aísla toda la lógica de RMI (conexión, lookup,
 * llamadas remotas) del resto de la aplicación. Los controllers nunca
 * hablan con RMI directamente, solo con esta clase.
 */
public class RmiService {

    private final String host;
    private final int port;
    private final String bindingName;
    private MyRemoteInterface remoteService;

    public RmiService(String host, int port, String bindingName) {
        this.host = host;
        this.port = port;
        this.bindingName = bindingName;
    }

    public void conectar() throws Exception {
        Registry registry = LocateRegistry.getRegistry(host, port);
        remoteService = (MyRemoteInterface) registry.lookup(bindingName);
        System.out.println("Conectado al servidor RMI en " + host + ":" + port);
    }

    public String saludar(String nombre) throws Exception {
        if (remoteService == null) {
            throw new IllegalStateException("RmiService no está conectado. Llama a conectar() primero.");
        }
        return remoteService.saludar(nombre);
    }

    // Añade aquí un método por cada operación remota que necesites exponer,
    // por ejemplo: obtenerUsuarios(), crearPedido(pedido), etc.
}
