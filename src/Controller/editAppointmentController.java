package Controller;

import Helper.Alerts;
import Model.*;
import javafx.collections.FXCollections;
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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

/**
 * This class allows the user to edit an Appointment in the Appointment list.
 */
public class editAppointmentController implements Initializable {

    @FXML
    TextField appointmentIDTextBox;
    @FXML
    TextField titleTextBox;
    @FXML
    TextArea descriptionTextBox;
    @FXML
    TextField locationTextBox;
    @FXML
    ComboBox<String> contactComboBox;
    @FXML
    TextField typeTextBox;
    @FXML
    ComboBox<Integer> customerComboBox;
    @FXML
    ComboBox<Integer> userComboBox;
    @FXML
    DatePicker apptDatePicker;
    @FXML
    TextField startTimeTextBox;
    @FXML
    TextField endTimeTextBox;
    @FXML
    Button saveButton;
    @FXML
    Button clearButton;
    @FXML
    Button backButton;
    @FXML
    Label timeZoneLabel;


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
     * custerDataPass
     * takes passed object from previous stage and populates it in this stage
     *
     * @param selectedAppointment appt from previous stage
     * @throws SQLException
     */
    public void custerDataPass(Appointment selectedAppointment) throws SQLException {


        // get the values to populate into the Date picker
        try {
            selectedAppointment.getStartDateTime().toLocalDateTime().toLocalDate();
        }
        catch (NullPointerException error) {
            ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
            Alert invalidInput = new Alert(Alert.AlertType.WARNING, "No selected Date", clickOkay);
            invalidInput.showAndWait();
            return;
        }
        ZonedDateTime startDateTimeUTC = selectedAppointment.getStartDateTime().toInstant().atZone(ZoneOffset.UTC);
        ZonedDateTime endDateTimeUTC = selectedAppointment.getEndDateTime().toInstant().atZone(ZoneOffset.UTC);

        ZonedDateTime localStartDateTime = startDateTimeUTC.withZoneSameInstant(LogonSession.getUserTimeZone());
        ZonedDateTime localEndDateTime = endDateTimeUTC.withZoneSameInstant(LogonSession.getUserTimeZone());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String localStartString = localStartDateTime.format(formatter);
        String localEndString = localEndDateTime.format(formatter);

        // populate values
        appointmentIDTextBox.setText(selectedAppointment.getAppointmentID().toString());
        titleTextBox.setText(selectedAppointment.getTitle());
        descriptionTextBox.setText(selectedAppointment.getDescription());
        locationTextBox.setText(selectedAppointment.getLocation());
        contactComboBox.setItems(ContactDB.getAllContactName());
        contactComboBox.getSelectionModel().select(selectedAppointment.getContactName());
        typeTextBox.setText(selectedAppointment.getType());
        customerComboBox.setItems(CustomerDB.getAllCustomerID());
        customerComboBox.getSelectionModel().select(selectedAppointment.getCustomerID());
        userComboBox.setItems(UserDB.getAllUserID());
        userComboBox.getSelectionModel().select(selectedAppointment.getUserID());
        apptDatePicker.setValue(selectedAppointment.getStartDateTime().toLocalDateTime().toLocalDate());
        startTimeTextBox.setText(localStartString);
        endTimeTextBox.setText(localEndString);

        // Set date/ times - handle time zones

    }

    /**
     * validateBusinessHours
     * input validation
     *
     * @param startDateTime start date time
     * @param endDateTime end date time
     * @param apptDate appointment date
     * @return Boolean for successful operation
     */
    public Boolean validateBusinessHours(LocalDateTime startDateTime, LocalDateTime endDateTime, LocalDate apptDate) {
        // (8am to 10pm EST, Not including weekends)
        // Turn into zonedDateTimeObject, so we can evaluate whatever time was entered in user time zone against EST

        ZonedDateTime startZonedDateTime = ZonedDateTime.of(startDateTime, LogonSession.getUserTimeZone());
        ZonedDateTime endZonedDateTime = ZonedDateTime.of(endDateTime, LogonSession.getUserTimeZone());

        ZonedDateTime startBusinessHours = ZonedDateTime.of(apptDate, LocalTime.of(8,0),
                ZoneId.of("America/New_York"));
        ZonedDateTime endBusinessHours = ZonedDateTime.of(apptDate, LocalTime.of(22, 0),
                ZoneId.of("America/New_York"));

        // If startTime is before or after business hours
        // If end time is before or after business hours
        // if startTime is after endTime - these should cover all possible times entered and validate input.
        return !(startZonedDateTime.isBefore(startBusinessHours) | startZonedDateTime.isAfter(endBusinessHours) |
                endZonedDateTime.isBefore(startBusinessHours) | endZonedDateTime.isAfter(endBusinessHours) |
                startZonedDateTime.isAfter(endZonedDateTime));

    }

    /**
     * validateCustomerOverlap
     * input validation
     *
     * @param inputCustomerID Customer ID
     * @param startDateTime Start date time for appointment
     * @param endDateTime end date time for appointment
     * @param apptDate appointment date
     * @return Boolean indicating if input is valid
     * @throws SQLException
     */
    public Boolean validateCustomerOverlap(Integer inputCustomerID, LocalDateTime startDateTime,
                                           LocalDateTime endDateTime, LocalDate apptDate) throws SQLException {

        // Get list of appointments that might have conflicts
        ObservableList<Appointment> possibleConflicts = AppointmentDB.getCustomerFilteredAppointments(apptDate,
                inputCustomerID);
        // for each possible conflict, evaluate:
        // if conflictApptStart is before newApptstart and conflictApptEnd is after newApptStart(starts before ends after)
        // if conflictApptStart is before newApptEnd & conflictApptStart after newApptStart (startime anywhere in appt)
        // if endtime is before end and endtime is after start (endtime falls anywhere in appt)
        if (possibleConflicts.isEmpty()) {
            return true;
        }
        else {
            for (Appointment conflictAppt : possibleConflicts) {

                LocalDateTime conflictStart = conflictAppt.getStartDateTime().toLocalDateTime();
                LocalDateTime conflictEnd = conflictAppt.getEndDateTime().toLocalDateTime();

                // Conflict starts before and Conflict ends any time after new appt ends - overlap
                if( conflictStart.isBefore(startDateTime) & conflictEnd.isAfter(endDateTime)) {
                    return false;
                }
                // ConflictAppt start time falls anywhere in the new appt
                if (conflictStart.isBefore(endDateTime) & conflictStart.isAfter(startDateTime)) {
                    return false;
                }
                if (conflictStart.isEqual(startDateTime) || conflictEnd.isEqual(endDateTime)){
                    return false;
                }
                // ConflictAppt end time falls anywhere in the new appt
                return !(conflictEnd.isBefore(endDateTime) & conflictEnd.isAfter(startDateTime));

            }
        }
        return true;

    }

    /**
     * clearButtonActivity
     * clears fields on screen
     *
     * @param event Button Click
     */
    public void clearButtonActivity(ActionEvent event) {
        titleTextBox.clear();
        descriptionTextBox.clear();
        locationTextBox.clear();
        typeTextBox.clear();
        startTimeTextBox.clear();
        endTimeTextBox.clear();
        contactComboBox.getSelectionModel().clearSelection();
        customerComboBox.getSelectionModel().clearSelection();
        userComboBox.getSelectionModel().clearSelection();
        apptDatePicker.getEditor().clear();

    }

    /**
     * backButtonActivity
     * loads previous stage
     *
     * @param event Button Click
     * @throws IOException
     */
    public void backButtonActivity(ActionEvent event) throws IOException {
        screenChange(event, "/View/appointmentView.fxml");

    }

    /**
     * saveButtonActivity
     * saves appointment
     *
     * @param event Button Click
     * @throws SQLException
     * @throws IOException
     */
    public void saveButtonActivity(ActionEvent event) throws SQLException, IOException {

        boolean validStartDateTime;
        boolean validEndDateTime;
        Boolean validOverlap;
        Boolean validBusinessHours;
        String errorMessage = "";

        Integer apptID = Integer.parseInt(appointmentIDTextBox.getText());
        String title = titleTextBox.getText();
        String description = descriptionTextBox.getText();
        String location = locationTextBox.getText();
        String contactName = contactComboBox.getValue();
        String type = typeTextBox.getText();
        Integer customerID = customerComboBox.getValue();
        Integer userID = userComboBox.getValue();
        LocalDate apptDate = apptDatePicker.getValue();
        LocalDateTime endDateTime = null;
        LocalDateTime startDateTime = null;
        ZonedDateTime zonedEndDateTime;
        ZonedDateTime zonedStartDateTime;

        // take user selected Contact_Name and find the contact_ID FK so we can add to appointments table.
        Integer contactID = ContactDB.findContactID(contactName);


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");


        // INPUT VALIDATION: catch parsing errors for start and enddatetime
        try {
            startDateTime = LocalDateTime.of(apptDatePicker.getValue(),
                    LocalTime.parse(startTimeTextBox.getText(), formatter));
            validStartDateTime = true;
        }
        catch(DateTimeParseException error) {
            validStartDateTime = false;
            errorMessage += "Invalid Start time. Please ensure proper format HH:MM, including leading 0's.\n";
        }

        try {
            endDateTime = LocalDateTime.of(apptDatePicker.getValue(),
                    LocalTime.parse(endTimeTextBox.getText(), formatter));
            validEndDateTime = true;
        }
        catch(DateTimeParseException error) {
            validEndDateTime = false;
            errorMessage += "Invalid End time. Please ensure proper format HH:MM, including leading 0's.\n";
        }

        // INPUT VALIDATION: Ensure all fields have been entered
        if (title.isBlank() || description.isBlank() || location.isBlank() || contactName == null || type.isBlank() ||
                customerID == null || userID == null || apptDate == null || endDateTime == null ||
                startDateTime == null) {

            errorMessage += "Please ensure a value has been entered in all fields.\n";
            // Throw error
            ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
            Alert invalidInput = new Alert(Alert.AlertType.WARNING, errorMessage, clickOkay);
            invalidInput.showAndWait();
            return;

        }

        // INPUT VALIDATION: check that business hours are valid and there is no double booked customers.
        validBusinessHours = validateBusinessHours(startDateTime, endDateTime, apptDate);
        validOverlap = validateCustomerOverlap(customerID, startDateTime, endDateTime, apptDate);

        // INPUT VALIDATION: set corresponding error for user
        if (!validBusinessHours) {
            errorMessage += "Invalid Business Hours.(8am to 10pm EST)\n";
        }
        if (!validOverlap) {
            errorMessage += "Invalid Customer Overlap. Cannot double book customers.\n";
        }


        // INPUT VALIDATION - if any requirements are false, show error and end method.
        if (!validOverlap || !validBusinessHours || !validEndDateTime || !validStartDateTime) {
            ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
            Alert invalidInput = new Alert(Alert.AlertType.WARNING, errorMessage, clickOkay);
            invalidInput.showAndWait();

        }
        else {
            // if input is valid we insert into DB and display success and clear.
            // prep start and endTime by turning them into a zonedDateTime so we can enter in the DB in UTC.
            zonedStartDateTime = ZonedDateTime.of(startDateTime, LogonSession.getUserTimeZone());
            zonedEndDateTime = ZonedDateTime.of(endDateTime, LogonSession.getUserTimeZone());
            String loggedOnUserName = LogonSession.getCurrentUser().getUsername();

            // Convert to UTC
            zonedStartDateTime = zonedStartDateTime.withZoneSameInstant(ZoneOffset.UTC);
            zonedEndDateTime = zonedEndDateTime.withZoneSameInstant(ZoneOffset.UTC);

            // Add appt to DB
            Boolean success = AppointmentDB.updateAppointment(apptID, title, description, location, type, zonedStartDateTime,
                    zonedEndDateTime, loggedOnUserName, customerID, userID, contactID );

            // notify user we successfully added to DB, and navigate back.
            if (success) {
                ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                Alert invalidInput = new Alert(Alert.AlertType.CONFIRMATION, "Appointment updated successfully!", clickOkay);
                invalidInput.showAndWait();
                screenChange(event, "/View/appointmentView.fxml");
            }
            else {
                ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                Alert invalidInput = new Alert(Alert.AlertType.WARNING, "failed to Update appointment", clickOkay);
                invalidInput.showAndWait();
            }

        }

    }

    /**
     * initialize
     * initializes stage
     *
     * @param url stage path
     * @param resourceBundle resources
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        timeZoneLabel.setText(LogonSession.getUserTimeZone().toString());

    }
}
