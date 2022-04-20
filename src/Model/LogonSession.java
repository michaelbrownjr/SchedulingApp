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

    // Login Attempt
    public static Boolean login(String Username, String Password) throws SQLException{
            Statement sqlQuery = DBConnection.getConnection().createStatement();
            String loginCheck = "SELECT * FROM user WHERE User_Name='" + Username + "' AND Password='" + Password + "'";
            ResultSet rs = sqlQuery.executeQuery(loginCheck);
            if (rs.next()) {
                currentUser = new User(rs.getString("User_Name"), rs.getInt("User_ID"));
                currentUser.setUsername(rs.getString("User_Name"));
                userLocale = Locale.getDefault();
                userTimeZone = ZoneId.systemDefault();
                sqlQuery.close();
                return true;

            } else {
                return false;
            }
    }
    public static Locale getUserLocale() {
        return userLocale;

    }
    public static ZoneId getUserTimeZone() {
        return userTimeZone;
    }
    public static void logOff() {
        currentUser = null;
        userLocale = null;
        userTimeZone = null;
    }
}
