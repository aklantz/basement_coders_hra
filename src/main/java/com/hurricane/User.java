package com.hurricane;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * A user class representing someone who uses the relief application
 */
public class User {

    private UUID userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private Date dateOfBirth;
    private String homeAddress;
    private String phoneNumber;
    private String emailAddress;
    private AccountStatus accountStatus;
    private USERTYPE type;
    private ArrayList<HouseholdMember> houseHoldMembers;
    private SafetyStatus safetyStatus;

    /**
     * Creates a new user
     */
    public User(UUID userId, String username, String passwordHash, String fullName,
                Date dateOfBirth, String homeAddress, String emailAddress,
                String phoneNumber, String login) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.homeAddress = homeAddress;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.houseHoldMembers = new ArrayList<HouseholdMember>();
    }

    /**
     * Sends a notifcation to the user
     * @param message text sent
     */
    public void sendNotification(String message) {
        System.out.println("To " + fullName + ": " + message);
    }

    /**
     * Checks a password against the stored password
     * @param password the password
     * @return true if both match
     */
    public boolean verifyPassword(String password) {
        return passwordHash.equals(password);
    }

    /**
     * Sets the user's preferred notification channel
     * @param channel the channel
     */
    public void setPreferredChannel(NotificationChannel channel) {
        
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getHomeAddress() {
        return homeAddress;
    }
}