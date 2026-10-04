package com.proyectoApi.api.config;

import com.proyectoApi.api.rmi.RmiGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conexión con el servidor RMI. No se conecta al arrancar: el RmiGateway busca los stubs la primera vez que se usan
 * (y los vuelve a buscar si el servidor se reinicia), así que el orden de encendido de los equipos no importa.
 */
@Configuration
public class RmiConfig {

    @Bean
    public RmiGateway rmiGateway(@Value("${rmi.host}") String host,
                                 @Value("${rmi.port}") int port,
                                 @Value("${rmi.name}") String name) {
        return new RmiGateway(host, port, name);
    }
}
