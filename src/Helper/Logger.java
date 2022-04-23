package Helper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
/**
 *  Logger class for appending strings of successful or failed login attempts
 *  accompanied by a timestamp
 */
public class Logger {
    private static final String FILENAME = "login_activity.txt";


    public Logger() {}

    /**
     * @param username
     * @param success
     */
    public static void log(String username, boolean success) {
        try (FileWriter fw = new FileWriter(FILENAME, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
            pw.println(username + (success ? " Login Successful" : " Login Failed") + " " + Instant.now().toString());
        } catch (IOException e) {
            System.out.println("Logger Error: " + e.getMessage());
        }
    }
}
