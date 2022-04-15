package Model;

import Helper.DBConnection;

import java.sql.*;

public class LogonSession {

    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    // Login Attempt
    public static Boolean login(String Username, String Password) {
        try {
            Statement statement = DBConnection.getConnection().createStatement();
            String loginCheck = "SELECT * FROM user WHERE userName='" + Username + "' AND password='" + Password + "'";
            ResultSet rs = statement.executeQuery(loginCheck);
            if (rs.next()) {
                currentUser = new User();
                currentUser.setUsername(rs.getString("userName"));
                statement.close();

                return Boolean.TRUE;

            } else {
                return Boolean.FALSE;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
