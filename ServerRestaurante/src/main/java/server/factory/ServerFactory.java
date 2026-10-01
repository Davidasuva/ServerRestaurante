package server.factory;

import environment.Environment;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.controller.SeccionBaseController;
import server.controller.employees.EmpleadosController;
import server.controller.ingredientes.IngredientesController;
import server.controller.mesas.MesasController;
import server.controller.pedidos.PedidosController;
import server.controller.productos.ProductosController;
import server.model.ServerModel;
import server.view.login.LoginView;

import java.io.IOException;
import java.net.URL;

public class ServerFactory {

    private static final String RUTA_VISTAS = "/server/view/";

    private ServerFactory() {
    }

    private static Scene sceneEmpleados;
    private static Scene scenePedidos;
    private static Scene sceneIngredientes;
    private static Scene sceneProductos;
    private static Scene sceneMesas;

    private static ServerModel compartido;

    private static EmpleadosController empleadosController;
    private static IngredientesController ingredientesController;
    private static PedidosController pedidosController;
    private static MesasController mesasController;
    private static ProductosController productosController;

    public static void start(Stage stage) {
        try {
            Environment env = Environment.getInstance();
            ServerModel model = new ServerModel(env.getIp(), env.getPort(), env.getServerName());
            compartido = model;

            LoginView.show(stage);
            buildMenuScenes(model);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al empezar las vistas");
        }
    }

    public static void buildMenuScenes(ServerModel model) {
        compartido = model;
        try {
            sceneEmpleados = buildScene("empleados/EmpleadosView.fxml");
            sceneIngredientes = buildScene("ingredientes/IngredientesView.fxml");
            sceneMesas = buildScene("mesas/MesasView.fxml");
            scenePedidos = buildScene("pedidos/PedidosView.fxml");
            sceneProductos = buildScene("productos/ProductosView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al pre-crear escenarios: " + e.getMessage());
        }
    }

    private static Scene buildScene(String fxml) throws Exception {
        URL url = ServerFactory.class.getResource(RUTA_VISTAS + fxml);
        if (url == null) {
            throw new IOException("No se encontró " + RUTA_VISTAS + fxml);
        }

        FXMLLoader loader = new FXMLLoader(url);

        loader.setControllerFactory(tipo -> {
            try {
                Object controller = tipo.getDeclaredConstructor().newInstance();
                return controller;
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException("No se pudo crear el controller " + tipo.getName(), ex);
            }
        });

        Parent root = loader.load();
        Scene scene = new Scene(root, 1280, 800);


        switch (loader.getController()) {
            case EmpleadosController c -> { empleadosController = c; c.setModel(compartido); }
            case IngredientesController c -> { ingredientesController = c; c.setModel(compartido); }
            case MesasController c -> { mesasController = c; c.setModel(compartido); }
            case PedidosController c -> { pedidosController = c; c.setModel(compartido); }
            case ProductosController c -> { productosController = c; c.setModel(compartido); }
            default -> throw new IllegalStateException("Controller desconocido: " + loader.getController());
        }
        return scene;
    }

    private static void applySceneAndMaximize(Stage stage, Scene scene, String title) {
        if (scene == null) {
            throw new IllegalStateException("Las escenas no están creadas: llama a buildMenuScenes(model) primero.");
        }
        if (stage.isMaximized()) {
            stage.setMaximized(false);
        }
        stage.setTitle(title);
        stage.setScene(scene);
        Platform.runLater(() -> stage.setMaximized(true));
    }

    private static void irA(Stage stage, Scene scene, SeccionBaseController controller, String titulo) {
        if (controller != null) {
            controller.refrescar();
        }
        applySceneAndMaximize(stage, scene, titulo);
    }

    public static void navigateToEmpleados(Stage stage) {
        irA(stage, sceneEmpleados, empleadosController, "Restaurante - Sección Empleados");
    }

    public static void navigateToMesas(Stage stage) {
        irA(stage, sceneMesas, mesasController, "Restaurante - Sección Mesas");
    }

    public static void navigateToIngredientes(Stage stage) {
        irA(stage, sceneIngredientes, ingredientesController, "Restaurante - Sección Ingredientes");
    }

    public static void navigateToPedidos(Stage stage) {
        irA(stage, scenePedidos, pedidosController, "Restaurante - Sección Pedidos");
    }

    public static void navigateToProductos(Stage stage) {
        irA(stage, sceneProductos, productosController, "Restaurante - Sección Productos");
    }
}
