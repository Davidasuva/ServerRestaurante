package com.proyectoApi.api.service;

import org.springframework.stereotype.Service;

import java.rmi.RemoteException;

/**
 * Capa de servicio: envuelve las llamadas al stub RMI. El controller
 * nunca llama a MyRemoteInterface directamente — siempre pasa por
 * aquí. Esto permite añadir lógica adicional (caché, reintentos,
 * logging, transformación) sin tocar el controller.
 */
@Service
public class SaludoService {

    private final MyRemoteInterface remoteService;

    // Spring inyecta automáticamente el Bean creado en RmiConfig
    public SaludoService(MyRemoteInterface remoteService) {
        this.remoteService = remoteService;
    }

    public String saludar(String nombre) throws RemoteException {
        return remoteService.saludar(nombre);
    }

    // Añade aquí un método por cada operación remota, ej:
    // public List<Usuario> obtenerUsuarios() throws RemoteException { ... }
}
