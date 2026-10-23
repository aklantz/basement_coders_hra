package com.model;

import java.time.LocalDateTime;

/**
 * Represents a household member, either a person or a pet, with a name, age,
 * safety status, most recent check-in time, and any medical need.
 */
public class HouseholdMember {

    private String name;
    private int age;
    private String petType;
    private String medicalNote;
    private LocalDateTime lastCheckInTime;
    private SafetyStatus safetyStatus;

    /**
     * Creates a member with an unknown safety status and the current local time
     * as the initial check-in time. The member is a person of unknown age (0)
     * with no medical need until the details are set.
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

    /**
     * Returns the member's age.
     *
     * @return the age in years (0 for a pet or when the age is not known)
     */
    public int getAge() {
        return age;
    }

    /**
     * Sets the member's age. A negative age is stored as 0.
     *
     * @param age the age in years
     */
    public void setAge(int age) {
        this.age = Math.max(age, 0);
    }

    /**
     * Returns the kind of pet this member is.
     *
     * @return the pet type, such as "dog", or null if the member is a person
     */
    public String getPetType() {
        return petType;
    }

    /**
     * Sets the kind of pet this member is. A blank value means the member is a person.
     *
     * @param petType the pet type, such as "dog", or null for a person
     */
    public void setPetType(String petType) {
        this.petType = isBlank(petType) ? null : petType.trim();
    }

    /**
     * Checks whether this member is a pet.
     *
     * @return true if the member is a pet
     */
    public boolean isPet() {
        return petType != null;
    }

    /**
     * Returns the medical assistance this member needs.
     *
     * @return the medical note, or null if no medical assistance is needed
     */
    public String getMedicalNote() {
        return medicalNote;
    }

    /**
     * Sets the medical assistance this member needs. A blank value means none is needed.
     *
     * @param medicalNote the medical note, or null if none
     */
    public void setMedicalNote(String medicalNote) {
        this.medicalNote = isBlank(medicalNote) ? null : medicalNote.trim();
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}