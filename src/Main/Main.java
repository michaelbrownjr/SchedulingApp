package Main;

import Helper.DBConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/View/loginView.fxml"));
        stage.setTitle("Software II - Advance Java Concept - 195");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    public static void main(String[] args){
        DBConnection.openConnection();
        //Locale.setDefault(new Locale("fr"));
        ResourceBundle rb = ResourceBundle.getBundle("Resources/Languages", Locale.getDefault());
        if (Locale.getDefault() == Locale.FRENCH){
            System.out.println(rb.getString("hello") + " " + rb.getString("world"));
        } else System.out.println("hello world");
        launch(args);

        DBConnection.closeConnection();

    }
}
