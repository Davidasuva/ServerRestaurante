package com.proyectoApi.api.views.ui;

import com.proyectoApi.api.views.ui.factory.ApiFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class ApiMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        ApiFactory.start(stage);
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Aplicación cerrada — apagando la vista móvil.");
        ApiFactory.shutdown();
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        Application.launch(ApiMain.class, args);
    }
}
