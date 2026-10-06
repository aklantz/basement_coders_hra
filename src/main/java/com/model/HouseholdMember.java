package com.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class HouseholdMember {
    private UUID memberUUID;
    private String name;
    private LocalDateTime lastCheckIn;
    private UUID householdOwnerUUID;
    private SafetyStatus safetyStatus;

    public SafetyStatus getSafetyStatus() {
        return safetyStatus;
    }
}