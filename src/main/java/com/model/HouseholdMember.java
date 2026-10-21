package com.model;

import java.time.LocalDateTime;

/**
 * Represents a household member with a name, safety status, and most recent
 * check-in time.
 */
public class HouseholdMember {
    
    private String name;
    private LocalDateTime lastCheckInTime;
    private SafetyStatus safetyStatus;

    /**
     * Creates a member with an unknown safety status and the current local time
     * as the initial check-in time.
     *
     * @param name the member's name
     */
    public HouseholdMember(String name) {
        this.name = name;
        this.lastCheckInTime = LocalDateTime.now();
        this.safetyStatus = SafetyStatus.UNKNOWN;
    }

    /**
     * Records the supplied safety status and updates the check-in time to now.
     *
     * @param safetyStatus the status to record
     */
    public void checkIn(SafetyStatus safetyStatus) {
        this.lastCheckInTime = LocalDateTime.now();
        this.safetyStatus = safetyStatus;
    }

    /**
     * Returns the member's most recently recorded safety status.
     *
     * @return the recorded status
     */
    public SafetyStatus getSafetyStatus() {
        return safetyStatus;
    }

    /**
     * Returns the time the member was created or most recently checked in.
     *
     * @return the most recent check-in time in local time
     */
    public LocalDateTime getLastCheckIn() {
        return lastCheckInTime;
    }

    /**
     * Returns the member's name.
     *
     * @return the name of the member
     */
    public String getName() {
        return name;
    }
}
