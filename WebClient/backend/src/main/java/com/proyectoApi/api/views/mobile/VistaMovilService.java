package com.proyectoApi.api.views.mobile;

import com.proyectoApi.api.ApiApplication;
import com.proyectoApi.api.net.LanAddresses;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/**
 * Enciende y apaga la "vista móvil": el servidor web de Spring Boot
 * (REST + página del celular). Mientras esté apagada no se abre ningún puerto.
 * Todo el trabajo pesado se hace en un hilo aparte para no congelar la ventana.
 */
public class VistaMovilService {

    public enum Estado { APAGADA, INICIANDO, ENCENDIDA, APAGANDO }

    /** Se invoca desde un hilo de fondo: la UI debe pasar a Platform.runLater. */
    public interface Listener {
        void cambio(Estado estado, List<String> urls, String error);
    }

    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "vista-movil");
        t.setDaemon(true);
        // En un jar de Spring Boot hay que usar el class loader de la app.
        t.setContextClassLoader(ApiApplication.class.getClassLoader());
        return t;
    });
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private ConfigurableApplicationContext contexto;
    private volatile Estado estado = Estado.APAGADA;
    private volatile List<String> urls = List.of();

    public void addListener(Listener l) {
        listeners.add(l);
    }

    public Estado getEstado() {
        return estado;
    }

    public List<String> getUrls() {
        return urls;
    }

    /** Inicia la vista móvil. Si ya está iniciando o encendida, no hace nada. */
    public void iniciar() {
        synchronized (this) {
            if (estado != Estado.APAGADA) {
                return;
            }
            cambiar(Estado.INICIANDO, List.of(), null);
        }
        ejecutor.execute(() -> {
            try {
                SpringApplication app = new SpringApplication(ApiApplication.class);
                app.setHeadless(false); // no interferir con JavaFX
                ConfigurableApplicationContext ctx = app.run();
                synchronized (this) {
                    contexto = ctx;
                }
                cambiar(Estado.ENCENDIDA, calcularUrls(ctx.getEnvironment()), null);
            } catch (Throwable t) {
                synchronized (this) {
                    contexto = null;
                }
                cambiar(Estado.APAGADA, List.of(), mensaje(t));
            }
        });
    }

    /** Apaga la vista móvil y libera el puerto. */
    public void apagar() {
        synchronized (this) {
            if (estado != Estado.ENCENDIDA) {
                return;
            }
            cambiar(Estado.APAGANDO, List.of(), null);
        }
        ejecutor.execute(this::cerrarContexto);
    }

    /** Apagado bloqueante para cuando se cierra la aplicación. */
    public void apagarYEsperar() {
        ejecutor.execute(this::cerrarContexto);
        ejecutor.shutdown();
        try {
            ejecutor.awaitTermination(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void cerrarContexto() {
        ConfigurableApplicationContext ctx;
        synchronized (this) {
            ctx = contexto;
            contexto = null;
        }
        if (ctx == null) {
            return;
        }
        String error = null;
        try {
            ctx.close();
        } catch (Throwable t) {
            error = "Error al apagar: " + mensaje(t);
        }
        cambiar(Estado.APAGADA, List.of(), error);
    }

    private void cambiar(Estado nuevo, List<String> nuevasUrls, String error) {
        this.estado = nuevo;
        this.urls = nuevasUrls;
        for (Listener l : listeners) {
            try {
                l.cambio(nuevo, nuevasUrls, error);
            } catch (RuntimeException ignored) {
                // un listener roto no debe afectar al servicio
            }
        }
    }

    private static List<String> calcularUrls(Environment env) {
        String port = env.getProperty("server.port", "8080");
        String sufijo = "80".equals(port) ? "/" : ":" + port + "/";
        List<String> out = new ArrayList<>();
        List<LanAddresses.Entry> direcciones = LanAddresses.list();
        for (LanAddresses.Entry e : direcciones) {
            if (e.primary()) {
                out.add("http://" + e.address().getHostAddress() + sufijo);
            }
        }
        if (out.isEmpty()) { // sin interfaz principal: ofrecer las reales (no virtuales)
            for (LanAddresses.Entry e : direcciones) {
                if (!e.virtual()) {
                    out.add("http://" + e.address().getHostAddress() + sufijo);
                }
            }
        }
        if (Boolean.parseBoolean(env.getProperty("app.mdns.enabled", "true"))) {
            out.add("http://" + env.getProperty("app.mdns.name", "restaurante") + ".local" + sufijo);
        }
        if (out.isEmpty()) {
            out.add("http://localhost" + sufijo);
        }
        return out;
    }

    private static String mensaje(Throwable t) {
        Throwable raiz = t;
        while (raiz.getCause() != null && raiz.getCause() != raiz) {
            raiz = raiz.getCause();
        }
        String m = raiz.getMessage();
        if (raiz instanceof java.net.BindException || (m != null && m.toLowerCase().contains("already in use"))) {
            return "El puerto ya está en uso. Cierra lo que lo ocupa o cambia server.port.";
        }
        return m == null || m.isBlank() ? raiz.getClass().getSimpleName() : m;
    }
}
