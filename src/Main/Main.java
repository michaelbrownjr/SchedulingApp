package Main;

import Helper.DBConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * This class creates a Scheduling app that connects to a DB opening a connect to manupulate the
 * scheduling data from the database.
 */
public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/View/loginView.fxml")));
        stage.setTitle("Software II - Advance Java Concept - 195");
        stage.setScene(new Scene(root, 450, 250));
        stage.show();
    }

    public static void main(String[] args) {
        DBConnection.openConnection();
        launch(args);

        DBConnection.closeConnection();

    }
}
