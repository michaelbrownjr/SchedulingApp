package Controller;

import Helper.DBConnection;
import Model.Appointment;
import Model.AppointmentDB;
import Model.ContactDB;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

/**
 * This class creates three ways to create a report reflecting the information from
 * the Appointment view and/or the Customer view.
 */
public class reportsController implements Initializable {
    @FXML
    Button appointmentReportButton;
    @FXML
    Button contactScheduleReportButton;
    @FXML
    Button hoursPerContactButton;
    @FXML
    TextArea reportTextField;
    @FXML
    Button backButton;

    /**
     * screenChange
     * loads new stage
     *
     * @param event Button Click
     * @param switchPath path to new stage
     * @throws IOException
     */
    public void screenChange(ActionEvent event, String switchPath) throws IOException {
        Parent parent = FXMLLoader.load(getClass().getResource(switchPath));
        Scene scene = new Scene(parent);
        Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    /**
     * backButtonActivity
     * navigates to previous stage
     *
     * @param event Button Click
     * @throws IOException
     */
    public void backButtonActivity(ActionEvent event) throws IOException {
        screenChange(event, "/View/appointmentView.fxml");

    }

    /**
     * appointmentReportButton
     * populates first report
     *
     * @param event Button Click
     * @throws SQLException
     */
    public void appointmentReportButton(ActionEvent event) throws SQLException {

        ObservableList<String> reportStrings = AppointmentDB.reportTotalsByTypeAndMonth();

        for (String str : reportStrings) reportTextField.appendText(str);

    }

    /**
     * hoursPerContactActivity
     * populates second report
     *
     * @param event Button Click
     * @throws SQLException
     */
    public void hoursPerContactActivity(ActionEvent event ) throws SQLException {
        ObservableList<String> contacts = ContactDB.getAllContactName();

        for (String contact: contacts) {
            String contactID = ContactDB.findContactID(contact).toString();
            reportTextField.appendText("Contact Name: " + contact + " ID: " + contactID + "\n");
            reportTextField.appendText("    Total Hours scheduled: " + ContactDB.getHoursScheduled(contactID) + "\n");
        }
    }



    /**
     * contactScheduleActivity
     * populates 3rd report
     *
     * @param event Button Click
     * @throws SQLException
     */
    public void contactScheduleActivity(ActionEvent event) throws SQLException {

        ObservableList<String> contacts = ContactDB.getAllContactName();

        for (String contact : contacts) {
            String contactID = ContactDB.findContactID(contact).toString();
            reportTextField.appendText("Contact Name: " + contact + " ID: " + contactID + "\n");

            ObservableList<String> appts = ContactDB.getContactAppts(contactID);
            if(appts.isEmpty()) {
                reportTextField.appendText("    No appointments for contact \n");
            }
            for (String appt : appts) {
                reportTextField.appendText(appt);
            }

        }
    }


    /**
     * initialize
     * populates stage
     *
     * @param location location / time zone
     * @param resources resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {



    }
}
