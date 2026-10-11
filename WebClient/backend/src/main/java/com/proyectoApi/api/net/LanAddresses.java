package com.proyectoApi.api.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Lista las direcciones IPv4 de este equipo que un celular podría usar para abrir la página,
 * marcando cuál es la interfaz por la que el equipo sale a la red y cuáles parecen redes virtuales
 * (VirtualBox, WSL, Hyper-V, Docker, VPN...), que normalmente NO sirven desde un celular.
 */
public final class LanAddresses {

    public record Entry(String interfaceName, Inet4Address address, boolean virtual, boolean primary) {
    }

    private static final String[] VIRTUAL_HINTS = {
            "virtual", "vethernet", "vmware", "vbox", "hyper-v", "wsl", "docker", "veth", "tap", "tun", "vpn",
            "loopback", "bluetooth", "pseudo", "host-only"
    };

    private LanAddresses() {
    }

    public static List<Entry> list() {
        List<Entry> result = new ArrayList<>();
        Inet4Address primary = MdnsResponder.primaryAddress();
        try {
            for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!nif.isUp() || nif.isLoopback()) continue;
                for (InetAddress a : Collections.list(nif.getInetAddresses())) {
                    if (a instanceof Inet4Address v4 && !v4.isLinkLocalAddress()) {
                        String label = nif.getDisplayName() != null ? nif.getDisplayName() : nif.getName();
                        boolean isPrimary = primary != null && primary.equals(v4);
                        result.add(new Entry(label, v4, nif.isVirtual() || looksVirtual(label), isPrimary));
                    }
                }
            }
        } catch (java.io.IOException ignored) {
            // se devuelve lo que se haya podido leer
        }
        // primero la principal, luego las reales, al final las virtuales
        result.sort((x, y) -> Integer.compare(rank(x), rank(y)));
        return result;
    }

    static boolean looksVirtual(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        for (String hint : VIRTUAL_HINTS) {
            if (lower.contains(hint)) return true;
        }
        return false;
    }

    private static int rank(Entry e) {
        if (e.primary()) return 0;
        return e.virtual() ? 2 : 1;
    }
}
