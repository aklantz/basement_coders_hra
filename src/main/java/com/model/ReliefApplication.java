package com.model;

import java.util.Date;
import java.util.UUID;

/**
 * Facade between the UI and the rest of the system. Singleton.
 * Trimmed to the account methods so it compiles before the other directories exist.
 */
public class ReliefApplication {

    private static ReliefApplication reliefApplication;

    private UserDirectory userDirectory;
    private User currentUser;

    private ReliefApplication() {
        userDirectory = UserDirectory.getInstance();
        currentUser = null;
    }

    public static ReliefApplication getInstance() {
        if (reliefApplication == null) {
            reliefApplication = new ReliefApplication();
        }
        return reliefApplication;
    }

    public User createAccount(String username, String password, String firstName, String lastName,
                              Date dateOfBirth, String homeAddress, String emailAddress,
                              String phoneNumber, USERTYPE userType) {
        String fullName = firstName + " " + lastName;
        User user = userDirectory.createUser(username, password, fullName, dateOfBirth,
                homeAddress, emailAddress, phoneNumber, userType);
        if (user == null) {
            return null;
        }
        userDirectory.save();
        logAction("CREATE_ACCOUNT", "User", user.getUserId());
        return user;
    }

    public User login(String username, String password) {
        User user = userDirectory.getUser(username, password);
        if (user == null) {
            return null;
        }
        currentUser = user;
        logAction("LOGIN", "User", user.getUserId());
        return currentUser;
    }

    public boolean logout() {
        if (currentUser == null) {
            return false;
        }
        logAction("LOGOUT", "User", currentUser.getUserId());
        currentUser = null;
        return true;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logAction(String action, String entityType, UUID entityId) {
        // TODO: record who did what to which entity (audit log)
    }
}