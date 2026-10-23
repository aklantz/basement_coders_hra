package com.model;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import com.location.Address;
import com.location.Coordinates;
import com.location.MapTools;

/**
 * Represents an emergency shelter with capacity, occupancy, and status.
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

    // names of the people staying and what pets are with them
    private ArrayList<String> occupants;
    private ArrayList<String> pets;

    /**
     * Creates a new shelter and sets status based on occupancy.
     *
     * @param resourceId unique id
     * @param name shelter name
     * @param capacity max people
     * @param occupancy current people
     */
    public Shelter(UUID resourceId, String name, int capacity, int occupancy) {
        this.resourceId = resourceId;
        this.name = name;

        // don't allow negatives
        this.capacity = Math.max(0, capacity);
        this.occupancy = Math.max(0, occupancy);

        this.lastUpdated = LocalDateTime.now();
        this.inventoryItems = new ArrayList<>();
        this.occupants = new ArrayList<>();
        this.pets = new ArrayList<>();

        refreshStatus();
    }

    /**
     * Used when loading a shelter from storage.
     *
     * @param resourceId unique id
     * @param name shelter name
     * @param capacity max people
     * @param occupancy current people
     * @param status OPEN, FULL, or CLOSED
     * @param lastUpdated timestamp from when it was saved
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
     * @return how many more people can fit
     */
    public int getRemainingCapacity() {
        return Math.max(0, capacity - occupancy);
    }

    /**
     * @param status the new status
     */
    public void updateStatus(ShelterStatus status) {
        this.status = status;
        touch();
    }

    /**
     * @param capacity the new max
     */
    public void updateCapacity(int capacity) {
        if (capacity < 0) return;

        this.capacity = capacity;
        refreshStatus();
        touch();
    }

    /**
     * @param occupancy the new count
     */
    public void updateOccupancy(int occupancy) {
        if (occupancy < 0) return;

        this.occupancy = occupancy;
        refreshStatus();
        touch();
    }

    /**
     * Sets status to FULL or OPEN based on occupancy.
     * Does nothing if the shelter is already CLOSED.
     */
    private void refreshStatus() {
        if (status == ShelterStatus.CLOSED) return;

        status = (occupancy >= capacity)
                ? ShelterStatus.FULL
                : ShelterStatus.OPEN;
    }

    /**
     * Updates the last modified timestamp.
     */
    private void touch() {
        lastUpdated = LocalDateTime.now();
    }

    // getters
    //Come back to later
    public UUID getResourceId() {
        return resourceId;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getOccupancy() {
        return occupancy;
    }

    public ShelterStatus getStatus() {
        return status;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public ArrayList<InventoryItem> getInventoryItems() {
        return inventoryItems;
    }

    // setters

    public void setInventoryItems(ArrayList<InventoryItem> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }

    /**
     * @return the zip code the shelter is in
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * @param zipCode the zip code the shelter is in, such as 29708
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * Sets the shelter's street address
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

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    /**
     * @return true if the shelter has a full address the map can locatae
     */
    public boolean hasAddress() {
        return streetAddress != null && city != null && state != null && zipCode != null;
    }

    /**
     * @return the shelter's address for the map tools, or null if it doesn't have one
     */
    public Address getAddress() {
        if (!hasAddress()) {
            return null;
        }
        return new Address(streetAddress, city, state, zipCode);
    }

    /**
     * Gets the shelter's map coordinates. The first time they are looked up
     * with the map API, they are kept so that they are saved and never looked up again.
     *
     * @return the coordinates, or null if the shelter has no address
     */
    public Coordinates getCoordinates() throws IOException, InterruptedException {
        if (latitude == null || longitude == null) {
            if (!hasAddress()) {
                return null;
            }
            Coordinates found = MapTools.getInstance().getCoordinates(getAddress());
            setCoordinates(found.getLatitude(), found.getLongitude());
        }
        return new Coordinates(latitude, longitude);
    }

    /**
     * @return the saved latitude, or null - not looked up
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * @return the saved longitude, or null - not looked up
     */
    public Double getLongitude() {
        return longitude;
    }

    public void setCoordinates(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Checks people and also the pets into the shelter.
     *
     * @param people the names of the people checking in
     * @param newPets the pets coming with them
     * @return true if they were checked in, false if there isn't enough room
     *         or someone is already checked in
     */


    public boolean checkIn(ArrayList<String> people, ArrayList<String> newPets) {
        // can't check in if the shelter is closed or has no room
        if (people == null || status == ShelterStatus.CLOSED
                || people.size() > getRemainingCapacity()) {
            return false;
        }

        // does not check in anyone twice
        for (String person : people) {
            if (occupants.contains(person)) {
                return false;
            }
        }
        

        // add each person to the list
        for (String person : people) {
            occupants.add(person);
        }

        // add each pet to the list
        if (newPets != null) {
            for (String pet : newPets) {
                pets.add(pet);
            }
        }

        // only people count toward occupancy
        updateOccupancy(occupancy + people.size());
        return true;
    }

    public ArrayList<String> getOccupants() {
        return occupants;
    }

    public void setOccupants(ArrayList<String> occupants) {
        this.occupants = occupants;
    }

    public ArrayList<String> getPets() {
        return pets;
    }

    public void setPets(ArrayList<String> pets) {
        this.pets = pets;
    }


    @Override
    public String toString() {
        return name + " | Status: " + status
                + " | Occupancy: " + occupancy + "/" + capacity
                + " | Spots left: " + getRemainingCapacity();
    }
}