package com.model;

import java.util.Date;
import java.util.UUID;

/**
 * An administrator of the relief system.
 */
public class Admin extends User {

    public Admin(UUID userId, String username, String password, String fullName,
                 Date dateOfBirth, String homeAddress, String emailAddress,
                 String phoneNumber, String login) {
        super(userId, username, password, fullName, dateOfBirth,
              homeAddress, emailAddress, phoneNumber, login);
    }
}