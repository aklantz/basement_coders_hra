package com.model;

import java.util.ArrayList;
import java.time.LocalDate;
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
	private AidRequestDirectory aidRequestDirectory;
	private ShelterDirectory shelterDirectory;
	private HurricaneDirectory hurricaneDirectory;
	private TripExporter tripExporter;
	private User currentUser;

	/**
	 * Creates the facade and gets the directories it passes work on to.
	 * Nobody is logged in at first.
	 */
	private ReliefApplication() {
		userDirectory = UserDirectory.getInstance();
		aidRequestDirectory = AidRequestDirectory.getInstance();
		shelterDirectory = ShelterDirectory.getInstance();
		hurricaneDirectory = HurricaneDirectory.getInstance();
		tripExporter = new TripExporter(shelterDirectory);
		currentUser = null;
	}

	/**
	 * Gets the single instance of the facade, creating it if needed.
	 *
	 * @return the facade
	 */
	public static ReliefApplication getInstance() {
		if (reliefApplication == null)
			reliefApplication = new ReliefApplication();
		return reliefApplication;
	}

	/**
	 * Creates a new account and saves it.
	 *
	 * @param username the username for the account
	 * @param password the password for the account
	 * @param firstName the user's first name
	 * @param lastName the user's last name
	 * @param dateOfBirth the user's date of birth
	 * @param homeAddress the user's home address
	 * @param emailAddress the user's email address
	 * @param phoneNumber the user's phone number
	 * @param userType the kind of account to create
	 * @return the new user, or null if the account could not be created
	 */
	public User createAccount(String username, String password, String firstName, String lastName,
			LocalDate dateOfBirth, String homeAddress, String emailAddress, String phoneNumber,
			USERTYPE userType) {
		return userDirectory.createUser(username, password, firstName + " " + lastName,
				dateOfBirth, homeAddress, emailAddress, phoneNumber, userType);
	}

	/**
	 * Logs a user in and remembers them as the current user.
	 *
	 * @param username the username
	 * @param password the password
	 * @return the user, or null if the username or password is wrong
	 */
	public User login(String username, String password) {
		currentUser = userDirectory.getUser(username, password);
		return currentUser;
	}

	/**
	 * Logs the current user out.
	 *
	 * @return true if someone was logged out, false if nobody was logged in
	 */
	public boolean logout() {
		boolean wasLoggedIn = isLoggedIn();
		currentUser = null;
		return wasLoggedIn;
	}

	/**
	 * Checks whether someone is logged in.
	 *
	 * @return true if a user is logged in
	 */
	public boolean isLoggedIn() {
		return currentUser != null;
	}

	/**
	 * Gets the user who is logged in.
	 *
	 * @return the current user, or null if nobody is logged in
	 */
	public User getCurrentUser() {
		return currentUser;
	}

	/**
	 * Finds a user by id.
	 *
	 * @param userId the user's id
	 * @return the user, or null if not found
	 */
	public User getUserById(UUID userId) {
		return userDirectory.getUserById(userId);
	}

	/**
	 * Finds the users whose home address is in a zip code.
	 *
	 * @param zipCode the zip code
	 * @return the matching users
	 */
	public ArrayList<User> getUsersByZipCode(String zipCode) {
		return userDirectory.getUsersByZipCode(zipCode);
	}

	/**
	 * Gets the coordinators whose accounts are waiting for approval.
	 *
	 * @return the pending coordinators
	 */
	public ArrayList<Coordinator> getPendingCoordinators() {
		return userDirectory.getPendingCoordinators();
	}

	/**
	 * Gets the active shelters in the zip codes the hurricane affects.
	 *
	 * @return the active shelters
	 */
	public ArrayList<Shelter> getShelterDirectory() {
		return shelterDirectory.getActiveShelters(hurricaneDirectory.getAffectedZipCodes());
	}

	/**
	 * Gets the requests assigned to the logged-in volunteer.
	 *
	 * @return the assigned requests, or an empty list if nobody is logged in
	 */
	public ArrayList<AidRequest> getAssignedRequests() {
		return isLoggedIn() ? aidRequestDirectory.getRequestsAssignedTo(currentUser)
				: new ArrayList<AidRequest>();
	}

	/**
	 * Finds the shelter closest to a request's address.
	 *
	 * @param request the request to find a shelter for
	 * @return the nearest available shelter, or null if there is none
	 */
	public Shelter findNearestShelter(AidRequest request) {
		return shelterDirectory.findNearestShelter(request.getStreetAddress());
	}

	/**
	 * Gets step-by-step directions between two addresses.
	 *
	 * @param fromAddress where the trip starts
	 * @param toAddress where the trip ends
	 * @return the directions, one step per entry
	 */
	public ArrayList<String> getDirections(String fromAddress, String toAddress) {
		return shelterDirectory.getDirections(fromAddress, toAddress);
	}

	/**
	 * Writes a printable .txt file with the trip information for a request.
	 *
	 * @param request the request being handled
	 * @param shelter the shelter the family is going to
	 * @param filePath where to save the file
	 * @return true if the file was written, false if nobody is logged in or the export failed
	 */
	public boolean exportTripInformation(AidRequest request, Shelter shelter, String filePath) {
		return isLoggedIn() && tripExporter.export(currentUser, request, shelter, filePath);
	}

    /**
     * Creates an aid request for the logged-in user.
     *
     * @param category the kind of aid needed
     * @param householdSize how many people need aid
     * @param streetAddress where the aid is needed
     * @return the new request, or null if creation fails
     */
    public AidRequest createAidRequest(RequestCategory category, int householdSize,
                                    String streetAddress) {
        return aidRequestDirectory.createAidRequest(
                currentUser, category, householdSize, streetAddress);
    }

	/**
     * Creates an aid request for the logged-in user.
     *
     * @param category the kind of aid needed
     * @param householdSize how many people need aid
     * @param streetAddress where the aid is needed
	 * @param note a note to leave in the aid request
     * @return the new request, or null if creation fails
     */
	public AidRequest createAidRequest(RequestCategory category, int householdSize,
                                    String streetAddress, String note) {
        return aidRequestDirectory.createAidRequest(
                currentUser, category, householdSize, streetAddress, note);
    }

	/**
	 * Marks a request complete if the logged-in user's role allows it.
	 *
	 * @param requestId the id of the request
	 * @return true if the request was completed
	 * @throws SecurityException if the user's role is not allowed to complete requests
	 */
	public boolean completeRequest(UUID requestId) {
		return aidRequestDirectory.completeRequest(currentUser, requestId);
	}

	/**
	 * Creates a household for the logged-in user and saves it.
	 *
	 * @param householdName the name of the household
	 * @return false if nobody is logged in or the household already exists
	 */
	public boolean createHousehold(String householdName) {
		return isLoggedIn() && currentUser.addHousehold(householdName) && userDirectory.save();
	}

	/**
	 * Adds a person to one of the logged-in user's households and saves.
	 *
	 * @param householdName the name of the household
	 * @param memberName the person's name
	 * @return false if nobody is logged in, the household doesn't exist,
	 *         or the person is already in it
	 */
	public boolean addHouseholdMember(String householdName, String memberName) {
		return isLoggedIn() && currentUser.addHouseholdMember(householdName, memberName)
				&& userDirectory.save();
	}

	/**
	 * Checks in a household member with a safety status and saves.
	 *
	 * @param householdName the name of the household
	 * @param memberName the person's name
	 * @param safetyStatus the status to record
	 * @return false if nobody is logged in or the household or person doesn't exist
	 */
	public boolean checkInHouseholdMember(String householdName, String memberName,
			SafetyStatus safetyStatus) {
		return isLoggedIn()
				&& currentUser.checkInHouseholdMember(householdName, memberName, safetyStatus)
				&& userDirectory.save();
	}

	/**
	 * Gets the logged-in user's households.
	 *
	 * @return the households, or an empty list if nobody is logged in
	 */
	public ArrayList<Household> getHouseholds() {
		return isLoggedIn() ? currentUser.getHouseHoldMembers() : new ArrayList<Household>();
	}

	/**
	 * Records who did what to which item.
	 *
	 * @param action what was done, such as LOGIN
	 * @param entityType the kind of item it was done to, such as User
	 * @param entityId the id of that item
	 */
	public void logAction(String action, String entityType, UUID entityId) {
		// TODO: record who did what to which entity (audit log)
	}
}