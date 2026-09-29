package server.view.mesas;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.view.ingredientes.IngredientesView;

import java.io.IOException;

public class MesasView {

    private MesasView(){
    }

    public static void show(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MesasView.class.getResource("MesasView.fxml")
        );
        Parent root = loader.load();
        Scene scene = new Scene(root, 720, 480);

        java.net.URL cssURL= MesasView.class.getResource("MesasView.css");
        if(cssURL!=null){
            scene.getStylesheets().add(cssURL.toExternalForm());
        }

        stage.setTitle("Restautante - Sección Mesas");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }
}
