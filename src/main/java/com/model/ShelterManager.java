package com.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * A manager who runs one shelter
 */
public class ShelterManager extends User {

    private String shelterId;

    /**
     * Creates a new shelter manager
     * @param shelterId the id of the shelter this manager runs
     */
    public ShelterManager(UUID userId, String username, String passwordHash,
            String fullName, LocalDate dateOfBirth, String homeAddress,
            String emailAddress, String phoneNumber, String login,
            String shelterId) {
        super(userId, username, passwordHash, fullName, dateOfBirth,
            homeAddress, emailAddress, phoneNumber, login);
        this.shelterId = shelterId;
    }

    @Override
    public USERTYPE getType() {
        return USERTYPE.SHELTER_MANAGER;
    }

    public String getShelterId() {
        return shelterId;
    }
}
