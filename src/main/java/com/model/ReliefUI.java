package com.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

/**
 * Text-based UI that runs demo scenarios against the relief application.
 * Talks to the system through the facade, except where a scenario loads data directly.
 *
 * @author Austin Lantz
 */
public class ReliefUI {
    /** Rosa's username in the users file (adjust to match). */
    private static final String ROSA_USERNAME = "rosa";
    /** Rosa's password in the users file (adjust to match). */
    private static final String ROSA_PASSWORD = "password";
    /** The username of the Carter family's account (adjust to match). */
    private static final String CARTER_USERNAME = "carter";
    /** The password of the Carter family' account */
    private static final String CARTER_PASSWORD = "password";
    /** The first name of the father in the Carter family */
    private static final String CARTER_FIRSTNAME = "John";
    /** The last name of the father in the Carter family */
    private static final String CARTER_LASTNAME = "Carter";
    /** The DOB of the father in the Carter family */
    private static final LocalDate CARTER_DOB = LocalDate.of(1970, 3, 1);
    /** The home address of the Carter family (Charleston Museum address) */
    private static final String CARTER_HOMEADDRESS = "360 Meeting Street Charleston, SC 29403";
    /** The email address of the father in the Carter family */
    private static final String CARTER_EMAIL = "carterj@email.sc.edu";
    /** The phone number of the father in the Carter family */
    private static final String CARTER_PHONE = "803-888-9999";
    /** Felix's username in the users file. */
    private static final String FELIX_USERNAME = "felix";
    /** Felix's password in the users file. */
    private static final String FELIX_PASSWORD = "password";
    /** The Carter family members who check into the shelter (change names to match the team's). */
    private static final String[] CARTER_FAMILY = {"John Carter", "Jane Carter", "Sophie Carter"};
    /** The Carter family's dog. */
    private static final String CARTER_PET = "Buddy (dog)";
    /** Where the exported trip information is saved. */
    private static final String TRIP_FILE = "carter_trip_information.txt";

    private ReliefApplication reliefApp;

    /**
     * Creates the UI and gets the facade it will use.
     */
    ReliefUI() {
        reliefApp = ReliefApplication.getInstance();
    }

    /**
     * Runs every scenario in order, then saves the users, aid requests and shelters.
     */
    public void run() {
        showSavedData("BEFORE");

        scenario1();
        scenario2();
        scenario3();
        scenario4();
        scenario5();
        scenario6();
        scenario7();
        scenario8();
        scenario9();
        scenario10();
        scenario11();
        scenario12();
        scenario13();

        DataWriter.saveUsers(UserDirectory.getInstance().getUsers());
        DataWriter.saveAidRequests(AidRequestDirectory.getInstance().getRequests());
        DataWriter.saveResources(ShelterDirectory.getInstance().getShelters());

        showSavedData("AFTER");
    }

    /**
     * Felix checks the Carter family and Buddy into his shelter,
     * then marks their aid request complete.
     */

    public void scenario13() {
        System.out.println();

        User felix = reliefApp.login(FELIX_USERNAME, FELIX_PASSWORD);
        if (felix == null) {
            System.out.println("Felix could not log in.");
            return;
        }
        System.out.println("Logged in: " + felix.getFullName() + " (" + felix.getType() + ")");

        // show Felix the shelter

        Shelter shelter = reliefApp.getMyShelter();
        if (shelter == null) {
            System.out.println("Felix's shelter was not found.");
            reliefApp.logout();
            return;
        }
        printShelter(shelter);

        // check in the family and their dog
        ArrayList<String> family = new ArrayList<String>();
        for (String person : CARTER_FAMILY) {
            family.add(person);
        }
        ArrayList<String> pets = new ArrayList<String>();
        pets.add(CARTER_PET);

        if (!reliefApp.checkInToShelter(family, pets)) {
            System.out.println("Check in failed. The family may already be checked in,"
                    + " or the shelter may not have room.");
            reliefApp.logout();
            return;
        }
        System.out.println("\nChecked in the Carter family and " + CARTER_PET + ".");
        printShelter(shelter);

        // Felix successfully marks the task as complete, unlike Rosa
        AidRequest request = reliefApp.getRequestFrom(CARTER_USERNAME);
        if (request == null) {
            System.out.println("The Carter family's request was not found.");
        } else {
            try {
                if (reliefApp.completeRequest(request.getRequestId())) {
                    System.out.println("\nRequest marked complete. Status: " + request.getStatus());
                } else {
                    System.out.println("\nThe request could not be completed.");
                }
            } catch (SecurityException e) {
                System.out.println("\nNot allowed: " + e.getMessage());
            }
        }

        reliefApp.logout();
    }



    /**
     * Prints what a shelter manager sees about the shelter they manage.
     *
     * @param shelter the shelter
     */
    
    private void printShelter(Shelter shelter) {
        System.out.println("Shelter: " + shelter.getName());
        System.out.println("Location: " + shelter.getStreetAddress() + ", " + shelter.getCity()
                + ", " + shelter.getState() + " " + shelter.getZipCode());
        System.out.println("Capacity: " + shelter.getCapacity());
        System.out.println("Occupancy: " + shelter.getOccupancy());
        System.out.println("Spaces available: " + shelter.getRemainingCapacity());
        System.out.println("Occupants: " + shelter.getOccupants());
        System.out.println("Pets: " + shelter.getPets());
    }

    /**
     *  Shows loaded data to confirm JSON was saved correctly.
     *
     * @param label BEFORE or AFTER
     */
    private void showSavedData(String label) {
        System.out.println("\n===== SAVED DATA " + label + " =====");

        User carter = null;
        for (User user : DataLoader.getUsers()) {
            if (user.getUsername().equals(CARTER_USERNAME)) {
                carter = user;
            }
        }
        System.out.println(carter == null
                ? "users.json: no account for " + CARTER_USERNAME
                : "users.json: " + carter.getUsername() + " (" + carter.getType()
                        + ", " + carter.getFullName() + ")");

        AidRequest carterRequest = null;
        for (AidRequest request : DataLoader.getAidRequests()) {
            if (request.getRequester() != null
                    && request.getRequester().getUsername().equals(CARTER_USERNAME)) {
                carterRequest = request;
            }
        }
        if (carterRequest == null) {
            System.out.println("aidrequests.json: no request from " + CARTER_USERNAME);
        } else {
            System.out.println("aidrequests.json: " + carterRequest.getCategory()
                    + " request, status " + carterRequest.getStatus());
            for (TaskAssignment task : carterRequest.getAssignments()) {
                String volunteer = (task.getVolunteer() == null)
                        ? "nobody" : task.getVolunteer().getUsername();
                System.out.println("  assigned to: " + volunteer + " (" + task.getStatus() + ")");
            }
        }

        for (Shelter shelter : DataLoader.getCachedShelters()) {
            if (shelter.getOccupants().isEmpty() && shelter.getPets().isEmpty()) {
                continue;
            }
            System.out.println("shelters.json: " + shelter.getName() + " | occupancy "
                    + shelter.getOccupancy() + "/" + shelter.getCapacity()
                    + " | occupants " + shelter.getOccupants() + " | pets " + shelter.getPets());
        }
        System.out.println("=============================\n");
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
     * Scenario 6: Household management.
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
     * Scenario 7: Volunteer task. Finishing the task leaves the request in progress.
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

        request.addAssignment(task);

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

        // The volunteer finishing the task does not complete the request
        if (task.getStatus() != TaskStatus.COMPLETED
                || request.getStatus() != RequestStatus.IN_PROGRESS) {
            System.out.println("Task completion failed.");
            return;
        }

        System.out.println("Completed: " + task.getStatus()
                + " | request: " + request.getStatus()
                + " (a coordinator or admin completes the request)");
    }

    /**
     * Scenario 8: Shelter lookup.
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

    /**
     * Scenario 10: Carter makes an account and submits an aid request
     */

    public void scenario10() {
        System.out.println();

        User Carter = reliefApp.createAccount(CARTER_USERNAME, CARTER_PASSWORD, CARTER_FIRSTNAME, CARTER_LASTNAME, CARTER_DOB, CARTER_HOMEADDRESS, CARTER_EMAIL, CARTER_PHONE, USERTYPE.RESIDENT);
        reliefApp.login(CARTER_USERNAME, CARTER_PASSWORD);
        
        reliefApp.createAidRequest(RequestCategory.MEDICAL, 3, CARTER_HOMEADDRESS, "We have a dog with us too.");

       
    }

    /**
     * Scenario 11: Rosa exports the Carter family's trip information to a printable .txt file.
     */
    public void scenario11() {
        System.out.println();

        User rosa = reliefApp.login(ROSA_USERNAME, ROSA_PASSWORD);

        if (rosa == null) {
            System.out.println("Login failed.");
            return;
        }

        try {
            AidRequest request = findAssignedRequestFrom(CARTER_USERNAME);

            if (request == null) {
                System.out.println("No assigned request from the Carter family.");
                return;
            }

            Shelter shelter = reliefApp.findNearestShelter(request);

            if (shelter == null) {
                System.out.println("No available shelters found.");
                return;
            }

            if (!reliefApp.exportTripInformation(request, shelter, TRIP_FILE)) {
                System.out.println("Export failed.");
                return;
            }

            System.out.println("Trip information exported to " + TRIP_FILE);
            System.out.println("----------------------------------------");

            List<String> lines = Files.readAllLines(Paths.get(TRIP_FILE));

            for (String line : lines) {
                System.out.println(line);
            }

            System.out.println("----------------------------------------");

            ArrayList<String> missing =
                    findMissingFromExport(rosa, request, shelter, lines);

            if (missing.isEmpty()) {
                System.out.println("Export includes everything required.");
            } else {
                System.out.println("Export is missing:");

                for (String item : missing) {
                    System.out.println("- " + item);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Couldn't read the exported file.");
        } catch (IllegalStateException e) {
            System.out.println("Map error: " + e.getMessage());
        } finally {
            reliefApp.logout();
        }
    }

    /**
     * Scenario 12: Rosa (volunteer) tries to mark the request complete and is refused.
     */
    public void scenario12() {
        System.out.println();

        User rosa = reliefApp.login(ROSA_USERNAME, ROSA_PASSWORD);

        if (rosa == null) {
            System.out.println("Login failed.");
            return;
        }

        try {
            AidRequest request = findAssignedRequestFrom(CARTER_USERNAME);

            if (request == null) {
                System.out.println("No assigned request from the Carter family.");
                return;
            }

            RequestStatus before = request.getStatus();

            try {
                reliefApp.completeRequest(request.getRequestId());
                System.out.println("Rosa was able to complete the request.");
                return;
            } catch (SecurityException e) {
                System.out.println("Blocked: " + e.getMessage());

                if (e.getMessage() == null
                        || !e.getMessage().toLowerCase().contains("volunteer")) {
                    System.out.println("The message doesn't explain the volunteer role.");
                }
            }

            boolean stillAssigned = false;

            for (TaskAssignment task : request.getAssignments()) {
                if (task.getVolunteer() != null
                        && task.getVolunteer().getUserId().equals(rosa.getUserId())) {
                    stillAssigned = true;
                }
            }

            if (request.getStatus() != before || !stillAssigned) {
                System.out.println("The request changed even though completion was blocked.");
                return;
            }

            System.out.println("Request status is still " + request.getStatus()
                    + " and remains assigned to " + rosa.getUsername());
        } finally {
            reliefApp.logout();
        }
    }

    /**
     * Finds a request from the given requester that is assigned to the logged-in volunteer.
     *
     * @param requesterUsername the username of the person who made the request
     * @return the request, or null if there isn't one
     */
    private AidRequest findAssignedRequestFrom(String requesterUsername) {
        for (AidRequest request : reliefApp.getAssignedRequests()) {
            if (request.getRequester() != null
                    && requesterUsername.equals(request.getRequester().getUsername())) {
                return request;
            }
        }
        return null;
    }

    /**
     * Checks the exported lines for everything the trip sheet is required to contain.
     *
     * @param rosa the volunteer making the trip
     * @param request the request being handled
     * @param shelter the shelter the family is going to
     * @param lines the lines of the exported file
     * @return a description of each missing item (empty if nothing is missing)
     */
    private ArrayList<String> findMissingFromExport(User rosa, AidRequest request,
            Shelter shelter, List<String> lines) {
        ArrayList<String> missing = new ArrayList<String>();
        User requester = request.getRequester();

        // family members (name and age), the dog (name and type), and any medical note
        for (Household household : requester.getHouseHoldMembers()) {
            for (HouseholdMember member : household.getMembers()) {
                String detail = (member.getPetType() == null)
                        ? String.valueOf(member.getAge())
                        : member.getPetType();

                if (!hasLine(lines, member.getName(), detail)) {
                    missing.add(member.getName() + " (" + detail + ")");
                }

                if (member.getMedicalNote() != null
                        && !hasText(lines, member.getMedicalNote())) {
                    missing.add("medical note for " + member.getName());
                }
            }
        }

        // contact information, pickup address, and shelter name and address
        String[] labels = {"phone number", "email address", "pickup address",
                "shelter name", "shelter address"};
        String[] required = {requester.getPhoneNumber(), requester.getEmailAddress(),
                request.getStreetAddress(), shelter.getName(), shelter.getAddress()};

        for (int i = 0; i < required.length; i++) {
            if (!hasText(lines, required[i])) {
                missing.add(labels[i]);
            }
        }

        // directions: Rosa to the Carter home, then the Carter home to the shelter
        for (String step : reliefApp.getDirections(rosa.getHomeAddress(),
                request.getStreetAddress())) {
            if (!hasText(lines, step)) {
                missing.add("direction to the Carter home: " + step);
            }
        }

        for (String step : reliefApp.getDirections(request.getStreetAddress(),
                shelter.getAddress())) {
            if (!hasText(lines, step)) {
                missing.add("direction to the shelter: " + step);
            }
        }

        return missing;
    }

    /**
     * Checks whether any line contains some text.
     *
     * @param lines the lines to search
     * @param text the text to look for
     * @return true if any line contains the text (ignoring case)
     */
    private boolean hasText(List<String> lines, String text) {
        if (text == null) {
            return false;
        }
        for (String line : lines) {
            if (line.toLowerCase().contains(text.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether a single line contains two pieces of text.
     *
     * @param lines the lines to search
     * @param first the first text to look for
     * @param second the second text to look for
     * @return true if one line contains both (ignoring case)
     */
    private boolean hasLine(List<String> lines, String first, String second) {
        for (String line : lines) {
            String lower = line.toLowerCase();
            if (lower.contains(first.toLowerCase()) && lower.contains(second.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Starts the UI and runs all the scenarios.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        ReliefUI reliefInterface = new ReliefUI();
        reliefInterface.run();
    }
}