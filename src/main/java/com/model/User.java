package com.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * A user of the hurricane relief application.
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
        this.accountStatus = AccountStatus.ACTIVE;
        this.safetyStatus = SafetyStatus.UNKNOWN;
    }

    /**
     * Returns the type of user (admin, volunteer, or coordinator).
     */
    public abstract USERTYPE getType();

    /**
     * Prints a message to the user.
     */
    public void sendNotification(String message) {
        System.out.println("To " + fullName + ": " + message);
    }

    /**
     * Returns true if the given password matches the stored one.
     */
    public boolean verifyPassword(String password) {
        return this.password.equals(password);
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public ArrayList<HouseholdMember> getHouseHoldMembers() {
        return houseHoldMembers;
    }

    public SafetyStatus getSafetyStatus() {
        return safetyStatus;
    }

    public void setSafetyStatus(SafetyStatus safetyStatus) {
        this.safetyStatus = safetyStatus;
    }
}