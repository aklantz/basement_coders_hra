package com.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * A user of the hurricane relief application
 */
public abstract class User {

    private UUID userId;
    private String username;
    private String password;
    private String fullName;
    private Date dateOfBirth;
    private String homeAddress;
    private String phoneNumber;
    private String emailAddress;
    private AccountStatus accountStatus;
    private ArrayList<HouseholdMember> houseHoldMembers;
    private SafetyStatus safetyStatus;

    /**
     * Creates a new user
     * @param userId the user's unique id
     * @param username the name the user logs in with
     * @param password the user's stored password
     * @param fullName the user's full name
     * @param dateOfBirth the user's date of birth
     * @param homeAddress the user's home address
     * @param emailAddress the user's email address
     * @param phoneNumber the user's phone number
     * @param login the user's login, kept to match the UML (not currently stored)
     */
    public User(UUID userId, String username, String password, String fullName,
                Date dateOfBirth, String homeAddress, String emailAddress,
                String phoneNumber, String login) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.homeAddress = homeAddress;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.houseHoldMembers = new ArrayList<HouseholdMember>();
        this.safetyStatus = SafetyStatus.UNKNOWN;
    }

    /**
     * @return the user's type (admin, volunteer, or coordinator)
     */
    public abstract USERTYPE getType();

    /**
     * Sends a notification to the user
     * @param message the text to send
     */
    public void sendNotification(String message) {
        System.out.println("To " + fullName + ": " + message);
    }

    /**
     * Checks a password against the stored password
     * @param password the password to check
     * @return true if the passwords match
     */
    public boolean verifyPassword(String password) {
        return password != null && password.equals(password);
    }

    /**
     * Sets the user's preferred notification channel
     * @param channel the channel to use
     */
    public void setPreferredChannel(NotificationChannel channel) {
        // TODO: store the channel once NotificationChannel is designed
    }

    /**
     * @return the user's unique id
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * @return the user's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the user's stored password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the user's full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @return the user's date of birth
     */
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * @return the user's home address
     */
    public String getHomeAddress() {
        return homeAddress;
    }

    /**
     * @return the user's phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @return the user's email address
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * @return the user's account status
     */
    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    /**
     * @param accountStatus the new account status
     */
    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    /**
     * @return the members of the user's household
     */
    public ArrayList<HouseholdMember> getHouseHoldMembers() {
        return houseHoldMembers;
    }

    /**
     * @return the user's safety status
     */
    public SafetyStatus getSafetyStatus() {
        return safetyStatus;
    }

    /**
     * @param safetyStatus the user's new safety status
     */
    public void setSafetyStatus(SafetyStatus safetyStatus) {
        this.safetyStatus = safetyStatus;
    }
}