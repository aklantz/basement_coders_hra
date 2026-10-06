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
     * Loads all shelters from the shelters file.
     *
     * @return a list of shelters
     */
    public static ArrayList<Shelter> getCachedShelters() {
        ArrayList<Shelter> shelters = new ArrayList<Shelter>();

        try (FileReader reader = new FileReader(SHELTER_FILE)) {
            JSONParser parser = new JSONParser();
            JSONArray sheltersJSON = (JSONArray) parser.parse(reader);

            for (Object obj : sheltersJSON) {
                shelters.add(getShelter((JSONObject) obj));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return shelters;
    }

    /**
     * Builds one shelter from its JSON object.
     *
     * @param shelterJSON 
     * @return the shelter
     */
    private static Shelter getShelter(JSONObject shelterJSON) {
        UUID id = UUID.fromString((String) shelterJSON.get(SHELTER_ID));
        String name = (String) shelterJSON.get(SHELTER_NAME);
        int capacity = ((Number) shelterJSON.get(SHELTER_CAPACITY)).intValue();
        int occupancy = ((Number) shelterJSON.get(SHELTER_OCCUPANCY)).intValue();
        ShelterStatus status = ShelterStatus.valueOf((String) shelterJSON.get(SHELTER_STATUS));

        LocalDateTime lastUpdated = OffsetDateTime.parse(
            (String) shelterJSON.get(SHELTER_LAST_UPDATED)
        ).toLocalDateTime();

        return new Shelter(id, name, capacity, occupancy, status, lastUpdated);
    }

    /**
     * Loads all users from the users file.
     *
     * @return a list of users
     */
    public static ArrayList<User> getUsers() {
        ArrayList<User> users = new ArrayList<User>();

        try (FileReader reader = new FileReader(USER_FILE)) {
            JSONParser parser = new JSONParser();
            JSONArray usersJSON = (JSONArray) parser.parse(reader);

            for (Object obj : usersJSON) {
                users.add(getUser((JSONObject) obj));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    /**
     * Builds one user from its JSON object.
     *
     * @param userJSON 
     * @return the user
     */
    private static User getUser(JSONObject userJSON) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        UUID id = UUID.fromString((String) userJSON.get(USER_ID));
        String username = (String) userJSON.get(USER_USERNAME);
        String password = (String) userJSON.get(USER_PASSWORD);
        String fullName = (String) userJSON.get(USER_FULL_NAME);
        Date dateOfBirth = dateFormat.parse((String) userJSON.get(USER_DOB));
        String address = (String) userJSON.get(USER_ADDRESS);
        String email = (String) userJSON.get(USER_EMAIL);
        String phone = (String) userJSON.get(USER_PHONE);

        User user = new User(id, username, password, fullName,
                dateOfBirth, address, email, phone, username);

        user.setType(USERTYPE.valueOf((String) userJSON.get(USER_TYPE)));
        user.setAccountStatus(AccountStatus.valueOf((String) userJSON.get(USER_ACCOUNT_STATUS)));
        user.setSafetyStatus(SafetyStatus.valueOf((String) userJSON.get(USER_SAFETY_STATUS)));

        return user;
    }
    

     /**
     * Tests the loader by printing all shelters and users.
     */
    public static void main(String[] args) {
        ArrayList<Shelter> shelters = getCachedShelters();

        for (Shelter shelter : shelters) {
            System.out.println(shelter);
        }

        ArrayList<User> users = getUsers();

        for (User user : users) {
            System.out.println(user.getUsername() + ", " + user.getFullName());
        }
    }
}