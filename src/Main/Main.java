package Main;

import Helper.DBConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
        launch(args);
        DBConnection.closeConnection();

    }
}
