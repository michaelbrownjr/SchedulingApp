package Controller;

import Model.*;
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
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * addAppointmentController
 * Creates a new Appointment from a new view that will be displayed
 * in the previous Appointment View.
 *
 */
public class addAppointmentController implements Initializable {
    @FXML
    TextField titleTextBox;
    @FXML
    TextArea descriptionTextBox;
    @FXML
    TextField locationTextBox;
    @ FXML
    ComboBox<String> contactComboBox;
    @ FXML
    TextField typeTextBox;
    @ FXML
    ComboBox<Integer> customerComboBox;
    @ FXML
    ComboBox<Integer> userComboBox;
    @ FXML
    Label timeZoneLabel;
    @ FXML
    DatePicker apptDatePicker;
    @ FXML
    TextField startTimeTextBox;
    @ FXML
    TextField endTimeTextBox;
    @ FXML
    Button saveButton;
    @ FXML
    Button clearButton;
    @ FXML
    Button backButton;

    /**
     * screenChange
     * Loads different stage
     *
     * @param event Button click
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
     * saveButtonActivity
     * handle pressing save button, validate input and add to DB
     *
     * @param event Button Click
     * @throws SQLException
     * @throws IOException
     */
    public void saveButtonActivity(ActionEvent event) throws SQLException, IOException {


        boolean validStartDateTime;
        boolean validEndDateTime;
        boolean validOverlap;
        boolean validBusinessHours;
        String errorMessage = "";


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


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");



        // INPUT VALIDATION: catch parsing errors for start and end datetime
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

        zonedStartDateTime = ZonedDateTime.of(startDateTime, LogonSession.getUserTimeZone());
        zonedEndDateTime = ZonedDateTime.of(endDateTime, LogonSession.getUserTimeZone());

        // Convert to UTC
        zonedStartDateTime = zonedStartDateTime.withZoneSameInstant(ZoneOffset.UTC);
        zonedEndDateTime = zonedEndDateTime.withZoneSameInstant(ZoneOffset.UTC);

        // INPUT VALIDATION: check that business hours are valid and there aren't any double booked customers.
        validBusinessHours = validateBusinessHours(startDateTime, endDateTime, apptDate);
        validOverlap = validateCustomerOverlap(customerID, zonedStartDateTime, zonedEndDateTime, apptDate);

        // INPUT VALIDATION: set corresponding error for user
        if (!validBusinessHours) {
            errorMessage += "Invalid Business Hours.(8am to 10pm EST)\n";
        }
        if (!validOverlap) {
            errorMessage += "Invalid Customer Overlap. Cannot double book customers.\n";
        }

        System.out.println(errorMessage);


        if (!validOverlap || !validBusinessHours || !validEndDateTime || !validStartDateTime) {
            ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
            Alert invalidInput = new Alert(Alert.AlertType.WARNING, errorMessage, clickOkay);
            invalidInput.showAndWait();

        }
        else {
            String loggedOnUserName = LogonSession.getCurrentUser().getUsername();



            Boolean success = AppointmentDB.addAppointment(title, description, location, type, zonedStartDateTime,
                    zonedEndDateTime, loggedOnUserName, loggedOnUserName, customerID, userID, contactID );


            if (success) {
                ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Appointment added successfully!", clickOkay);
                alert.showAndWait();
                screenChange(event, "/View/appointmentView.fxml");
            }
            else {
                ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                Alert alert = new Alert(Alert.AlertType.WARNING, "failed to add appointment", clickOkay);
                alert.showAndWait();
            }

        }

    }

    /**
     * clearButtonActivity
     * clears all values from the page
     */
    public void clearButtonActivity() {
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
     * Loads previous stage
     *
     * @param event button click
     * @throws IOException
     */
    public void backButtonActivity(ActionEvent event) throws IOException {
        screenChange(event, "/View/appointmentView.fxml");

    }

    /**
     * validateBusinessHours
     * Makes sure appointment is scheduled during business hours
     *
     * @param startDateTime start appointment datetime
     * @param endDateTime end appointment datetime
     * @param apptDate appointment date
     *
     * @return Boolean indicating valid input
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

        return !(startZonedDateTime.isBefore(startBusinessHours) | startZonedDateTime.isAfter(endBusinessHours) |
                endZonedDateTime.isBefore(startBusinessHours) | endZonedDateTime.isAfter(endBusinessHours) |
                startZonedDateTime.isAfter(endZonedDateTime));

    }

    /**
     * validateCustomerOverlap
     * ensures customer does not have overlapping appointments
     *
     * @param inputCustomerID customer ID of new appointment
     * @param zonedStartDateTime start dateime of appointment
     * @param zonedEndDateTime end datetime of appointment
     * @param apptDate date of appointment
     *
     * @return Boolean indicating valid input
     * @throws SQLException
     */
    public Boolean validateCustomerOverlap(Integer inputCustomerID, ZonedDateTime zonedStartDateTime,
                                           ZonedDateTime zonedEndDateTime, LocalDate apptDate) throws SQLException {

        ObservableList<Appointment> possibleConflicts = AppointmentDB.getCustomerFilteredAppointments(apptDate,
                inputCustomerID);

//        System.out.println("Started ZonedDateTime: " + zonedStartDateTime);
//        System.out.println("Ended ZonedDateTime: " + zonedEndDateTime);

        // Converts the passed startDateTime and endDateTime variables back to LocalDate Time
        // to compare them in the bottom condition statements
        LocalDateTime startDateTime = zonedStartDateTime.toLocalDateTime();
        LocalDateTime endDateTime = zonedEndDateTime.toLocalDateTime();

                
        
        if (possibleConflicts.isEmpty()) {
            return true;
        }
        else {
            for (Appointment conflictAppt : possibleConflicts) {
                LocalDateTime conflictStart = conflictAppt.getStartDateTime().toLocalDateTime();
                LocalDateTime conflictEnd = conflictAppt.getEndDateTime().toLocalDateTime();

                if (conflictStart.isBefore(startDateTime) & conflictEnd.isAfter(endDateTime)) {
                    return false;
                }

                if (conflictStart.isBefore(endDateTime) & conflictStart.isAfter(startDateTime)) {
                    return false;
                }
                if (conflictStart.isEqual(startDateTime) || conflictEnd.isEqual(endDateTime)){
                    return false;
                }

                return !(conflictEnd.isBefore(endDateTime) & conflictEnd.isAfter(startDateTime));

            }
        }
        return true;

    }

    /**
     * initialize the stage and items on screen
     *
     * Lambda expression - disables users from picking dates in past or weekend without needing a whole new method.
     * @param location Stage path
     * @param resources resources
     */
    public void initialize(URL location, ResourceBundle resources) {

        timeZoneLabel.setText("Your Time Zone is " + LogonSession.getUserTimeZone());

        //Lambda Expression - Disables users from picking dates in the past or weekend.

        apptDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate apptDatePicker, boolean empty) {
                super.updateItem(apptDatePicker, empty);
                setDisable(
                        empty || apptDatePicker.getDayOfWeek() == DayOfWeek.SATURDAY ||
                                apptDatePicker.getDayOfWeek() == DayOfWeek.SUNDAY || apptDatePicker.isBefore(LocalDate.now()));
            }
        });

        // Populate ComboBoxes
        try {
            customerComboBox.setItems(CustomerDB.getAllCustomerID());
            userComboBox.setItems(UserDB.getAllUserID());
            contactComboBox.setItems(ContactDB.getAllContactName());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }
}
