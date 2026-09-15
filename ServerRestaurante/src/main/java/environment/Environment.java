package environment;

public class Environment {

    private String ip;
    private int port;
    private String serverName;
    private static Environment instance;

    private Environment(){
        System.getProperties().put("config.file.path",System.getenv().getOrDefault("CONFIG_FILE_PATH","config.properties"));

        this.ip=System.getProperties().getProperty("server.ip",System.getenv().getOrDefault("SERVER_IP","localhost"));
        this.port=Integer.parseInt(System.getProperties().getProperty("server.port",System.getenv().getOrDefault("SERVER_PORT","3456")));
        this.serverName=System.getProperties().getProperty("server.name",System.getenv().getOrDefault("SERVER_NAME","UwU"));
    }

    public static Environment getInstance(){
        if(instance==null){
            instance=new Environment();
        }
        return instance;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getServerName() {
        return serverName;
    }

}
