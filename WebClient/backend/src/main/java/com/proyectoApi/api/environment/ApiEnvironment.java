package com.proyectoApi.api.environment;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

/**
 * Lee la conexión RMI (rmi.host, rmi.port, rmi.name) para el login.
 * Orden de prioridad (de menor a mayor), igual que Spring Boot:
 *   application.properties del jar  &lt;  ./application.properties  &lt;  ./config/application.properties
 *   &lt;  variables de entorno RMI_HOST / RMI_PORT / RMI_NAME  &lt;  -Drmi.host / -Drmi.port / -Drmi.name
 */
public class ApiEnvironment {

    private static ApiEnvironment instance;

    private final String rmiHost;
    private final int rmiPort;
    private final String rmiName;

    private ApiEnvironment() {
        Properties p = new Properties();
        try (InputStream in = ApiEnvironment.class.getResourceAsStream("/application.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        overlay(p, new File("application.properties"));
        overlay(p, new File("config", "application.properties"));

        this.rmiHost = leer(p, "rmi.host", "RMI_HOST", "localhost");
        this.rmiPort = Integer.parseInt(leer(p, "rmi.port", "RMI_PORT", "1099"));
        this.rmiName = leer(p, "rmi.name", "RMI_NAME", "restaurante");
    }

    private static void overlay(Properties base, File file) {
        if (!file.isFile()) {
            return;
        }
        try (var in = new java.io.FileInputStream(file)) {
            base.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String leer(Properties p, String key, String envKey, String def) {
        String v = System.getProperty(key);
        if (v == null || v.isBlank()) {
            v = System.getenv(envKey);
        }
        if (v == null || v.isBlank()) {
            v = p.getProperty(key);
        }
        return v == null || v.isBlank() ? def : v.trim();
    }

    public static synchronized ApiEnvironment getInstance() {
        if (instance == null) {
            instance = new ApiEnvironment();
        }
        return instance;
    }

    public String getRmiHost() { return rmiHost; }

    public int getRmiPort() { return rmiPort; }

    public String getRmiName() { return rmiName; }
}
