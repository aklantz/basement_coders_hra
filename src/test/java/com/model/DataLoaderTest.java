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
 * Tests for DataLoader.
 * Created with AI assistance.
 *
 * +---------------------------------------------------+------------------------------------------------------+
 * | Test                                              | Reasoning                                            |
 * +---------------------------------------------------+------------------------------------------------------+
 * | empty files return empty lists                    | no saved records should mean no loaded records       |
 * | users keep account and contact details            | loaded accounts should match saved information       |
 * | each user type creates the correct subclass       | roles need their own behavior                        |
 * | volunteers and managers keep optional fields      | specialty and shelter links should be restored       |
 * | households restore members and older defaults     | nested data and older files should both load          |
 * | legacy household key is accepted                  | older user files should remain readable              |
 * | shelters restore details and inventory            | cached resources should retain their saved data      |
 * | missing shelter fields use defaults               | optional lists and flags should not prevent loading  |
 * | requests link to the existing requester           | requests should use users from the directory         |
 * | assignments restore users, status, and times       | saved task history should survive loading            |
 * | missing or wrong-role assignees become null        | invalid links should not create incorrect user types |
 * | optional request fields use defaults              | older requests should still load                     |
 * | hurricane details and affected areas load          | hurricane getters should return the saved values     |
 * +---------------------------------------------------+------------------------------------------------------+
 */
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
    public void emptyFilesReturnEmptyLists() {
        assertTrue(DataLoader.getUsers().isEmpty());
        assertTrue(DataLoader.getCachedShelters().isEmpty());
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
    public void getUsersCreatesEachUserType() throws Exception {
        writeFixture("users.json", array(makeUser(USERTYPE.RESIDENT), makeUser(USERTYPE.VOLUNTEER),
                makeUser(USERTYPE.COORDINATOR), makeUser(USERTYPE.ADMIN), makeUser(USERTYPE.SHELTER_MANAGER)));

        ArrayList<User> users = DataLoader.getUsers();

        assertEquals(5, users.size());
        assertTrue(users.get(0) instanceof Resident);
        assertTrue(users.get(1) instanceof Volunteer);
        assertTrue(users.get(2) instanceof Coordinator);
        assertTrue(users.get(3) instanceof Admin);
        assertTrue(users.get(4) instanceof ShelterManager);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getUsersRestoresVolunteerSpecialtyAndManagerShelter() throws Exception {
        JSONObject volunteer = makeUser(USERTYPE.VOLUNTEER);
        volunteer.put("specialty", "MEDICAL");
        JSONObject manager = makeUser(USERTYPE.SHELTER_MANAGER);
        manager.put("shelterId", shelterId.toString());
        writeFixture("users.json", array(volunteer, manager));

        ArrayList<User> users = DataLoader.getUsers();

        assertEquals(2, users.size());
        assertEquals(RequestCategory.MEDICAL, ((Volunteer) users.get(0)).getSpecialty());
        assertEquals(shelterId, ((ShelterManager) users.get(1)).getShelterId());
    }

    @Test
    public void getUsersAllowsMissingRoleFields() throws Exception {
        writeFixture("users.json", array(makeUser(USERTYPE.VOLUNTEER), makeUser(USERTYPE.SHELTER_MANAGER)));

        ArrayList<User> users = DataLoader.getUsers();

        assertEquals(2, users.size());
        assertNull(((Volunteer) users.get(0)).getSpecialty());
        assertNull(((ShelterManager) users.get(1)).getShelterId());
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
    @SuppressWarnings("unchecked")
    public void getAidRequestsAllowsMissingAndWrongRoleAssignees() throws Exception {
        addRequester();
        JSONObject request = makeRequest();
        request.put("assignments", array(
                object("taskId", UUID.randomUUID().toString(), "status", "PENDING"),
                object("taskId", UUID.randomUUID().toString(), "status", "PENDING",
                        "volunteer", userId.toString(), "coordinator", UUID.randomUUID().toString())));
        writeFixture("aidrequests.json", array(request));

        ArrayList<AidRequest> requests = DataLoader.getAidRequests();

        assertEquals(1, requests.size());
        assertEquals(2, requests.get(0).getAssignments().size());
        for (TaskAssignment task : requests.get(0).getAssignments()) {
            assertNull(task.getVolunteer());
            assertNull(task.getCoordinator());
            assertNull(task.getAcceptedAt());
            assertNull(task.getCompletedAt());
            assertEquals(TaskStatus.PENDING, task.getStatus());
        }
    }

    @Test
    public void getHurricaneDetailsMatchesSavedValues() {
        assertEquals(HurricaneCategory.CATEGORY_4, DataLoader.getHurricaneCategory());
        assertEquals(140, DataLoader.getWindSpeed());
        ArrayList<ZipCode> zipCodes = DataLoader.getAffectedZipCodes();
        assertEquals(2, zipCodes.size());
        assertEquals("29401", zipCodes.get(0).getCode());
        assertEquals("29403", zipCodes.get(1).getCode());
    }

    @Test
    public void getAffectedZipCodesAllowsNoAffectedAreas() throws Exception {
        writeFixture("hurricanedirectory.json", object("category", "CATEGORY_1",
                "windSpeed", 75, "affectedAreas", new JSONArray()));

        assertTrue(DataLoader.getAffectedZipCodes().isEmpty());
        assertEquals(HurricaneCategory.CATEGORY_1, DataLoader.getHurricaneCategory());
        assertEquals(75, DataLoader.getWindSpeed());
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
