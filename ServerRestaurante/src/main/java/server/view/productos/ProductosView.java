package server.view.productos;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;

public class ProductosView {

    private ProductosView(){
    }

    public static void show(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                ProductosView.class.getResource("ProductosView.fxml")
        );
        Parent root = loader.load();
        Scene scene = new Scene(root, 720, 480);

        java.net.URL cssURL= ProductosView.class.getResource("ProductosView.css");
        if(cssURL!=null){
            scene.getStylesheets().add(cssURL.toExternalForm());
        }

        stage.setTitle("Restautante - Sección Producto");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }
}
