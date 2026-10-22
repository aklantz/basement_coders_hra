package com.model;

import java.time.LocalDate;
import java.util.UUID;

public class Admin extends User {

    public Admin(UUID userId,
                 String username,
                 String password,
                 String fullName,
                 LocalDate dateOfBirth,
                 String homeAddress,
                 String emailAddress,
                 String phoneNumber,
                 String createdBy) {

        super(userId, username, password, fullName,
              dateOfBirth, homeAddress, emailAddress,
              phoneNumber, createdBy);
    }

    @Override
    public USERTYPE getType() {
        return USERTYPE.ADMIN;
    }
}
