package com.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
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
            // return reportFailure(SHELTER_FILE, exception);
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
            // return reportFailure(SHELTER_FILE,
            //         new IllegalArgumentException("The shelter list cannot be null."));
            return false;
        }
        try {
            JSONArray sheltersJSON = new JSONArray();
            for (Shelter shelter : shelters) {
                sheltersJSON.add(getShelterJSON(shelter));
            }
            return writeFile(SHELTER_FILE, sheltersJSON);
        } catch (RuntimeException exception) {
            // return reportFailure(SHELTER_FILE, exception);
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
        userJSON.put(USER_PASSWORD, user.getPasswordHash());
        userJSON.put(USER_FULL_NAME, user.getFullName());
        userJSON.put(USER_DOB, new SimpleDateFormat("yyyy-MM-dd").format(user.getDateOfBirth()));
        userJSON.put(USER_ADDRESS, user.getHomeAddress());
        userJSON.put(USER_EMAIL, user.getEmailAddress());
        userJSON.put(USER_PHONE, user.getPhoneNumber());
        userJSON.put(USER_TYPE, user.getType().name());
        userJSON.put(USER_ACCOUNT_STATUS, user.getAccountStatus().name());
        userJSON.put(USER_SAFETY_STATUS, user.getSafetyStatus().name());

        JSONArray membersJSON = new JSONArray();
        for (HouseholdMember member : user.getHouseHoldMembers()) {
            membersJSON.add(member.getMemberUUID().toString());
        }
        userJSON.put("houseHoldMembers", membersJSON);
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
        requestJSON.put("urgency", request.getUrgency().name());
        requestJSON.put("status", request.getStatus().name());
        requestJSON.put("householdSize", request.getHouseholdSize());
        requestJSON.put("streetAddress", request.getStreetAddress());
        requestJSON.put("isDuplicate", request.isDuplicate());

        JSONArray assignmentsJSON = new JSONArray();
        for (TaskAssignment assignment : request.getAssignments()) {
            assignmentsJSON.add(assignment.getTaskId().toString());
        }
        requestJSON.put("assignments", assignmentsJSON);

        JSONArray historyJSON = new JSONArray();
        for (StatusChange change : request.getStatusHistory()) {
            JSONObject changeJSON = new JSONObject();
            changeJSON.put("status", change.getStatus().name());
            changeJSON.put("changedAt", change.getChangedAt()
                    .atOffset(ZoneOffset.UTC).toString());
            historyJSON.add(changeJSON);
        }
        requestJSON.put("statusHistory", historyJSON);
        return requestJSON;
    }

    /**
     * Builds one JSON object from a shelter.
     *
     * @param shelter the shelter to save
     * @return the shelter's JSON object
     */
    private static JSONObject getShelterJSON(Shelter shelter) {
        JSONObject shelterJSON = new JSONObject();
        shelterJSON.put(SHELTER_ID, shelter.getResourceId().toString());
        shelterJSON.put(SHELTER_NAME, shelter.getName());
        shelterJSON.put(SHELTER_CAPACITY, shelter.getCapacity());
        shelterJSON.put(SHELTER_OCCUPANCY, shelter.getOccupancy());
        shelterJSON.put(SHELTER_STATUS, shelter.getStatus().name());
        shelterJSON.put(SHELTER_LAST_UPDATED, shelter.getLastUpdated()
                .atOffset(ZoneOffset.UTC).toString());
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
