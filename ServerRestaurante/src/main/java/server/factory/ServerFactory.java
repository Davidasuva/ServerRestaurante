package server.factory;

import environment.Environment;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import server.controller.login.LoginController;
import server.model.ServerModel;
import server.model.empleado.Empleado;
import server.model.empleado.EmpleadoInterface;
import server.model.empleado.EmpleadoService;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.function.Consumer;

public class ServerFactory {

    private ServerFactory() {
    }

    public static ServerModel crearServer() {
        Environment env = Environment.getInstance();
        return new ServerModel(env.getIp(), env.getPort(), env.getServerName());
    }
    public static EmpleadoInterface crearEmpleadoService(ServerModel server) {
        try {
            return new EmpleadoService(server.getHistory());
        } catch (RemoteException e) {
            throw new RuntimeException("No se pudo crear el servicio de empleados: " + e.getMessage(), e);
        }
    }

    public static Parent crearLoginView(ServerModel server, Consumer<Empleado> onLoginExitoso) {
        URL fxml = ServerFactory.class.getResource("/server/view/login/LoginView.fxml");
        if (fxml == null) {
            throw new IllegalStateException(
                    "No se encontró LoginView.fxml en el classpath. Haz Maven -> Reload project y un clean.");
        }

        EmpleadoInterface empleadoService = crearEmpleadoService(server);

        try {
            FXMLLoader loader = new FXMLLoader(fxml);
            loader.setControllerFactory(clase -> new LoginController(empleadoService, onLoginExitoso));
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar LoginView.fxml: " + e.getMessage(), e);
        }
    }
}