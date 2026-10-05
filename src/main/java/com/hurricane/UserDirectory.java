package com.hurricane;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Holds all users
 */
public class UserDirectory {

    private static UserDirectory userDirectory;
    private ArrayList<User> users;

    private UserDirectory() {
        users = new ArrayList<User>();
    }

    /**
     * @return the directory
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
     * @return the user or null if not found
     */
    public User getUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Finds a user by username and password
     * @param username the username
     * @param password the password
     * @return the user or null if the login is wrong
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
     * @return true if added
     */
    public boolean addUser(User user) {
        if (getUser(user.getUsername()) != null) {
            return false;
        }
        users.add(user);
        return true;
    }

    /**
     * Finds users near the zip code
     * @param zipCode the zip code
     * @return matching users
     */
    public ArrayList<User> getUsersByZipCode(String zipCode) {
        ArrayList<User> matches = new ArrayList<User>();
        for (User user : users) {
            if (user.getHomeAddress().contains(zipCode)) {
                matches.add(user);
            }
        }
        return matches;
    }

    /**
     * Gets coordinators waiting for approval
     * @return pending coordinators
     */
    public ArrayList<Coordinator> getPendingCoordinators() {
        // TODO: 
        return new ArrayList<Coordinator>();
    }

    /**
     * Saves all users to file
     * @return true if saved
     */
    public boolean save() {
        return DataWriter.saveUsers();
    }

    /**
     * Finds a user by id
     * @param userId the id
     * @return the user or null if not found
     */
    public User getUserById(UUID userId) {
        for (User user : users) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
    }
}