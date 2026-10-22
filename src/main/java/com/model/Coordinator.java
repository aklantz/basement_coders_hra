package com.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * A coordinator of the hurricane relief application
 */
public class Coordinator extends User {

    public Coordinator(UUID userId, String username, String passwordHash, String fullName,
                       LocalDate dateOfBirth, String homeAddress, String emailAddress,
                       String phoneNumber, String login) {
        super(userId, username, passwordHash, fullName, dateOfBirth,
                homeAddress, emailAddress, phoneNumber, login);
    }

    @Override
    public USERTYPE getType() {
        return USERTYPE.COORDINATOR;
    }
}