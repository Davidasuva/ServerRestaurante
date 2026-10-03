package server.view.pedidos;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PedidosView {

    private PedidosView(){
    }

    public static void show(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                PedidosView.class.getResource("PedidosView.fxml")
        );
        Parent root = loader.load();
        Scene scene = new Scene(root, 720, 480);

        java.net.URL cssURL= PedidosView.class.getResource("PedidosView.css");
        if(cssURL!=null){
            scene.getStylesheets().add(cssURL.toExternalForm());
        }

        stage.setTitle("Restautante - Sección Pedidos");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }
}
