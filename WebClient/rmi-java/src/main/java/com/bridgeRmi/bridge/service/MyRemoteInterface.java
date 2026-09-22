package com.bridgeRmi.bridge.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * PLACEHOLDER: esta interfaz debe ser EXACTAMENTE la misma (mismo paquete,
 * mismos métodos) que la que expone tu servidor RMI real. Cópiala tal cual
 * desde el proyecto del servidor, o comparte el .jar de interfaces entre
 * ambos proyectos.
 */
public interface MyRemoteInterface extends Remote {
    String saludar(String nombre) throws RemoteException;
    // Añade aquí el resto de métodos remotos de tu servidor
}
