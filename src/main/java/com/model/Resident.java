package com.model;

import java.util.Date;
import java.util.UUID;

/**
 * A resident who requests relief
 */
public class Resident extends User {

    /**
     * Creates a new resident
     */
    public Resident(UUID userId, String username, String passwordHash,
            String fullName, Date dateOfBirth, String homeAddress,
            String emailAddress, String phoneNumber, String login) {
        super(userId, username, passwordHash, fullName, dateOfBirth,
            homeAddress, emailAddress, phoneNumber, login);
    }

    @Override
    public USERTYPE getType() {
        return USERTYPE.RESIDENT;
    }
}