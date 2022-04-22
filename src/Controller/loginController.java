package Controller;

import Helper.Alerts;
import Helper.Logger;
import Model.Appointment;
import Model.LogonSession;
import Model.UserDB;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
/*
* LoginPageController
*
* @author Michael Brown
* */
public class loginController implements Initializable {

    @ FXML
    private TextField passwordTextField;
    @ FXML
    private TextField userTextField;
    @ FXML
    private Label titleTextLabel;
    @ FXML
    private Label usernameTextLabel;
    @ FXML
    private Label passwordTextLabel;
    @ FXML
    private Button loginButton;
    @ FXML
    private Button clearButton;
    @ FXML
    private Button exitButton;
    @ FXML
    private Label zoneLabel;

    /**
     * screenChange
     * loads new stage
     * @param event Button Click
     * @param switchPath path to new stage
     * @throws IOException
     */
    public void screenChange(ActionEvent event, String switchPath) throws IOException {

        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(switchPath)));
        Scene scene = new Scene(parent);
        Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();


    }

    /**
     * logonButtonActivity
     * attempts logon
     *
     * @param event Button Click
     * @throws IOException
     * @throws SQLException
     */
    public void logonButtonActivity(ActionEvent event) throws IOException, SQLException {
        String userName = userTextField.getText();
        String password = passwordTextField.getText();

        // Attempt Login
        boolean logon = LogonSession.login(userName, password);

        // Log Login attempt
        Logger.log(userName, logon, "Happy Days");

        if (logon) {


            // Get appointments in 15 minutes and display notification if there is.
            ObservableList<Appointment> upcomingAppts = Model.AppointmentDB.getAppointmentsIn15Mins();

            if (!upcomingAppts.isEmpty()) {
                for (Appointment upcoming : upcomingAppts ) {

                    String message = "Upcoming appointmentID: " + upcoming.getAppointmentID() + " Start: " +
                            upcoming.getStartDateTime().toString();
                    ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                    Alert invalidInput = new Alert(Alert.AlertType.WARNING, message, clickOkay);
                    invalidInput.showAndWait();

                }

            }
            // If no appointments in 15 minutes, display notification that no upcoming appointments.
            else {
                ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                Alert invalidInput = new Alert(Alert.AlertType.CONFIRMATION, "No upcoming Appointments", clickOkay);
                invalidInput.showAndWait();
            }

            screenChange(event, "/View/appointmentView.fxml");

        }
        else {
            Locale userLocale = Locale.getDefault();
            ResourceBundle resources = ResourceBundle.getBundle("Resources/Languages");
            ButtonType clickOkay = new ButtonType(resources.getString("okayButton"), ButtonBar.ButtonData.OK_DONE);
            Alert failedLogon = new Alert(Alert.AlertType.WARNING, resources.getString("logonFailedButton"),
                    clickOkay);
            failedLogon.showAndWait();
        }

    }

    /**
     * clearButtonActivity
     * clears fields on page
     *
     * @param event Button Click
     * @throws IOException
     */
    public void clearButtonActivity(ActionEvent event) throws IOException {
        userTextField.clear();
        passwordTextField.clear();

    }

    /**
     * exitButtonActivity
     * closes Application
     *
     * @param event Button Click
     */
    public void exitButtonActivity(ActionEvent event) {
        ButtonType clickYes = ButtonType.YES;
        ButtonType clickNo = ButtonType.NO;

        // show alert and ensure user wants to exit
        Alert exit = new Alert(Alert.AlertType.WARNING,"Are you sure you want to exit?", clickYes, clickNo);
        exit.setTitle("Alert!");
        exit.setHeaderText("Confirm");



        exit.showAndWait().ifPresent((response -> {
            if (response == clickYes) {
                LogonSession.exit();
                System.exit(0);
            }
        }));
    }

    /**
     * initialize
     * populates stage
     *
     * @param location Time zone/ location
     * @param resources resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Locale userLocale = Locale.getDefault();
        zoneLabel.setText(ZoneId.systemDefault().toString());
        resources = ResourceBundle.getBundle("Resources/Languages");
        titleTextLabel.setText(resources.getString("titleTextLabel"));
        usernameTextLabel.setText(resources.getString("usernameTextLabel"));
        passwordTextLabel.setText(resources.getString("passwordTextLabel"));
        loginButton.setText(resources.getString("loginButton"));
        clearButton.setText(resources.getString("clearButton"));
        exitButton.setText(resources.getString("exitButton"));




    }
}
