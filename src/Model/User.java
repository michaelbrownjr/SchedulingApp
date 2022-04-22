package Model;

public class User {
    private String userName;
    private final Integer userID;

    public User(String inputUsername, Integer inputUserID) {
       userName = inputUsername;
       userID = inputUserID;
    }

    public String getUsername(){
        return userName;
    }

    public void setUserName(String username) {
        this.userName = username;
    }
    public Integer getUserID() {
        return userID;
    }
}
