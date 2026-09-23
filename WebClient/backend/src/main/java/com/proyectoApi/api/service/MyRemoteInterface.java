package com.proyectoApi.api.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * PLACEHOLDER: debe ser EXACTAMENTE la misma interfaz (mismo paquete,
 * mismas firmas) que expone tu servidor RMI. Lo ideal es compartir el
 * .jar de interfaces entre el servidor RMI y este proyecto Spring Boot,
 * para no tener que mantenerla sincronizada a mano.
 */
public interface MyRemoteInterface extends Remote {
    String saludar(String nombre) throws RemoteException;
    // Añade aquí el resto de métodos remotos de tu servidor
}
