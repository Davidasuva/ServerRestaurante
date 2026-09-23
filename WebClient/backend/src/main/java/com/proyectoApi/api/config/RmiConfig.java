package com.proyectoApi.api.config;

import com.proyectoApi.api.service.MyRemoteInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Configuración de la conexión RMI. Spring construye este Bean UNA VEZ
 * al arrancar la aplicación (no en cada petición) y lo inyecta donde
 * se necesite, típicamente en la capa de servicio.
 *
 * Si el servidor RMI se cae y luego vuelve, el stub sigue siendo válido:
 * RMI reintenta la conexión subyacente en cada llamada remota.
 */
@Configuration
public class RmiConfig {

    @Value("${rmi.host}")
    private String rmiHost;

    @Value("${rmi.port}")
    private int rmiPort;

    @Value("${rmi.binding-name}")
    private String bindingName;

    @Bean
    public MyRemoteInterface myRemoteService() throws Exception {
        Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);
        MyRemoteInterface stub = (MyRemoteInterface) registry.lookup(bindingName);
        System.out.println("Conectado al servidor RMI en " + rmiHost + ":" + rmiPort);
        return stub;
    }
}
