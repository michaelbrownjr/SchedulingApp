package Controller;

import Model.CustomerDB;
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
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * addCustomerControll
 * This class allows the user to add a customer in the customer View.
 */
public class addCustomerController implements Initializable {
    @FXML
    TextField customerIDTextBox;
    @FXML
    ComboBox<String> countryComboBox;
    @FXML
    ComboBox<String> divisionComboBox;
    @FXML
    TextField customerNameTextBox;
    @FXML
    TextField addressTextBox;
    @FXML
    TextField postalCodeTextBox;
    @FXML
    TextField phoneNumberTextBox;
    @FXML
    Button saveButton;
    @FXML
    Button clearButton;
    @FXML
    Button backButton;

    /**
     * screenChange
     * Loads new view
     *
     * @param event button click
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
     * validates input and saves to DB
     *
     * @param event Button click
     * @throws SQLException
     * @throws IOException
     */
    public void saveButtonActivity(ActionEvent event) throws SQLException, IOException {
        // INPUT VALIDATION - check for nulls
        String country = countryComboBox.getValue();
        String division = divisionComboBox.getValue();
        String name = customerNameTextBox.getText();
        String address = addressTextBox.getText();
        String postalCode = postalCodeTextBox.getText();
        String phone = phoneNumberTextBox.getText();

        if (country.isBlank() || division.isBlank() || name.isBlank() || address.isBlank() || postalCode.isBlank() ||
                phone.isBlank()) {

            ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
            Alert emptyVal = new Alert(Alert.AlertType.WARNING, "Please ensure all fields are completed.",
                    clickOkay);
            emptyVal.showAndWait();
            return;

        }

        // Add customer to DB
        Boolean success = CustomerDB.addCustomer(country, division, name, address, postalCode, phone,
                CustomerDB.getSpecificDivisionID(division));

        // notify user we successfully added to DB, or if there was an error.
        if (success) {
            ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Customer added successfully!", clickOkay);
            alert.showAndWait();
            clearButtonActivity(event);
            screenChange(event, "/View/customerView.fxml");
        }
        else {
            ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
            Alert alert = new Alert(Alert.AlertType.WARNING, "Failed to add Customer", clickOkay);
            alert.showAndWait();
        }

    }

    /**
     * clearButtonActivity
     * clears fields on page
     *
     * @param event Button Click
     */
    public void clearButtonActivity(ActionEvent event) {
        countryComboBox.getItems().clear();
        divisionComboBox.getItems().clear();
        customerNameTextBox.clear();
        addressTextBox.clear();
        postalCodeTextBox.clear();
        phoneNumberTextBox.clear();

    }

    /**
     * backButtonActivity
     * goes back to previous stage
     *
     * @param event Button Click
     * @throws IOException
     */
    public void backButtonActivity(ActionEvent event) throws IOException {
        screenChange(event, "/View/customerView.fxml");

    }


    /**
     * initialize
     * Loads page and sets items on it
     * Lambda expression - creates a listener for changes in a combo box
     *
     * @param url path of stage
     * @param resourceBundle resources
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            countryComboBox.setItems(CustomerDB.getAllCountries());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        //Lambda Expression - Listener for combo box change
        countryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterControlCase(newVal);
        });

    }

    /**
     * filterControlCase filters division on Customers
     * @param newVal
     */
    private void filterControlCase(String newVal) {
        if (newVal == null) {
            divisionComboBox.getItems().clear();
            divisionComboBox.setDisable(true);

        }
        else {
            divisionComboBox.setDisable(false);
            try {
                divisionComboBox.setItems(CustomerDB.getFilteredDivisions(countryComboBox.getValue()));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }
    }
}
