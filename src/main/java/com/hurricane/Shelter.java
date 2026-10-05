package com.hurricane;

import java.time.LocalDateTime;
import java.util.UUID;

public class Shelter {

    private UUID resourceId;
    private String name;
    private int capacity;
    private int occupancy;
    private ShelterStatus status;
    private LocalDateTime lastUpdated;

    public Shelter(UUID resourceId, String name, int capacity, int occupancy) {
        this.resourceId = resourceId;
        this.name = name;
        this.capacity = capacity;
        this.occupancy = occupancy;
        this.status = ShelterStatus.OPEN;
        this.lastUpdated = LocalDateTime.now();
    }

    public int getRemainingCapacity() {
        return 0;
    }

    public void updateStatus(ShelterStatus status) {
    }

    public void updateCapacity(int capacity) {
    }

    public void updateOccupancy(int occupancy) {
    }
}