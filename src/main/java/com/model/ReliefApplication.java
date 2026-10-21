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
	private ShelterDirectory shelterDirectory;
	private HurricaneDirectory hurricaneDirectory;
	private User currentUser;

	private ReliefApplication() {
		userDirectory = UserDirectory.getInstance();
		shelterDirectory = ShelterDirectory.getInstance();
		hurricaneDirectory = HurricaneDirectory.getInstance();
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

	public ArrayList<Shelter> getShelterDirectory() {
		return shelterDirectory.getActiveShelters(hurricaneDirectory.getAffectedZipCodes());
	}

	// Returns false if nobody is logged in or the household already exists
	public boolean createHousehold(String householdName) {
		return isLoggedIn() && currentUser.addHousehold(householdName) && userDirectory.save();
	}

	public boolean addHouseholdMember(String householdName, String memberName) {
		return isLoggedIn() && currentUser.addHouseholdMember(householdName, memberName)
				&& userDirectory.save();
	}

	public boolean checkInHouseholdMember(String householdName, String memberName,
			SafetyStatus safetyStatus) {
		return isLoggedIn()
				&& currentUser.checkInHouseholdMember(householdName, memberName, safetyStatus)
				&& userDirectory.save();
	}

	public ArrayList<Household> getHouseholds() {
		return isLoggedIn() ? currentUser.getHouseholds() : new ArrayList<Household>();
	}

	public void logAction(String action, String entityType, UUID entityId) {
		// TODO: record who did what to which entity (audit log)
	}
}