package server.view.empleados;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class EmpleadosView {

    private EmpleadosView(){
    }
    public static void show(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                EmpleadosView.class.getResource("EmpleadosView.fxml")
        );
        Parent root = loader.load();
        Scene scene = new Scene(root, 720, 480);

        java.net.URL cssURL= EmpleadosView.class.getResource("EmpleadosView.css");
        if(cssURL!=null){
            scene.getStylesheets().add(cssURL.toExternalForm());
        }

        stage.setTitle("Restautante - Sección Empleados");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }
}
