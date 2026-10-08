package com.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * Holds all users of the relief application
 */
public class UserDirectory {

    private static UserDirectory userDirectory;
    private ArrayList<User> users;

    /**
     * Creates the directory with temporary sample users
     */
    private UserDirectory() {
        users = new ArrayList<User>();

        User volunteer = new Volunteer(UUID.randomUUID(), "volunteer1", "pass123", "Val Volunteer",
                new GregorianCalendar(2001, Calendar.MARCH, 22).getTime(),
                "200 Oak Ave, Columbia, SC 29201", "val@relief.org",
                "803-555-0102", "volunteer1");
        volunteer.setAccountStatus(AccountStatus.ACTIVE);

        User coordinator = new Coordinator(UUID.randomUUID(), "coord1", "pass123", "Casey Coordinator",
                new GregorianCalendar(1985, Calendar.JULY, 9).getTime(),
                "300 Pine Rd, Columbia, SC 29201", "casey@relief.org",
                "803-555-0103", "coord1");
        coordinator.setAccountStatus(AccountStatus.ACTIVE);

        users.add(volunteer);
        users.add(coordinator);
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
     * Adds a user if the username is not taken
     * @param user the user to add
     * @return true if the user was added
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
     * @param zipCode the zip code
     * @return the matching users
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
     * @return the pending coordinators
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
     * @return true if the users were saved
     */
    public boolean save() {
        return DataWriter.saveUsers();
    }

    /**
     * Finds a user by id
     * @param userId the id
     * @return the user, or null if not found
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
}