package com.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Holds all users of the relief application
 */
public class UserDirectory {

    private static UserDirectory userDirectory;
    private ArrayList<User> users;

    /**
     * Creates the directory and loads the saved users through the DataLoader
     */
    private UserDirectory() {
        users = DataLoader.getUsers();
        if (users == null) {
            users = new ArrayList<User>();
        }
    }

    /**
     * @return the single instance of the directory
     */
    public static UserDirectory getInstance() {
        if (userDirectory == null) {
            userDirectory = new UserDirectory();
        }
        return userDirectory;
    }

    /**
     * Returns ALL users — needed for DataWriter.saveUsers()
     */
    public ArrayList<User> getUsers() {
        return users;
    }

    /**
     * Finds a user by username
     * @param username the username
     * @return the user, or null if not found
     */
    public User getUser(String username) {
        if (username == null) {
            return null;
        }
        for (User user : users) {
            if (username.equals(user.getUsername())) {
                return user;
            }
        }
        return null;
    }

    /**
     * Finds a user by username and password
     * @param username the username
     * @param password the password
     * @return the user, or null if the login is wrong
     */
    public User getUser(String username, String password) {
        User user = getUser(username);
        if (user != null && user.verifyPassword(password)) {
            return user;
        }
        return null;
    }

    /**
     * Creates a new user and adds it to the directory.
     */
    public User createUser(String username, String password, String fullName, Date dateOfBirth,
                           String homeAddress, String emailAddress, String phoneNumber,
                           USERTYPE userType) {
        if (isBlank(username) || isBlank(password) || isBlank(fullName) || userType == null) {
            return null;
        }

        UUID userId = UUID.randomUUID();
        User user;

        switch (userType) {
            case VOLUNTEER:
                user = new Volunteer(userId, username, password, fullName, dateOfBirth,
                        homeAddress, emailAddress, phoneNumber, username);
                user.setAccountStatus(AccountStatus.ACTIVE);
                break;

            case COORDINATOR:
                user = new Coordinator(userId, username, password, fullName, dateOfBirth,
                        homeAddress, emailAddress, phoneNumber, username);
                user.setAccountStatus(AccountStatus.PENDING);
                break;

            default:
                return null;
        }

        return addUser(user) ? user : null;
    }

    /**
     * Adds a user if the username is not taken
     */
    public boolean addUser(User user) {
        if (user == null || getUser(user.getUsername()) != null) {
            return false;
        }
        users.add(user);
        return true;
    }

    /**
     * Finds users whose home address is in the given zip code
     */
    public ArrayList<User> getUsersByZipCode(String zipCode) {
        ArrayList<User> matches = new ArrayList<User>();
        if (zipCode == null) {
            return matches;
        }
        for (User user : users) {
            String address = user.getHomeAddress();
            if (address != null && address.trim().endsWith(zipCode.trim())) {
                matches.add(user);
            }
        }
        return matches;
    }

    /**
     * Gets coordinators whose accounts are waiting for approval
     */
    public ArrayList<Coordinator> getPendingCoordinators() {
        ArrayList<Coordinator> pending = new ArrayList<Coordinator>();
        for (User user : users) {
            if (user.getType() == USERTYPE.COORDINATOR
                    && user.getAccountStatus() == AccountStatus.PENDING) {
                pending.add((Coordinator) user);
            }
        }
        return pending;
    }

    /**
     * Saves all users to file
     */
    public boolean save() {
        return DataWriter.saveUsers(users);
    }

    /**
     * Finds a user by id
     */
    public User getUserById(UUID userId) {
        if (userId == null) {
            return null;
        }
        for (User user : users) {
            if (userId.equals(user.getUserId())) {
                return user;
            }
        }
        return null;
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
