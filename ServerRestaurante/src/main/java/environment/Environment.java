package environment;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class Environment {

    private String ip;
    private int port;
    private String database;
    private String serverName;
    private static Environment instance;

    private Environment(){
        loadConfig();

        this.ip = System.getProperty(
                "server.ip",
                System.getenv().getOrDefault("SERVER_IP", "10.153.96.175")
        );

        this.port = Integer.parseInt(
                System.getProperty(
                        "server.port",
                        System.getenv().getOrDefault("SERVER_PORT", "1808")
                )
        );

        this.serverName = System.getProperty(
                "server.name",
                System.getenv().getOrDefault("SERVER_NAME", "Uwu")
        );

        this.database = System.getProperty(
                "server.database",
                System.getenv().getOrDefault("DATABASE_IP", "A")
        );
    }

    private void loadConfig() {
        Properties config = new Properties();

        try {
            String basePath = System.getProperty("user.dir");

            File configFile = new File(basePath + File.separator +
                    "server" + File.separator +
                    "config.properties");

            if (!configFile.exists()) {
                configFile = new File(basePath + File.separator + "config.properties");
            }

            FileInputStream fin = new FileInputStream(configFile);
            config.load(fin);
            fin.close();

            System.setProperty("server.ip", config.getProperty("SERVER_IP"));
            System.setProperty("server.port", config.getProperty("SERVER_PORT"));
            System.setProperty("server.name", config.getProperty("SERVER_NAME"));
            System.setProperty("server.database", config.getProperty("DATABASE_IP"));

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public String getDatabase() {
        return database;
    }
}
