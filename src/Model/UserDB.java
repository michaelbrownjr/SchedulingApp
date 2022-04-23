package Model;

import Helper.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;


/**
 * This class gets all the users from the DB
 */
public class UserDB {
    /**
     * getAllUserID
     * get a list of all user ID's from the DB
     *
     * @return List of all user ID's
     * @throws SQLException
     */
    public static ObservableList<Integer> getAllUserID() throws SQLException {
        ObservableList<Integer> allUserID = FXCollections.observableArrayList();
        PreparedStatement sqlCommand = DBConnection.getConnection().prepareStatement("SELECT DISTINCT User_ID" +
                " FROM users;");
        ResultSet results = sqlCommand.executeQuery();

        while ( results.next() ) {
            allUserID.add(results.getInt("User_ID"));
        }
        sqlCommand.close();
        return allUserID;
    }
}
