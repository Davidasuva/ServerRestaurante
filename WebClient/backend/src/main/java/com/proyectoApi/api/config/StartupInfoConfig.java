package com.proyectoApi.api.config;

import com.proyectoApi.api.net.LanAddresses;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;

/** Al arrancar, escribe en la consola la dirección exacta que hay que abrir desde el celular. */
@Component
public class StartupInfoConfig {

    private static final Logger LOG = Logger.getLogger(StartupInfoConfig.class.getName());

    private final int port;
    private final String rmiHost;

    public StartupInfoConfig(@Value("${server.port:8080}") int port, @Value("${rmi.host:}") String rmiHost) {
        this.port = port;
        this.rmiHost = rmiHost;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        List<LanAddresses.Entry> addresses = LanAddresses.list();
        StringBuilder sb = new StringBuilder("\n=== Para entrar desde el celular (misma red Wi-Fi que este equipo) ===\n");
        if (addresses.isEmpty()) {
            sb.append("  No se encontró ninguna IPv4 de red. ¿Este equipo está conectado a la red?\n");
        }
        for (LanAddresses.Entry e : addresses) {
            sb.append("  http://").append(e.address().getHostAddress()).append(port == 80 ? "" : ":" + port).append("/");
            if (e.primary()) sb.append("   <-- usa esta (interfaz por la que sale a la red)");
            else if (e.virtual()) sb.append("   (red virtual: normalmente NO sirve desde el celular)");
            sb.append("   [").append(e.interfaceName()).append("]\n");
        }
        sb.append("  Si no abre: ejecuta abrir-puerto-celular.bat como administrador y revisa LEEME-CELULAR.md.\n");
        sb.append("  Diagnóstico rápido: http://localhost:").append(port).append("/api/salud  (servidor RMI: ").append(rmiHost).append(")\n");
        LOG.info(sb.toString());
    }
}
