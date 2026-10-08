package com.model;

public class LoginTest {

    public static void main(String[] args) {
        UserDirectory directory = UserDirectory.getInstance();

        User good = directory.getUser("volunteer1", "pass123");
        System.out.println("Right password: " + (good != null ? good.getFullName() + " (" + good.getType() + ")" : "login failed"));

        User bad = directory.getUser("volunteer1", "wrongpass");
        System.out.println("Wrong password: " + (bad != null ? bad.getFullName() : "login failed"));

        User none = directory.getUser("nobody", "pass123");
        System.out.println("Unknown user: " + (none != null ? none.getFullName() : "login failed"));
    }
}