package server.factory;

import environment.Environment;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.controller.employees.EmpleadosController;
import server.controller.ingredientes.IngredientesController;
import server.controller.login.LoginController;
import server.controller.mesas.MesasController;
import server.controller.pedidos.PedidosController;
import server.controller.productos.ProductosController;
import server.model.ServerModel;

import server.view.login.LoginView;

import java.io.IOException;
public class ServerFactory {

    private ServerFactory() {
    }

    private static Scene sceneEmpleados;
    private static Scene scenePedidos;
    private static Scene sceneIngredientes;
    private static Scene sceneProductos;
    private static Scene sceneMesas;

    private static ServerModel compartido;

    private static EmpleadosController empleadosController = new EmpleadosController();
    private static IngredientesController ingredientesController = new IngredientesController();
    private static LoginController loginController = new LoginController();
    private static PedidosController pedidosController = new PedidosController();
    private static MesasController mesasController = new MesasController();
    private static ProductosController productosController= new ProductosController();

    public static void start(Stage stage){
        try{
            Environment env=Environment.getInstance();
            ServerModel model= new ServerModel(env.getIp(),env.getPort(),env.getServerName());

            LoginView.show(stage);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al empezar las vistas");
        }
    }

    public static void buildMenuScenes(ServerModel model){
        compartido=model;
        try{
            sceneEmpleados=buildScene("/server/view/empleados/EmpleadosView.fxm",server.view.empleados.EmpleadosView.class,"Empleados");
            sceneIngredientes=buildScene("/server/view/ingredientes/IngredientesView.fxm",server.view.ingredientes.IngredientesView.class,"Ingredientes");
            sceneMesas=buildScene("/server/view/mesas/MesasView.fxm",server.view.mesas.MesasView.class,"Mesas");
            scenePedidos=buildScene("/server/view/pedidos/PedidosView.fxm",server.view.pedidos.PedidosView.class,"Pedidos");
            sceneProductos=buildScene("/server/view/productos/ProductosView.fxm",server.view.productos.ProductosView.class,"Productos");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al pre-crear excenarios "+e.getMessage());
        }
    }

    private static Scene buildScene(String fxmlPath,Class<?> refClass, String tipo)throws Exception{
        FXMLLoader loader = new FXMLLoader(refClass.getResource(fxmlPath));
        if (loader.getLocation() == null)
            loader = new FXMLLoader(ServerFactory.class.getResource(fxmlPath));

        Parent root  = loader.load();
        Scene  scene = new Scene(root, 1280, 800);

        Object ctrl = loader.getController();

        switch (tipo){
            case "Empleados" -> {empleadosController=(EmpleadosController) ctrl; empleadosController.setModel(compartido);}
            case "Ingredientes" -> {ingredientesController=(IngredientesController) ctrl; ingredientesController.setModel(compartido);}
            case "Mesas" -> {mesasController=(MesasController) ctrl; mesasController.setModel(compartido);}
            case "Pedidos" -> {pedidosController=(PedidosController) ctrl; pedidosController.setModel(compartido);}
            case "Productos" -> {productosController=(ProductosController) ctrl; productosController.setModel(compartido);}
        }

        return scene;
    }

    private static void applySceneAndMaximize(Stage stage, javafx.scene.Scene scene,String title){
        if(stage.isMaximized()){
            stage.setMaximized(false);
        }
        stage.setTitle(title);
        stage.setScene(scene);
        Platform.runLater(()->stage.setMaximized(true));
    }

    public static void navigateToEmpleados(Stage stage){
        if(empleadosController!=null){
            empleadosController.refreshEmpleados();
        }
        applySceneAndMaximize(stage,sceneEmpleados,"Restaurante - Sección Empleados");
    }
    public static void navigateToMesas(Stage stage){
        if(empleadosController!=null){
            empleadosController.refreshEmpleados();
        }
        applySceneAndMaximize(stage,sceneEmpleados,"Restaurante - Sección Empleados");

    }
    public static void navigateToIngredientes(Stage stage){
        if(ingredientesController!=null){
            ingredientesController.refreshEmpleados();
        }
        applySceneAndMaximize(stage,sceneEmpleados,"Restaurante - Sección Ingredientes");

    }
    public static void navigateToPedidos(Stage stage){
        if(pedidosController!=null){
            pedidosController.refreshEmpleados();
        }
        applySceneAndMaximize(stage,sceneEmpleados,"Restaurante - Sección Pedidos");

    }
    public static void navigateToProductos(Stage stage){
        if(productosController!=null){
            productosController.refreshEmpleados();
        }
        applySceneAndMaximize(stage,sceneEmpleados,"Restaurante - Sección Productos");

    }

}