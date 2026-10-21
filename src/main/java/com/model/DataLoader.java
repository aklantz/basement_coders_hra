package com.model;

import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Loads data from the JSON files.
 */
public class DataLoader extends DataConstants {

    /**
     * Loads all users from the users file.
     *
     * @return a list of users
     */
    public static ArrayList<User> getUsers() {
        ArrayList<User> users = new ArrayList<User>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try (FileReader reader = new FileReader(USER_FILE)) {
            JSONParser parser = new JSONParser();
            JSONArray usersJSON = (JSONArray) parser.parse(reader);

            for (Object obj : usersJSON) {
                JSONObject userJSON = (JSONObject) obj;

                UUID id = UUID.fromString((String) userJSON.get(USER_ID));
                String username = (String) userJSON.get(USER_USERNAME);
                String password = (String) userJSON.get(USER_PASSWORD);
                String fullName = (String) userJSON.get(USER_FULL_NAME);
                Date dateOfBirth = dateFormat.parse((String) userJSON.get(USER_DOB));
                String address = (String) userJSON.get(USER_ADDRESS);
                String email = (String) userJSON.get(USER_EMAIL);
                String phone = (String) userJSON.get(USER_PHONE);
                USERTYPE type = USERTYPE.valueOf((String) userJSON.get(USER_TYPE));

                User user = createUser(type, id, username, password, fullName,
                        dateOfBirth, address, email, phone);

                if (user != null) {
                    user.setAccountStatus(AccountStatus.valueOf((String) userJSON.get(USER_ACCOUNT_STATUS)));
                    user.setSafetyStatus(SafetyStatus.valueOf((String) userJSON.get(USER_SAFETY_STATUS)));
                    loadHouseholds(user, (JSONArray) userJSON.get("houseHoldMembers"));
                    users.add(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    /**
     * Loads a user's households from JSON and adds them to the user.
     *
     * @param user the user who owns the households
     * @param householdsJSON the saved households
     */
    private static void loadHouseholds(User user, JSONArray householdsJSON) {
        for (Object householdObj : householdsJSON) {
            JSONObject householdJSON = (JSONObject) householdObj;
            HouseholdMember household = new HouseholdMember(
                    UUID.fromString((String) householdJSON.get("householdOwnerUUID")),
                    (String) householdJSON.get("householdName"));

            for (Object memberObj : (JSONArray) householdJSON.get("members")) {
                JSONObject memberJSON = (JSONObject) memberObj;
                String name = (String) memberJSON.get("name");
                household.addHouseholdMember(name);
                household.checkIn(name, SafetyStatus.valueOf((String) memberJSON.get("safetyStatus")));
            }
            user.getHouseHoldMembers().add(household);
        }
    }

    /**
     * Creates the correct type of user based on the user's type.
     *
     * @return the new user, or null if the type is not recognized
     */
    private static User createUser(USERTYPE type, UUID id, String username, String password,
                                   String fullName, Date dateOfBirth, String address,
                                   String email, String phone) {
        switch (type) {
            case VOLUNTEER:
                return new Volunteer(id, username, password, fullName,
                        dateOfBirth, address, email, phone, username);
            case COORDINATOR:
                return new Coordinator(id, username, password, fullName,
                        dateOfBirth, address, email, phone, username);
            case ADMIN:
                return new Admin(id, username, password, fullName,
                        dateOfBirth, address, email, phone, username);
            default:
                return null;
        }
    }

    /**
     * Loads all shelters from the shelters file.
     *
     * @return a list of shelters
     */
    public static ArrayList<Shelter> getCachedShelters() {
        ArrayList<Shelter> shelters = new ArrayList<Shelter>();

        try (FileReader reader = new FileReader(RESOURCE_FILE)) {
            JSONParser parser = new JSONParser();
            JSONArray sheltersJSON = (JSONArray) parser.parse(reader);

            for (Object obj : sheltersJSON) {
                JSONObject shelterJSON = (JSONObject) obj;

                UUID id = UUID.fromString((String) shelterJSON.get(SHELTER_ID));
                String name = (String) shelterJSON.get(SHELTER_NAME);
                int capacity = ((Number) shelterJSON.get(SHELTER_CAPACITY)).intValue();
                int occupancy = ((Number) shelterJSON.get(SHELTER_OCCUPANCY)).intValue();
                ShelterStatus status = ShelterStatus.valueOf((String) shelterJSON.get(SHELTER_STATUS));
                LocalDateTime lastUpdated = OffsetDateTime.parse(
                        (String) shelterJSON.get(SHELTER_LAST_UPDATED)).toLocalDateTime();

                Shelter shelter = new Shelter(
                        id,
                        name,
                        capacity,
                        occupancy,
                        status,
                        lastUpdated);
                shelter.setZipCode((String) shelterJSON.get(SHELTER_ZIP_CODE));
                shelter.setAddress(
                        (String) shelterJSON.get(SHELTER_STREET_ADDRESS),
                        (String) shelterJSON.get(SHELTER_CITY),
                        (String) shelterJSON.get(SHELTER_STATE));
                Number latitude = (Number) shelterJSON.get(SHELTER_LATITUDE);
                Number longitude = (Number) shelterJSON.get(SHELTER_LONGITUDE);
                if (latitude != null && longitude != null) {
                    shelter.setCoordinates(latitude.doubleValue(), longitude.doubleValue());
                }

                JSONArray inventoryJSON =
                        (JSONArray) shelterJSON.get("inventoryItems");

                if (inventoryJSON != null) {

                    ArrayList<InventoryItem> inventoryItems =
                            new ArrayList<>();

                    for (Object inventoryObject : inventoryJSON) {

                        JSONObject itemJSON =
                                (JSONObject) inventoryObject;

                        UUID itemId =
                                UUID.fromString(
                                        (String) itemJSON.get("itemId"));

                        String supplyType =
                                (String) itemJSON.get("supplyType");

                        int quantity =
                                ((Number) itemJSON.get("quantity"))
                                        .intValue();

                        int reorderThreshold =
                                ((Number) itemJSON.get("reorderThreshold"))
                                        .intValue();

                        inventoryItems.add(
                                new InventoryItem(
                                        itemId,
                                        supplyType,
                                        quantity,
                                        reorderThreshold));
                    }

                    shelter.setInventoryItems(inventoryItems);
                }

                shelters.add(shelter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return shelters;
    }

    /**
     * Loads all aid requests from the aid requests file.
     * Users must be loaded first, since each request links to its requester.
     *
     * @return a list of aid requests
     */
    public static ArrayList<AidRequest> getAidRequests() {
        ArrayList<AidRequest> requests = new ArrayList<AidRequest>();
        UserDirectory userDirectory = UserDirectory.getInstance();

        try (FileReader reader = new FileReader(AID_REQUEST_FILE)) {
            JSONParser parser = new JSONParser();
            JSONArray requestsJSON = (JSONArray) parser.parse(reader);

            for (Object obj : requestsJSON) {
                JSONObject requestJSON = (JSONObject) obj;

                UUID requestId = UUID.fromString((String) requestJSON.get(REQUEST_ID));
                UUID requesterId = UUID.fromString((String) requestJSON.get(REQUEST_REQUESTER));
                User requester = userDirectory.getUserById(requesterId);
                RequestCategory category = RequestCategory.valueOf((String) requestJSON.get(REQUEST_CATEGORY));
                RequestStatus status = RequestStatus.valueOf((String) requestJSON.get(REQUEST_STATUS));
                int householdSize = ((Number) requestJSON.get(REQUEST_HOUSEHOLD_SIZE)).intValue();
                String streetAddress = (String) requestJSON.get(REQUEST_STREET_ADDRESS);
                boolean isDuplicate = Boolean.TRUE.equals(requestJSON.get(REQUEST_IS_DUPLICATE));

                AidRequest request = new AidRequest(requestId, requester, category,
                        householdSize, streetAddress);
                request.updateStatus(status);
                if (isDuplicate) {
                    request.markDuplicate();
                }
                LocalDateTime createdAt = parseTime((String) requestJSON.get(REQUEST_CREATED_AT));
                if (createdAt != null) {
                    request.setCreatedAt(createdAt);
                }
                request.setAssignments(loadAssignments(request,
                        (JSONArray) requestJSON.get(REQUEST_ASSIGNMENTS), userDirectory));

                requests.add(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return requests;
    }

    /**
     * Loads a request's task assignments from JSON.
     * Users must already be loaded, since each assignment links to a volunteer and coordinator.
     *
     * @param request the request the assignments belong to
     * @param assignmentsJSON the saved assignments
     * @param userDirectory the directory used to look up volunteers and coordinators
     * @return the loaded assignments
     */
    private static ArrayList<TaskAssignment> loadAssignments(AidRequest request,
            JSONArray assignmentsJSON, UserDirectory userDirectory) {
        ArrayList<TaskAssignment> assignments = new ArrayList<>();
        for (Object assignmentObj : assignmentsJSON) {
            JSONObject assignmentJSON = (JSONObject) assignmentObj;

            TaskAssignment assignment = new TaskAssignment(
                    UUID.fromString((String) assignmentJSON.get("taskId")),
                    request,
                    findUser(userDirectory, (String) assignmentJSON.get("volunteer"), Volunteer.class),
                    findUser(userDirectory, (String) assignmentJSON.get("coordinator"), Coordinator.class));

            assignment.restoreState(
                    parseTime((String) assignmentJSON.get("assignedAt")),
                    parseTime((String) assignmentJSON.get("acceptedAt")),
                    parseTime((String) assignmentJSON.get("completedAt")),
                    TaskStatus.valueOf((String) assignmentJSON.get("status")));

            assignments.add(assignment);
        }
        return assignments;
    }

    /**
     * Looks up a user by ID and returns them only if they are the expected type.
     *
     * @param userDirectory the directory to search
     * @param idString the user's ID as a string, or null
     * @param userClass the expected user type, such as Volunteer.class
     * @return the user, or null if not found or not the expected type
     */
    private static <T extends User> T findUser(UserDirectory userDirectory, String idString,
                                               Class<T> userClass) {
        if (idString == null) {
            return null;
        }
        User user = userDirectory.getUserById(UUID.fromString(idString));
        return userClass.isInstance(user) ? userClass.cast(user) : null;
    }

    /**
     * Parses a saved time, or returns null if there is no time.
     *
     * @param text the saved time, such as 2026-10-18T14:30:00Z
     * @return the time, or null
     */
    private static LocalDateTime parseTime(String text) {
        return text == null ? null : OffsetDateTime.parse(text).toLocalDateTime();
    }

    /**
     * Loads the affected zip codes from the hurricane file.
     *
     * @return a list of affected zip codes
     */
    public static ArrayList<ZipCode> getAffectedZipCodes() {
        ArrayList<ZipCode> zipCodes = new ArrayList<ZipCode>();
        JSONObject hurricaneJSON = getHurricaneJSON();
        if (hurricaneJSON == null) {
            return zipCodes;
        }
        for (Object obj : (JSONArray) hurricaneJSON.get("affectedAreas")) {
            JSONObject areaJSON = (JSONObject) obj;
            zipCodes.add(new ZipCode((String) areaJSON.get("zipCode")));
        }
        return zipCodes;
    }

    /**
     * Loads the wind speed from the hurricane file.
     *
     * @return the wind speed, or 0 if it could not be loaded
     */
    public static int getWindSpeed() {
        JSONObject hurricaneJSON = getHurricaneJSON();
        if (hurricaneJSON == null) {
            return 0;
        }
        return ((Number) hurricaneJSON.get("windSpeed")).intValue();
    }

    /**
     * Loads the category from the hurricane file.
     *
     * @return the hurricane category, or null if it could not be loaded
     */
    public static HurricaneCategory getHurricaneCategory() {
        JSONObject hurricaneJSON = getHurricaneJSON();
        if (hurricaneJSON == null) {
            return null;
        }
        return HurricaneCategory.valueOf((String) hurricaneJSON.get("category"));
    }

    /**
     * Reads the hurricane file.
     *
     * @return the hurricane's JSON object, or null if reading failed
     */
    private static JSONObject getHurricaneJSON() {
        try (FileReader reader = new FileReader(HURRICANE_FILE)) {
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(reader);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}