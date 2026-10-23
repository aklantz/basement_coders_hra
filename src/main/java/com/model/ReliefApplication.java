package com.model;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
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
	 * Gets every shelter in the system.
	 *
	 * @return all shelters
	 */
	public ArrayList<Shelter> getAllShelters() {
		return shelterDirectory.getShelters();
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
	 * Gets every aid request, so volunteers can see which ones are already being handled.
	 *
	 * @return all requests
	 */
	public ArrayList<AidRequest> getRequests() {
		return aidRequestDirectory.getRequests();
	}

	/**
	 * Gets the open requests for the logged-in volunteer. Recommended requests come first,
	 * and each one says why it is or is not recommended.
	 *
	 * @return the open requests, or an empty list if the user is not a volunteer
	 * @throws IOException if a map lookup fails
	 * @throws InterruptedException if a map request is interrupted
	 */
	public ArrayList<RecommendedRequest> getRecommendedRequests()
			throws IOException, InterruptedException {
		return aidRequestDirectory.getRecommendedRequests(currentUser);
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
	 * Finds the nearest available shelter for an aid request.
	 *
	 * @param request the request to find a shelter for
	 * @return the nearest available shelter, or null if the request is null
	 *         or no shelter is found
	 * @throws IOException if a map lookup fails
	 * @throws InterruptedException if a map request is interrupted
	 */
	public Shelter findNearestShelter(AidRequest request)
			throws IOException, InterruptedException {
		return shelterDirectory.findNearestShelter(request);
	}

	/**
	 * Finds the shelter closest by driving distance that can take a request's family.
	 * It must have room for everyone, accept the family's pets, and have medical staff
	 * if the request needs medical assistance.
	 *
	 * @param request the request to find a shelter for
	 * @return the suitable shelter, or null if there is none
	 * @throws IOException if a map lookup fails
	 * @throws InterruptedException if a map request is interrupted
	 */
	public Shelter findSuitableShelter(AidRequest request)
			throws IOException, InterruptedException {
		return shelterDirectory.findSuitableShelter(request);
	}

	/**
	 * Assigns an open request to the logged-in volunteer and saves the shelter they chose.
	 * The request becomes ASSIGNED.
	 *
	 * @param requestId the id of the request
	 * @param shelter the shelter the volunteer selected
	 * @return true if the request was assigned
	 */
	public boolean assignRequestToMe(UUID requestId, Shelter shelter) {
		return aidRequestDirectory.assignToVolunteer(currentUser, requestId, shelter);
	}

	/**
	 * Gets the shelter that was selected when a request was assigned.
	 *
	 * @param request the request
	 * @return the selected shelter, or null if none was selected
	 */
	public Shelter getSelectedShelter(AidRequest request) {
		return request == null ? null : shelterDirectory.getShelterById(request.getShelterId());
	}

	/**
	 * Gets driving directions between two addresses.
	 *
	 * @param fromAddress where the trip starts
	 * @param toAddress where the trip ends
	 * @return one direction per entry, or an empty list if either address is invalid
	 * @throws IOException if a map lookup fails
	 * @throws InterruptedException if a map request is interrupted
	 */
	public ArrayList<String> getDirections(String fromAddress, String toAddress)
			throws IOException, InterruptedException {
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
	 * Creates an aid request with a note for the logged-in user.
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
	 * Finds the open aid request made by a user.
	 *
	 * @param username the username of the person who made the request
	 * @return their request, or null if they don't have one
	 */
	public AidRequest getRequestFrom(String username) {
		User requester = userDirectory.getUser(username);
		if (requester == null) {
			return null;
		}
		return aidRequestDirectory.getRequest(requester.getUserId());
	}

	/**
	 * Gets the shelter run by the logged-in shelter manager.
	 *
	 * @return the manager's shelter, or null if the user is not a shelter manager
	 */
	public Shelter getMyShelter() {
		if (!(currentUser instanceof ShelterManager)) {
			return null;
		}
		ShelterManager manager = (ShelterManager) currentUser;
		return shelterDirectory.getShelterById(manager.getShelterId());
	}

	/**
	 * Checks people and their pets into the logged-in manager's shelter and saves.
	 * Only the people count toward the shelter's occupancy; pets are recorded separately.
	 *
	 * @param people the names of the people being checked in
	 * @param pets the pets being checked in
	 * @return true if they were checked in, false if the user isn't a shelter manager
	 *         or the shelter doesn't have room
	 */
	public boolean checkInToShelter(ArrayList<String> people, ArrayList<String> pets) {
		Shelter shelter = getMyShelter();
		if (shelter == null) {
			return false;
		}
		return shelter.checkIn(people, pets) && shelterDirectory.save();
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
	 * Adds a person or pet, with details, to one of the logged-in user's households and saves.
	 *
	 * @param householdName the name of the household
	 * @param memberName the person's or pet's name
	 * @param age the person's age (use 0 for a pet)
	 * @param petType the kind of pet, such as "dog", or null for a person
	 * @param medicalNote the medical assistance needed, or null if none
	 * @return false if nobody is logged in, the household doesn't exist,
	 *         or the member is already in it
	 */
	public boolean addHouseholdMember(String householdName, String memberName, int age,
			String petType, String medicalNote) {
		return isLoggedIn() && currentUser.addHouseholdMember(householdName, memberName, age,
				petType, medicalNote) && userDirectory.save();
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
		return isLoggedIn() ? currentUser.getHouseholds() : new ArrayList<Household>();
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