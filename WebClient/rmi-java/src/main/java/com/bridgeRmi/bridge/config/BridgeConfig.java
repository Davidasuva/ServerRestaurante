package com.bridgeRmi.bridge.config;

public class BridgeConfig {
    public final String rmiHost;
    public final int rmiPort;
    public final String rmiBindingName;
    public final int httpPort;

    public BridgeConfig() {
        this.rmiHost = System.getenv().getOrDefault("RMI_HOST", "localhost");
        this.rmiPort = Integer.parseInt(System.getenv().getOrDefault("RMI_PORT", "1099"));
        this.rmiBindingName = System.getenv().getOrDefault("RMI_BINDING_NAME", "MyRemoteService");
        this.httpPort = Integer.parseInt(System.getenv().getOrDefault("BRIDGE_HTTP_PORT", "8081"));
    }
}
