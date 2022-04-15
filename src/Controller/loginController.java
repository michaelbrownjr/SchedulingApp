package Controller;

import Helper.Alerts;
import Helper.Logger;
import Model.LogonSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
/*
* LoginPageController
*
* @author Michael Brown
* */
public class loginController implements Initializable {

    @FXML Label userNameLabel;
    @FXML Label passwordLabel;

    @FXML TextField userTextBox;
    @FXML TextField passwordTextBox;

    @FXML Button loginButton;
    @FXML Button clearButton;
    @FXML Button exitButton;

    private String alertTitle;
    private String alertHeader;
    private String alertContext;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Locale locale = Locale.getDefault();
        resourceBundle = ResourceBundle.getBundle("resources/Languages", locale);
        userNameLabel.setText(resourceBundle.getString("Username"));
        passwordLabel.setText(resourceBundle.getString("Password"));
        loginButton.setText(resourceBundle.getString("login"));
        alertTitle = resourceBundle.getString("alertTitle");
        alertHeader = resourceBundle.getString("alertHeader");
        alertContext = resourceBundle.getString("alertContext");
        exitButton.setText(resourceBundle.getString("Exit"));
    }

    public void pressLogonButton(ActionEvent actionEvent) throws IOException {
        String userName = userTextBox.getText();
        String password = passwordTextBox.getText();
        boolean validUser = LogonSession.login(userName, password);
        if(validUser == true){
            // Launch Main Screen
            Logger.log(userName, true, "Login Attempt");

            Stage stage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("appointmentView.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(alertTitle);
            alert.setHeaderText(alertHeader);
            alert.setContentText(alertContext);
            alert.showAndWait();
        }
    }

    public void pressClearButton(ActionEvent actionEvent) {
    }

    public void pressExitButton(ActionEvent actionEvent) {
        Alerts.confirmDialog("Exit", "Are you sure you would like to exit the program?");
        {
            System.exit(0);
        }
    }
}
