package com.model;

import java.util.ArrayList;
import java.time.LocalDate;
import java.util.UUID;

import com.location.Address;

/**
 * A user of the hurricane relief application.
 */
public abstract class User {

    private UUID userId;
    private String username;
    private String password;
    private String fullName;
    private LocalDate dateOfBirth;
    private String homeAddress;
    private String phoneNumber;
    private String emailAddress;
    private AccountStatus accountStatus;
    private ArrayList<Household> households;
    private SafetyStatus safetyStatus;

    public User(UUID userId, String username, String password, String fullName,
                LocalDate dateOfBirth, String homeAddress, String emailAddress,
                String phoneNumber, String login) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.homeAddress = homeAddress;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.households = new ArrayList<Household>();
        this.accountStatus = AccountStatus.ACTIVE;
        this.safetyStatus = SafetyStatus.UNKNOWN;
    }

    /**
     * Returns the type of user (admin, volunteer, or coordinator).
     */
    public abstract USERTYPE getType();

    /**
     * Checks if this user is allowed to mark aid requests complete, only Shelter Managers can do this
     *
     * @return false for most users
     */
    public boolean canCompleteRequests() {
        return false;
    }

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

    /**
     * @param fullName the user's new full name
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return the user's date of birth
     */
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Turns the home address into an address the map tools can use.
     * The home address should look like "142 Riverbend Dr, Tega Cay, SC 29708".
     *
     * @return the address, or null if the home address isn't in that format
     */
    public Address getAddress() {
        if (homeAddress == null) {
            return null;
        }
        String[] parts = homeAddress.split(",");
        if (parts.length < 3) {
            return null;
        }
        String street = parts[0].trim();
        String city = parts[1].trim();
        String[] stateAndZip = parts[2].trim().split("\\s+");
        String state = stateAndZip[0];
        String zip = stateAndZip.length > 1 ? stateAndZip[1] : "";
        return new Address(street, city, state, zip);
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    /**
     * @param homeAddress the user's new home address
     */
    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    /**
     * @return the user's phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @param phoneNumber the user's new phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * @return the user's email address
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * @param emailAddress the user's new email address
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * @return the user's account status
     */
    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public ArrayList<Household> getHouseholds() {
        return households;
    }

    public SafetyStatus getSafetyStatus() {
        return safetyStatus;
    }

    public void setSafetyStatus(SafetyStatus safetyStatus) {
        this.safetyStatus = safetyStatus;
    }

    // Add these methods inside your existing User class
// (needs: import java.util.ArrayList; uses getHouseholds() and the userId field)

    /**
     * Creates a household owned by this user.
     *
     * @param householdName the name of the household
     * @return true if it was created, false if the name is blank or already used
     */
    public boolean addHousehold(String householdName) {
        if (householdName == null || householdName.trim().isEmpty()
                || getHousehold(householdName) != null) {
            return false;
        }
        getHouseholds().add(new Household(userId, householdName));
        return true;
    }

    /**
     * Finds one of this user's households by name.
     *
     * @param householdName the name of the household
     * @return the household, or null if not found
     */
    public Household getHousehold(String householdName) {
        if (householdName == null) {
            return null;
        }
        for (Household household : getHouseholds()) {
            if (householdName.equals(household.getHouseholdName())) {
                return household;
            }
        }
        return null;
    }

    /**
     * Adds a person to one of this user's households.
     *
     * @return true if added, false if the household doesn't exist, the name is blank,
     *         or the person is already in the household
     */
    public boolean addHouseholdMember(String householdName, String memberName) {
        Household household = getHousehold(householdName);
        if (household == null || memberName == null || memberName.trim().isEmpty()
                || household.getMembers().stream()
                        .anyMatch(member -> memberName.equals(member.getName()))) {
            return false;
        }
        return household.addHouseholdMember(memberName);
    }

    /**
     * Checks in a person who is already in one of this user's households.
     *
     * @return true if checked in, false if the household or person doesn't exist
     */
    public boolean checkInHouseholdMember(String householdName, String memberName,
            SafetyStatus safetyStatus) {
        Household household = getHousehold(householdName);
        if (household == null || safetyStatus == null || memberName == null
                || household.getMembers().stream()
                        .noneMatch(member -> memberName.equals(member.getName()))) {
            return false;
        }
        return household.checkIn(memberName, safetyStatus);
    }
}