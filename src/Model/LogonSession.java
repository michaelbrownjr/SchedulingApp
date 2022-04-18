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
            Connection connect = DBConnection.getConnection();
            String loginCheck = "SELECT * FROM user WHERE userName='" + Username + "' AND password='" + Password + "'";
            PreparedStatement sqlQuery = connect.prepareStatement(loginCheck);
            ResultSet rs = sqlQuery.executeQuery(loginCheck);
            if (!rs.next()) {
                currentUser = new User();
                currentUser.setUsername(rs.getString("userName"));
                userLocale = Locale.getDefault();
                userTimeZone = ZoneId.systemDefault();
                sqlQuery.close();
                return true;

            } else {
                return false;
            }
    }
}
