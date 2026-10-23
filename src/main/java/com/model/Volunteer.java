package com.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * A volunteer of the hurricane relief application.
 *
 * @author Austin Lantz
 */
public class Volunteer extends User {

    private RequestCategory specialty;

    /**
     * Creates a volunteer with no specialty selected.
     *
     * @param userId the user's unique id
     * @param username the username
     * @param passwordHash the stored password hash
     * @param fullName the user's full name
     * @param dateOfBirth the user's date of birth
     * @param homeAddress the user's home address
     * @param emailAddress the user's email address
     * @param phoneNumber the user's phone number
     * @param login the login value passed to User
     */
    public Volunteer(UUID userId, String username, String passwordHash,
                     String fullName, LocalDate dateOfBirth,
                     String homeAddress, String emailAddress,
                     String phoneNumber, String login) {
        super(userId, username, passwordHash, fullName, dateOfBirth,
                homeAddress, emailAddress, phoneNumber, login);
    }

    /**
     * Gets this user's type.
     *
     * @return the volunteer user type
     */
    @Override
    public USERTYPE getType() {
        return USERTYPE.VOLUNTEER;
    }

    /**
     * Gets the volunteer's specialty.
     *
     * @return the specialty, or null if none is selected
     */
    public RequestCategory getSpecialty() {
        return specialty;
    }

    /**
     * Sets the volunteer's specialty.
     *
     * @param specialty the specialty, or null to clear it
     */
    public void setSpecialty(RequestCategory specialty) {
        this.specialty = specialty;
    }
}