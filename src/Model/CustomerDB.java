package Model;

import Helper.Alerts;
import Helper.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CustomerDB {

    private static ObservableList<Customer> allCustomers = FXCollections.observableArrayList();
    private static String error;

//    public static Customer getCustomer(int id) {
//        try {
//            Statement statement = DBConnection.getConnection().createStatement();
//            String query = "SELECT * FROM customers WHERE customerId='" + id + "'";
//            ResultSet rs = statement.executeQuery(query);
//            if (rs.next()) {
//                Customer customer = new Customer();
//                customer.setCustomerName(rs.getString("customerName"));
//                statement.close();
//                return customer;
//            }
//        } catch (SQLException e) {
//            System.out.println("SQLException: " + e.getMessage());
//        }
//        return null;
//    }


    public static ObservableList<Customer> getAllCustomers() throws SQLException {
        // Prepare SQL and execute query
        ObservableList<Customer> allCustomers = FXCollections.observableArrayList();
        PreparedStatement sqlCommand = DBConnection.getConnection().prepareStatement(
                "SELECT cx.Customer_ID, cx.Customer_Name, cx.Address, cx.Postal_Code, cx.Phone, cx.Division_ID, " +
                        "f.Division, f.COUNTRY_ID, co.Country FROM customers as cx INNER JOIN first_level_divisions " +
                        "as f on cx.Division_ID = f.Division_ID INNER JOIN countries as co ON f.COUNTRY_ID = co.Country_ID");
        ResultSet results = sqlCommand.executeQuery();


        while( results.next() ) {
            // get data from the returned rows
            Integer custID = results.getInt("Customer_ID");
            String custName = results.getString("Customer_Name");
            String custAddress = results.getString("Address");
            String custPostalCode = results.getString("Postal_Code");
            String custPhoneNum = results.getString("Phone");
            String custDivision = results.getString("Division");
            Integer divID = results.getInt("Division_ID");
            String custCountry = results.getString("Country");

            // populate into an customer object
            Customer newCust = new Customer(custID, custName, custAddress, custPostalCode, custPhoneNum, custDivision,
                    divID, custCountry);


            // Add to the observablelist
            allCustomers.add(newCust);

        }
        sqlCommand.close();
        return allCustomers;

    }

    public static ObservableList<Integer> getAllCustomerID() throws SQLException {

        ObservableList<Integer> allCustomerID = FXCollections.observableArrayList();
        PreparedStatement sqlCommand = DBConnection.getConnection().prepareStatement("SELECT DISTINCT Customer_ID" +
                " FROM customers;");
        ResultSet results = sqlCommand.executeQuery();

        while ( results.next() ) {
            allCustomerID.add(results.getInt("Customer_ID"));
        }
        sqlCommand.close();
        return allCustomerID;
    }

    /**
     * getFilteredDivisions
     * takes the country and filters the matching first level divisions
     *
     * @param inputCountry input country to find the corresponding divisions
     * @return list of corresponding first level divisions
     * @throws SQLException
     */
    public static ObservableList<String> getFilteredDivisions(String inputCountry) throws SQLException {

        ObservableList<String> filteredDivs = FXCollections.observableArrayList();
        PreparedStatement sqlCommand = DBConnection.getConnection().prepareStatement(
                "SELECT c.Country, c.Country_ID,  d.Division_ID, d.Division FROM countries as c RIGHT OUTER JOIN " +
                        "first_level_divisions AS d ON c.Country_ID = d.Country_ID WHERE c.Country = ?");

        sqlCommand.setString(1, inputCountry);
        ResultSet results = sqlCommand.executeQuery();

        while (results.next()) {
            filteredDivs.add(results.getString("Division"));
        }

        sqlCommand.close();
        return filteredDivs;

    }

    /**
     * getAllCountries
     * queries DB to get all countries
     *
     * @return List of countries
     * @throws SQLException
     */
    public static ObservableList<String> getAllCountries() throws SQLException {

        ObservableList<String> allCountries = FXCollections.observableArrayList();
        PreparedStatement sqlCommand = DBConnection.getConnection().prepareStatement("SELECT DISTINCT Country FROM countries");
        ResultSet results = sqlCommand.executeQuery();

        while (results.next()) {
            allCountries.add(results.getString("Country"));
        }
        sqlCommand.close();
        return allCountries;

    }



    /**
     * addCustomer
     * Add customer to the Database
     *
     * @param country customer Country
     * @param division customer Division
     * @param name customer name
     * @param address customer address
     * @param postalCode customer postal code
     * @param phoneNum customer phone number
     * @param divisionID customer division ID
     * @return Boolean to indicate successful operation
     * @throws SQLException
     */
    public static Boolean addCustomer(String country, String division, String name, String address, String postalCode,
                                      String phoneNum, Integer divisionID) throws SQLException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        PreparedStatement sqlCommand = DBConnection.getConnection().prepareStatement(
                "INSERT INTO customers (Customer_Name, Address, Postal_Code, Phone, Create_Date, Created_By, " +
                        "Last_Update, Last_Updated_By, Division_ID) \n" +
                        "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);");

        sqlCommand.setString(1, name);
        sqlCommand.setString(2, address);
        sqlCommand.setString(3, postalCode);
        sqlCommand.setString(4, phoneNum);
        sqlCommand.setString(5, ZonedDateTime.now(ZoneOffset.UTC).format(formatter).toString());
        sqlCommand.setString(6, LogonSession.getCurrentUser().getUsername());
        sqlCommand.setString(7, ZonedDateTime.now(ZoneOffset.UTC).format(formatter).toString());
        sqlCommand.setString(8, LogonSession.getCurrentUser().getUsername());
        sqlCommand.setInt(9, divisionID);

        // Execute query
        try {
            sqlCommand.executeUpdate();
            sqlCommand.close();
            return true;
        }
        catch (SQLException e) {
            //TODO- log error
            e.printStackTrace();
            sqlCommand.close();
            return false;
        }

    }


    /**
     * updateCustomer
     * takes input values and updates it in the DB
     *
     * @param division customer division
     * @param name customer name
     * @param address customer address
     * @param postalCode customer postal code
     * @param phoneNum customer phone number
     * @param customerID customer ID
     * @return Boolean indicating operation successful
     * @throws SQLException
     */
    public static Boolean updateCustomer( String division, String name, String address,
                                          String postalCode, String phoneNum, Integer customerID) throws SQLException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        PreparedStatement sqlCommand = DBConnection.getConnection().prepareStatement("UPDATE customers "
                + "SET Customer_Name=?, Address=?, Postal_Code=?, Phone=?, Last_Update=?," +
                " Last_Updated_By=?, Division_ID=? WHERE Customer_ID = ?");

        sqlCommand.setString(1, name);
        sqlCommand.setString(2, address);
        sqlCommand.setString(3, postalCode);
        sqlCommand.setString(4, phoneNum);
        sqlCommand.setString(5, ZonedDateTime.now(ZoneOffset.UTC).format(formatter).toString());
        sqlCommand.setString(6, LogonSession.getCurrentUser().getUsername());
        sqlCommand.setInt(7, CustomerDB.getSpecificDivisionID(division));
        sqlCommand.setInt(8, customerID);

        // Execute query
        try {
            sqlCommand.executeUpdate();
            sqlCommand.close();
            return true;
        }
        catch (SQLException e) {
            //TODO- log error
            e.printStackTrace();
            sqlCommand.close();
            return false;
        }

    }
    /**
     * getSpecificDivisionID
     * takes a division string and finds the corresponding ID
     *
     * @param division division to find ID for
     * @return ID of the division
     * @throws SQLException
     */
    public static Integer getSpecificDivisionID(String division) throws SQLException {
        Integer divID = 0;
        PreparedStatement sqlCommand = DBConnection.getConnection().prepareStatement("SELECT Division, Division_ID FROM " +
                "first_level_divisions WHERE Division = ?");

        sqlCommand.setString(1, division);

        ResultSet result = sqlCommand.executeQuery();

        while ( result.next() ) {
            divID = result.getInt("Division_ID");
        }

        sqlCommand.close();
        return divID;

    }

    public static Integer assignAddressID() {
        Integer addressID = 1;
        try {
            Statement stmt = DBConnection.getConnection().createStatement();
            String query = "SELECT addressId FROM `U05TGe`.`address` ORDER BY addressId;";
            ResultSet rs = stmt.executeQuery(query);


            while(rs.next()) {
                if (rs.getInt("addressId") == addressID) {
                    addressID++;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return addressID;
    }

    public static Integer assignCustomerID(){
        Integer customerID = 1;
        try {
            Statement stmt = DBConnection.getConnection().createStatement();
            String query = "SELECT customerId FROM `U05TGe`.`customer` ORDER BY customerId;";
            ResultSet rs = stmt.executeQuery(query);


            while(rs.next()) {
                if (rs.getInt("customerId") == customerID) {
                    customerID++;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return customerID;
    }

    public static Boolean deleteCustomer(Integer customerID) throws SQLException{
        PreparedStatement sqlCommand = DBConnection.getConnection().prepareStatement("DELETE FROM customers " +
                "WHERE Customer_ID = ?");

        sqlCommand.setInt(1, customerID);

        try {
            sqlCommand.executeUpdate();
            sqlCommand.close();
            return true;
        }
        catch (SQLException e) {
            //TODO- log error
            e.printStackTrace();
            return false;
        }
    }

    public static Boolean validateCustomer(String name, String phone, String street, String city, String zip){
        error = "";
        if(!validName(name) || !validPhone(phone) || !validStreet(street) || !validCity(city) || !validZip(zip)){
            System.out.println(error);
            Alerts.infoDialog("Error", "Please fix the following error:", error);
            return Boolean.FALSE;
        }
        else {
            return Boolean.TRUE;
        }
    }

    private static boolean validZip(String zip) {
        if(zip.isEmpty()) {
            error = "Please enter a Zip";
            return Boolean.FALSE;
        }
        else{
            return Boolean.TRUE;
        }
    }

    private static boolean validCity(String city) {
        if(city.isEmpty()) {
            error = "Please enter a City";
            return Boolean.FALSE;
        }
        else{
            return Boolean.TRUE;
        }
    }

    private static boolean validStreet(String street) {
        if(street.isEmpty()) {
            error = "Please enter Street";
            return Boolean.FALSE;
        }
        else{
            return Boolean.TRUE;
        }
    }

    private static boolean validPhone(String phone) {
        if(phone.isEmpty()) {
            error = "Please enter Phone Number";
            return Boolean.FALSE;
        }
        else{
            return Boolean.TRUE;
        }
    }

    private static boolean validName(String name) {
        if(name.isEmpty()) {
            error = "Please enter Customer Name";
            return Boolean.FALSE;
        }
        else{
            return Boolean.TRUE;
        }
    }
}
