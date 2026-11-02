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
 *
 * +--------------------------------------------------------------------------------+----------------------------------------------------------------------------+
 * | Test                                                                           | Reasoning                                                                  |
 * +--------------------------------------------------------------------------------+----------------------------------------------------------------------------+
 * | savedUsersCanBeLoadedAgain                                                     | Public save and load methods must agree on the stored record.              |
 * | savedRequestsCanBeLoadedAgain                                                  | Public save and load methods must agree on the stored record.              |
 * | cachedSheltersCanBeLoadedAgain                                                 | Public save and load methods must agree on the stored record.              |
 * | saveUsersLeavesApplicationFileUnchanged                                        | JUnit writes belong in test resources, not application JSON.               |
 * | saveAidRequestsLeavesApplicationFileUnchanged                                  | JUnit writes belong in test resources, not application JSON.               |
 * | saveResourcesLeavesApplicationFileUnchanged                                    | JUnit writes belong in test resources, not application JSON.               |
 * | cacheSheltersLeavesApplicationFileUnchanged                                    | JUnit writes belong in test resources, not application JSON.               |
 * | saveUsersRejectsNullList                                                       | A missing collection is invalid input and must return false.               |
 * | saveAidRequestsRejectsNullList                                                 | A missing collection is invalid input and must return false.               |
 * | saveResourcesRejectsNullList                                                   | A missing collection is invalid input and must return false.               |
 * | cacheSheltersRejectsNullList                                                   | A missing collection is invalid input and must return false.               |
 * | saveUsersRejectsNullEntry                                                      | A null record cannot be serialized and must return false.                  |
 * | saveAidRequestsRejectsNullEntry                                                | A null record cannot be serialized and must return false.                  |
 * | saveResourcesRejectsNullEntry                                                  | A null record cannot be serialized and must return false.                  |
 * | cacheSheltersRejectsNullEntry                                                  | A null record cannot be serialized and must return false.                  |
 * | userJSONKeepsAccountAndContactDetails                                          | The saved record must match the explicit fixture values.                   |
 * | userJSONKeepsHouseholdMembers                                                  | Household ownership and member details must remain persistence.            |
 * | userJSONIncludesVolunteerSpecialtyWhenPresent                                  | The MEDICAL specialty must remain persistence.                             |
 * | userJSONIncludesManagerShelterWhenPresent                                      | The manager must retain the saved shelter UUID.                            |
 * | requestJSONKeepsDetailsAndShelter                                              | The shelter record must retain its saved resource information.             |
 * | requestJSONOmitsMissingShelter                                                 | Absent optional data must use the specified default.                       |
 * | requestJSONKeepsAssignmentHistory                                              | Saved task identities, participants, status, and times must remain.        |
 * | requestJSONAllowsMissingAssignmentPeopleAndTimes                               | Absent optional data must use the specified default.                       |
 * | shelterJSONKeepsDetailsInventoryAndOccupants                                   | The shelter record must retain its saved resource information.             |
 * | requestJSONFormatsCreatedTimeAsUTC                                             | The fixed creation time must be written with its UTC offset.               |
 * | requestJSONAllowsNullCreatedTime                                               | An absent timestamp must remain JSON null.                                 |
 * | writeFileCreatesDirectoriesAndPreservesSpecialCharacters                       | The saved role must construct the matching user subclass.                  |
 * | writeFileReplacesExistingDataWithEmptyArray                                    | Replacing a file with [] must remove its prior records.                    |
 * | writeFileFailsForNonemptyDirectoryAndCleansUp                                  | An unwritable destination must fail and clean up temporary files.          |
 * | userJSONOmitsMissingVolunteerSpecialty                                         | Absent optional data must use the specified default.                       |
 * | userJSONOmitsMissingManagerShelter                                             | Absent optional data must use the specified default.                       |
 * | saveUsersReplacesSavedRecordsWithEmptyList                                     | An empty collection must not retain stale records.                         |
 * | saveUsersPreservesSavedFileWhenSerializationFails                              | A failed save must leave the previous file bytes intact.                   |
 * | saveUsersWritesMultipleRecords                                                 | Every supplied record must remain, in its original order.                  |
 * | saveAidRequestsReplacesSavedRecordsWithEmptyList                               | An empty collection must not retain stale records.                         |
 * | saveAidRequestsPreservesSavedFileWhenSerializationFails                        | A failed save must leave the previous file bytes intact.                   |
 * | saveAidRequestsWritesMultipleRecords                                           | Every supplied record must remain, in its original order.                  |
 * | saveResourcesReplacesSavedRecordsWithEmptyList                                 | An empty collection must not retain stale records.                         |
 * | saveResourcesPreservesSavedFileWhenSerializationFails                          | A failed save must leave the previous file bytes intact.                   |
 * | saveResourcesWritesMultipleRecords                                             | Every supplied record must remain, in its original order.                  |
 * | cacheSheltersReplacesSavedRecordsWithEmptyList                                 | An empty collection must not retain stale records.                         |
 * | cacheSheltersPreservesSavedFileWhenSerializationFails                          | A failed save must leave the previous file bytes intact.                   |
 * | cacheSheltersWritesMultipleRecords                                             | Every supplied record must remain, in its original order.                  |
 * +--------------------------------------------------------------------------------+----------------------------------------------------------------------------+
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
    public void saveUsersLeavesApplicationFileUnchanged() throws Exception {
        Path applicationFile = Paths.get("json/users.json");
        byte[] before = Files.readAllBytes(applicationFile);
        ArrayList<User> values = new ArrayList<User>();
        values.add(resident);
        assertTrue(DataWriter.saveUsers(values));
        assertEquals(1, readFile(testFile("users.json")).size());
        assertArrayEquals(before, Files.readAllBytes(applicationFile));
    }

    @Test
    public void saveAidRequestsLeavesApplicationFileUnchanged() throws Exception {
        Path applicationFile = Paths.get("json/aidrequests.json");
        byte[] before = Files.readAllBytes(applicationFile);
        ArrayList<AidRequest> values = new ArrayList<AidRequest>();
        values.add(request);
        assertTrue(DataWriter.saveAidRequests(values));
        assertEquals(1, readFile(testFile("aidrequests.json")).size());
        assertArrayEquals(before, Files.readAllBytes(applicationFile));
    }

    @Test
    public void saveResourcesLeavesApplicationFileUnchanged() throws Exception {
        Path applicationFile = Paths.get("json/shelters.json");
        byte[] before = Files.readAllBytes(applicationFile);
        ArrayList<Shelter> values = new ArrayList<Shelter>();
        values.add(new Shelter(UUID.randomUUID(), "School", 100, 0));
        assertTrue(DataWriter.saveResources(values));
        assertEquals(1, readFile(testFile("shelters.json")).size());
        assertArrayEquals(before, Files.readAllBytes(applicationFile));
    }

    @Test
    public void cacheSheltersLeavesApplicationFileUnchanged() throws Exception {
        Path applicationFile = Paths.get("json/shelters.json");
        byte[] before = Files.readAllBytes(applicationFile);
        ArrayList<Shelter> values = new ArrayList<Shelter>();
        values.add(new Shelter(UUID.randomUUID(), "School", 100, 0));
        assertTrue(DataWriter.cacheShelters(values));
        assertEquals(1, readFile(testFile("shelters.json")).size());
        assertArrayEquals(before, Files.readAllBytes(applicationFile));
    }

    @Test
    public void saveUsersRejectsNullList() throws Exception {
        assertFalse(DataWriter.saveUsers(null));
    }

    @Test
    public void saveAidRequestsRejectsNullList() throws Exception {
        assertFalse(DataWriter.saveAidRequests(null));
    }

    @Test
    public void saveResourcesRejectsNullList() throws Exception {
        assertFalse(DataWriter.saveResources(null));
    }

    @Test
    public void cacheSheltersRejectsNullList() throws Exception {
        assertFalse(DataWriter.cacheShelters(null));
    }

    @Test
    public void saveUsersRejectsNullEntry() throws Exception {
        ArrayList<User> values = new ArrayList<User>();
        values.add(null);
        assertFalse(DataWriter.saveUsers(values));
    }

    @Test
    public void saveAidRequestsRejectsNullEntry() throws Exception {
        ArrayList<AidRequest> values = new ArrayList<AidRequest>();
        values.add(null);
        assertFalse(DataWriter.saveAidRequests(values));
    }

    @Test
    public void saveResourcesRejectsNullEntry() throws Exception {
        ArrayList<Shelter> values = new ArrayList<Shelter>();
        values.add(null);
        assertFalse(DataWriter.saveResources(values));
    }

    @Test
    public void cacheSheltersRejectsNullEntry() throws Exception {
        ArrayList<Shelter> values = new ArrayList<Shelter>();
        values.add(null);
        assertFalse(DataWriter.cacheShelters(values));
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

        volunteer.setSpecialty(RequestCategory.MEDICAL);
        assertEquals("MEDICAL", saveAndRead(volunteer).get("specialty"));
    }

    @Test
    public void userJSONIncludesManagerShelterWhenPresent() throws Exception {
        ShelterManager manager = new ShelterManager(UUID.randomUUID(), "manager", "password",
                "Shelter Manager", LocalDate.of(1990, 1, 1), null, null, null, null, null);

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
    public void requestJSONFormatsCreatedTimeAsUTC() throws Exception {
        assertEquals("2026-10-01T09:30Z", saveAndRead(request).get("createdAt"));
    }

    @Test
    public void requestJSONAllowsNullCreatedTime() throws Exception {
        request.setCreatedAt(null);
        assertNull(saveAndRead(request).get("createdAt"));
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

    @Test
    public void userJSONOmitsMissingVolunteerSpecialty() throws Exception {
        Volunteer volunteer = new Volunteer(UUID.randomUUID(), "val", "password", "Val",
                LocalDate.of(1990, 1, 1), null, null, null, null);
        assertFalse(saveAndRead(volunteer).containsKey("specialty"));
    }

    @Test
    public void userJSONOmitsMissingManagerShelter() throws Exception {
        ShelterManager manager = new ShelterManager(UUID.randomUUID(), "manager", "password", "Manager",
                LocalDate.of(1990, 1, 1), null, null, null, null, null);
        assertFalse(saveAndRead(manager).containsKey("shelterId"));
    }

    @Test
    public void saveUsersReplacesSavedRecordsWithEmptyList() throws Exception {
        ArrayList<User> values = new ArrayList<User>();
        values.add(resident);
        assertTrue(DataWriter.saveUsers(values));
        assertTrue(DataWriter.saveUsers(new ArrayList<User>()));
        assertTrue(readFile(testFile("users.json")).isEmpty());
    }

    @Test
    public void saveUsersPreservesSavedFileWhenSerializationFails() throws Exception {
        ArrayList<User> values = new ArrayList<User>();
        values.add(resident);
        assertTrue(DataWriter.saveUsers(values));
        byte[] before = Files.readAllBytes(testFile("users.json"));
        values.add(null);
        assertFalse(DataWriter.saveUsers(values));
        assertArrayEquals(before, Files.readAllBytes(testFile("users.json")));
    }

    @Test
    public void saveUsersWritesMultipleRecords() throws Exception {
        ArrayList<User> values = new ArrayList<User>();
        values.add(resident);
        values.add(new Resident(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"), "ben", "password", "Ben", LocalDate.of(1991, 2, 2), null, null, null, null));
        assertTrue(DataWriter.saveUsers(values));
        JSONArray saved = readFile(testFile("users.json"));
        assertEquals(2, saved.size());
        assertEquals(values.get(0).getUserId().toString(), ((JSONObject) saved.get(0)).get("userId"));
        assertEquals("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", ((JSONObject) saved.get(1)).get("userId"));
    }

    @Test
    public void saveAidRequestsReplacesSavedRecordsWithEmptyList() throws Exception {
        ArrayList<AidRequest> values = new ArrayList<AidRequest>();
        values.add(request);
        assertTrue(DataWriter.saveAidRequests(values));
        assertTrue(DataWriter.saveAidRequests(new ArrayList<AidRequest>()));
        assertTrue(readFile(testFile("aidrequests.json")).isEmpty());
    }

    @Test
    public void saveAidRequestsPreservesSavedFileWhenSerializationFails() throws Exception {
        ArrayList<AidRequest> values = new ArrayList<AidRequest>();
        values.add(request);
        assertTrue(DataWriter.saveAidRequests(values));
        byte[] before = Files.readAllBytes(testFile("aidrequests.json"));
        values.add(null);
        assertFalse(DataWriter.saveAidRequests(values));
        assertArrayEquals(before, Files.readAllBytes(testFile("aidrequests.json")));
    }

    @Test
    public void saveAidRequestsWritesMultipleRecords() throws Exception {
        ArrayList<AidRequest> values = new ArrayList<AidRequest>();
        values.add(request);
        values.add(new AidRequest(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"), resident, RequestCategory.WATER, 1, "20 Oak St"));
        assertTrue(DataWriter.saveAidRequests(values));
        JSONArray saved = readFile(testFile("aidrequests.json"));
        assertEquals(2, saved.size());
        assertEquals(values.get(0).getRequestId().toString(), ((JSONObject) saved.get(0)).get("requestId"));
        assertEquals("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", ((JSONObject) saved.get(1)).get("requestId"));
    }

    @Test
    public void saveResourcesReplacesSavedRecordsWithEmptyList() throws Exception {
        ArrayList<Shelter> values = new ArrayList<Shelter>();
        values.add(new Shelter(UUID.randomUUID(), "School", 100, 0));
        assertTrue(DataWriter.saveResources(values));
        assertTrue(DataWriter.saveResources(new ArrayList<Shelter>()));
        assertTrue(readFile(testFile("shelters.json")).isEmpty());
    }

    @Test
    public void saveResourcesPreservesSavedFileWhenSerializationFails() throws Exception {
        ArrayList<Shelter> values = new ArrayList<Shelter>();
        values.add(new Shelter(UUID.randomUUID(), "School", 100, 0));
        assertTrue(DataWriter.saveResources(values));
        byte[] before = Files.readAllBytes(testFile("shelters.json"));
        values.add(null);
        assertFalse(DataWriter.saveResources(values));
        assertArrayEquals(before, Files.readAllBytes(testFile("shelters.json")));
    }

    @Test
    public void saveResourcesWritesMultipleRecords() throws Exception {
        ArrayList<Shelter> values = new ArrayList<Shelter>();
        values.add(new Shelter(UUID.randomUUID(), "School", 100, 0));
        values.add(new Shelter(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"), "Gym", 50, 0));
        assertTrue(DataWriter.saveResources(values));
        JSONArray saved = readFile(testFile("shelters.json"));
        assertEquals(2, saved.size());
        assertEquals(values.get(0).getResourceId().toString(), ((JSONObject) saved.get(0)).get("resourceId"));
        assertEquals("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", ((JSONObject) saved.get(1)).get("resourceId"));
    }

    @Test
    public void cacheSheltersReplacesSavedRecordsWithEmptyList() throws Exception {
        ArrayList<Shelter> values = new ArrayList<Shelter>();
        values.add(new Shelter(UUID.randomUUID(), "School", 100, 0));
        assertTrue(DataWriter.cacheShelters(values));
        assertTrue(DataWriter.cacheShelters(new ArrayList<Shelter>()));
        assertTrue(readFile(testFile("shelters.json")).isEmpty());
    }

    @Test
    public void cacheSheltersPreservesSavedFileWhenSerializationFails() throws Exception {
        ArrayList<Shelter> values = new ArrayList<Shelter>();
        values.add(new Shelter(UUID.randomUUID(), "School", 100, 0));
        assertTrue(DataWriter.cacheShelters(values));
        byte[] before = Files.readAllBytes(testFile("shelters.json"));
        values.add(null);
        assertFalse(DataWriter.cacheShelters(values));
        assertArrayEquals(before, Files.readAllBytes(testFile("shelters.json")));
    }

    @Test
    public void cacheSheltersWritesMultipleRecords() throws Exception {
        ArrayList<Shelter> values = new ArrayList<Shelter>();
        values.add(new Shelter(UUID.randomUUID(), "School", 100, 0));
        values.add(new Shelter(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"), "Gym", 50, 0));
        assertTrue(DataWriter.cacheShelters(values));
        JSONArray saved = readFile(testFile("shelters.json"));
        assertEquals(2, saved.size());
        assertEquals(values.get(0).getResourceId().toString(), ((JSONObject) saved.get(0)).get("resourceId"));
        assertEquals("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", ((JSONObject) saved.get(1)).get("resourceId"));
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
