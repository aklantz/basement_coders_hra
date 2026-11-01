package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for UserDirectory.
 * Created with AI assistance.
 *
 * +---------------------------------------------------+------------------------------------------------------+
 * | Test                                              | Reasoning                                            |
 * +---------------------------------------------------+------------------------------------------------------+
 * | directory is a singleton                          | callers should share one directory                  |
 * | find user by username and credentials             | login lookups should accept only matching passwords|
 * | add user rejects null and duplicate usernames     | usernames should be unique and valid                |
 * | find user by id handles missing ids               | users should be retrievable by their identifiers   |
 * | edit user changes contact details                 | updates should apply to the requested user          |
 * | delete user removes an existing user              | only existing users should be removed               |
 * | users by zip code match address suffix            | residents in the same zip should be discoverable    |
 * | pending coordinators excludes active accounts     | only coordinators awaiting approval should return   |
 * +---------------------------------------------------+------------------------------------------------------+
 */
public class UserDirectoryTest {

    private UserDirectory directory;
    private ArrayList<User> originalUsers;
    private Resident resident;

    @Before
    public void setUp() {
        directory = UserDirectory.getInstance();
        originalUsers = new ArrayList<User>(directory.getUsers());
        directory.getUsers().clear();
        resident = makeResident("resident_" + UUID.randomUUID(),
                "10 Main St, Columbia, SC 29201");
        directory.getUsers().add(resident);
    }

    @After
    public void tearDown() {
        directory.getUsers().clear();
        directory.getUsers().addAll(originalUsers);
    }

    @Test
    public void getInstanceReturnsTheSameDirectory() {
        assertSame(directory, UserDirectory.getInstance());
    }

    @Test
    public void findUserByUsernameAndCredentials() {
        assertSame(resident, directory.getUser(resident.getUsername()));
        assertSame(resident, directory.getUser(resident.getUsername(), "password"));
        assertNull(directory.getUser(resident.getUsername(), "wrong-password"));
        assertNull(directory.getUser("missing"));
        assertNull(directory.getUser(null));
        assertNull(directory.getUser(resident.getUsername(), null));
    }

    @Test
    public void addUserRejectsNullAndDuplicateUsernames() {
        assertFalse(directory.addUser(null));
        assertFalse(directory.addUser(makeResident(resident.getUsername(), null)));

        Resident another = makeResident("resident_" + UUID.randomUUID(), null);
        assertTrue(directory.addUser(another));
        assertSame(another, directory.getUserById(another.getUserId()));
    }

    @Test
    public void findUserByIdHandlesMissingIds() {
        assertSame(resident, directory.getUserById(resident.getUserId()));
        assertNull(directory.getUserById(null));
        assertNull(directory.getUserById(UUID.randomUUID()));
    }

    @Test
    public void editUserChangesContactDetailsAndRejectsUnknownIds() {
        assertTrue(directory.editUser(resident.getUserId(), "Rae Updated",
                "20 Oak St, Columbia, SC 29201", "rae@example.com", "803-555-0100"));
        assertEquals("Rae Updated", resident.getFullName());
        assertEquals("20 Oak St, Columbia, SC 29201", resident.getHomeAddress());
        assertEquals("rae@example.com", resident.getEmailAddress());
        assertEquals("803-555-0100", resident.getPhoneNumber());
        assertFalse(directory.editUser(UUID.randomUUID(), "Unknown", null, null, null));
    }

    @Test
    public void deleteUserRemovesOnlyAUserThatExists() {
        assertTrue(directory.deleteUser(resident.getUserId()));
        assertNull(directory.getUserById(resident.getUserId()));
        assertFalse(directory.deleteUser(resident.getUserId()));
        assertFalse(directory.deleteUser(null));
    }

    @Test
    public void getUsersByZipCodeMatchesTrimmedAddressSuffix() {
        Resident sameZip = makeResident("resident_" + UUID.randomUUID(),
                "20 Oak St, Columbia, SC 29201 ");
        Resident otherZip = makeResident("resident_" + UUID.randomUUID(),
                "30 Pine St, Columbia, SC 29000");
        Resident noAddress = makeResident("resident_" + UUID.randomUUID(), null);
        directory.getUsers().add(sameZip);
        directory.getUsers().add(otherZip);
        directory.getUsers().add(noAddress);

        ArrayList<User> matches = directory.getUsersByZipCode(" 29201 ");

        assertEquals(2, matches.size());
        assertTrue(matches.contains(resident));
        assertTrue(matches.contains(sameZip));
        assertTrue(directory.getUsersByZipCode(null).isEmpty());
    }

    @Test
    public void getPendingCoordinatorsReturnsOnlyPendingCoordinatorAccounts() {
        Coordinator pending = new Coordinator(UUID.randomUUID(),
                "coordinator_" + UUID.randomUUID(), "password", "Cora Coordinator",
                LocalDate.of(1980, 1, 1), null, null, null, null);
        pending.setAccountStatus(AccountStatus.PENDING);
        Coordinator active = new Coordinator(UUID.randomUUID(),
                "coordinator_" + UUID.randomUUID(), "password", "Casey Coordinator",
                LocalDate.of(1980, 1, 1), null, null, null, null);
        active.setAccountStatus(AccountStatus.ACTIVE);
        directory.getUsers().add(pending);
        directory.getUsers().add(active);

        ArrayList<Coordinator> pendingCoordinators = directory.getPendingCoordinators();

        assertEquals(1, pendingCoordinators.size());
        assertSame(pending, pendingCoordinators.get(0));
    }

    private Resident makeResident(String username, String address) {
        return new Resident(UUID.randomUUID(), username, "password", "Rae Resident",
                LocalDate.of(1990, 1, 1), address, "rae@example.com", "803-555-0100", username);
    }
}
