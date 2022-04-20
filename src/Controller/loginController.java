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
import java.util.ResourceBundle;
/*
* LoginPageController
*
* @author Michael Brown
* */
public class loginController implements Initializable {

    @ FXML
    private TextField passwordTextBox;
    @ FXML
    private TextField userTextBox;
    @ FXML
    private Label titleLabel;
    @ FXML
    private Label userNameLabel;
    @ FXML
    private Label passwordLabel;
    @ FXML
    private Button loginButton;
    @ FXML
    private Button clearButton;
    @ FXML
    private Button exitButton;
    @ FXML
    private Label zoneLabel;

    /**
     * switchScreen
     * loads new stage
     * @param event Button Click
     * @param switchPath path to new stage
     * @throws IOException
     */
    public void switchScreen(ActionEvent event, String switchPath) throws IOException {

        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(switchPath)));
        Scene scene = new Scene(parent);
        Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();


    }

    /**
     * pressLogonButton
     * attempts logon
     *
     * @param event Button Click
     * @throws IOException
     * @throws SQLException
     */
    public void pressLogonButton(ActionEvent event) throws IOException, SQLException {
        String userName = userTextBox.getText();
        String password = passwordTextBox.getText();

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

            switchScreen(event, "/View/appointmentView.fxml");

        }
        else {
            Locale userLocale = Locale.getDefault();
            ResourceBundle resources = ResourceBundle.getBundle("language_property/Languages");
            ButtonType clickOkay = new ButtonType(resources.getString("okayButton"), ButtonBar.ButtonData.OK_DONE);
            Alert failedLogon = new Alert(Alert.AlertType.WARNING, resources.getString("logonFailedButton"),
                    clickOkay);
            failedLogon.showAndWait();
        }

    }

    /**
     * pressClearButton
     * clears fields on page
     *
     * @param event Button Click
     * @throws IOException
     */
    public void pressClearButton(ActionEvent event) throws IOException {
        userTextBox.clear();
        passwordTextBox.clear();

    }

    /**
     * pressExitButton
     * closes Application
     *
     * @param event Button Click
     */
    public void pressExitButton(ActionEvent event) {
        LogonSession.logOff();
        System.exit(0);

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
        titleLabel.setText(resources.getString("titleLabel"));
        userNameLabel.setText(resources.getString("userNameLabel"));
        passwordLabel.setText(resources.getString("passwordLabel"));
        loginButton.setText(resources.getString("loginButton"));
        clearButton.setText(resources.getString("clearButton"));
        exitButton.setText(resources.getString("exitButton"));




    }
}
