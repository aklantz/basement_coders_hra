package com.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Saves data to the JSON files.
 */
public class DataWriter extends DataConstants {

    /**
     * Saves the given users to the users file.
     *
     * @param users the users to save
     * @return true if the users were saved, or false if saving failed
     */
    public static boolean saveUsers(ArrayList<User> users) {
        try {
            JSONArray usersJSON = new JSONArray();
            for (User user : users) {
                usersJSON.add(getUserJSON(user));
            }
            return writeFile(USER_FILE, usersJSON);
        } catch (RuntimeException exception) {
            // return reportFailure(USER_FILE, exception);
            return false;
        }
    }

    /**
     * Saves the given aid requests to the aid requests file.
     *
     * @param requests the aid requests to save
     * @return true if the requests were saved, or false if saving failed
     */
    public static boolean saveAidRequests(ArrayList<AidRequest> requests) {
        try {
            JSONArray requestsJSON = new JSONArray();
            for (AidRequest request : requests) {
                requestsJSON.add(getAidRequestJSON(request));
            }
            return writeFile(AID_REQUEST_FILE, requestsJSON);
        } catch (RuntimeException exception) {
            // return reportFailure(AID_REQUEST_FILE, exception);
            return false;
        }
    }

    /**
     * Saves the given shelters to the shelters file.
     *
     * @param shelters the shelters to save
     * @return true if the shelters were saved, or false if saving failed
     */
    public static boolean saveResources(ArrayList<Shelter> shelters) {
        try {
            return cacheShelters(shelters);
        } catch (RuntimeException exception) {
            // return reportFailure(RESOURCE_FILE, exception);
            return false;
        }
    }

    /**
     * Saves the given shelters to the shelters file.
     *
     * @param shelters the shelters to cache
     * @return true if the shelters were saved, or false if saving failed
     */
    public static boolean cacheShelters(ArrayList<Shelter> shelters) {
        if (shelters == null) {
            // return reportFailure(RESOURCE_FILE,
            //         new IllegalArgumentException("The shelter list cannot be null."));
            return false;
        }
        try {
            JSONArray sheltersJSON = new JSONArray();
            for (Shelter shelter : shelters) {
                sheltersJSON.add(getShelterJSON(shelter));
            }
            return writeFile(RESOURCE_FILE, sheltersJSON);
        } catch (RuntimeException exception) {
            // return reportFailure(RESOURCE_FILE, exception);
            return false;
        }
    }

    /**
     * Builds one JSON object from a user.
     *
     * @param user the user to save
     * @return the user's JSON object
     */
    private static JSONObject getUserJSON(User user) {
        JSONObject userJSON = new JSONObject();
        userJSON.put(USER_ID, user.getUserId().toString());
        userJSON.put(USER_USERNAME, user.getUsername());
        userJSON.put(USER_PASSWORD, user.getPassword());
        userJSON.put(USER_FULL_NAME, user.getFullName());
        userJSON.put(USER_DOB, user.getDateOfBirth().toString());
        userJSON.put(USER_ADDRESS, user.getHomeAddress());
        userJSON.put(USER_EMAIL, user.getEmailAddress());
        userJSON.put(USER_PHONE, user.getPhoneNumber());
        userJSON.put(USER_TYPE, user.getType().name());
        userJSON.put(USER_ACCOUNT_STATUS, user.getAccountStatus().name());
        userJSON.put(USER_SAFETY_STATUS, user.getSafetyStatus().name());

        // saving what shelter a shelter manager runs
        if (user instanceof ShelterManager) {
            ShelterManager manager = (ShelterManager) user;
            if (manager.getShelterId() != null) {
                userJSON.put(USER_SHELTER_ID, manager.getShelterId().toString());
            }
        }

        // saving a volunteer's specialty
        if (user instanceof Volunteer) {
            Volunteer volunteer = (Volunteer) user;
            if (volunteer.getSpecialty() != null) {
                userJSON.put(USER_SPECIALTY, volunteer.getSpecialty().name());
            }
        }

        JSONArray householdsJSON = new JSONArray();
        for (Household household : user.getHouseholds()) {
            JSONObject householdJSON = new JSONObject();
            householdJSON.put("householdName", household.getHouseholdName());
            householdJSON.put("householdOwnerUUID", household.getHouseholdOwnerUUID().toString());

            JSONArray membersJSON = new JSONArray();
            for (HouseholdMember member : household.getMembers()) {
                JSONObject memberJSON = new JSONObject();
                memberJSON.put("name", member.getName());
                memberJSON.put("safetyStatus", member.getSafetyStatus().name());
                memberJSON.put(MEMBER_AGE, member.getAge());
                memberJSON.put(MEMBER_PET_TYPE, member.getPetType());
                memberJSON.put(MEMBER_MEDICAL_NOTE, member.getMedicalNote());
                membersJSON.add(memberJSON);
            }
            householdJSON.put("members", membersJSON);
            householdsJSON.add(householdJSON);
        }
        userJSON.put("households", householdsJSON);
        return userJSON;
    }

    /**
     * Builds one JSON object from an aid request.
     *
     * @param request the aid request to save
     * @return the request's JSON object
     */
    private static JSONObject getAidRequestJSON(AidRequest request) {
        JSONObject requestJSON = new JSONObject();
        requestJSON.put("requestId", request.getRequestId().toString());
        requestJSON.put("requester", request.getRequester().getUserId().toString());
        requestJSON.put("category", request.getCategory().name());
        requestJSON.put("status", request.getStatus().name());
        requestJSON.put("householdSize", request.getHouseholdSize());
        requestJSON.put("streetAddress", request.getStreetAddress());
        requestJSON.put("isDuplicate", request.isDuplicate());
        requestJSON.put(REQUEST_CREATED_AT, formatTime(request.getCreatedAt()));
        requestJSON.put(REQUEST_NOTE, request.getNote());
        if (request.getShelterId() != null) {
            requestJSON.put(REQUEST_SHELTER_ID, request.getShelterId().toString());
        }

        JSONArray assignmentsJSON = new JSONArray();
        for (TaskAssignment assignment : request.getAssignments()) {
            JSONObject assignmentJSON = new JSONObject();
            assignmentJSON.put("taskId", assignment.getTaskId().toString());
            assignmentJSON.put("volunteer", assignment.getVolunteer() == null
                    ? null : assignment.getVolunteer().getUserId().toString());
            assignmentJSON.put("coordinator", assignment.getCoordinator() == null
                    ? null : assignment.getCoordinator().getUserId().toString());
            assignmentJSON.put("assignedAt", formatTime(assignment.getAssignedAt()));
            assignmentJSON.put("acceptedAt", formatTime(assignment.getAcceptedAt()));
            assignmentJSON.put("completedAt", formatTime(assignment.getCompletedAt()));
            assignmentJSON.put("status", assignment.getStatus().name());
            assignmentsJSON.add(assignmentJSON);
        }
        requestJSON.put("assignments", assignmentsJSON);
        return requestJSON;
    }

    /**
     * Formats a time the same way other times are saved, or null if there is no time.
     *
     * @param time the time to format
     * @return the formatted time, or null
     */
    private static String formatTime(LocalDateTime time) {
        return time == null ? null : time.atOffset(ZoneOffset.UTC).toString();
    }

    /**
     * Builds one JSON object from a shelter.
     *
     * @param shelter the shelter to save
     * @return the shelter's JSON object
     */
    private static JSONObject getShelterJSON(Shelter shelter) {

        JSONObject shelterJSON = new JSONObject();

        shelterJSON.put(
                SHELTER_ID,
                shelter.getResourceId().toString());

        shelterJSON.put(
                SHELTER_NAME,
                shelter.getName());

        shelterJSON.put(
                SHELTER_CAPACITY,
                shelter.getCapacity());

        shelterJSON.put(
                SHELTER_OCCUPANCY,
                shelter.getOccupancy());

        shelterJSON.put(
                SHELTER_STATUS,
                shelter.getStatus().name());

        shelterJSON.put(
                SHELTER_ZIP_CODE,
                shelter.getZipCode());

        shelterJSON.put(SHELTER_STREET_ADDRESS, shelter.getStreetAddress());
        shelterJSON.put(SHELTER_CITY, shelter.getCity());
        shelterJSON.put(SHELTER_STATE, shelter.getState());
        shelterJSON.put(SHELTER_LATITUDE, shelter.getLatitude());
        shelterJSON.put(SHELTER_LONGITUDE, shelter.getLongitude());
        shelterJSON.put(SHELTER_MEDICAL_STAFF, shelter.hasMedicalStaff());
        shelterJSON.put(SHELTER_DOGS_ALLOWED, shelter.acceptsDogs());

        shelterJSON.put(
                SHELTER_LAST_UPDATED,
                shelter.getLastUpdated()
                        .atOffset(ZoneOffset.UTC)
                        .toString());

        JSONArray inventoryJSON =
                new JSONArray();

        for (InventoryItem item :
                shelter.getInventoryItems()) {

            JSONObject itemJSON =
                    new JSONObject();

            itemJSON.put(
                    "itemId",
                    item.getItemId().toString());

            itemJSON.put(
                    "supplyType",
                    item.getSupplyType());

            itemJSON.put(
                    "quantity",
                    item.getQuantity());

            itemJSON.put(
                    "reorderThreshold",
                    item.getReorderThreshold());

            inventoryJSON.add(itemJSON);
        }

        shelterJSON.put(
                "inventoryItems",
                inventoryJSON);

        // save the people and pets staying at the shelter

        JSONArray occupantsJSON = new JSONArray();
        for (String person : shelter.getOccupants()) {
            occupantsJSON.add(person);
        }
        shelterJSON.put(SHELTER_OCCUPANTS, occupantsJSON);

        JSONArray petsJSON = new JSONArray();
        for (String pet : shelter.getPets()) {
            petsJSON.add(pet);
        }
        shelterJSON.put(SHELTER_PETS, petsJSON);

        return shelterJSON;
    }

    /**
     * Writes JSON to a temporary file before replacing the saved file.
     *
     * @param fileName the file to save
     * @param data the JSON array to write
     * @return true if the file was saved, or false if saving failed
     */
    private static boolean writeFile(String fileName, JSONArray data) {
        Path temporaryFile = null;
        try {
            Path destination = Paths.get(fileName).toAbsolutePath();
            Files.createDirectories(destination.getParent());
            temporaryFile = Files.createTempFile(destination.getParent(), "relief-", ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(
                    temporaryFile, StandardCharsets.UTF_8)) {
                writer.write(data.toJSONString());
                writer.newLine();
            }
            try {
                Files.move(temporaryFile, destination,
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException | RuntimeException exception) {
            // return reportFailure(fileName, exception);
            return false;
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException | RuntimeException exception) {
                    System.err.println("Unable to remove the temporary save file.");
                }
            }
        }
    }

    private static String getFileWritingPath(String PATH_NAME, String JUNIT_PATH_NAME) {
		try {
			if(isJUnitTest()){
				URI url = DataWriter.class.getResource(JUNIT_PATH_NAME).toURI();
				return url.getPath();
			} else {
				return PATH_NAME;
			}
		} catch(Exception e){
			System.out.println("Difficulty getting resource path");
			return "";
		}
	}

    /**
     * Prints an error when saving fails.
     *
     * @param fileName the file that could not be saved
     * @param exception the error that caused the failure
     * @return false to indicate that saving failed
     */
    // Used only for testing.
    // private static boolean reportFailure(String fileName, Exception exception) {
    //     System.err.println("Unable to save " + fileName + " ("
    //             + exception.getClass().getSimpleName() + ").");
    //     return false;
    // }
}