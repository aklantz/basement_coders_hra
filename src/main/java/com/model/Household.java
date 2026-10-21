package com.model;

import java.util.UUID;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a named household owned by a user and tracks each member's safety
 * status and most recent check-in time.
 */
public class Household {

    private HashMap<String, SafetyStatus> memberStatuses;
    private HashMap<String, LocalDateTime> memberCheckInTimes;
    private String householdName;
    private UUID householdOwnerUUID;

    /**
     * Creates an empty household with the given owner and name.
     *
     * @param householdOwnerUUID the owner's unique identifier
     * @param householdName the name of the household
     */
    public Household(UUID householdOwnerUUID, String householdName) {
        this.householdName = householdName;
        this.householdOwnerUUID = householdOwnerUUID;
        this.memberStatuses = new HashMap<>();
        this.memberCheckInTimes = new HashMap<>();
    }

    /**
     * Adds a member with an unknown safety status and the current check-in time.
     * Adding an existing name resets that member's status and check-in time.
     *
     * @param memberName the member's name
     * @return true after the member's information is stored
     */
    public boolean addHouseholdMember(String memberName) {
        memberStatuses.put(memberName, SafetyStatus.UNKNOWN);
        memberCheckInTimes.put(memberName, LocalDateTime.now());
        return true;
    }

    /**
     * Returns a copy of the member names in no guaranteed order.
     *
     * @return a new list containing the household's member names
     */
    public ArrayList<String> getMembers() {
        ArrayList<String> members = new ArrayList<>(memberStatuses.keySet());
        return members;
    }

    /**
     * Returns the stored safety status for a member.
     *
     * @param memberName the member's name
     * @return the stored status, or null if no status is recorded
     */
    public SafetyStatus getSafetyStatus(String memberName) {
        return memberStatuses.get(memberName);
    }

    /**
     * Returns the household's name.
     *
     * @return the household name
     */
    public String getHouseholdName() {
        return householdName;
    }

    /**
     * Returns the most recent check-in time recorded for a member.
     *
     * @param memberName the member's name
     * @return the recorded time, or null if the member has no recorded check-in
     */
    public LocalDateTime getLastCheckIn(String memberName) {
        return memberCheckInTimes.get(memberName);
    }

    /**
     * Records a member's safety status and sets their check-in time to now.
     * If the name is not already present, this also adds the member.
     *
     * @param memberName the member's name
     * @param safetyStatus the safety status to record
     * @return true after the check-in is stored
     */
    public boolean checkIn(String memberName, SafetyStatus safetyStatus) {
        memberStatuses.put(memberName, safetyStatus);
        memberCheckInTimes.put(memberName, LocalDateTime.now());
        return true;
    }

    /**
     * Returns the household owner's unique identifier.
     *
     * @return the owner's identifier
     */
    public UUID getHouseholdOwnerUUID() {
        return householdOwnerUUID;
    }
}
