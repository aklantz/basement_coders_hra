package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

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

    @Override
    public String toString() {
        return name + " | Status: " + status
                + " | Occupancy: " + occupancy + "/" + capacity
                + " | Spots left: " + getRemainingCapacity();
    }
}