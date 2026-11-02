package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for DataWriter.
 * Created with AI assistance.
 *
 * +---------------------------------------------------+------------------------------------------------------+
 * | Test                                              | Reasoning                                            |
 * +---------------------------------------------------+------------------------------------------------------+
 * | null lists and entries fail                       | invalid data should not be saved                     |
 * | user details and households are serialized        | saved users should keep their nested data            |
 * | volunteer and manager fields are serialized       | role-specific information should be preserved        |
 * | request details and assignments are serialized    | saved requests should keep their task history        |
 * | optional request and assignment fields work       | missing optional values should not prevent saving    |
 * | shelter details and inventory are serialized      | saved shelters should keep resources and occupants   |
 * | times use UTC and allow null                      | timestamps should use a consistent format            |
 * | writing creates directories and replaces data     | saving should produce complete, readable JSON        |
 * | writing an empty array clears saved data          | removed records should not remain in the file        |
 * | invalid destination fails without leftover files  | failed writes should clean up temporary files        |
 * | public saves can be loaded again                  | reader and writer must use the same test resources   |
 * | public saves leave application JSON unchanged     | JUnit saves must use the test copies                 |
 * +---------------------------------------------------+------------------------------------------------------+
 */
public class DataWriterTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Resident resident;
    private AidRequest request;
    private Map<Path, byte[]> originalFiles;

    @Before
    public void setUp() throws Exception {
        originalFiles = new LinkedHashMap<Path, byte[]>();
        for (String name : new String[] { "users.json", "aidrequests.json", "shelters.json" }) {
            Path path = testFile(name);
            originalFiles.put(path, Files.readAllBytes(path));
        }
        resident = new Resident(UUID.randomUUID(), "rae", "password", "Rae Resident",
                LocalDate.of(1990, 1, 1), "10 Main St", "rae@example.com",
                "803-555-0100", "rae");
        request = new AidRequest(UUID.randomUUID(), resident, RequestCategory.FOOD,
                2, "10 Main St", "Please bring food");
        request.setCreatedAt(LocalDateTime.of(2026, 10, 1, 9, 30));
    }

    @Test
    public void savedUsersCanBeLoadedAgain() throws Exception {
        saveAndRead(resident);

        ArrayList<User> loaded = DataLoader.getUsers();

        assertEquals(1, loaded.size());
        assertEquals(resident.getUserId(), loaded.get(0).getUserId());
        assertEquals("rae", loaded.get(0).getUsername());
        assertEquals("Rae Resident", loaded.get(0).getFullName());
    }

    @Test
    public void savedRequestsCanBeLoadedAgain() throws Exception {
        UserDirectory directory = UserDirectory.getInstance();
        directory.getUsers().add(resident);
        try {
            saveAndRead(request);
            ArrayList<AidRequest> loaded = DataLoader.getAidRequests();

            assertEquals(1, loaded.size());
            assertEquals(request.getRequestId(), loaded.get(0).getRequestId());
            assertEquals(resident, loaded.get(0).getRequester());
            assertEquals("Please bring food", loaded.get(0).getNote());
            assertEquals(request.getCreatedAt(), loaded.get(0).getCreatedAt());
        } finally {
            directory.getUsers().remove(resident);
        }
    }

    @Test
    public void cachedSheltersCanBeLoadedAgain() throws Exception {
        Shelter shelter = new Shelter(UUID.randomUUID(), "School", 100, 1);
        ArrayList<Shelter> shelters = new ArrayList<Shelter>();
        shelters.add(shelter);

        assertTrue(DataWriter.cacheShelters(shelters));
        ArrayList<Shelter> loaded = DataLoader.getCachedShelters();

        assertEquals(1, loaded.size());
        assertEquals(shelter.getResourceId(), loaded.get(0).getResourceId());
        assertEquals("School", loaded.get(0).getName());
        assertEquals(100, loaded.get(0).getCapacity());
        assertEquals(1, loaded.get(0).getOccupancy());
    }

    @Test
    public void publicSavesUseTestResourcesAndLeaveApplicationFilesUnchanged() throws Exception {
        assertTrue(DataConstants.isJUnitTest());
        Map<Path, byte[]> applicationFiles = new LinkedHashMap<Path, byte[]>();
        for (String file : new String[] { DataConstants.USER_FILE,
                DataConstants.AID_REQUEST_FILE, DataConstants.RESOURCE_FILE }) {
            Path path = Paths.get(file);
            applicationFiles.put(path, Files.readAllBytes(path));
        }

        saveAndRead(resident);
        saveAndRead(request);
        saveAndRead(new Shelter(UUID.randomUUID(), "School", 100, 0));
        assertTrue(DataWriter.saveUsers(new ArrayList<User>()));
        assertTrue(DataWriter.saveAidRequests(new ArrayList<AidRequest>()));
        assertTrue(DataWriter.saveResources(new ArrayList<Shelter>()));
        assertTrue(readFile(testFile("users.json")).isEmpty());
        assertTrue(readFile(testFile("aidrequests.json")).isEmpty());
        assertTrue(readFile(testFile("shelters.json")).isEmpty());

        for (Map.Entry<Path, byte[]> entry : applicationFiles.entrySet()) {
            assertArrayEquals(entry.getKey().toString(), entry.getValue(), Files.readAllBytes(entry.getKey()));
        }
    }

    @Test
    public void saveMethodsRejectNullLists() {
        assertFalse(DataWriter.saveUsers(null));
        assertFalse(DataWriter.saveAidRequests(null));
        assertFalse(DataWriter.saveResources(null));
        assertFalse(DataWriter.cacheShelters(null));
    }

    @Test
    public void saveMethodsRejectNullEntries() {
        ArrayList<User> users = new ArrayList<User>();
        users.add(null);
        ArrayList<AidRequest> requests = new ArrayList<AidRequest>();
        requests.add(null);
        ArrayList<Shelter> shelters = new ArrayList<Shelter>();
        shelters.add(null);

        assertFalse(DataWriter.saveUsers(users));
        assertFalse(DataWriter.saveAidRequests(requests));
        assertFalse(DataWriter.saveResources(shelters));
        assertFalse(DataWriter.cacheShelters(shelters));
    }

    @Test
    public void userJSONKeepsAccountAndContactDetails() throws Exception {
        JSONObject saved = saveAndRead(resident);

        assertEquals(resident.getUserId().toString(), saved.get("userId"));
        assertEquals("rae", saved.get("username"));
        assertEquals("password", saved.get("password"));
        assertEquals("Rae Resident", saved.get("fullName"));
        assertEquals("1990-01-01", saved.get("dateOfBirth"));
        assertEquals("10 Main St", saved.get("homeAddress"));
        assertEquals("rae@example.com", saved.get("emailAddress"));
        assertEquals("803-555-0100", saved.get("phoneNumber"));
        assertEquals(resident.getType().name(), saved.get("type"));
        assertEquals(resident.getAccountStatus().name(), saved.get("accountStatus"));
        assertEquals(resident.getSafetyStatus().name(), saved.get("safetyStatus"));
        assertTrue(((JSONArray) saved.get("households")).isEmpty());
    }

    @Test
    public void userJSONKeepsHouseholdMembers() throws Exception {
        Household household = new Household(resident.getUserId(), "Family");
        household.addHouseholdMember("Biscuit", 3, "dog", "Needs medicine");
        resident.getHouseholds().add(household);

        JSONObject saved = saveAndRead(resident);
        JSONArray households = (JSONArray) saved.get("households");
        assertEquals(1, households.size());
        JSONObject savedHousehold = (JSONObject) households.get(0);
        assertEquals("Family", savedHousehold.get("householdName"));
        assertEquals(resident.getUserId().toString(), savedHousehold.get("householdOwnerUUID"));
        JSONArray members = (JSONArray) savedHousehold.get("members");
        assertEquals(1, members.size());
        JSONObject member = (JSONObject) members.get(0);
        assertEquals("Biscuit", member.get("name"));
        assertEquals(3L, member.get("age"));
        assertEquals("dog", member.get("petType"));
        assertEquals("Needs medicine", member.get("medicalNote"));
        assertEquals(household.getMembers().get(0).getSafetyStatus().name(), member.get("safetyStatus"));
    }

    @Test
    public void userJSONIncludesVolunteerSpecialtyWhenPresent() throws Exception {
        Volunteer volunteer = new Volunteer(UUID.randomUUID(), "val", "password",
                "Val Volunteer", LocalDate.of(1990, 1, 1), null, null, null, null);
        volunteer.setSpecialty(null);
        assertFalse(saveAndRead(volunteer).containsKey("specialty"));

        volunteer.setSpecialty(RequestCategory.MEDICAL);
        assertEquals("MEDICAL", saveAndRead(volunteer).get("specialty"));
    }

    @Test
    public void userJSONIncludesManagerShelterWhenPresent() throws Exception {
        ShelterManager manager = new ShelterManager(UUID.randomUUID(), "manager", "password",
                "Shelter Manager", LocalDate.of(1990, 1, 1), null, null, null, null, null);
        assertFalse(saveAndRead(manager).containsKey("shelterId"));

        UUID shelterId = UUID.randomUUID();
        manager.setShelterId(shelterId);
        assertEquals(shelterId.toString(), saveAndRead(manager).get("shelterId"));
    }

    @Test
    public void requestJSONKeepsDetailsAndShelter() throws Exception {
        UUID shelterId = UUID.randomUUID();
        request.setShelterId(shelterId);
        request.markDuplicate();
        JSONObject saved = saveAndRead(request);

        assertEquals(request.getRequestId().toString(), saved.get("requestId"));
        assertEquals(resident.getUserId().toString(), saved.get("requester"));
        assertEquals("FOOD", saved.get("category"));
        assertEquals(request.getStatus().name(), saved.get("status"));
        assertEquals(2L, saved.get("householdSize"));
        assertEquals("10 Main St", saved.get("streetAddress"));
        assertEquals(true, saved.get("isDuplicate"));
        assertEquals("2026-10-01T09:30Z", saved.get("createdAt"));
        assertEquals("Please bring food", saved.get("note"));
        assertEquals(shelterId.toString(), saved.get("shelterId"));
        assertTrue(((JSONArray) saved.get("assignments")).isEmpty());
    }

    @Test
    public void requestJSONOmitsMissingShelter() throws Exception {
        JSONObject saved = saveAndRead(request);

        assertFalse(saved.containsKey("shelterId"));
        assertEquals(false, saved.get("isDuplicate"));
    }

    @Test
    public void requestJSONKeepsAssignmentHistory() throws Exception {
        Volunteer volunteer = new Volunteer(UUID.randomUUID(), "val", "password",
                "Val Volunteer", LocalDate.of(1990, 1, 1), null, null, null, null);
        Coordinator coordinator = new Coordinator(UUID.randomUUID(), "cora", "password",
                "Cora Coordinator", LocalDate.of(1990, 1, 1), null, null, null, null);
        TaskAssignment task = new TaskAssignment(UUID.randomUUID(), request, volunteer, coordinator);
        task.restoreState(LocalDateTime.of(2026, 10, 1, 9, 0),
                LocalDateTime.of(2026, 10, 1, 9, 30),
                LocalDateTime.of(2026, 10, 1, 10, 0), TaskStatus.COMPLETED);
        request.addAssignment(task);

        JSONObject saved = saveAndRead(request);
        JSONArray assignments = (JSONArray) saved.get("assignments");
        assertEquals(1, assignments.size());
        JSONObject assignment = (JSONObject) assignments.get(0);
        assertEquals(task.getTaskId().toString(), assignment.get("taskId"));
        assertEquals(volunteer.getUserId().toString(), assignment.get("volunteer"));
        assertEquals(coordinator.getUserId().toString(), assignment.get("coordinator"));
        assertEquals("2026-10-01T09:00Z", assignment.get("assignedAt"));
        assertEquals("2026-10-01T09:30Z", assignment.get("acceptedAt"));
        assertEquals("2026-10-01T10:00Z", assignment.get("completedAt"));
        assertEquals("COMPLETED", assignment.get("status"));
    }

    @Test
    public void requestJSONAllowsMissingAssignmentPeopleAndTimes() throws Exception {
        request.addAssignment(new TaskAssignment(UUID.randomUUID(), request, null, null));
        JSONObject saved = saveAndRead(request);
        JSONObject assignment = (JSONObject) ((JSONArray) saved.get("assignments")).get(0);

        assertTrue(assignment.containsKey("volunteer"));
        assertNull(assignment.get("volunteer"));
        assertTrue(assignment.containsKey("coordinator"));
        assertNull(assignment.get("coordinator"));
        assertNull(assignment.get("acceptedAt"));
        assertNull(assignment.get("completedAt"));
        assertEquals("PENDING", assignment.get("status"));
    }

    @Test
    public void shelterJSONKeepsDetailsInventoryAndOccupants() throws Exception {
        Shelter shelter = new Shelter(UUID.randomUUID(), "School", 100, 1,
                ShelterStatus.OPEN, LocalDateTime.of(2026, 10, 1, 8, 0));
        shelter.setAddress("20 Oak St", "Columbia", "SC");
        shelter.setZipCode("29201");
        shelter.setCoordinates(34.0, -81.0);
        shelter.setMedicalStaff(true);
        shelter.setDogsAllowed(true);
        shelter.getOccupants().add("Rae");
        shelter.getPets().add("Biscuit");
        InventoryItem item = new InventoryItem(UUID.randomUUID(), "Water", 20, 5);
        shelter.getInventoryItems().add(item);

        JSONObject saved = saveAndRead(shelter);

        assertEquals(shelter.getResourceId().toString(), saved.get("resourceId"));
        assertEquals("School", saved.get("name"));
        assertEquals(100L, saved.get("capacity"));
        assertEquals(1L, saved.get("occupancy"));
        assertEquals("OPEN", saved.get("status"));
        assertEquals(shelter.getLastUpdated().atOffset(ZoneOffset.UTC).toString(), saved.get("lastUpdated"));
        assertEquals("20 Oak St", saved.get("streetAddress"));
        assertEquals("Columbia", saved.get("city"));
        assertEquals("SC", saved.get("state"));
        assertEquals("29201", saved.get("zipCode"));
        assertEquals(34.0, saved.get("latitude"));
        assertEquals(-81.0, saved.get("longitude"));
        assertEquals(true, saved.get("medicalStaff"));
        assertEquals(true, saved.get("dogsAllowed"));
        assertEquals(shelter.getOccupants(), saved.get("occupants"));
        assertEquals(shelter.getPets(), saved.get("pets"));
        JSONArray inventory = (JSONArray) saved.get("inventoryItems");
        assertEquals(1, inventory.size());
        JSONObject savedItem = (JSONObject) inventory.get(0);
        assertEquals(item.getItemId().toString(), savedItem.get("itemId"));
        assertEquals("Water", savedItem.get("supplyType"));
        assertEquals(20L, savedItem.get("quantity"));
        assertEquals(5L, savedItem.get("reorderThreshold"));
    }

    @Test
    public void formatTimeUsesUTCAndAllowsNull() throws Exception {
        Method method = DataWriter.class.getDeclaredMethod("formatTime", LocalDateTime.class);
        method.setAccessible(true);

        assertEquals("2026-10-01T09:30Z", method.invoke(null, LocalDateTime.of(2026, 10, 1, 9, 30)));
        assertNull(method.invoke(null, new Object[] { null }));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void writeFileCreatesDirectoriesAndPreservesSpecialCharacters() throws Exception {
        Path destination = temporaryFolder.getRoot().toPath().resolve("nested/users.json");
        JSONArray data = new JSONArray();
        data.add("Rae \"Resident\"\nCaf\u00e9");

        assertTrue(writeFile(destination, data));
        assertEquals(data, readFile(destination));
        assertEquals(1, destination.getParent().toFile().list().length);
    }

    @Test
    public void writeFileReplacesExistingDataWithEmptyArray() throws Exception {
        Path destination = temporaryFolder.newFile("users.json").toPath();
        Files.writeString(destination, "[\"old user\"]", StandardCharsets.UTF_8);

        assertTrue(writeFile(destination, new JSONArray()));
        assertTrue(readFile(destination).isEmpty());
        assertEquals(1, temporaryFolder.getRoot().list().length);
    }

    @Test
    public void writeFileFailsForNonemptyDirectoryAndCleansUp() throws Exception {
        Path destination = temporaryFolder.newFolder("destination").toPath();
        Path existing = destination.resolve("keep.txt");
        Files.writeString(existing, "keep", StandardCharsets.UTF_8);

        assertFalse(writeFile(destination, new JSONArray()));
        assertEquals("keep", Files.readString(existing, StandardCharsets.UTF_8));
        assertEquals(1, temporaryFolder.getRoot().list().length);
    }

    @After
    public void tearDown() throws Exception {
        if (originalFiles != null) {
            for (Map.Entry<Path, byte[]> entry : originalFiles.entrySet()) {
                Files.write(entry.getKey(), entry.getValue());
            }
        }
    }

    // Inspect JSON written through the public save methods.
    private JSONObject saveAndRead(User user) throws Exception {
        ArrayList<User> users = new ArrayList<User>();
        users.add(user);
        assertTrue(DataWriter.saveUsers(users));
        return readOnlyRecord("users.json");
    }

    private JSONObject saveAndRead(AidRequest request) throws Exception {
        ArrayList<AidRequest> requests = new ArrayList<AidRequest>();
        requests.add(request);
        assertTrue(DataWriter.saveAidRequests(requests));
        return readOnlyRecord("aidrequests.json");
    }

    private JSONObject saveAndRead(Shelter shelter) throws Exception {
        ArrayList<Shelter> shelters = new ArrayList<Shelter>();
        shelters.add(shelter);
        assertTrue(DataWriter.saveResources(shelters));
        return readOnlyRecord("shelters.json");
    }

    private JSONObject readOnlyRecord(String name) throws Exception {
        JSONArray records = readFile(testFile(name));
        assertEquals(1, records.size());
        return (JSONObject) records.get(0);
    }

    private Path testFile(String name) throws Exception {
        return Paths.get(DataWriter.class.getResource("/data/" + name).toURI());
    }

    private boolean writeFile(Path destination, JSONArray data) throws Exception {
        Method method = DataWriter.class.getDeclaredMethod("writeFile", String.class, JSONArray.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(null, destination.toString(), data);
    }

    private JSONArray readFile(Path source) throws Exception {
        return (JSONArray) new JSONParser().parse(Files.readString(source, StandardCharsets.UTF_8));
    }
}
