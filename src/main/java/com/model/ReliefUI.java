package com.model;

import java.util.ArrayList;
import java.util.UUID;

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
        scenario7();
        scenario8();

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
        System.out.println("Wind speed: " + hurricane.getWindSpeed() + " mph");
        System.out.println(hurricane.getAffectedZipCodes().size() + " affected zip code(s)");

        for (ZipCode zipCode : hurricane.getAffectedZipCodes()) {
            System.out.println("- " + zipCode.getCode());
        }
    }

    /**
     * Scenario 6: Create a household, add members and check them in.
     */
    public void scenario6() {
        System.out.println();

        HouseholdMember household = new HouseholdMember(UUID.randomUUID(), "The Smiths");
        household.addHouseholdMember("Alice");
        household.addHouseholdMember("Bob");

        if (household.getMembers().size() != 2) {
            System.out.println("Sorry, the household members couldn't be added.");
            return;
        }

        System.out.println(household.getHouseHoldName() + " has "
                + household.getMembers().size() + " member(s)");

        for (String member : household.getMembers()) {
            System.out.println("- " + member
                    + " (status: " + household.getSafetyStatus(member)
                    + ", last check-in: " + household.getLastCheckIn(member) + ")");
        }

        household.checkIn("Alice", SafetyStatus.SAFE);
        household.checkIn("Bob", SafetyStatus.IN_NEED);

        if (household.getSafetyStatus("Alice") != SafetyStatus.SAFE
                || household.getSafetyStatus("Bob") != SafetyStatus.IN_NEED) {
            System.out.println("Sorry, the members couldn't be checked in.");
            return;
        }

        System.out.println("\nAfter check-in:");
        System.out.println("Alice is " + household.getSafetyStatus("Alice"));
        System.out.println("Bob is " + household.getSafetyStatus("Bob"));
    }

    /**
     * Scenario 7: A volunteer accepts and completes a task for an aid request.
     * The request is not added to the directory, so running this again
     * doesn't change the saved data.
     */
    public void scenario7() {
        System.out.println();

        User user = UserDirectory.getInstance().getUser("mrivera");
        if (!(user instanceof Volunteer)) {
            System.out.println("Sorry, the volunteer couldn't be found.");
            return;
        }

        AidRequest request = new AidRequest(UUID.randomUUID(), user, RequestCategory.FOOD,
                3, "45 Harbor Rd, Tega Cay, SC");
        request.updateStatus(RequestStatus.ASSIGNED);
        TaskAssignment task = new TaskAssignment(UUID.randomUUID(), request,
                (Volunteer) user, null);

        System.out.println("New task: " + task.getStatus()
                + " | request: " + request.getStatus());

        task.confirmCompletion();
        if (task.getStatus() != TaskStatus.PENDING) {
            System.out.println("Sorry, a task was completed before it was accepted.");
            return;
        }
        System.out.println("Completing before accepting is ignored: " + task.getStatus());

        task.acceptAssignment();
        if (task.getStatus() != TaskStatus.ACCEPTED
                || request.getStatus() != RequestStatus.IN_PROGRESS) {
            System.out.println("Sorry, the task couldn't be accepted.");
            return;
        }
        System.out.println("After accepting: " + task.getStatus()
                + " | request: " + request.getStatus()
                + " | accepted at: " + task.getAcceptedAt());

        task.confirmCompletion();
        if (task.getStatus() != TaskStatus.COMPLETED
                || request.getStatus() != RequestStatus.FULFILLED) {
            System.out.println("Sorry, the task couldn't be completed.");
            return;
        }
        System.out.println("After completing: " + task.getStatus()
                + " | request: " + request.getStatus()
                + " | completed at: " + task.getCompletedAt());
    }

    /**
     * Scenario 8: Find the active shelters in the hurricane's affected zip codes,
     * and the shelters that are closed.
     */
    public void scenario8() {
        System.out.println();

        ShelterDirectory shelterDirectory = ShelterDirectory.getInstance();
        ArrayList<ZipCode> affected = HurricaneDirectory.getInstance().getAffectedZipCodes();

        ArrayList<Shelter> active = shelterDirectory.getActiveShelters(affected);
        System.out.println(active.size() + " active shelter(s) in affected zip codes");
        for (Shelter shelter : active) {
            System.out.println("- " + shelter.getName() + " (" + shelter.getZipCode()
                    + ", " + shelter.getStatus()
                    + ", " + shelter.getRemainingCapacity() + " spots left)");
        }

        ArrayList<Shelter> offline = shelterDirectory.getOfflineShelters();
        System.out.println(offline.size() + " offline shelter(s)");
        for (Shelter shelter : offline) {
            System.out.println("- " + shelter.getName() + " (" + shelter.getZipCode() + ")");
        }
    }

    public static void main(String[] args) {
        ReliefUI reliefInterface = new ReliefUI();
        reliefInterface.run();
    }
}