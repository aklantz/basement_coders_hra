package com.model;

import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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