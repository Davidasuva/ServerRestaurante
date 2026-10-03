package server.view.login;

import com.sun.javafx.fxml.FXMLLoaderHelper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginView {

    private LoginView(){
    }

    public static void show(Stage stage) throws IOException{
        FXMLLoader loader = new FXMLLoader(
                LoginView.class.getResource("LoginView.fxml")
        );
        Parent root = loader.load();
        Scene scene = new Scene(root, 720, 480);

        java.net.URL cssURL= LoginView.class.getResource("LoginView.css");
        if(cssURL!=null){
            scene.getStylesheets().add(cssURL.toExternalForm());
        }

        stage.setTitle("Restautante - Iniciar Sesión");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }
}
