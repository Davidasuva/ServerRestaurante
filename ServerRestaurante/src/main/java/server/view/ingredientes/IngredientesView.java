package server.view.ingredientes;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class IngredientesView {

    private IngredientesView(){
    }

    public static void show(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                IngredientesView.class.getResource("IngredientesView.fxml")
        );
        Parent root = loader.load();
        Scene scene = new Scene(root, 720, 480);

        java.net.URL cssURL= IngredientesView.class.getResource("IngredientesView.css");
        if(cssURL!=null){
            scene.getStylesheets().add(cssURL.toExternalForm());
        }

        stage.setTitle("Restautante - Sección Ingredientes");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }
}
