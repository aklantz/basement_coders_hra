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
        scenario9();

        DataWriter.saveUsers(UserDirectory.getInstance().getUsers());
        DataWriter.saveAidRequests(AidRequestDirectory.getInstance().getRequests());
        DataWriter.saveResources(ShelterDirectory.getInstance().getShelters());
    }

    /**
     * Scenario 1: Volunteer login.
     */
    public void scenario1() {
        System.out.println();

        User user = reliefApp.login("mrivera", "password");

        if (user == null) {
            System.out.println("Login failed.");
            return;
        }

        System.out.println("Logged in: " + user.getUsername());

        if (!reliefApp.logout()) {
            System.out.println("Logout failed.");
            return;
        }

        System.out.println("Logged out.");
    }

    /**
     * Scenario 2: Admin login.
     */
    public void scenario2() {
        System.out.println();

        User user = reliefApp.login("jsmith", "password");

        if (user == null) {
            System.out.println("Login failed.");
            return;
        }

        System.out.println("Logged in: " + user.getUsername());

        if (!reliefApp.logout()) {
            System.out.println("Logout failed.");
            return;
        }

        System.out.println("Logged out.");
    }

    /**
     * Scenario 3: Load shelters and inventory.
     */
    public void scenario3() {
        System.out.println();

        ArrayList<Shelter> shelters =
                DataLoader.getCachedShelters();

        if (shelters.isEmpty()) {
            System.out.println("No shelters loaded.");
            return;
        }

        System.out.println(shelters.size() + " shelter(s) loaded");

        for (Shelter shelter : shelters) {
            System.out.println("\nShelter: " + shelter.getName());

            if (shelter.getInventoryItems() == null) {
                System.out.println("Inventory items: none");
            } else {
                System.out.println("Inventory items: "
                        + shelter.getInventoryItems().size());
            }
        }
    }

    /**
     * Scenario 4: Load aid requests and assignments.
     */
    public void scenario4() {
        System.out.println();

        ArrayList<AidRequest> requests =
                DataLoader.getAidRequests();

        if (requests.isEmpty()) {
            System.out.println("No aid requests loaded.");
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
                System.out.println("  Assignments: none");
            } else {
                System.out.println("  Assignments: "
                        + request.getAssignments().size());
            }
        }
    }

    /**
     * Scenario 5: Hurricane and affected zip codes.
     */
    public void scenario5() {
        System.out.println();

        HurricaneDirectory hurricane =
                HurricaneDirectory.getInstance();

        if (hurricane.getHurricaneCategory() == null) {
            System.out.println("Hurricane not loaded.");
            return;
        }

        System.out.println("Hurricane category: "
                + hurricane.getHurricaneCategory());
        System.out.println("Wind speed: "
                + hurricane.getWindSpeed() + " mph");
        System.out.println(hurricane.getAffectedZipCodes().size()
                + " affected zip code(s)");

        for (ZipCode zipCode : hurricane.getAffectedZipCodes()) {
            System.out.println("- " + zipCode.getCode());
        }
    }

    /**
     * Scenario 6: Household management
     */

    public void scenario6() {
        System.out.println();


        if (reliefApp.createHousehold("Rivera Family")) {
            System.out.println("Household created without login.");
            return;
        }

        if (reliefApp.login("mrivera", "password") == null) {
            System.out.println("Login failed.");
            return;
        }

        reliefApp.createHousehold("Rivera Family");
        reliefApp.addHouseholdMember("Rivera Family", "Maria");
        reliefApp.addHouseholdMember("Rivera Family", "Carlos");

        if (!reliefApp.checkInHouseholdMember("Rivera Family", "Maria", SafetyStatus.SAFE)
                || !reliefApp.checkInHouseholdMember("Rivera Family", "Carlos", SafetyStatus.IN_NEED)) {
            System.out.println("Check in failed.");
            reliefApp.logout();
            return;
        }

        if (reliefApp.checkInHouseholdMember("Rivera Family", "Nobody", SafetyStatus.SAFE)) {
            System.out.println("Unknown member checked in.");
            reliefApp.logout();
            return;
        }

        System.out.println(reliefApp.getHouseholds().size() + " household(s)");

        for (Household household : reliefApp.getHouseholds()) {
            System.out.println("\nHousehold: " + household.getHouseholdName());

            for (HouseholdMember member : household.getMembers()) {
                System.out.println("- " + member.getName()
                        + " (status: " + member.getSafetyStatus()
                        + ", last check-in: " + member.getLastCheckIn() + ")");
            }
        }

        reliefApp.logout();

        if (!reliefApp.getHouseholds().isEmpty()) {
            System.out.println("Households still visible after logout.");
        }
    }

    /**
     * Scenario 7: Volunteer task.
     */

    public void scenario7() {
        System.out.println();

        User user = UserDirectory.getInstance().getUser("mrivera");

        if (!(user instanceof Volunteer)) {
            System.out.println("Volunteer not found.");
            return;
        }

        AidRequest request = new AidRequest(
                UUID.randomUUID(),
                user,
                RequestCategory.FOOD,
                3,
                "45 Harbor Rd, Tega Cay, SC");

        request.updateStatus(RequestStatus.ASSIGNED);

        TaskAssignment task = new TaskAssignment(
                UUID.randomUUID(),
                request,
                (Volunteer) user,
                null);

        System.out.println("New task: " + task.getStatus()
                + " | request: " + request.getStatus());

        task.confirmCompletion();

        if (task.getStatus() != TaskStatus.PENDING) {
            System.out.println("Task completed too early.");
            return;
        }

        System.out.println("Complete before accept ignored: "
                + task.getStatus());

        task.acceptAssignment();

        if (task.getStatus() != TaskStatus.ACCEPTED
                || request.getStatus() != RequestStatus.IN_PROGRESS) {
            System.out.println("Task acceptance failed.");
            return;
        }

        System.out.println("Accepted: " + task.getStatus()
                + " | request: " + request.getStatus());

        task.confirmCompletion();

        if (task.getStatus() != TaskStatus.COMPLETED
                || request.getStatus() != RequestStatus.FULFILLED) {
            System.out.println("Task completion failed.");
            return;
        }

        System.out.println("Completed: " + task.getStatus()
                + " | request: " + request.getStatus());
    }

    /**
     * Scenario 8: Shelter lookup
     */
    
    public void scenario8() {
        System.out.println();

        ShelterDirectory shelterDirectory =
                ShelterDirectory.getInstance();

        ArrayList<ZipCode> affected =
                HurricaneDirectory.getInstance().getAffectedZipCodes();

        ArrayList<Shelter> active =
                shelterDirectory.getActiveShelters(affected);

        System.out.println(active.size() + " active shelter(s)");

        for (Shelter shelter : active) {
            System.out.println("- "
                    + shelter.getName()
                    + " ("
                    + shelter.getZipCode()
                    + ")");
        }

        ArrayList<Shelter> offline =
                shelterDirectory.getOfflineShelters();

        System.out.println(offline.size() + " offline shelter(s)");

        for (Shelter shelter : offline) {
            System.out.println("- "
                    + shelter.getName()
                    + " ("
                    + shelter.getZipCode()
                    + ")");
        }
    }

    /**
     * Scenario 9: Find nearest shelter.
     */
    public void scenario9() {
        System.out.println();

        User user = UserDirectory.getInstance().getUser("mrivera");

        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        ShelterDirectory shelterDirectory =
                ShelterDirectory.getInstance();

        try {
            Shelter nearest =
                    shelterDirectory.findNearestShelter(user);

            if (nearest == null) {
                System.out.println("No available shelters found.");
                return;
            }

            System.out.println("Home: " + user.getHomeAddress());
            System.out.println("Nearest shelter: " + nearest.getName());

            System.out.println("Route:");

            for (String direction :
                    shelterDirectory.getDirections(user, nearest)) {
                System.out.println("- " + direction);
            }


        } catch (IllegalStateException e) {
            System.out.println("Map error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Shelter lookup failed.");
        }
    }

    public static void main(String[] args) {
        ReliefUI reliefInterface = new ReliefUI();
        reliefInterface.run();
    }
}