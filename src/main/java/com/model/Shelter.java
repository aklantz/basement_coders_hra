package com.model;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import com.location.Address;
import com.location.Coordinates;
import com.location.MapTools;

/**
 * Represents an emergency shelter with capacity, occupancy,
 * location, medical availability, and a dog policy.
 *
 * @author Austin Lantz
 */
public class Shelter {

    private UUID resourceId;
    private String name;
    private int capacity;
    private int occupancy;
    private ShelterStatus status;
    private LocalDateTime lastUpdated;
    private ArrayList<InventoryItem> inventoryItems;
    private String zipCode;
    private String streetAddress;
    private String city;
    private String state;
    private Double latitude;
    private Double longitude;

    private ArrayList<String> occupants;
    private ArrayList<String> pets;

    private boolean medicalStaff;
    private boolean dogsAllowed;

    /**
     * Creates a shelter and sets its status based on occupancy.
     * Medical availability and permission for dogs default to false.
     *
     * @param resourceId the shelter's unique id
     * @param name the shelter's name
     * @param capacity the maximum number of people
     * @param occupancy the current number of people
     */
    public Shelter(UUID resourceId, String name, int capacity, int occupancy) {
        this.resourceId = resourceId;
        this.name = name;
        this.capacity = Math.max(0, capacity);
        this.occupancy = Math.max(0, occupancy);
        this.lastUpdated = LocalDateTime.now();
        this.inventoryItems = new ArrayList<>();
        this.occupants = new ArrayList<>();
        this.pets = new ArrayList<>();

        refreshStatus();
    }

    /**
     * Restores a shelter's saved capacity, occupancy, status, and timestamp.
     * Other information can be restored using setters.
     *
     * @param resourceId the shelter's unique id
     * @param name the shelter's name
     * @param capacity the maximum number of people
     * @param occupancy the current number of people
     * @param status the saved shelter status
     * @param lastUpdated the saved modification time
     */
    public Shelter(UUID resourceId, String name, int capacity, int occupancy,
                   ShelterStatus status, LocalDateTime lastUpdated) {
        this.resourceId = resourceId;
        this.name = name;
        this.capacity = Math.max(0, capacity);
        this.occupancy = Math.max(0, occupancy);
        this.status = status;
        this.lastUpdated = lastUpdated;
        this.inventoryItems = new ArrayList<>();
        this.occupants = new ArrayList<>();
        this.pets = new ArrayList<>();
    }

    /**
     * Gets the shelter's remaining capacity.
     *
     * @return how many additional people can fit
     */
    public int getRemainingCapacity() {
        return Math.max(0, capacity - occupancy);
    }

    /**
     * Changes the shelter's status.
     *
     * @param status the new status
     */
    public void updateStatus(ShelterStatus status) {
        this.status = status;
        touch();
    }

    /**
     * Changes capacity and refreshes the shelter's status.
     *
     * @param capacity the new capacity; negative values are ignored
     */
    public void updateCapacity(int capacity) {
        if (capacity < 0) {
            return;
        }

        this.capacity = capacity;
        refreshStatus();
        touch();
    }

    /**
     * Changes occupancy and refreshes the shelter's status.
     *
     * @param occupancy the new occupancy; negative values are ignored
     */
    public void updateOccupancy(int occupancy) {
        if (occupancy < 0) {
            return;
        }

        this.occupancy = occupancy;
        refreshStatus();
        touch();
    }

    /**
     * Sets the status to FULL or OPEN based on occupancy.
     * A closed shelter stays closed.
     */
    private void refreshStatus() {
        if (status == ShelterStatus.CLOSED) {
            return;
        }

        status = occupancy >= capacity
                ? ShelterStatus.FULL
                : ShelterStatus.OPEN;
    }

    /**
     * Records the current time as the last modification time.
     */
    private void touch() {
        lastUpdated = LocalDateTime.now();
    }

    /**
     * @return the shelter's unique id
     */
    public UUID getResourceId() {
        return resourceId;
    }

    /**
     * @return the shelter's name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the maximum number of people
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * @return the current number of people
     */
    public int getOccupancy() {
        return occupancy;
    }

    /**
     * @return the shelter's status
     */
    public ShelterStatus getStatus() {
        return status;
    }

    /**
     * @return the last modification time
     */
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    /**
     * @return the shelter's inventory items
     */
    public ArrayList<InventoryItem> getInventoryItems() {
        return inventoryItems;
    }

    /**
     * Replaces the inventory list.
     *
     * @param inventoryItems the inventory items to use
     */
    public void setInventoryItems(ArrayList<InventoryItem> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }

    /**
     * @return the shelter's zip code
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * Sets the shelter's zip code.
     *
     * @param zipCode the zip code, such as 29708
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * Sets the shelter's street address, city, and state.
     *
     * @param streetAddress the street, such as 7725 Tega Cay Dr
     * @param city the city
     * @param state the state
     */
    public void setAddress(String streetAddress, String city, String state) {
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
    }

    /**
     * @return the shelter's street address
     */
    public String getStreetAddress() {
        return streetAddress;
    }

    /**
     * @return the shelter's city
     */
    public String getCity() {
        return city;
    }

    /**
     * @return the shelter's state
     */
    public String getState() {
        return state;
    }

    /**
     * Checks whether all address fields are present.
     *
     * @return true if no address field is null
     */
    public boolean hasAddress() {
        return streetAddress != null
                && city != null
                && state != null
                && zipCode != null;
    }

    /**
     * Gets the shelter's address for the map tools.
     *
     * @return the address, or null if an address field is missing
     */
    public Address getAddress() {
        if (!hasAddress()) {
            return null;
        }

        return new Address(streetAddress, city, state, zipCode);
    }

    /**
     * Gets the shelter's coordinates, looking them up if not already cached.
     * Cached coordinates are available to the storage code through getters.
     *
     * @return the coordinates, or null if an address is needed but missing
     * @throws IOException if the map lookup fails
     * @throws InterruptedException if the map request is interrupted
     */
    public Coordinates getCoordinates()
            throws IOException, InterruptedException {
        if (latitude == null || longitude == null) {
            if (!hasAddress()) {
                return null;
            }

            Coordinates found = MapTools.getInstance()
                    .getCoordinates(getAddress());

            setCoordinates(found.getLatitude(), found.getLongitude());
        }

        return new Coordinates(latitude, longitude);
    }

    /**
     * @return the saved latitude, or null if unknown
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * @return the saved longitude, or null if unknown
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Sets the cached coordinates.
     *
     * @param latitude the latitude
     * @param longitude the longitude
     */
    public void setCoordinates(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Checks whether medical staff are available.
     *
     * @return true if medical staff are available
     */
    public boolean hasMedicalStaff() {
        return medicalStaff;
    }

    /**
     * Sets whether medical staff are available.
     *
     * @param medicalStaff whether medical staff are available
     */
    public void setMedicalStaff(boolean medicalStaff) {
        this.medicalStaff = medicalStaff;
        touch();
    }

    /**
     * Checks whether the shelter allows dogs.
     *
     * @return true if dogs are allowed
     */
    public boolean acceptsDogs() {
        return dogsAllowed;
    }

    /**
     * Sets whether the shelter allows dogs.
     *
     * @param dogsAllowed whether dogs are allowed
     */
    public void setDogsAllowed(boolean dogsAllowed) {
        this.dogsAllowed = dogsAllowed;
        touch();
    }

    /**
     * Checks people and their pets into the shelter.
     * Only people count toward occupancy.
     *
     * @param people the names of the people checking in
     * @param newPets the pets coming with them
     * @return true if checked in, false if the people list is null,
     *         the shelter is closed, space is insufficient,
     *         or a person is already in the occupants list
     */
    public boolean checkIn(ArrayList<String> people,
                           ArrayList<String> newPets) {
        if (people == null || status == ShelterStatus.CLOSED
                || people.size() > getRemainingCapacity()) {
            return false;
        }

        for (String person : people) {
            if (occupants.contains(person)) {
                return false;
            }
        }

        for (String person : people) {
            occupants.add(person);
        }

        if (newPets != null) {
            for (String pet : newPets) {
                pets.add(pet);
            }
        }

        updateOccupancy(occupancy + people.size());
        return true;
    }

    /**
     * @return the names of the people staying at the shelter
     */
    public ArrayList<String> getOccupants() {
        return occupants;
    }

    /**
     * Replaces the occupants list without changing occupancy.
     *
     * @param occupants the occupants list
     */
    public void setOccupants(ArrayList<String> occupants) {
        this.occupants = occupants;
    }

    /**
     * @return the pets staying at the shelter
     */
    public ArrayList<String> getPets() {
        return pets;
    }

    /**
     * Replaces the pets list.
     *
     * @param pets the pets list
     */
    public void setPets(ArrayList<String> pets) {
        this.pets = pets;
    }

    /**
     * Gets a summary of the shelter's availability.
     *
     * @return the shelter summary
     */
    @Override
    public String toString() {
        return name + " | Status: " + status
                + " | Occupancy: " + occupancy + "/" + capacity
                + " | Spots left: " + getRemainingCapacity();
    }
}