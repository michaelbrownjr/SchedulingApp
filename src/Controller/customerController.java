package Controller;

import Model.AppointmentDB;
import Model.Customer;
import Model.CustomerDB;
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
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class customerController implements Initializable {
    @FXML
    Button addButton;
    @FXML
    Button editButton;
    @FXML
    Button deleteButton;
    @FXML
    Button backButton;
    @FXML
    TableView<Customer> customerTable;
    @FXML
    TableColumn<Customer, Integer> customerIDColumn;
    @FXML
    TableColumn<Customer, String> customerNameColumn;
    @FXML
    TableColumn<Customer, String> countryColumn;
    @FXML
    TableColumn<Customer, String> divisionColumn;
    @FXML
    TableColumn<Customer, String> addressColumn;
    @FXML
    TableColumn<Customer, String> postalCodeColumn;
    @FXML
    TableColumn<Customer, String> phoneNumberColumn;

    /**
     * switchScreen
     * loads next stage
     *
     * @param event Button Click
     * @param switchPath Path to next stage
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
     * populateCustomers
     * populates customers on screen
     *
     * @param inputList list of customers
     */
    public void populateCustomers(ObservableList<Customer> inputList) {
        customerIDColumn.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
        divisionColumn.setCellValueFactory(new PropertyValueFactory<>("division"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        postalCodeColumn.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        customerTable.setItems(inputList);

    }

    /**
     * pressAddButton
     * switches screen to add customer
     *
     * @param event Button Click
     * @throws IOException
     */
    public void pressAddButton(ActionEvent event) throws IOException {
        switchScreen(event, "/View/addCustomerView.fxml");

    }

    /**
     * editButtonActivity
     * passes object and loads next stage
     *
     * @param event Button Click
     * @throws IOException
     * @throws SQLException
     */
    public void editButtonActivity(ActionEvent event) throws IOException, SQLException {

        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        // throw error if no selection
        if (selectedCustomer == null) {
            ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
            Alert alert = new Alert(Alert.AlertType.WARNING, "No selected Customer", clickOkay);
            alert.showAndWait();
            return;

        }

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/View/editCustomerView.fxml"));
        Parent parent = loader.load();
        Scene scene = new Scene(parent);
        // get the controller and load our selected appointment into it
        editCustomerController controller = loader.getController();
        controller.initData(selectedCustomer);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(scene);

    }

    /**
     * deleteButtonActivity
     * deletes selected object from DB
     *
     * @throws SQLException
     */
    public void deleteButtonActivity() throws SQLException {

        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();

        // check that user selected an appointment in the table
        if (selectedCustomer == null) {
            ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
            Alert alert = new Alert(Alert.AlertType.WARNING, "No selected Customer", clickOkay);
            alert.showAndWait();
        }
        else {
            // show alert and ensure user wants to delete
            ButtonType clickYes = ButtonType.YES;
            ButtonType clickNo = ButtonType.NO;
            Alert deleteAlert = new Alert(Alert.AlertType.WARNING, "Are you sure you want to delete Customer: "
                    + selectedCustomer.getCustomerID() + " and all related appointments?", clickYes, clickNo);
            Optional<ButtonType> result = deleteAlert.showAndWait();

            // if user confirms, delete appointment + related appointments due to FK contraints
            if (result.get() == ButtonType.YES) {
                Boolean customerApptSuccess = AppointmentDB.deleteCustomersAppointments(selectedCustomer.getCustomerID());

                Boolean customerSuccess = CustomerDB.deleteCustomer(selectedCustomer.getCustomerID());


                // if successful prompt success, if not alert user error.
                if (customerSuccess && customerApptSuccess) {
                    ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                    Alert deletedCustomer = new Alert(Alert.AlertType.CONFIRMATION,
                            "Customer + related appointments deleted", clickOkay);
                    deletedCustomer.showAndWait();

                }
                else {

                    ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                    Alert deleteAppointment = new Alert(Alert.AlertType.WARNING,
                            "Failed to delete Customer or related appointments ", clickOkay);
                    deleteAppointment.showAndWait();

                }

                // Re-load appointments on view
                try {
                    populateCustomers(CustomerDB.getAllCustomers());
                }
                catch (SQLException error){
                    error.printStackTrace();
                }

            }
        }
    }

    /**
     * backButtonActivity
     * loads previous stage
     *
     * @param event ButtonClick
     * @throws IOException
     */
    public void backButtonActivity(ActionEvent event) throws IOException {

        switchScreen(event, "/View/appointmentView.fxml");

    }

    /**
     * initialize
     * initializes stage and objects on it
     *
     * @param location user location / time zone
     * @param resources resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            populateCustomers(CustomerDB.getAllCustomers());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}
