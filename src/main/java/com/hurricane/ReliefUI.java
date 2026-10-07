package com.hurricane;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.UUID;

/**
 * Console UI. Only talks to ReliefApplication (the facade) and never touches the
 * directories directly. The UML box for ReliefUI is cut off ("- >"), so the single
 * attribute here is assumed to be a reference to the facade.
 */
public class ReliefUI {

    private ReliefApplication application;
    private Scanner scanner;

    public ReliefUI() {
        application = ReliefApplication.getInstance();
        scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("=== Hurricane Relief ===");
        boolean running = true;
        while (running) {
            running = (isLoggedIn()) ? userMenu() : mainMenu();
        }
        System.out.println("Stay safe. Goodbye.");
    }

    // ---------- menus ----------

    /** @return false when the user chooses to exit */
    private boolean mainMenu() {
        System.out.println("\n1) Log in");
        System.out.println("2) Create account");
        System.out.println("0) Exit");
        switch (prompt("Choose: ")) {
            case "1": login(); break;
            case "2": createAccount(); break;
            case "0": return false;
            default: System.out.println("Invalid choice.");
        }
        return true;
    }

    private boolean userMenu() {
        System.out.println("\n1) Submit aid request");
        System.out.println("2) View my requests");
        System.out.println("3) View shelters");
        System.out.println("4) Assign task (coordinators)");
        System.out.println("5) Log out");
        switch (prompt("Choose: ")) {
            case "1": submitAidRequest(); break;
            case "2": viewMyRequests(); break;
            case "3": viewShelters(); break;
            case "4": assignTask(); break;
            case "5": logout(); break;
            default: System.out.println("Invalid choice.");
        }
        return true;
    }

    // ---------- actions ----------

    private void login() {
        String username = prompt("Username: ");
        String password = prompt("Password: ");
        if (application.login(username, password) != null) {
            System.out.println("Welcome, " + username + ".");
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private void createAccount() {
        String username = prompt("Username: ");
        String password = prompt("Password: ");
        String firstName = prompt("First name: ");
        String lastName = prompt("Last name: ");
        Date dob = promptDate("Date of birth (yyyy-MM-dd): ");
        if (dob == null) {
            System.out.println("Invalid date. Account not created.");
            return;
        }
        String address = prompt("Home address: ");
        String email = prompt("Email: ");
        String phone = prompt("Phone: ");

        if (application.createAccount(username, password, firstName, lastName,
                dob, address, email, phone) != null) {
            System.out.println("Account created. You can now log in.");
        } else {
            System.out.println("Could not create account (username may already be taken).");
        }
    }

    private void logout() {
        if (application.logout()) {
            System.out.println("Logged out.");
        }
    }

    private void submitAidRequest() {
        System.out.println("Categories:");
        for (RequestCategory c : RequestCategory.values()) {
            System.out.println("  " + c);
        }
        RequestCategory category;
        UrgencyLevel urgency;
        try {
            category = RequestCategory.valueOf(prompt("Category: ").toUpperCase());
            // UrgencyLevel isn't defined in the UML yet; values are whatever you give it
            urgency = UrgencyLevel.valueOf(prompt("Urgency: ").toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Unrecognized category or urgency.");
            return;
        }
        int householdSize;
        try {
            householdSize = Integer.parseInt(prompt("Household size: "));
        } catch (NumberFormatException e) {
            System.out.println("Household size must be a number.");
            return;
        }
        String streetAddress = prompt("Street address: ");

        if (application.submitAidRequest(category, householdSize, urgency, streetAddress)) {
            System.out.println("Request submitted.");
        } else {
            System.out.println("Request could not be submitted (it may be a duplicate).");
        }
    }

    private void viewMyRequests() {
        ArrayList<AidRequest> requests = application.getMyRequests();
        if (requests.isEmpty()) {
            System.out.println("You have no requests.");
            return;
        }
        // relies on AidRequest.toString(); swap for getters once they exist
        for (AidRequest request : requests) {
            System.out.println("- " + request);
        }
    }

    private void viewShelters() {
        ArrayList<Shelter> shelters = application.getShelterDirectory();
        if (shelters.isEmpty()) {
            System.out.println("No active shelters found.");
            return;
        }
        // relies on Shelter.toString(); swap for getters once they exist
        for (Shelter shelter : shelters) {
            System.out.println("- " + shelter);
        }
    }

    private void assignTask() {
        // TODO: only show this option for coordinators (needs a role check on User)
        try {
            UUID requestId = UUID.fromString(prompt("Request ID: "));
            UUID volunteerId = UUID.fromString(prompt("Volunteer ID: "));
            TaskAssignment task = application.assignTask(requestId, volunteerId);
            System.out.println(task != null ? "Task assigned." : "Could not assign task.");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid ID format.");
        }
    }

    // ---------- helpers ----------

    /** UI can't see currentUser, so a successful login is tracked with a simple check. */
    private boolean isLoggedIn() {
        return application.isLoggedIn();
    }

    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    private Date promptDate(String message) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(prompt(message));
        } catch (ParseException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        new ReliefUI().run();
    }
}
