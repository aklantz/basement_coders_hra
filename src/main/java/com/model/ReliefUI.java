package com.model;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Text-based UI that runs demo scenarios against the relief application.
 * Talks to the system through the facade, except where a scenario loads data directly.
 *
 * Run it normally to play every scenario. Run it with the argument "verify" afterwards
 * (in a new program run) to show that the saved changes survived a restart.
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
    private static final String CARTER_HOMEADDRESS = "360 Meeting Street, Charleston, SC 29403";
    /** The email address of the father in the Carter family */
    private static final String CARTER_EMAIL = "carterj@email.sc.edu";
    /** The phone number of the father in the Carter family */
    private static final String CARTER_PHONE = "803-888-9999";
    /** The name of the Carter family's household. */
    private static final String CARTER_HOUSEHOLD = "Carter Family";
    /** The Carter family members (change names to match the team's). */
    private static final String[] CARTER_FAMILY = {"John Carter", "Jane Carter", "Sophie Carter"};
    /** The ages of the Carter family members, in the same order as the names. */
    private static final int[] CARTER_AGES = {56, 52, 9};
    /** Sophie's medical need. */
    private static final String CARTER_MEDICAL_NOTE =
            "Asthma symptoms have worsened; needs medical assistance";
    /** The Carter family's dog. */
    private static final String CARTER_PET_NAME = "Buddy";
    /** The Carter family's dog, as it is recorded at the shelter. */
    private static final String CARTER_PET = "Buddy (dog)";
    /** The note on the Carter family's request. */
    private static final String CARTER_NOTE =
            "Sophie needs medical assistance (asthma). We are bringing our dog, Buddy.";
    /** Felix's username in the users file. */
    private static final String FELIX_USERNAME = "felix";
    /** Felix's password in the users file. */
    private static final String FELIX_PASSWORD = "password";
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
     * The saved files are shown before and after.
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
        scenario14();
        scenario15();

        DataWriter.saveUsers(UserDirectory.getInstance().getUsers());
        DataWriter.saveAidRequests(AidRequestDirectory.getInstance().getRequests());
        DataWriter.saveResources(ShelterDirectory.getInstance().getShelters());

        showSavedData("AFTER");
        System.out.println("Now run the program again with the argument \"verify\""
                + " to show the changes survive a restart.");
    }

    /**
     * Runs after a restart. Everything shown here is loaded from the saved files.
     */
    public void verify() {
        System.out.println("Restarted: everything below was loaded from the saved files.");
        showSavedData("AFTER RESTART");

        System.out.println();
        User carter = reliefApp.login(CARTER_USERNAME, CARTER_PASSWORD);
        if (carter == null) {
            System.out.println("The Carter account was not saved.");
        } else {
            System.out.println("The Carter account loads and can log in: " + carter.getFullName());
            reliefApp.logout();
        }

        AidRequest request = reliefApp.getRequestFrom(CARTER_USERNAME);
        if (request == null) {
            System.out.println("The Carter request was not saved.");
        } else {
            System.out.println("The Carter request loads. Status: " + request.getStatus());
            for (TaskAssignment task : request.getAssignments()) {
                String volunteer = (task.getVolunteer() == null)
                        ? "nobody" : task.getVolunteer().getUsername();
                System.out.println("  assigned to: " + volunteer + " (" + task.getStatus() + ")");
            }
        }

        User felix = reliefApp.login(FELIX_USERNAME, FELIX_PASSWORD);
        if (felix != null) {
            System.out.println("\nFelix's shelter after the restart:");
            Shelter shelter = reliefApp.getMyShelter();
            if (shelter != null) {
                printShelter(shelter);
            }
            reliefApp.logout();
        }
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
     * Shows the saved entries that matter to the scenario, straight from the JSON files,
     * and says what each file is used for.
     *
     * @param label BEFORE, AFTER or AFTER RESTART
     */
    private void showSavedData(String label) {
        System.out.println("\n===== SAVED FILES " + label + " =====");

        printJsonEntries(DataConstants.USER_FILE
                + " (new account, household, Buddy's pet information)",
                DataConstants.USER_FILE, CARTER_USERNAME);

        printJsonEntries(DataConstants.AID_REQUEST_FILE
                + " (relief request, assignment, status)",
                DataConstants.AID_REQUEST_FILE, CARTER_HOMEADDRESS);

        printJsonEntries(DataConstants.RESOURCE_FILE
                + " (shelter check-in: occupants and pet)",
                DataConstants.RESOURCE_FILE, getFelixShelterId());

        System.out.println("=============================\n");
    }

    /**
     * Prints the entries of a JSON file that contain some text.
     *
     * @param heading what to print above the entries
     * @param fileName the JSON file to read
     * @param keyword only entries containing this text (ignoring case) are shown
     */
    private void printJsonEntries(String heading, String fileName, String keyword) {
        System.out.println("\n" + heading);
        boolean found = false;
        try (FileReader reader = new FileReader(fileName)) {
            JSONArray entries = (JSONArray) new JSONParser().parse(reader);
            for (Object entry : entries) {
                String json = ((JSONObject) entry).toJSONString();
                if (keyword != null && json.toLowerCase().contains(keyword.toLowerCase())) {
                    System.out.println(json.replace("{\"", "{\n  \"").replace(",\"", ",\n  \""));
                    found = true;
                }
            }
        } catch (Exception e) {
            System.out.println("(could not read " + fileName + ")");
            return;
        }
        if (!found) {
            System.out.println("(no matching entries)");
        }
    }

    /**
     * Gets the id of the shelter Felix manages, as saved in the users file.
     *
     * @return the shelter id, or null if Felix or his shelter is not found
     */
    private String getFelixShelterId() {
        for (User user : DataLoader.getUsers()) {
            if (user instanceof ShelterManager && FELIX_USERNAME.equals(user.getUsername())) {
                UUID id = ((ShelterManager) user).getShelterId();
                return (id == null) ? null : id.toString();
            }
        }
        return null;
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
                + " (a shelter manager completes the request)");
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
     * Scenario 10: John Carter creates an account, his household, and a relief request.
     */
    public void scenario10() {
        System.out.println();

        User carter = reliefApp.createAccount(CARTER_USERNAME, CARTER_PASSWORD,
                CARTER_FIRSTNAME, CARTER_LASTNAME, CARTER_DOB, CARTER_HOMEADDRESS,
                CARTER_EMAIL, CARTER_PHONE, USERTYPE.RESIDENT);

        if (carter == null) {
            System.out.println("The account was not created (it may already exist).");
        } else {
            System.out.println("Account created: " + carter.getFullName()
                    + " (" + carter.getType() + ")");
        }

        if (reliefApp.login(CARTER_USERNAME, CARTER_PASSWORD) == null) {
            System.out.println("Login failed.");
            return;
        }

        try {
            // the family: two adults, Sophie (with her medical need), and Buddy the dog
            reliefApp.createHousehold(CARTER_HOUSEHOLD);
            for (int i = 0; i < CARTER_FAMILY.length; i++) {
                String note = CARTER_FAMILY[i].startsWith("Sophie") ? CARTER_MEDICAL_NOTE : null;
                reliefApp.addHouseholdMember(CARTER_HOUSEHOLD, CARTER_FAMILY[i],
                        CARTER_AGES[i], null, note);
            }
            reliefApp.addHouseholdMember(CARTER_HOUSEHOLD, CARTER_PET_NAME, 0, "dog", null);

            AidRequest request = reliefApp.createAidRequest(RequestCategory.TRANSPORT,
                    CARTER_FAMILY.length, CARTER_HOMEADDRESS, CARTER_NOTE);

            if (request == null) {
                System.out.println("The request was not submitted"
                        + " (an identical request may already exist).");
                return;
            }

            System.out.println("Request submitted: " + request.getCategory()
                    + " for " + request.getHouseholdSize() + " people and a dog");
            System.out.println("Location: " + request.getStreetAddress());
            System.out.println("Contact: " + CARTER_PHONE + ", " + CARTER_EMAIL);
            System.out.println("Medical: " + CARTER_MEDICAL_NOTE);
            System.out.println("Status: " + request.getStatus());
        } finally {
            reliefApp.logout();
        }
    }

    /**
     * Scenario 11: Rosa sees the open requests, with the Carter family's recommended
     * at the top, and opens it.
     */
    public void scenario11() {
        System.out.println();

        User rosa = reliefApp.login(ROSA_USERNAME, ROSA_PASSWORD);

        if (rosa == null) {
            System.out.println("Login failed.");
            return;
        }

        try {
            ArrayList<RecommendedRequest> open = reliefApp.getRecommendedRequests();

            System.out.println("Rosa sees " + open.size() + " open relief request(s)");
            if (open.size() != 7) {
                System.out.println("(The scenario expects 7: seed six other open requests.)");
            }
            if (open.isEmpty()) {
                return;
            }

            for (int i = 0; i < open.size(); i++) {
                RecommendedRequest item = open.get(i);
                AidRequest request = item.getRequest();
                System.out.println((i + 1) + ". " + (item.isRecommended() ? "[RECOMMENDED] " : "")
                        + request.getCategory() + " | " + request.getStreetAddress()
                        + " | " + (item.getDrivingMinutes() == Double.MAX_VALUE
                                ? "distance unknown"
                                : String.format("%.0f", item.getDrivingMinutes()) + " min"));
                if (item.isRecommended()) {
                    System.out.println("   Why: " + item.getReason());
                }
            }

            RecommendedRequest top = open.get(0);
            AidRequest request = top.getRequest();
            User requester = request.getRequester();

            if (!top.isRecommended() || requester == null
                    || !CARTER_USERNAME.equals(requester.getUsername())) {
                System.out.println("The Carter family's request is not at the top.");
                return;
            }

            System.out.println("\nThe Carter family's request is at the top and recommended.");
            System.out.println("Opened request at " + request.getStreetAddress());
            System.out.println("Needs transportation for: " + describeGroup(requester));
            for (Household household : requester.getHouseholds()) {
                for (HouseholdMember member : household.getMembers()) {
                    if (member.getMedicalNote() != null) {
                        System.out.println("Medical assistance: " + member.getName()
                                + " - " + member.getMedicalNote());
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Map error: " + e.getMessage());
        } finally {
            reliefApp.logout();
        }
    }

    /**
     * Scenario 12: Rosa searches the shelters, picks the closest suitable one, assigns the
     * request to herself, and another volunteer sees that she is handling it.
     */
    public void scenario12() {
        System.out.println();

        User rosa = reliefApp.login(ROSA_USERNAME, ROSA_PASSWORD);

        if (rosa == null) {
            System.out.println("Login failed.");
            return;
        }

        boolean assigned = false;

        try {
            AidRequest request = reliefApp.getRequestFrom(CARTER_USERNAME);

            if (request == null) {
                System.out.println("The Carter family's request was not found.");
                return;
            }

            System.out.println("Searching " + reliefApp.getAllShelters().size()
                    + " shelters for the closest one that accepts dogs, has medical staff,"
                    + " and has room for " + request.getHouseholdSize()
                    + " people and a dog");

            Shelter shelter = reliefApp.findSuitableShelter(request);

            if (shelter == null) {
                System.out.println("No suitable shelter found.");
                return;
            }

            System.out.println("Selected shelter: " + shelter.getName());
            System.out.println("Address: " + shelter.getAddress());
            System.out.println("Spaces available: " + shelter.getRemainingCapacity());

            if (!reliefApp.assignRequestToMe(request.getRequestId(), shelter)) {
                System.out.println("The request could not be assigned.");
                return;
            }

            assigned = true;
            System.out.println("Assigned to " + rosa.getUsername()
                    + ". Request status: " + request.getStatus());

            if (request.getStatus() != RequestStatus.ASSIGNED) {
                System.out.println("The status should be ASSIGNED.");
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Map error: " + e.getMessage());
        } finally {
            reliefApp.logout();
        }

        if (assigned) {
            showAssignmentToOtherVolunteer();
        }
    }

    /**
     * Shows that a different volunteer can see who is handling the Carter family's request.
     */
    private void showAssignmentToOtherVolunteer() {
        User maria = reliefApp.login("mrivera", "password");

        if (maria == null) {
            System.out.println("Login failed.");
            return;
        }

        try {
            for (AidRequest request : reliefApp.getRequests()) {
                if (request.getRequester() != null
                        && CARTER_USERNAME.equals(request.getRequester().getUsername())) {
                    String handler = "nobody";
                    for (TaskAssignment task : request.getAssignments()) {
                        if (task.getVolunteer() != null) {
                            handler = task.getVolunteer().getUsername();
                        }
                    }
                    System.out.println(maria.getUsername() + " sees the Carter request: status "
                            + request.getStatus() + ", handled by " + handler);
                }
            }
        } finally {
            reliefApp.logout();
        }
    }

    /**
     * Scenario 13: Rosa exports the Carter family's trip information to a printable .txt file.
     */
    public void scenario13() {
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

            // the shelter Rosa selected when she took the request
            Shelter shelter = reliefApp.getSelectedShelter(request);

            if (shelter == null) {
                System.out.println("No shelter was selected for this request.");
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
            System.out.println("Couldn't create or read the exported file.");
        } catch (IllegalStateException e) {
            System.out.println("Map error: " + e.getMessage());
        } finally {
            reliefApp.logout();
        }
    }

    /**
     * Scenario 14: Rosa (volunteer) tries to mark the request complete and is refused.
     */
    public void scenario14() {
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
     * Scenario 15: Felix checks the Carter family and Buddy into his shelter,
     * then marks their aid request complete.
     */
    public void scenario15() {
        System.out.println();

        User felix = reliefApp.login(FELIX_USERNAME, FELIX_PASSWORD);
        if (felix == null) {
            System.out.println("Felix could not log in.");
            return;
        }

        try {
            System.out.println("Logged in: " + felix.getFullName() + " (" + felix.getType() + ")");

            Shelter shelter = reliefApp.getMyShelter();
            if (shelter == null) {
                System.out.println("Felix's shelter was not found.");
                return;
            }

            System.out.println("\nBefore check-in:");
            printShelter(shelter);
            int occupancyBefore = shelter.getOccupancy();
            int petsBefore = shelter.getPets().size();

            ArrayList<String> family = new ArrayList<String>();
            for (String person : CARTER_FAMILY) {
                family.add(person);
            }
            ArrayList<String> pets = new ArrayList<String>();
            pets.add(CARTER_PET);

            if (!reliefApp.checkInToShelter(family, pets)) {
                System.out.println("Check in failed. The family may already be checked in,"
                        + " or the shelter may not have room.");
                return;
            }

            System.out.println("\nChecked in the Carter family and " + CARTER_PET + ".");
            System.out.println("After check-in:");
            printShelter(shelter);

            if (shelter.getOccupancy() != occupancyBefore + family.size()) {
                System.out.println("Occupancy should have gone up by " + family.size() + ".");
            }
            if (shelter.getPets().size() != petsBefore + 1) {
                System.out.println("Buddy should be recorded as an accompanying pet.");
            }

            // Felix can complete the request, unlike Rosa
            AidRequest request = reliefApp.getRequestFrom(CARTER_USERNAME);
            if (request == null) {
                System.out.println("The Carter family's request was not found.");
                return;
            }

            try {
                if (reliefApp.completeRequest(request.getRequestId())) {
                    System.out.println("\nRequest marked complete. Status: " + request.getStatus());
                } else {
                    System.out.println("\nThe request could not be completed.");
                }
            } catch (SecurityException e) {
                System.out.println("\nNot allowed: " + e.getMessage());
            }
        } finally {
            reliefApp.logout();
        }
    }

    /**
     * Describes the people and pets in a user's households, such as
     * "2 adults, 1 child, 1 dog".
     *
     * @param requester the user whose households to count
     * @return the description
     */
    private String describeGroup(User requester) {
        int adults = 0;
        int children = 0;
        Map<String, Integer> pets = new LinkedHashMap<String, Integer>();

        for (Household household : requester.getHouseholds()) {
            for (HouseholdMember member : household.getMembers()) {
                if (member.isPet()) {
                    pets.merge(member.getPetType(), 1, Integer::sum);
                } else if (member.getAge() >= 18) {
                    adults++;
                } else {
                    children++;
                }
            }
        }

        ArrayList<String> parts = new ArrayList<String>();
        parts.add(adults + (adults == 1 ? " adult" : " adults"));
        parts.add(children + (children == 1 ? " child" : " children"));
        for (Map.Entry<String, Integer> pet : pets.entrySet()) {
            parts.add(pet.getValue() + " " + pet.getKey() + (pet.getValue() == 1 ? "" : "s"));
        }
        return String.join(", ", parts);
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
     * Checks the exported lines for the required trip information.
     *
     * @param rosa the volunteer making the trip
     * @param request the request being handled
     * @param shelter the destination shelter
     * @param lines the exported file's lines
     * @return descriptions of missing information
     * @throws IOException if a map lookup fails
     * @throws InterruptedException if a map request is interrupted
     */
    private ArrayList<String> findMissingFromExport(
            User rosa, AidRequest request, Shelter shelter, List<String> lines)
            throws IOException, InterruptedException {
        ArrayList<String> missing = new ArrayList<String>();
        User requester = request.getRequester();
        // Check household names, ages, pet types, and medical notes.
        for (Household household : requester.getHouseholds()) {
            for (HouseholdMember member : household.getMembers()) {
                String detail = member.isPet()
                        ? member.getPetType()
                        : String.valueOf(member.getAge());
                if (detail == null
                        || !hasLine(lines, member.getName(), detail)) {
                    missing.add("Details for " + member.getName());
                }
                String medicalNote = member.getMedicalNote();

                if (medicalNote != null && !medicalNote.trim().isEmpty()
                        && !hasText(lines, medicalNote)) {
                    missing.add("Medical note for " + member.getName());
                }
            }
        }
        // Convert the shelter's Address into a String.
        String shelterAddress = shelter.getAddress() == null
                ? null
                : shelter.getAddress().toString();
        String[] labels = {
            "phone number",
            "email address",
            "pickup address",
            "shelter name",
            "shelter address"
        };
        String[] required = {
            requester.getPhoneNumber(),
            requester.getEmailAddress(),
            request.getStreetAddress(),
            shelter.getName(),
            shelterAddress
        };
        for (int i = 0; i < required.length; i++) {
            if (!hasText(lines, required[i])) {
                missing.add(labels[i]);
            }
        }
        // Check directions from the volunteer's home to the pickup address.
        ArrayList<String> pickupDirections = reliefApp.getDirections(
                rosa.getHomeAddress(), request.getStreetAddress());

        if (pickupDirections.isEmpty()) {
            missing.add("Directions to the pickup address could not be verified");
        } else {
            for (String step : pickupDirections) {
                if (!hasText(lines, step)) {
                    missing.add("Direction to the pickup address: " + step);
                }
            }
        }
        // Check directions from the pickup address to the shelter.
        if (shelterAddress != null) {
            ArrayList<String> shelterDirections = reliefApp.getDirections(
                    request.getStreetAddress(), shelterAddress);

            if (shelterDirections.isEmpty()) {
                missing.add("Directions to the shelter could not be verified");
            } else {
                for (String step : shelterDirections) {
                    if (!hasText(lines, step)) {
                        missing.add("Direction to the shelter: " + step);
                    }
                }
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
     * Starts the UI. With no argument it runs all the scenarios; with the argument
     * "verify" it shows what was saved, to be run after a restart.
     *
     * @param args "verify" to check the saved data after a restart, otherwise not used
     */
    public static void main(String[] args) {
        ReliefUI reliefInterface = new ReliefUI();
        if (args.length > 0 && args[0].equalsIgnoreCase("verify")) {
            reliefInterface.verify();
        } else {
            reliefInterface.run();
        }
    }
}