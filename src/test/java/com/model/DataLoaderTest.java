package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * +--------------------------------------------------------------------------------+----------------------------------------------------------------------------+
 * | Test                                                                           | Reasoning                                                                  |
 * +--------------------------------------------------------------------------------+----------------------------------------------------------------------------+
 * | getUsersReturnsEmptyListForEmptyFile                                           | An empty collection must not retain stale records.                         |
 * | getCachedSheltersReturnsEmptyListForEmptyFile                                  | An empty collection must not retain stale records.                         |
 * | getAidRequestsReturnsEmptyListForEmptyFile                                     | An empty collection must not retain stale records.                         |
 * | getUsersRestoresAccountAndContactDetails                                       | The saved record must match the explicit fixture values.                   |
 * | getUsersCreatesResident                                                        | The saved role must construct the matching user subclass.                  |
 * | getUsersCreatesVolunteer                                                       | The saved role must construct the matching user subclass.                  |
 * | getUsersCreatesCoordinator                                                     | The saved role must construct the matching user subclass.                  |
 * | getUsersCreatesAdmin                                                           | The saved role must construct the matching user subclass.                  |
 * | getUsersCreatesShelterManager                                                  | The saved role must construct the matching user subclass.                  |
 * | getUsersRestoresVolunteerSpecialty                                             | The MEDICAL specialty must remain persistence.                            |
 * | getUsersRestoresManagerShelter                                                 | The manager must retain the saved shelter UUID.                            |
 * | getUsersAllowsMissingVolunteerSpecialty                                        | Absent optional data must use the specified default.         |
 * | getUsersAllowsMissingManagerShelter                                            | Absent optional data must use the specified default.         |
 * | getUsersRestoresHouseholdMembersAndOlderDefaults                               | Older records must receive defaults for optional fields.                   |
 * | getUsersAcceptsLegacyHouseholdKey                                              | Previously saved household keys must remain readable.                      |
 * | getCachedSheltersRestoresDetailsInventoryAndOccupants                          | The shelter record must retain its saved resource information.             |
 * | getCachedSheltersDefaultsMissingOptionalFields                                 | Absent optional data must use the specified default.         |
 * | getAidRequestsRestoresDetailsAndRequester                                      | The saved record must match the explicit fixture values.                   |
 * | getAidRequestsDefaultsMissingOptionalFields                                    | Absent optional data must use the specified default.         |
 * | getAidRequestsRestoresAssignmentUsersAndHistory                                | Saved task identities, participants, status, and times must remain.       |
 * | getAidRequestsAllowsMissingAssignees                                           | Absent optional data must use the specified default.         |
 * | getAidRequestsAllowsUnknownAssignees                                           | Unresolvable user IDs must not create assignment participants.             |
 * | getAidRequestsAllowsWrongRoleAssignees                                         | A resident must not be loaded as a volunteer or coordinator.               |
 * | getHurricaneCategoryReturnsSavedCategory                                       | CATEGORY_4 in the fixture must load as CATEGORY_4.                         |
 * | getWindSpeedReturnsSavedSpeed                                                  | The fixture wind speed of 140 must load unchanged.                         |
 * | getAffectedZipCodesReturnsSavedAreas                                           | Both fixture zip codes must load in their saved order.                     |
 * | getAffectedZipCodesAllowsNoAffectedAreas                                       | No affected areas must produce an empty zip-code list.                     |
 * | getUsersReturnsEmptyListForMalformedJSON                                       | Unreadable JSON must return the documented empty/default result.           |
 * | getUsersReturnsEmptyListForInvalidRecord                                       | Missing required fields must not create an incomplete record.              |
 * | getCachedSheltersReturnsEmptyListForMalformedJSON                              | Unreadable JSON must return the documented empty/default result.           |
 * | getCachedSheltersReturnsEmptyListForInvalidRecord                              | Missing required fields must not create an incomplete record.              |
 * | getAidRequestsReturnsEmptyListForMalformedJSON                                 | Unreadable JSON must return the documented empty/default result.           |
 * | getAidRequestsReturnsEmptyListForInvalidRecord                                 | Missing required fields must not create an incomplete record.              |
 * | getWindSpeedHandlesMalformedFile                                               | Unreadable JSON must return the documented empty/default result.           |
 * | getHurricaneCategoryHandlesMalformedFile                                       | Unreadable JSON must return the documented empty/default result.           |
 * | getAffectedZipCodesHandlesMalformedFile                                        | Unreadable JSON must return the documented empty/default result.           |
 * | getUsersLoadsMultipleRecordsInOrder                                            | Every supplied record must remain, in its original order.                 |
 * | getSheltersLoadsMultipleRecordsInOrder                                         | Every supplied record must remain, in its original order.                 |
 * | getRequestsLoadsMultipleRecordsInOrder                                         | Every supplied record must remain, in its original order.                 |
 * +--------------------------------------------------------------------------------+----------------------------------------------------------------------------+
 */
@SuppressWarnings("unchecked")
public class DataLoaderTest {

    private Path resourceDirectory;
    private Map<Path, byte[]> originalFiles;
    private Field directoryField;
    private UserDirectory originalDirectory;
    private ArrayList<User> originalUsers;
    private UserDirectory directory;
    private UUID userId;
    private UUID requestId;
    private UUID shelterId;

    @Before
    public void setUp() throws Exception {
        // Use the copied test resources, matching the loader resource paths.
        // Back up every file before changing it; source and application JSON stay untouched.
        resourceDirectory = Paths.get(DataLoader.class.getResource("/data/users.json").toURI()).getParent();
        originalFiles = new LinkedHashMap<Path, byte[]>();
        directoryField = UserDirectory.class.getDeclaredField("userDirectory");
        directoryField.setAccessible(true);
        originalDirectory = (UserDirectory) directoryField.get(null);
        if (originalDirectory != null) {
            originalUsers = new ArrayList<User>(originalDirectory.getUsers());
        }
        userId = UUID.randomUUID();
        requestId = UUID.randomUUID();
        shelterId = UUID.randomUUID();
        writeFixture("users.json", new JSONArray());
        writeFixture("shelters.json", new JSONArray());
        writeFixture("aidrequests.json", new JSONArray());
        writeFixture("hurricanedirectory.json", object("category", "CATEGORY_4",
                "windSpeed", 140, "affectedAreas", array(object("zipCode", "29401"),
                        object("zipCode", "29403"))));
        directory = UserDirectory.getInstance();
        directory.getUsers().clear();
    }

    @After
    public void tearDown() throws Exception {
        if (originalDirectory != null && originalUsers != null) {
            originalDirectory.getUsers().clear();
            originalDirectory.getUsers().addAll(originalUsers);
        }
        if (directoryField != null) {
            directoryField.set(null, originalDirectory);
        }
        if (originalFiles != null) {
            for (Map.Entry<Path, byte[]> entry : originalFiles.entrySet()) {
                if (entry.getValue() == null) {
                    Files.deleteIfExists(entry.getKey());
                } else {
                    Files.write(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Test
    public void getUsersReturnsEmptyListForEmptyFile() throws Exception {
        assertTrue(DataLoader.getUsers().isEmpty());
    }

    @Test
    public void getCachedSheltersReturnsEmptyListForEmptyFile() throws Exception {
        assertTrue(DataLoader.getCachedShelters().isEmpty());
    }

    @Test
    public void getAidRequestsReturnsEmptyListForEmptyFile() throws Exception {
        assertTrue(DataLoader.getAidRequests().isEmpty());
    }

    @Test
    public void getUsersRestoresAccountAndContactDetails() throws Exception {
        writeFixture("users.json", array(makeUser(USERTYPE.RESIDENT)));

        ArrayList<User> users = DataLoader.getUsers();

        assertEquals(1, users.size());
        User user = users.get(0);
        assertEquals(userId, user.getUserId());
        assertEquals("rae", user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals("Rae Resident", user.getFullName());
        assertEquals(LocalDate.of(1990, 1, 1), user.getDateOfBirth());
        assertEquals("10 Main St", user.getHomeAddress());
        assertEquals("rae@example.com", user.getEmailAddress());
        assertEquals("803-555-0100", user.getPhoneNumber());
        assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
        assertEquals(SafetyStatus.IN_NEED, user.getSafetyStatus());
        assertTrue(user.getHouseholds().isEmpty());
    }

    @Test
    public void getUsersCreatesResident() throws Exception {
        writeFixture("users.json", array(makeUser(USERTYPE.RESIDENT)));
        ArrayList<User> users = DataLoader.getUsers();
        assertEquals(1, users.size());
        assertTrue(users.get(0) instanceof Resident);
    }

    @Test
    public void getUsersCreatesVolunteer() throws Exception {
        writeFixture("users.json", array(makeUser(USERTYPE.VOLUNTEER)));
        ArrayList<User> users = DataLoader.getUsers();
        assertEquals(1, users.size());
        assertTrue(users.get(0) instanceof Volunteer);
    }

    @Test
    public void getUsersCreatesCoordinator() throws Exception {
        writeFixture("users.json", array(makeUser(USERTYPE.COORDINATOR)));
        ArrayList<User> users = DataLoader.getUsers();
        assertEquals(1, users.size());
        assertTrue(users.get(0) instanceof Coordinator);
    }

    @Test
    public void getUsersCreatesAdmin() throws Exception {
        writeFixture("users.json", array(makeUser(USERTYPE.ADMIN)));
        ArrayList<User> users = DataLoader.getUsers();
        assertEquals(1, users.size());
        assertTrue(users.get(0) instanceof Admin);
    }

    @Test
    public void getUsersCreatesShelterManager() throws Exception {
        writeFixture("users.json", array(makeUser(USERTYPE.SHELTER_MANAGER)));
        ArrayList<User> users = DataLoader.getUsers();
        assertEquals(1, users.size());
        assertTrue(users.get(0) instanceof ShelterManager);
    }

    @Test
    public void getUsersRestoresVolunteerSpecialty() throws Exception {
        JSONObject user = makeUser(USERTYPE.VOLUNTEER);
        user.put("specialty", "MEDICAL");
        writeFixture("users.json", array(user));
        assertEquals(RequestCategory.MEDICAL, ((Volunteer) DataLoader.getUsers().get(0)).getSpecialty());
    }

    @Test
    public void getUsersRestoresManagerShelter() throws Exception {
        JSONObject user = makeUser(USERTYPE.SHELTER_MANAGER);
        user.put("shelterId", shelterId.toString());
        writeFixture("users.json", array(user));
        assertEquals(shelterId, ((ShelterManager) DataLoader.getUsers().get(0)).getShelterId());
    }

    @Test
    public void getUsersAllowsMissingVolunteerSpecialty() throws Exception {
        writeFixture("users.json", array(makeUser(USERTYPE.VOLUNTEER)));
        assertNull(((Volunteer) DataLoader.getUsers().get(0)).getSpecialty());
    }

    @Test
    public void getUsersAllowsMissingManagerShelter() throws Exception {
        writeFixture("users.json", array(makeUser(USERTYPE.SHELTER_MANAGER)));
        assertNull(((ShelterManager) DataLoader.getUsers().get(0)).getShelterId());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getUsersRestoresHouseholdMembersAndOlderDefaults() throws Exception {
        JSONObject user = makeUser(USERTYPE.RESIDENT);
        user.put("households", array(makeHousehold()));
        writeFixture("users.json", array(user));

        ArrayList<User> users = DataLoader.getUsers();
        assertEquals(1, users.size());
        assertEquals(1, users.get(0).getHouseholds().size());
        Household household = users.get(0).getHouseholds().get(0);
        assertEquals("Family", household.getHouseholdName());
        assertEquals(userId, household.getHouseholdOwnerUUID());
        assertEquals(2, household.getMembers().size());
        HouseholdMember pet = household.getMembers().get(0);
        assertEquals("Biscuit", pet.getName());
        assertEquals(3, pet.getAge());
        assertEquals("dog", pet.getPetType());
        assertEquals("Needs medicine", pet.getMedicalNote());
        assertEquals(SafetyStatus.SAFE, pet.getSafetyStatus());
        HouseholdMember person = household.getMembers().get(1);
        assertEquals("Rae", person.getName());
        assertEquals(0, person.getAge());
        assertNull(person.getPetType());
        assertNull(person.getMedicalNote());
        assertEquals(SafetyStatus.IN_NEED, person.getSafetyStatus());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getUsersAcceptsLegacyHouseholdKey() throws Exception {
        JSONObject user = makeUser(USERTYPE.RESIDENT);
        user.put("houseHoldMembers", array(makeHousehold()));
        writeFixture("users.json", array(user));

        ArrayList<User> users = DataLoader.getUsers();

        assertEquals(1, users.size());
        assertEquals(1, users.get(0).getHouseholds().size());
        assertEquals("Family", users.get(0).getHouseholds().get(0).getHouseholdName());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getCachedSheltersRestoresDetailsInventoryAndOccupants() throws Exception {
        UUID itemId = UUID.randomUUID();
        JSONObject shelter = makeShelter();
        shelter.putAll(object("streetAddress", "20 Oak St", "city", "Columbia", "state", "SC",
                "zipCode", "29201", "latitude", 34.0, "longitude", -81.0,
                "medicalStaff", true, "dogsAllowed", true,
                "occupants", array("Rae"), "pets", array("Biscuit"),
                "inventoryItems", array(object("itemId", itemId.toString(), "supplyType", "Water",
                        "quantity", 20, "reorderThreshold", 5))));
        writeFixture("shelters.json", array(shelter));

        ArrayList<Shelter> shelters = DataLoader.getCachedShelters();

        assertEquals(1, shelters.size());
        Shelter loaded = shelters.get(0);
        assertEquals(shelterId, loaded.getResourceId());
        assertEquals("School", loaded.getName());
        assertEquals(100, loaded.getCapacity());
        assertEquals(1, loaded.getOccupancy());
        assertEquals(ShelterStatus.CLOSED, loaded.getStatus());
        assertEquals("20 Oak St", loaded.getStreetAddress());
        assertEquals("Columbia", loaded.getCity());
        assertEquals("SC", loaded.getState());
        assertEquals("29201", loaded.getZipCode());
        assertEquals(34.0, loaded.getLatitude(), 0.0001);
        assertEquals(-81.0, loaded.getLongitude(), 0.0001);
        assertTrue(loaded.hasMedicalStaff());
        assertTrue(loaded.acceptsDogs());
        assertEquals(Arrays.asList("Rae"), loaded.getOccupants());
        assertEquals(Arrays.asList("Biscuit"), loaded.getPets());
        assertEquals(1, loaded.getInventoryItems().size());
        InventoryItem item = loaded.getInventoryItems().get(0);
        assertEquals(itemId, item.getItemId());
        assertEquals("Water", item.getSupplyType());
        assertEquals(20, item.getQuantity());
        assertEquals(5, item.getReorderThreshold());
    }

    @Test
    public void getCachedSheltersDefaultsMissingOptionalFields() throws Exception {
        writeFixture("shelters.json", array(makeShelter()));

        ArrayList<Shelter> shelters = DataLoader.getCachedShelters();

        assertEquals(1, shelters.size());
        Shelter shelter = shelters.get(0);
        assertTrue(shelter.getInventoryItems().isEmpty());
        assertTrue(shelter.getOccupants().isEmpty());
        assertTrue(shelter.getPets().isEmpty());
        assertNull(shelter.getLatitude());
        assertNull(shelter.getLongitude());
        assertFalse(shelter.hasMedicalStaff());
        assertFalse(shelter.acceptsDogs());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAidRequestsRestoresDetailsAndRequester() throws Exception {
        User requester = addRequester();
        JSONObject request = makeRequest();
        request.putAll(object("isDuplicate", true, "note", "Please bring food",
                "shelterId", shelterId.toString(), "createdAt", "2026-10-01T09:30:00Z"));
        writeFixture("aidrequests.json", array(request));

        ArrayList<AidRequest> requests = DataLoader.getAidRequests();

        assertEquals(1, requests.size());
        AidRequest loaded = requests.get(0);
        assertEquals(requestId, loaded.getRequestId());
        assertSame(requester, loaded.getRequester());
        assertEquals(RequestCategory.FOOD, loaded.getCategory());
        assertEquals(RequestStatus.SUBMITTED, loaded.getStatus());
        assertEquals(2, loaded.getHouseholdSize());
        assertEquals("10 Main St", loaded.getStreetAddress());
        assertTrue(loaded.isDuplicate());
        assertEquals("Please bring food", loaded.getNote());
        assertEquals(shelterId, loaded.getShelterId());
        assertEquals(LocalDateTime.of(2026, 10, 1, 9, 30), loaded.getCreatedAt());
        assertTrue(loaded.getAssignments().isEmpty());
    }

    @Test
    public void getAidRequestsDefaultsMissingOptionalFields() throws Exception {
        addRequester();
        writeFixture("aidrequests.json", array(makeRequest()));

        ArrayList<AidRequest> requests = DataLoader.getAidRequests();

        assertEquals(1, requests.size());
        AidRequest request = requests.get(0);
        assertFalse(request.isDuplicate());
        assertNull(request.getNote());
        assertNull(request.getShelterId());
        assertNotNull(request.getCreatedAt());
        assertTrue(request.getAssignments().isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAidRequestsRestoresAssignmentUsersAndHistory() throws Exception {
        addRequester();
        Volunteer volunteer = new Volunteer(UUID.randomUUID(), "val", "password", "Val Volunteer",
                LocalDate.of(1990, 1, 1), null, null, null, null);
        Coordinator coordinator = new Coordinator(UUID.randomUUID(), "cora", "password", "Cora Coordinator",
                LocalDate.of(1990, 1, 1), null, null, null, null);
        directory.getUsers().add(volunteer);
        directory.getUsers().add(coordinator);
        UUID taskId = UUID.randomUUID();
        JSONObject request = makeRequest();
        request.put("assignments", array(object("taskId", taskId.toString(),
                "volunteer", volunteer.getUserId().toString(), "coordinator", coordinator.getUserId().toString(),
                "assignedAt", "2026-10-01T09:00:00Z", "acceptedAt", "2026-10-01T09:30:00Z",
                "completedAt", "2026-10-01T10:00:00Z", "status", "COMPLETED")));
        writeFixture("aidrequests.json", array(request));

        ArrayList<AidRequest> requests = DataLoader.getAidRequests();

        assertEquals(1, requests.size());
        AidRequest loaded = requests.get(0);
        assertEquals(1, loaded.getAssignments().size());
        TaskAssignment task = loaded.getAssignments().get(0);
        assertEquals(taskId, task.getTaskId());
        assertSame(loaded, task.getRequest());
        assertSame(volunteer, task.getVolunteer());
        assertSame(coordinator, task.getCoordinator());
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertEquals(LocalDateTime.of(2026, 10, 1, 9, 0), task.getAssignedAt());
        assertEquals(LocalDateTime.of(2026, 10, 1, 9, 30), task.getAcceptedAt());
        assertEquals(LocalDateTime.of(2026, 10, 1, 10, 0), task.getCompletedAt());
    }

    @Test
    public void getAidRequestsAllowsMissingAssignees() throws Exception {
        addRequester();
        JSONObject request = makeRequest();
        JSONObject assignment = object("taskId", UUID.randomUUID().toString(), "status", "PENDING");
        request.put("assignments", array(assignment));
        writeFixture("aidrequests.json", array(request));
        TaskAssignment loaded = DataLoader.getAidRequests().get(0).getAssignments().get(0);
        assertNull(loaded.getVolunteer());
        assertNull(loaded.getCoordinator());
    }

    @Test
    public void getAidRequestsAllowsUnknownAssignees() throws Exception {
        addRequester();
        JSONObject request = makeRequest();
        JSONObject assignment = object("taskId", UUID.randomUUID().toString(), "status", "PENDING");
        assignment.put("volunteer", UUID.randomUUID().toString());
        assignment.put("coordinator", UUID.randomUUID().toString());
        request.put("assignments", array(assignment));
        writeFixture("aidrequests.json", array(request));
        TaskAssignment loaded = DataLoader.getAidRequests().get(0).getAssignments().get(0);
        assertNull(loaded.getVolunteer());
        assertNull(loaded.getCoordinator());
    }

    @Test
    public void getAidRequestsAllowsWrongRoleAssignees() throws Exception {
        addRequester();
        JSONObject request = makeRequest();
        JSONObject assignment = object("taskId", UUID.randomUUID().toString(), "status", "PENDING");
        assignment.put("volunteer", userId.toString());
        assignment.put("coordinator", userId.toString());
        request.put("assignments", array(assignment));
        writeFixture("aidrequests.json", array(request));
        TaskAssignment loaded = DataLoader.getAidRequests().get(0).getAssignments().get(0);
        assertNull(loaded.getVolunteer());
        assertNull(loaded.getCoordinator());
    }

    @Test
    public void getHurricaneCategoryReturnsSavedCategory() throws Exception {
        assertEquals(HurricaneCategory.CATEGORY_4, DataLoader.getHurricaneCategory());
    }

    @Test
    public void getWindSpeedReturnsSavedSpeed() throws Exception {
        assertEquals(140, DataLoader.getWindSpeed());
    }

    @Test
    public void getAffectedZipCodesReturnsSavedAreas() throws Exception {
        ArrayList<ZipCode> areas = DataLoader.getAffectedZipCodes();
        assertEquals(2, areas.size());
        assertEquals("29401", areas.get(0).getCode());
        assertEquals("29403", areas.get(1).getCode());
    }

    @Test
    public void getAffectedZipCodesAllowsNoAffectedAreas() throws Exception {
        writeFixture("hurricanedirectory.json", object("category", "CATEGORY_1",
                "windSpeed", 75, "affectedAreas", new JSONArray()));

        assertTrue(DataLoader.getAffectedZipCodes().isEmpty());
    }

    @Test
    public void getUsersReturnsEmptyListForMalformedJSON() throws Exception {
        writeFixture("users.json", "[broken");
        assertTrue(DataLoader.getUsers().isEmpty());
    }

    @Test
    public void getUsersReturnsEmptyListForInvalidRecord() throws Exception {
        writeFixture("users.json", array(object("invalid", true)));
        assertTrue(DataLoader.getUsers().isEmpty());
    }

    @Test
    public void getCachedSheltersReturnsEmptyListForMalformedJSON() throws Exception {
        writeFixture("shelters.json", "[broken");
        assertTrue(DataLoader.getCachedShelters().isEmpty());
    }

    @Test
    public void getCachedSheltersReturnsEmptyListForInvalidRecord() throws Exception {
        writeFixture("shelters.json", array(object("invalid", true)));
        assertTrue(DataLoader.getCachedShelters().isEmpty());
    }

    @Test
    public void getAidRequestsReturnsEmptyListForMalformedJSON() throws Exception {
        writeFixture("aidrequests.json", "[broken");
        assertTrue(DataLoader.getAidRequests().isEmpty());
    }

    @Test
    public void getAidRequestsReturnsEmptyListForInvalidRecord() throws Exception {
        writeFixture("aidrequests.json", array(object("invalid", true)));
        assertTrue(DataLoader.getAidRequests().isEmpty());
    }

    @Test
    public void getWindSpeedHandlesMalformedFile() throws Exception {
        writeFixture("hurricanedirectory.json", "{broken");
        assertEquals(0, DataLoader.getWindSpeed());
    }

    @Test
    public void getHurricaneCategoryHandlesMalformedFile() throws Exception {
        writeFixture("hurricanedirectory.json", "{broken");
        assertNull(DataLoader.getHurricaneCategory());
    }

    @Test
    public void getAffectedZipCodesHandlesMalformedFile() throws Exception {
        writeFixture("hurricanedirectory.json", "{broken");
        assertTrue(DataLoader.getAffectedZipCodes().isEmpty());
    }

    @Test
    public void getUsersLoadsMultipleRecordsInOrder() throws Exception {
        JSONObject first = makeUser(USERTYPE.RESIDENT);
        JSONObject second = makeUser(USERTYPE.RESIDENT);
        UUID secondId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        second.put("userId", secondId.toString());
        writeFixture("users.json", array(first, second));
        ArrayList<User> loaded = DataLoader.getUsers();
        assertEquals(2, loaded.size());
        assertEquals(first.get("userId"), loaded.get(0).getUserId().toString());
        assertEquals(secondId, loaded.get(1).getUserId());
    }

    @Test
    public void getSheltersLoadsMultipleRecordsInOrder() throws Exception {
        JSONObject first = makeShelter();
        JSONObject second = makeShelter();
        UUID secondId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        second.put("resourceId", secondId.toString());
        writeFixture("shelters.json", array(first, second));
        ArrayList<Shelter> loaded = DataLoader.getCachedShelters();
        assertEquals(2, loaded.size());
        assertEquals(first.get("resourceId"), loaded.get(0).getResourceId().toString());
        assertEquals(secondId, loaded.get(1).getResourceId());
    }

    @Test
    public void getRequestsLoadsMultipleRecordsInOrder() throws Exception {
        JSONObject first = makeRequest();
        JSONObject second = makeRequest();
        UUID secondId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        second.put("requestId", secondId.toString());
        writeFixture("aidrequests.json", array(first, second));
        ArrayList<AidRequest> loaded = DataLoader.getAidRequests();
        assertEquals(2, loaded.size());
        assertEquals(first.get("requestId"), loaded.get(0).getRequestId().toString());
        assertEquals(secondId, loaded.get(1).getRequestId());
    }

    private JSONObject makeUser(USERTYPE type) {
        return object("userId", userId.toString(), "username", "rae", "password", "password",
                "fullName", "Rae Resident", "dateOfBirth", "1990-01-01", "homeAddress", "10 Main St",
                "emailAddress", "rae@example.com", "phoneNumber", "803-555-0100", "type", type.name(),
                "accountStatus", "ACTIVE", "safetyStatus", "IN_NEED");
    }

    private JSONObject makeHousehold() {
        return object("householdName", "Family", "householdOwnerUUID", userId.toString(), "members",
                array(object("name", "Biscuit", "safetyStatus", "SAFE", "age", 3,
                        "petType", "dog", "medicalNote", "Needs medicine"),
                        object("name", "Rae", "safetyStatus", "IN_NEED")));
    }

    private JSONObject makeShelter() {
        return object("resourceId", shelterId.toString(), "name", "School", "capacity", 100,
                "occupancy", 1, "status", "CLOSED", "lastUpdated", "2026-10-01T08:00:00Z");
    }

    private JSONObject makeRequest() {
        return object("requestId", requestId.toString(), "requester", userId.toString(), "category", "FOOD",
                "status", "SUBMITTED", "householdSize", 2, "streetAddress", "10 Main St",
                "assignments", new JSONArray());
    }

    private User addRequester() {
        Resident resident = new Resident(userId, "rae", "password", "Rae Resident",
                LocalDate.of(1990, 1, 1), null, null, null, null);
        directory.getUsers().add(resident);
        return resident;
    }

    @SuppressWarnings("unchecked")
    private JSONObject object(Object... pairs) {
        JSONObject object = new JSONObject();
        for (int i = 0; i < pairs.length; i += 2) {
            object.put((String) pairs[i], pairs[i + 1]);
        }
        return object;
    }

    @SuppressWarnings("unchecked")
    private JSONArray array(Object... values) {
        JSONArray array = new JSONArray();
        array.addAll(Arrays.asList(values));
        return array;
    }

    private void writeFixture(String name, Object data) throws Exception {
        Path path = resourceDirectory.resolve(name);
        if (!originalFiles.containsKey(path)) {
            originalFiles.put(path, Files.exists(path) ? Files.readAllBytes(path) : null);
        }
        Files.writeString(path, data.toString(), StandardCharsets.UTF_8);
    }
}
