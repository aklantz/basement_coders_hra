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

                User user = null;
                if (type == USERTYPE.VOLUNTEER) {
                    user = new Volunteer(id, username, password, fullName,
                            dateOfBirth, address, email, phone, username);
                } else if (type == USERTYPE.COORDINATOR) {
                    user = new Coordinator(id, username, password, fullName,
                            dateOfBirth, address, email, phone, username);
                }

                if (user != null) {
                    user.setAccountStatus(AccountStatus.valueOf((String) userJSON.get(USER_ACCOUNT_STATUS)));
                    user.setSafetyStatus(SafetyStatus.valueOf((String) userJSON.get(USER_SAFETY_STATUS)));
                    users.add(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

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
                JSONObject shelterJSON = (JSONObject) obj;

                UUID id = UUID.fromString((String) shelterJSON.get(SHELTER_ID));
                String name = (String) shelterJSON.get(SHELTER_NAME);
                int capacity = ((Number) shelterJSON.get(SHELTER_CAPACITY)).intValue();
                int occupancy = ((Number) shelterJSON.get(SHELTER_OCCUPANCY)).intValue();
                ShelterStatus status = ShelterStatus.valueOf((String) shelterJSON.get(SHELTER_STATUS));

                LocalDateTime lastUpdated = OffsetDateTime.parse(
                    (String) shelterJSON.get(SHELTER_LAST_UPDATED)
                ).toLocalDateTime();

                shelters.add(new Shelter(id, name, capacity, occupancy, status, lastUpdated));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return shelters;
    }
}