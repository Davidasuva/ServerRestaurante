package com.bridgeRmi.bridge;

import java.net.InetSocketAddress;

import com.bridgeRmi.bridge.config.BridgeConfig;
import com.bridgeRmi.bridge.service.RmiService;
import com.sun.net.httpserver.HttpServer;

/**
 * Punto de entrada: solo hace "wiring" (conectar las piezas), sin lógica
 * de negocio propia. Equivalente al app.js/index.js de Express.
 */
public class Application {

    public static void main(String[] args) throws Exception {
        BridgeConfig config = new BridgeConfig();

        RmiService rmiService = new RmiService(config.rmiHost, config.rmiPort, config.rmiBindingName);
        rmiService.conectar();

        HttpServer server = HttpServer.create(new InetSocketAddress(config.httpPort), 0);
        // Añade aquí un createContext por cada nuevo controller/endpoint
        server.setExecutor(null);
        server.start();

        System.out.println("Bridge HTTP escuchando en http://localhost:" + config.httpPort);
    }
}
