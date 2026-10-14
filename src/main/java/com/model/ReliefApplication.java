package com.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Facade between the UI and the rest of the system. Passes work on to the
 * directories and keeps track of the user who is logged in.
 *
 * @author Austin Lantz
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
		if (reliefApplication == null)
			reliefApplication = new ReliefApplication();
		return reliefApplication;
	}

	public User createAccount(String username, String password, String firstName, String lastName,
			Date dateOfBirth, String homeAddress, String emailAddress, String phoneNumber,
			USERTYPE userType) {
		return userDirectory.createUser(username, password, firstName + " " + lastName,
				dateOfBirth, homeAddress, emailAddress, phoneNumber, userType);
	}

	public User login(String username, String password) {
		currentUser = userDirectory.getUser(username, password);
		return currentUser;
	}

	public boolean logout() {
		boolean wasLoggedIn = isLoggedIn();
		currentUser = null;
		return wasLoggedIn;
	}

	public boolean isLoggedIn() {
		return currentUser != null;
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public User getUserById(UUID userId) {
		return userDirectory.getUserById(userId);
	}

	public ArrayList<User> getUsersByZipCode(String zipCode) {
		return userDirectory.getUsersByZipCode(zipCode);
	}

	public ArrayList<Coordinator> getPendingCoordinators() {
		return userDirectory.getPendingCoordinators();
	}

	public void logAction(String action, String entityType, UUID entityId) {
		// TODO: record who did what to which entity (audit log)
	}
}