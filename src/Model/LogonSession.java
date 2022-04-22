package Model;

import Helper.DBConnection;

import java.sql.*;
import java.time.ZoneId;
import java.util.Locale;

public class LogonSession {

    private static User currentUser;
    private static Locale userLocale;
    private static ZoneId userTimeZone;


    public static User getCurrentUser() {
        return currentUser;
    }
    public static ZoneId getUserTimeZone() {
        return userTimeZone;
    }

    // Login Attempt
    public static Boolean login(String Username, String Password) throws SQLException{
            Statement sqlQuery = DBConnection.getConnection().createStatement();
            String loginCheck = "SELECT * FROM users WHERE User_Name='" + Username + "' AND Password='" + Password + "'";
            ResultSet set = sqlQuery.executeQuery(loginCheck);
            if (set.next()) {
                currentUser = new User(set.getString("User_Name"), set.getInt("User_ID"));
                currentUser.setUserName(set.getString("User_Name"));
                userLocale = Locale.getDefault();
                userTimeZone = ZoneId.systemDefault();
                sqlQuery.close();
                return true;

            } else {
                return false;
            }
    }


    public static void exit() {
        currentUser = null;
        userLocale = null;
        userTimeZone = null;
    }
}
