package com.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * A user who manages one shelter.
 *
 * @author Austin Lantz
 */
public class ShelterManager extends User {

    private UUID shelterId;

    /**
     * Creates a shelter manager with an active account.
     *
     * @param userId the user's unique id
     * @param username the user's username
     * @param passwordHash the user's stored password hash
     * @param fullName the user's full name
     * @param dateOfBirth the user's date of birth
     * @param homeAddress the user's home address
     * @param emailAddress the user's email address
     * @param phoneNumber the user's phone number
     * @param login the login value passed to User
     */
    public ShelterManager(UUID userId, String username, String passwordHash,
            String fullName, LocalDate dateOfBirth, String homeAddress,
            String emailAddress, String phoneNumber, String login,
            String shelterId) {
        super(userId, username, passwordHash, fullName, dateOfBirth,
                homeAddress, emailAddress, phoneNumber, login);
    }

    /**
     * Returns this user's type.
     *
     * @return the shelter manager user type
     */
    @Override
    public USERTYPE getType() {
        return USERTYPE.SHELTER_MANAGER;
    }

    /**
     * Gets the id of the shelter this person manages.
     *
     * @return the shelter id, or null if none is set
     */
    public UUID getShelterId() {
        return shelterId;
    }

    /**
     * Sets the shelter this person manages.
     *
     * @param shelterId the shelter's id
     */
    public void setShelterId(UUID shelterId) {
        this.shelterId = shelterId;
    }
}