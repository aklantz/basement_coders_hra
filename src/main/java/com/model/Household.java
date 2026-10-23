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
     * Adds a person with an unknown safety status and the current check-in time.
     * Their age is 0 and they have no medical need.
     *
     * @param memberName the member's name
     * @return true if the member was added, false if they were already in the household
     */
    public boolean addHouseholdMember(String memberName) {
        return addHouseholdMember(memberName, 0, null, null);
    }

    /**
     * Adds a person or pet, with details, with an unknown safety status and the current
     * check-in time.
     *
     * @param memberName the member's name
     * @param age the member's age (use 0 for a pet)
     * @param petType the kind of pet, such as "dog", or null for a person
     * @param medicalNote the medical assistance the member needs, or null if none
     * @return true if the member was added, false if the name is blank
     *         or they were already in the household
     */
    public boolean addHouseholdMember(String memberName, int age, String petType,
                                      String medicalNote) {
        if (memberName == null || memberName.trim().isEmpty()) {
            return false;
        }
        // don't add someone who is already in the household
        for (HouseholdMember member : householdMembers) {
            if (member.getName().equals(memberName)) {
                return false;
            }
        }
        HouseholdMember member = new HouseholdMember(memberName);
        member.setAge(age);
        member.setPetType(petType);
        member.setMedicalNote(medicalNote);
        householdMembers.add(member);
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
     *
     * @param memberName the member's name
     * @param safetyStatus the safety status to record
     * @return true if the check-in is stored, false if the member is not in the household
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