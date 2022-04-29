package Controller;

import Helper.DBConnection;
import Model.Appointment;
import Model.AppointmentDB;
import Model.LogonSession;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * This class populates the view with all of the appointments on the schedule
 * with buttons to edit, delete, and appointments. Also has the following buttons: Reports,
 * Customers, and LogOut.
 */
public class appointmentController implements Initializable {

    @FXML
    Button newAppointmentButton;
    @FXML
    Button editAppointmentButton;
    @FXML
    Button deleteButton;
    @FXML
    Button customersViewButton;
    @FXML
    Button reportsButton;
    @FXML
    Button logOutButton;
    @FXML
    Button nextItemButton;
    @FXML
    Button previousItemButton;
    @FXML
    RadioButton monthFilterRadio;
    @FXML
    RadioButton weekFilterRadio;
    @FXML
    RadioButton emptyFilterRadio;
    @FXML
    TableView<Appointment> appointmentTable;
    @FXML
    TableColumn<Appointment, Integer> appointmentIDColumn;
    @FXML
    TableColumn<Appointment, String> appointmentTitleColumn;
    @FXML
    TableColumn<Appointment, String> appointmentDescriptionColumn;
    @FXML
    TableColumn<Appointment, String> appointmentLocationColumn;
    @FXML
    TableColumn<Appointment, String> appointmentContactColumn;
    @FXML
    TableColumn<Appointment, String> appointmentTypeColumn;
    @FXML
    TableColumn<Appointment, Integer> customerIdColumn;
    @FXML
    TableColumn<Appointment, Integer> userIdColumn;
    @FXML
    TableColumn<Appointment, ZonedDateTime> startDateTimeColumn;
    @FXML
    TableColumn<Appointment, ZonedDateTime> endDateTimeColumn;

    @FXML
    ToggleGroup filterToggle;
    @FXML
    Label selectedTimeLabel;

    // Markers for date filtering.
    ZonedDateTime startRangeMarker;
    ZonedDateTime endRangeMarker;

    /**
     * screenChange
     * loads new stage
     *
     * @param event button click
     * @param switchPath path of new stage
     */
    public void screenChange(ActionEvent event, String switchPath) throws IOException {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(switchPath)));
        Scene scene = new Scene(parent);
        Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    /**
     * toggleRadioButtons
     * creates new toggle group preventing multiple selections
     */
    public void toggleRadioButtons() {

        filterToggle = new ToggleGroup();

        for (RadioButton radioButton : Arrays.asList(emptyFilterRadio, weekFilterRadio, monthFilterRadio)) {
            radioButton.setToggleGroup(filterToggle);
        }

    }

    /**
     * emptyFilterRadioActivity
     * loads all appointments on page
     *
     */
    public void emptyFilterRadioActivity() {
        // only one selection at a time!
        for (RadioButton radioButton : Arrays.asList(monthFilterRadio, weekFilterRadio)) {
            radioButton.setSelected(false);
        }

        ObservableList<Appointment> allAppointments;
        try {
            allAppointments = AppointmentDB.getAllAppointments();
        }
        catch (SQLException error){
            error.printStackTrace();
            DBConnection.openConnection();
            try {
                allAppointments = AppointmentDB.getAllAppointments();
            } catch (SQLException anotherError) {
                anotherError.printStackTrace();
                ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                Alert invalidInput = new Alert(Alert.AlertType.WARNING, "DB connection failed. please restart", clickOkay);
                invalidInput.showAndWait();
                return;
            }

        }
        populateAppointments(allAppointments);
        selectedTimeLabel.setText("All Appointments");
        startRangeMarker = null;


    }

    /**
     * weekFilterRadioActivity
     * filters appts by week
     *
     * @throws SQLException
     */
    public void weekFilterRadioActivity() throws SQLException {
        // Only one selection at a time!
        for (RadioButton radioButton : Arrays.asList(monthFilterRadio, emptyFilterRadio)) {
            radioButton.setSelected(false);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        ObservableList<Appointment> filteredAppts;
        startRangeMarker = ZonedDateTime.now(LogonSession.getUserTimeZone());
        endRangeMarker = startRangeMarker.plusWeeks(1);

        // Convert to UTC
        ZonedDateTime startRange = startRangeMarker.withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime endRange = endRangeMarker.withZoneSameInstant(ZoneOffset.UTC);

        // query DB for time frame
        filteredAppts = AppointmentDB.getDateFilteredAppointments(startRange, endRange);
        // populate
        populateAppointments(filteredAppts);
        // update label
        selectedTimeLabel.setText(startRangeMarker.format(formatter) + " - " + endRangeMarker.format(formatter) + " " +
                LogonSession.getUserTimeZone());
        // update filterRangeMarker to next week.




    }

    /**
     * monthFilterRadioActivity
     * filters appts by month
     *
     * @throws SQLException
     */
    public void monthFilterRadioActivity() throws SQLException {
        for (RadioButton radioButton : Arrays.asList(weekFilterRadio, emptyFilterRadio)) {
            radioButton.setSelected(false);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        ObservableList<Appointment> filteredAppts;
        startRangeMarker = ZonedDateTime.now(LogonSession.getUserTimeZone());
        endRangeMarker = startRangeMarker.plusMonths(1);

        // Convert to UTC
        ZonedDateTime startRange = startRangeMarker.withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime endRange = endRangeMarker.withZoneSameInstant(ZoneOffset.UTC);

        // query DB for time frame
        filteredAppts = AppointmentDB.getDateFilteredAppointments(startRange, endRange);
        // populate
        populateAppointments(filteredAppts);
        // update label
        selectedTimeLabel.setText(startRangeMarker.format(formatter) + " - " + endRangeMarker.format(formatter) + " " +
                LogonSession.getUserTimeZone());
    }

    /**
     * nextButtonActivity
     * Moves selected Appointment time to next on the list
     *
     * @throws SQLException
     */
    public void nextButtonActivity() throws SQLException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        ObservableList<Appointment> filteredAppts;

        if (filterToggle.getSelectedToggle() == weekFilterRadio) {

            ZonedDateTime startRange = startRangeMarker.plusWeeks(1);
            ZonedDateTime endRange = endRangeMarker.plusWeeks(1);

            // update markers
            startRangeMarker = startRange;
            endRangeMarker = endRange;

            // convert to UTC for the DB
            startRange = startRange.withZoneSameInstant(ZoneOffset.UTC);
            endRange = endRange.withZoneSameInstant(ZoneOffset.UTC);

            filteredAppts = AppointmentDB.getDateFilteredAppointments(startRange, endRange);

            populateAppointments(filteredAppts);

            // update label
            selectedTimeLabel.setText(startRangeMarker.format(formatter) + " - " + endRangeMarker.format(formatter) + " " +
                    LogonSession.getUserTimeZone());

        }
        if (filterToggle.getSelectedToggle() == monthFilterRadio) {

            ZonedDateTime startRange = startRangeMarker.plusMonths(1);
            ZonedDateTime endRange = endRangeMarker.plusMonths(1);

            // update markers
            startRangeMarker = startRange;
            endRangeMarker = endRange;

            // convert to UTC for the DB
            startRange = startRange.withZoneSameInstant(ZoneOffset.UTC);
            endRange = endRange.withZoneSameInstant(ZoneOffset.UTC);

            filteredAppts = AppointmentDB.getDateFilteredAppointments(startRange, endRange);

            populateAppointments(filteredAppts);

            // update label
            selectedTimeLabel.setText(startRangeMarker.format(formatter) + " - " + endRangeMarker.format(formatter) + " " +
                    LogonSession.getUserTimeZone());

        }

    }

    /**
     * backButtonActivity
     * Moves selected Appointment time to the previous time on the list
     *
     * @throws SQLException
     */
    public void backButtonActivity() throws SQLException {


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        ObservableList<Appointment> filteredAppts;

        if (filterToggle.getSelectedToggle() == weekFilterRadio) {

            ZonedDateTime startRange;
            ZonedDateTime endRange;
            startRange = startRangeMarker.minusWeeks(1);

            endRange = endRangeMarker.minusWeeks(1);

            // update markers
            startRangeMarker = startRange;
            endRangeMarker = endRange;

            // convert to UTC for the DB
            startRange = startRange.withZoneSameInstant(ZoneOffset.UTC);
            endRange = endRange.withZoneSameInstant(ZoneOffset.UTC);

            filteredAppts = AppointmentDB.getDateFilteredAppointments(startRange, endRange);

            populateAppointments(filteredAppts);

            // update label
            selectedTimeLabel.setText(startRangeMarker.format(formatter) + " - " + endRangeMarker.format(formatter) + " " +
                    LogonSession.getUserTimeZone());

        }
        if (filterToggle.getSelectedToggle() == monthFilterRadio) {

            ZonedDateTime startRange;
            ZonedDateTime endRange;

            startRange = startRangeMarker.minusMonths(1);
            endRange = endRangeMarker.minusMonths(1);

            // update markers
            startRangeMarker = startRange;
            endRangeMarker = endRange;

            // convert to UTC for the DB
            startRange = startRange.withZoneSameInstant(ZoneOffset.UTC);
            endRange = endRange.withZoneSameInstant(ZoneOffset.UTC);

            filteredAppts = AppointmentDB.getDateFilteredAppointments(startRange, endRange);

            populateAppointments(filteredAppts);

            // update label
            selectedTimeLabel.setText(startRangeMarker.format(formatter) + " - " + endRangeMarker.format(formatter) + " " +
                    LogonSession.getUserTimeZone());
        }

    }

    /**
     * deleteButtonActivity
     * Deletes selected appointments from database and rebuilds the Appointments TableView
     *
     * @throws SQLException
     */
    public void deleteButtonActivity() throws SQLException {

        Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();

        if (selectedAppointment == null) {
            ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
            Alert invalidInput = new Alert(Alert.AlertType.WARNING, "No selected Appointment", clickOkay);
            invalidInput.showAndWait();
        }
        else {
            // Prompt alert for deletion confirmation
            ButtonType clickYes = ButtonType.YES;
            ButtonType clickNo = ButtonType.NO;
            Alert deleteAlert = new Alert(Alert.AlertType.WARNING, "Are you sure you want to delete Appointment ID: "
                    + selectedAppointment.getAppointmentID() + " with Type: \"" + selectedAppointment.getType() + "\"?", clickYes, clickNo);
            Optional<ButtonType> result = deleteAlert.showAndWait();

            String test = "\"Hello World!\"";

            // if confirmed, delete selected appointment
            if (result.get() == ButtonType.YES) {
                Boolean success = AppointmentDB.deleteAppointment(selectedAppointment.getAppointmentID());

                // if successful, else alert error.
                if (success) {
                    ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                    Alert deletedAppt = new Alert(Alert.AlertType.CONFIRMATION, "Appointment deleted", clickOkay);
                    deletedAppt.showAndWait();

                }
                else {
                    //TODO - log error if it occurs
                    ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                    Alert deleteAppointment = new Alert(Alert.AlertType.WARNING, "Failed to delete Appointment", clickOkay);
                    deleteAppointment.showAndWait();

                }

                // Re-load appointments on screen
                try {
                    populateAppointments(AppointmentDB.getAllAppointments());
                }
                catch (SQLException error){
                    //TODO - log error
                    error.printStackTrace();
                }

            }
        }
    }

    /**
     * newButtonActivity
     * loads stage to add appointment
     *
     * @param event Button Click
     * @throws IOException
     */
    public void newButtonActivity(ActionEvent event) throws IOException {
        screenChange(event, "/View/addAppointmentView.fxml");
    }

    /**
     * logoutButtonActivity
     * logs user out
     *
     * @param event Button Click
     * @throws IOException
     */
    public void logoutButtonActivity(ActionEvent event) throws IOException {
        ButtonType clickYes = ButtonType.YES;
        ButtonType clickNo = ButtonType.NO;
        Alert exit = new Alert(Alert.AlertType.WARNING, "Are you sure you want to Log Off?", clickYes, clickNo);

        exit.showAndWait().ifPresent((response -> {
            if (response == clickYes) {
                LogonSession.exit();
                try {
                    screenChange(event, "/View/loginView.fxml");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    /**
     * editButtonActivity
     * passes object to next stage and loads stage
     *
     * @param event Button Click
     * @throws IOException
     * @throws SQLException
     */
    public void editButtonActivity(ActionEvent event) throws IOException, SQLException {

        Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
        // throw error if no selection
        if (selectedAppointment == null) {
            ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
            Alert invalidInput = new Alert(Alert.AlertType.WARNING, "No selected Appointment", clickOkay);
            invalidInput.showAndWait();
            return;

        }

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/View/editAppointmentView.fxml"));
        Parent parent = loader.load();
        Scene scene = new Scene(parent);
        // get the controller and load our selected appointment into it
        editAppointmentController controller = loader.getController();
        controller.custerDataPass(selectedAppointment);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);

    }

    /**
     * customerButtonActivity
     * loads customer stage
     *
     * @param event Button Click
     * @throws IOException
     */
    public void customerButtonActivity(ActionEvent event) throws IOException {

        screenChange(event, "/View/customerView.fxml");

    }

    /**
     * reportsButtonActivity
     * loads reports page
     *
     * @param event Button Click
     * @throws IOException
     */
    public void reportsButtonActivity(ActionEvent event) throws IOException {
        screenChange(event, "/View/reportsView.fxml");

    }

    /**
     * loads appointments on page
     *
     * @param inputList list of appointments
     */
    public void populateAppointments(ObservableList<Appointment> inputList) {
        // Takes an observable list of appointments and populates them on screen.

        appointmentIDColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentID"));
        appointmentTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        appointmentDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        appointmentLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        appointmentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        appointmentContactColumn.setCellValueFactory(new PropertyValueFactory<>("contactName"));
        startDateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startDateTime"));
        endDateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endDateTime"));
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userID"));
        appointmentTable.setItems(inputList);

    }

    /**
     * checkCanceled
     * checks to see if any appointments have type cancelled
     *
     * @param inputList list of all appointments
     */
    public void checkCanceled(ObservableList<Appointment> inputList) {

        inputList.forEach((appt) -> {
            if (appt.getType().equalsIgnoreCase("canceled")) {
                ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                Alert invalidInput = new Alert(Alert.AlertType.WARNING, "Appointment " + appt.getAppointmentID() +
                        " is canceled.", clickOkay);
                invalidInput.showAndWait();
            }
        });

    }


    /**
     * initialize
     * Initializes stage and loads objects on screen
     *
     * @param location location / time zone
     * @param resources resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources)   {


        emptyFilterRadio.setSelected(true);
        toggleRadioButtons();

        // populate table view, handle DB connection breakage by retry.
        ObservableList<Appointment> allAppointments;
        try {
            allAppointments = AppointmentDB.getAllAppointments();
        }
        catch (SQLException error){
            // Sometimes the connection to DB breaks here.(not sure why) If it does, re-connnect and try again.
            error.printStackTrace();
            DBConnection.openConnection();
            try {
                allAppointments = AppointmentDB.getAllAppointments();
            } catch (SQLException anotherError) {
                anotherError.printStackTrace();
                ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                Alert invalidInput = new Alert(Alert.AlertType.WARNING, "DB connection failed. please restart", clickOkay);
                invalidInput.showAndWait();
                return;
            }

        }
        populateAppointments(allAppointments);
        checkCanceled(allAppointments);


    }
}
