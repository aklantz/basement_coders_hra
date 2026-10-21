package com.model;

import java.util.UUID;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Represents a named household owned by a user and tracks each member's safety
 * status and most recent check-in time.
 */
public class Household {

    private String householdName;
    private UUID householdOwnerUUID;
    private ArrayList<HouseholdMember> householdMembers;

    /**
     * Creates an empty household with the given owner and name.
     *
     * @param householdOwnerUUID the owner's UUID
     * @param householdName the name of the household
     */
    public Household(UUID householdOwnerUUID, String householdName) {
        this.householdName = householdName;
        this.householdOwnerUUID = householdOwnerUUID;
        this.householdMembers = new ArrayList<HouseholdMember>();
    }

    /**
     * Adds a member with an unknown safety status and the current check-in time.     *
     * @param memberName the member's name
     * @return true after the member's information is stored
     */
    public boolean addHouseholdMember(String memberName) {
        householdMembers.add(new HouseholdMember(memberName));
        return true;
    }

    /**
     * Returns the members.
     *
     * @return an arraylist of HouseholdMembers
     */
    public ArrayList<HouseholdMember> getMembers() {
        return householdMembers;
    }

    /**
     * Returns the stored safety status for a member.
     *
     * @param memberName the member's name
     * @return the stored status, or null if no status is recorded
     */
    public SafetyStatus getSafetyStatus(String memberName) {
        for(HouseholdMember member : householdMembers) {
            if(memberName.equals(member.getName())) {
                return member.getSafetyStatus();
            }
        }
        return null;
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
        for(HouseholdMember member : householdMembers) {
            if(memberName.equals(member.getName())) {
                return member.getLastCheckIn();
            }
        }
        return null;
    }

    /**
     * Records a member's safety status and sets their check-in time to now.
     *      *
     * @param memberName the member's name
     * @param safetyStatus the safety status to record
     * @return true after the check-in is stored
     */
    public boolean checkIn(String memberName, SafetyStatus safetyStatus) {
        for(HouseholdMember member : householdMembers) {
            if(memberName.equals(member.getName())) {
                member.checkIn(safetyStatus);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the household owner's unique identifier.
     *
     * @return the owner's UUID
     */
    public UUID getHouseholdOwnerUUID() {
        return householdOwnerUUID;
    }
}
