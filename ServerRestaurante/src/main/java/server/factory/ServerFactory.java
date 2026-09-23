package server.factory;

import environment.Environment;
import server.model.ServerModel;

public class ServerFactory {

    private ServerFactory() {
    }

    public static ServerModel crearServer() {
        Environment env = Environment.getInstance();
        return new ServerModel(env.getIp(), env.getPort(), env.getServerName());
    }
}