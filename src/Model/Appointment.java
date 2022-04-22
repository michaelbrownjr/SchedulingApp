package Model;

import java.sql.Timestamp;

public class Appointment {
    private final Integer appointmentID;
    private final String title;
    private final String description;
    private final String location;
    private final String type;
    private final Timestamp startDateTime;
    private final Timestamp endDateTime;
    private final Integer customerID;
    private final Integer userID;
    private final String contactName;

    /**
     * Appointment constructor
     *
     * @param inputAppointmentID appointment ID (Primary Key).
     * @param inputContactName Name of contact.
     * @param inputCustomerID customer ID (Foreign Key).
     * @param inputDescription appointment description.
     * @param inputEndDateTime end date/time of appointment.
     * @param inputLocation Appointment Location.
     * @param inputStartDateTime Start Date/time of app
     * @param inputTitle Appointment title
     * @param inputType Appointment Type
     * @param inputUserID User ID(Foreign Key).
     *
     */
    public Appointment(Integer inputAppointmentID, String inputTitle, String inputDescription, String inputLocation, String inputType, Timestamp inputStartDateTime, Timestamp inputEndDateTime, Integer inputCustomerID, Integer inputUserID, String inputContactName) {

        appointmentID = inputAppointmentID;
        title = inputTitle;
        description = inputDescription;
        location = inputLocation;
        type = inputType;
        startDateTime = inputStartDateTime;
        endDateTime = inputEndDateTime;
        customerID = inputCustomerID;
        userID = inputUserID;
        contactName = inputContactName;

    }


    //
    /**
     * getter - Appointment ID
     * @return ID of the appointment
     */
    public Integer getAppointmentID() {
        return appointmentID;
    }

    /**
     * Getter - Title
     * @return title of the appointment
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter - Description
     * @return description of appointment
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter - Location
     * @return location of appointment
     */
    public String getLocation() {
        return location;
    }

    /**
     * Getter - Type
     * @return type of the appointment
     */
    public String getType() {
        return  type;
    }

    /**
     * Getter - Start Date time
     * @return start datetime of appointment
     */
    public Timestamp getStartDateTime() {
        return startDateTime;
    }

    /**
     * Getter - end date time
     * @return end datetime of appointment
     */
    public Timestamp getEndDateTime() {
        return endDateTime;
    }

    /**
     * getter - customer ID
     * @return customer ID
     */
    public Integer getCustomerID() {
        return customerID;
    }

    /**
     * Getter - user ID
     * @return user ID
     */
    public Integer getUserID() {
        return userID;
    }

    /**
     * Getter - contact name
     * @return name of contact
     */
    public String getContactName() {
        return contactName;
    }
}
