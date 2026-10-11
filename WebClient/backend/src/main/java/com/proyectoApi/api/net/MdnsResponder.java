package com.proyectoApi.api.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Anuncia en la red local el nombre "<nombre>.local" (mDNS / Bonjour) para que el navegador pueda abrir
 * http://restaurante.local:8080 en lugar de la IP del equipo. Sin dependencias: solo el JDK.
 *
 * Responde a las consultas de tipo A (IPv4) de ese nombre y lo anuncia al arrancar.
 * Windows 10+, macOS, iOS y Android reciente resuelven nombres .local por mDNS sin instalar nada.
 * Si falla (puerto ocupado, sin red...) solo se registra un aviso: el sitio sigue funcionando por IP.
 */
public class MdnsResponder implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(MdnsResponder.class.getName());
    public static final int MDNS_PORT = 5353;
    private static final String GROUP = "224.0.0.251";
    private static final int TTL_SECONDS = 120;

    private final String fqdn;                // p. ej. "restaurante.local"
    private final List<byte[]> addresses;     // IPv4 (4 bytes) que se anuncian
    private final int port;
    private final boolean multicast;
    private MulticastSocket socket;
    private Thread thread;
    private volatile boolean running;

    public MdnsResponder(String name, List<Inet4Address> addresses) {
        this(name, addresses, MDNS_PORT, true);
    }

    /** Constructor con puerto y multicast configurables (para pruebas). */
    MdnsResponder(String name, List<Inet4Address> addresses, int port, boolean multicast) {
        String clean = name.trim().toLowerCase(Locale.ROOT);
        this.fqdn = clean.endsWith(".local") ? clean : clean + ".local";
        this.addresses = addresses.stream().map(Inet4Address::getAddress).toList();
        this.port = port;
        this.multicast = multicast;
    }

    public String hostName() {
        return fqdn;
    }

    /* ------------------------------------------------------------- ciclo de vida */

    public void start() throws IOException {
        socket = new MulticastSocket(null);
        socket.setReuseAddress(true); // Windows/macOS ya usan el puerto 5353: hay que compartirlo
        socket.bind(new InetSocketAddress(port));
        socket.setTimeToLive(255);
        if (multicast) joinAllInterfaces();
        running = true;
        thread = new Thread(this::loop, "mdns-responder");
        thread.setDaemon(true);
        thread.start();
        announce();
        LOG.info("mDNS: el nombre " + fqdn + " apunta a " + describe());
    }

    @Override
    public void close() {
        running = false;
        if (socket != null) socket.close();
    }

    private void joinAllInterfaces() throws IOException {
        InetSocketAddress group = new InetSocketAddress(InetAddress.getByName(GROUP), 0);
        int joined = 0;
        for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            try {
                if (!nif.isUp() || nif.isLoopback() || !nif.supportsMulticast()) continue;
                socket.joinGroup(group, nif);
                joined++;
            } catch (IOException e) {
                LOG.log(Level.FINE, "mDNS: no se pudo unir a " + nif.getName(), e);
            }
        }
        if (joined == 0) LOG.warning("mDNS: ninguna interfaz de red admite multicast; el nombre " + fqdn + " no será visible.");
    }

    private String describe() {
        List<String> ips = new ArrayList<>();
        for (byte[] a : addresses) ips.add((a[0] & 255) + "." + (a[1] & 255) + "." + (a[2] & 255) + "." + (a[3] & 255));
        return String.join(", ", ips);
    }

    private void announce() {
        if (!multicast) return;
        try {
            byte[] packet = buildAnswer(fqdn, addresses, 0);
            for (int i = 0; i < 2; i++) {
                socket.send(new DatagramPacket(packet, packet.length, InetAddress.getByName(GROUP), MDNS_PORT));
            }
        } catch (IOException e) {
            LOG.log(Level.FINE, "mDNS: no se pudo enviar el anuncio inicial", e);
        }
    }

    private void loop() {
        byte[] buf = new byte[1500];
        while (running) {
            try {
                DatagramPacket in = new DatagramPacket(buf, buf.length);
                socket.receive(in);
                Query q = parse(in.getData(), in.getLength());
                if (q == null || !q.asksFor(fqdn)) continue;
                boolean legacy = in.getPort() != MDNS_PORT; // consulta de un resolvedor "clásico": se responde directo con su id
                byte[] reply = buildAnswer(fqdn, addresses, legacy ? q.id() : 0);
                if (legacy || q.unicastResponse()) {
                    socket.send(new DatagramPacket(reply, reply.length, in.getAddress(), in.getPort()));
                } else {
                    socket.send(new DatagramPacket(reply, reply.length, InetAddress.getByName(GROUP), MDNS_PORT));
                }
            } catch (IOException e) {
                if (running) LOG.log(Level.FINE, "mDNS: error al atender una consulta", e);
            } catch (RuntimeException e) {
                LOG.log(Level.FINE, "mDNS: paquete ignorado", e);
            }
        }
    }

    /* ---------------------------------------------------------- formato DNS (puro) */

    record Query(int id, List<String> names, List<Integer> types, List<Boolean> unicast) {
        boolean asksFor(String fqdn) {
            for (int i = 0; i < names.size(); i++) {
                if (names.get(i).equalsIgnoreCase(fqdn) && (types.get(i) == 1 || types.get(i) == 255)) return true;
            }
            return false;
        }

        boolean unicastResponse() {
            return unicast.contains(Boolean.TRUE);
        }
    }

    /** Lee las preguntas de un paquete DNS; devuelve null si no es una consulta válida. */
    static Query parse(byte[] data, int length) {
        if (length < 12) return null;
        int flags = u16(data, 2);
        if ((flags & 0x8000) != 0) return null; // es una respuesta, no una consulta
        int id = u16(data, 0);
        int questions = u16(data, 4);
        List<String> names = new ArrayList<>();
        List<Integer> types = new ArrayList<>();
        List<Boolean> unicast = new ArrayList<>();
        int pos = 12;
        for (int i = 0; i < questions; i++) {
            StringBuilder name = new StringBuilder();
            int[] end = new int[1];
            readName(data, length, pos, name, end);
            pos = end[0];
            if (pos + 4 > length) return null;
            types.add(u16(data, pos));
            unicast.add((u16(data, pos + 2) & 0x8000) != 0);
            names.add(name.toString());
            pos += 4;
        }
        return new Query(id, names, types, unicast);
    }

    private static void readName(byte[] d, int len, int start, StringBuilder out, int[] endPos) {
        int pos = start;
        boolean jumped = false;
        int hops = 0;
        while (true) {
            if (pos >= len || hops++ > 20) throw new IllegalArgumentException("nombre DNS inválido");
            int l = d[pos] & 0xFF;
            if (l == 0) {
                if (!jumped) endPos[0] = pos + 1;
                return;
            }
            if ((l & 0xC0) == 0xC0) { // puntero de compresión
                int target = ((l & 0x3F) << 8) | (d[pos + 1] & 0xFF);
                if (!jumped) endPos[0] = pos + 2;
                jumped = true;
                pos = target;
                continue;
            }
            if (pos + 1 + l > len) throw new IllegalArgumentException("etiqueta DNS fuera de rango");
            if (out.length() > 0) out.append('.');
            out.append(new String(d, pos + 1, l, StandardCharsets.UTF_8));
            pos += 1 + l;
        }
    }

    /** Respuesta autoritativa con un registro A por dirección (clase IN con el bit "cache-flush"). */
    static byte[] buildAnswer(String fqdn, List<byte[]> ipv4, int id) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        w16(out, id);
        w16(out, 0x8400);          // respuesta + autoritativa
        w16(out, 0);               // preguntas
        w16(out, ipv4.size());     // respuestas
        w16(out, 0);
        w16(out, 0);
        for (byte[] ip : ipv4) {
            for (String label : fqdn.split("\\.")) {
                byte[] b = label.getBytes(StandardCharsets.UTF_8);
                out.write(b.length);
                out.write(b, 0, b.length);
            }
            out.write(0);
            w16(out, 1);           // tipo A
            w16(out, 0x8001);      // clase IN + cache-flush
            out.write(TTL_SECONDS >>> 24); out.write(TTL_SECONDS >>> 16); out.write(TTL_SECONDS >>> 8); out.write(TTL_SECONDS);
            w16(out, 4);
            out.write(ip, 0, 4);
        }
        return out.toByteArray();
    }

    private static int u16(byte[] d, int i) {
        return ((d[i] & 0xFF) << 8) | (d[i + 1] & 0xFF);
    }

    private static void w16(ByteArrayOutputStream o, int v) {
        o.write(v >>> 8);
        o.write(v);
    }

    /* ------------------------------------------------------------ elegir la IP */

    /** IP de la interfaz con la que este equipo sale a la red local (sin enviar ningún paquete). */
    public static Inet4Address primaryAddress() {
        try (DatagramSocket s = new DatagramSocket()) {
            s.connect(InetAddress.getByName("192.0.2.1"), 9); // dirección de documentación: solo para consultar la ruta
            if (s.getLocalAddress() instanceof Inet4Address a && !a.isAnyLocalAddress() && !a.isLoopbackAddress()) return a;
        } catch (IOException ignored) {
            // sin ruta: se usan todas las interfaces
        }
        return null;
    }

    /** Todas las IPv4 de las interfaces activas (sin loopback ni 169.254.x.x). */
    public static List<Inet4Address> allAddresses() {
        List<Inet4Address> result = new ArrayList<>();
        try {
            for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!nif.isUp() || nif.isLoopback() || nif.isVirtual()) continue;
                for (InetAddress a : Collections.list(nif.getInetAddresses())) {
                    if (a instanceof Inet4Address v4 && !v4.isLinkLocalAddress()) result.add(v4);
                }
            }
        } catch (IOException ignored) {
            // se devuelve lo que haya
        }
        return result;
    }
}
