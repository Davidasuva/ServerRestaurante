package com.proyectoApi.api.views.ui.factory;

import com.proyectoApi.api.views.mobile.VistaMovilService;
import com.proyectoApi.api.views.ui.login.LoginView;
import com.proyectoApi.api.views.ui.panel.PanelController;
import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Equivalente a ServerFactory: crea las escenas y navega entre ellas. */
public class ApiFactory {

    private static final String RUTA_VISTAS = "/";

    private static VistaMovilService compartido;
    private static Scene scenePanel;
    private static PanelController panelController;

    private ApiFactory() {
    }

    public static void start(Stage stage) {
        try {
            compartido = new VistaMovilService();
            LoginView.show(stage);
            buildMenuScenes(compartido);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al empezar las vistas");
        }
    }

    public static void buildMenuScenes(VistaMovilService vista) {
        compartido = vista;
        try {
            scenePanel = buildScene("panel/PanelView.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al pre-crear escenarios: " + e.getMessage());
        }
    }

    private static Scene buildScene(String fxml) throws Exception {
        URL url = ApiFactory.class.getResource(RUTA_VISTAS + fxml);
        if (url == null) {
            throw new IOException("No se encontró " + RUTA_VISTAS + fxml);
        }
        FXMLLoader loader = new FXMLLoader(url);
        loader.setControllerFactory(tipo -> {
            try {
                return tipo.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException("No se pudo crear el controller " + tipo.getName(), ex);
            }
        });
        Parent root = loader.load();
        Scene scene = new Scene(root, 720, 480);

        Object controller = loader.getController();
        if (controller instanceof PanelController c) {
            panelController = c;
            c.setVistaMovil(compartido);
        } else {
            throw new IllegalStateException("Controller desconocido: " + controller);
        }
        return scene;
    }

    public static void navigateToPanel(Stage stage) {
        if (scenePanel == null) {
            throw new IllegalStateException("Las escenas no están creadas: llama a buildMenuScenes(vista) primero.");
        }
        if (panelController != null) {
            panelController.refrescar();
        }
        stage.setTitle("Restaurante API - Vista móvil");
        stage.setScene(scenePanel);
        stage.centerOnScreen();
    }

    /** Se llama al cerrar la aplicación: apaga la vista móvil si estaba encendida. */
    public static void shutdown() {
        if (compartido != null) {
            compartido.apagarYEsperar();
        }
    }
}
