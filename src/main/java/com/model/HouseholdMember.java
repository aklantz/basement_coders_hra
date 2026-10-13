package com.model;

import java.util.UUID;
import java.time.LocalDateTime;

public class HouseholdMember {

    private UUID memberUUID;
    private String name;
    private LocalDateTime lastCheckIn;
    private UUID householdOwnerUUID;
    private SafetyStatus safetyStatus;

    public HouseholdMember(UUID memberUUID) {
        this.memberUUID = memberUUID;
    }

    public UUID getMemberUUID() {
        return memberUUID;
    }

    public SafetyStatus getSafetyStatus() {
        return safetyStatus;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getLastCheckIn() {
        return lastCheckIn;
    }

    public boolean checkIn(SafetyStatus safetyStatus) {
        this.safetyStatus = safetyStatus;
        this.lastCheckIn = LocalDateTime.now();
        return true;
    }

    public UUID getHouseholdOwnerUUID() {
        return householdOwnerUUID;
    }
}
