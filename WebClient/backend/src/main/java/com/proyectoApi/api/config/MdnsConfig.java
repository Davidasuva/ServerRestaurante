package com.proyectoApi.api.config;

import com.proyectoApi.api.net.MdnsResponder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Publica un nombre fácil (p. ej. restaurante.local) para entrar desde el navegador sin mostrar la IP del equipo.
 * Se configura en application.properties: app.mdns.enabled / app.mdns.name / app.mdns.address.
 */
@Configuration
@ConditionalOnProperty(name = "app.mdns.enabled", havingValue = "true", matchIfMissing = true)
public class MdnsConfig {

    private static final Logger LOG = Logger.getLogger(MdnsConfig.class.getName());

    @Bean(destroyMethod = "close")
    public MdnsResponder mdnsResponder(@Value("${app.mdns.name:restaurante}") String name,
                                       @Value("${app.mdns.address:}") String forcedAddress,
                                       @Value("${server.port:8080}") int port) {
        List<Inet4Address> addresses;
        try {
            if (!forcedAddress.isBlank()) {
                addresses = List.of((Inet4Address) InetAddress.getByName(forcedAddress.trim()));
            } else {
                Inet4Address primary = MdnsResponder.primaryAddress();
                addresses = primary != null ? List.of(primary) : MdnsResponder.allAddresses();
            }
            MdnsResponder responder = new MdnsResponder(name, addresses);
            if (addresses.isEmpty()) {
                LOG.warning("mDNS desactivado: no se encontró ninguna dirección IPv4 de red. Entra por la IP del equipo.");
                return responder; // sin start(): no anuncia nada
            }
            responder.start();
            LOG.info("Abre la página en: http://" + responder.hostName() + (port == 80 ? "" : ":" + port) + "/");
            return responder;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "No se pudo publicar el nombre " + name + ".local (la página sigue disponible por IP): " + e);
            return new MdnsResponder(name, List.of()); // bean inerte
        }
    }
}
