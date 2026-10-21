package com.model;

import java.util.ArrayList;

public class ReliefUI {
    private ReliefApplication reliefApp;

    ReliefUI() {
        reliefApp = ReliefApplication.getInstance();
    }

    public void run() {
        scenario1();
        scenario2();
        scenario3();
        scenario4();
        scenario5();
        scenario6();

        // Save all data after scenarios
        DataWriter.saveUsers(UserDirectory.getInstance().getUsers());
        DataWriter.saveAidRequests(AidRequestDirectory.getInstance().getRequests());
        DataWriter.saveResources(ShelterDirectory.getInstance().getShelters());

        System.out.println("\nAll data saved to JSON.");
    }

    /**
     * Scenario 1: Maria Rivera (volunteer) logs in and logs out.
     */
    public void scenario1() {
        System.out.println();

        User user = reliefApp.login("mrivera", "password");
        if (user == null) {
            System.out.println("Sorry, Maria Rivera couldn't log in.");
            return;
        }

        System.out.println("Maria Rivera is now logged in");

        if (!reliefApp.logout()) {
            System.out.println("Sorry, Maria Rivera could not be logged out.");
            return;
        }

        System.out.println("Maria Rivera has logged out");
    }

    /**
     * Scenario 2: Jordan Smith (admin) logs in and logs out.
     */
    public void scenario2() {
        System.out.println();

        User user = reliefApp.login("jsmith", "password");
        if (user == null) {
            System.out.println("Sorry, Jordan Smith couldn't log in.");
            return;
        }

        System.out.println("Jordan Smith is now logged in");

        if (!reliefApp.logout()) {
            System.out.println("Sorry, Jordan Smith could not be logged out.");
            return;
        }

        System.out.println("Jordan Smith has logged out");
    }

    /**
     * Scenario 3: Load and display shelters and inventory.
     */
    public void scenario3() {
        System.out.println();

        ArrayList<Shelter> shelters = DataLoader.getCachedShelters();

        if (shelters.isEmpty()) {
            System.out.println("Sorry, no shelters were loaded.");
            return;
        }

        System.out.println(shelters.size() + " shelter(s) loaded");

        for (Shelter shelter : shelters) {
            System.out.println("\nShelter: " + shelter.getName());

            if (shelter.getInventoryItems() == null) {
                System.out.println("Inventory Items: NULL");
            } else {
                System.out.println("Inventory Items Count: "
                        + shelter.getInventoryItems().size());
            }
        }
    }

    /**
     * Scenario 4: Load and display aid requests and assignments.
     */
    public void scenario4() {
        System.out.println();

        ArrayList<AidRequest> requests = DataLoader.getAidRequests();

        if (requests.isEmpty()) {
            System.out.println("Sorry, no aid requests were loaded.");
            return;
        }

        System.out.println(requests.size() + " aid request(s) loaded");

        for (AidRequest request : requests) {

            String requesterName = (request.getRequester() == null)
                    ? "UNKNOWN USER"
                    : request.getRequester().getUsername();

            System.out.println("- "
                    + request.getCategory()
                    + " request from "
                    + requesterName
                    + " (status: "
                    + request.getStatus()
                    + ")");

            if (request.getAssignments() == null) {
                System.out.println("  Assignments: NULL");
            } else {
                System.out.println("  Assignments Count: "
                        + request.getAssignments().size());
            }
        }
    }

    /**
     * Scenario 5: Load and display the hurricane and affected zip codes.
     */
    public void scenario5() {
        System.out.println();

        HurricaneDirectory hurricane = HurricaneDirectory.getInstance();

        if (hurricane.getHurricaneCategory() == null) {
            System.out.println("Sorry, the hurricane couldn't be loaded.");
            return;
        }

        System.out.println("Hurricane category: " + hurricane.getHurricaneCategory());
        System.out.println(hurricane.getAffectedZipCodes().size() + " affected zip code(s)");

        for (ZipCode zipCode : hurricane.getAffectedZipCodes()) {
            System.out.println("- " + zipCode.getCode());
        }
    }

    /**
     * Scenario 6: Maria Rivera creates a household, adds members and checks them in.
     */
    public void scenario6() {
        System.out.println();

        // Not logged in yet, so this should be refused
        if (reliefApp.createHousehold("Rivera Family")) {
            System.out.println("Sorry, a household was created without anyone logged in.");
            return;
        }

        if (reliefApp.login("mrivera", "password") == null) {
            System.out.println("Sorry, Maria Rivera couldn't log in.");
            return;
        }

        // These return false when the household or member already exists (from an earlier run)
        reliefApp.createHousehold("Rivera Family");
        reliefApp.addHouseholdMember("Rivera Family", "Maria");
        reliefApp.addHouseholdMember("Rivera Family", "Carlos");

        if (!reliefApp.checkInHouseholdMember("Rivera Family", "Maria", SafetyStatus.SAFE)
                || !reliefApp.checkInHouseholdMember("Rivera Family", "Carlos", SafetyStatus.IN_NEED)) {
            System.out.println("Sorry, the household members couldn't be checked in.");
            reliefApp.logout();
            return;
        }

        // A member who was never added can't check in
        if (reliefApp.checkInHouseholdMember("Rivera Family", "Nobody", SafetyStatus.SAFE)) {
            System.out.println("Sorry, an unknown member was able to check in.");
            reliefApp.logout();
            return;
        }

        System.out.println(reliefApp.getHouseholds().size() + " household(s) for Maria Rivera");

        for (HouseholdMember household : reliefApp.getHouseholds()) {
            System.out.println("\nHousehold: " + household.getHouseHoldName());

            for (String member : household.getMembers()) {
                System.out.println("- " + member
                        + " (status: " + household.getSafetyStatus(member)
                        + ", last check-in: " + household.getLastCheckIn(member) + ")");
            }
        }

        reliefApp.logout();

        // Logged out again, so the households should no longer be available
        if (!reliefApp.getHouseholds().isEmpty()) {
            System.out.println("Sorry, households were visible after logging out.");
        }
    }

    public static void main(String[] args) {
        ReliefUI reliefInterface = new ReliefUI();
        reliefInterface.run();
    }
}