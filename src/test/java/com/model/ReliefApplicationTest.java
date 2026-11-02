package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for ReliefApplication.
 *
 * The directories load from and save to the real JSON files. Each test backs
 * those files up and resets the singletons. Cleanup restores the original files.
 * These tests should not run in parallel with other tests using the same files.
 *
 * Test                                          Reasoning
 * --------------------------------------------  ----------------------------------------
 * Singleton returns the same application         Only one facade should exist.
 * New application is logged out                  Nobody has logged in yet.
 * New application has no current user            No account should be selected initially.
 * Create account returns a resident              Checks normal account creation.
 * Create account does not log in                 Registration should not start a session.
 * Correct password returns the account           Checks normal login.
 * Successful login sets current user             The facade should remember the user.
 * Successful login updates login status          A successful login starts a session.
 * Wrong password returns null                    Incorrect credentials should be rejected.
 * Unknown username returns null                  Missing accounts cannot log in.
 * Failed login clears previous session           Login replaces the current user.
 * Logout without a user returns false            Nobody was available to log out.
 * Logout with a user returns true                Confirms a user was logged out.
 * Logout clears current user                     Removes the current user's reference.
 * Logout updates login status                    The session should become inactive.
 * Second logout returns false                    The first logout already ended the session.
 * User lookup returns created account            Checks lookup by user ID.
 * Logged-out assigned requests are empty         There is no volunteer to look up.
 * Null request has no selected shelter           Missing requests should be handled safely.
 * Logged-out export returns false                Exporting requires a logged-in user.
 * Logged-out export creates no file              Rejected exports should leave no output.
 * Unknown user has no request                    Missing users should be handled safely.
 * Logged-out user has no managed shelter         Shelter management requires a session.
 * Resident has no managed shelter                Residents are not shelter managers.
 * Volunteer has no managed shelter               Volunteers are not shelter managers.
 * Logged-out shelter check-in fails              Check-in requires a shelter manager.
 * Logged-out household creation fails            A household needs a logged-in owner.
 * Logged-out member addition fails               Adding a member requires a session.
 * Logged-out detailed member addition fails      Both overloads should check login.
 * Logged-out households are empty                There is no current user's household.
 *
 * @author Austin Lantz
 */
public class ReliefApplicationTest {

    private static final LocalDate TEST_DATE_OF_BIRTH =
            LocalDate.of(1990, 1, 1);

    private static final String[] DATA_FILES = {
            DataConstants.USER_FILE,
            DataConstants.AID_REQUEST_FILE,
            DataConstants.RESOURCE_FILE,
            DataConstants.HURRICANE_FILE
    };

    @Rule
    public TemporaryFolder backupFolder = new TemporaryFolder();

    private ReliefApplication app;

    @Before
    public void setUp() throws IOException {
        backupDataFiles();
        resetSingletons();
        app = ReliefApplication.getInstance();
    }

    @After
    public void tearDown() throws IOException {
        try {
            if (app != null) {
                app.logout();
            }
        } finally {
            try {
                restoreDataFiles();
            } finally {
                resetSingletons();
            }
        }
    }

    // ----- helpers -----

    private String newUsername() {
        return "test_" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12);
    }

    private User createAccount(String username, USERTYPE type) {
        return app.createAccount(
                username,
                "password123",
                "Test",
                "User",
                TEST_DATE_OF_BIRTH,
                "1 Test St, Charleston, SC 29401",
                username + "@email.com",
                "803-555-0100",
                type);
    }

    private User createAndLogin(String username, USERTYPE type) {
        createAccount(username, type);
        return app.login(username, "password123");
    }

    // ----- setup and cleanup -----

    private void backupDataFiles() throws IOException {
        Path folder = backupFolder.getRoot().toPath();

        for (int i = 0; i < DATA_FILES.length; i++) {
            Path source = Paths.get(DATA_FILES[i]);

            if (Files.exists(source)) {
                Files.copy(
                        source,
                        folder.resolve("file" + i + ".bak"),
                        StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.createFile(folder.resolve("file" + i + ".missing"));
            }
        }
    }

    private void restoreDataFiles() throws IOException {
        Path folder = backupFolder.getRoot().toPath();

        for (int i = 0; i < DATA_FILES.length; i++) {
            Path destination = Paths.get(DATA_FILES[i]);
            Path backup = folder.resolve("file" + i + ".bak");
            Path missing = folder.resolve("file" + i + ".missing");

            if (Files.exists(missing)) {
                Files.deleteIfExists(destination);
            } else if (Files.exists(backup)) {
                Files.copy(
                        backup,
                        destination,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void resetSingletons() {
        clear(UserDirectory.class, "userDirectory");
        clear(AidRequestDirectory.class, "aidRequestDirectory");
        clear(ShelterDirectory.class, "shelterDirectory");
        clear(HurricaneDirectory.class, "hurricaneDirectory");
        clear(ReliefApplication.class, "reliefApplication");
    }

    private void clear(Class<?> type, String fieldName) {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Could not reset " + type.getSimpleName()
                            + "." + fieldName,
                    e);
        }
    }

    // ----- getInstance -----

    @Test
    public void getInstanceReturnsSameApplication() {
        assertSame(app, ReliefApplication.getInstance());
    }

    // ----- isLoggedIn -----

    @Test
    public void newApplicationHasNobodyLoggedIn() {
        assertFalse(app.isLoggedIn());
    }

    @Test
    public void successfulLoginMakesUserLoggedIn() {
        createAndLogin(newUsername(), USERTYPE.RESIDENT);

        assertTrue(app.isLoggedIn());
    }

    // ----- getCurrentUser -----

    @Test
    public void newApplicationHasNoCurrentUser() {
        assertNull(app.getCurrentUser());
    }

    @Test
    public void successfulLoginSetsCurrentUser() {
        User user = createAndLogin(newUsername(), USERTYPE.RESIDENT);

        assertEquals(user, app.getCurrentUser());
    }

    // ----- createAccount -----

    @Test
    public void createAccountReturnsNewResident() {
        assertNotNull(createAccount(newUsername(), USERTYPE.RESIDENT));
    }

    @Test
    public void createAccountDoesNotLogUserIn() {
        createAccount(newUsername(), USERTYPE.RESIDENT);

        assertFalse(app.isLoggedIn());
    }

    // ----- login -----

    @Test
    public void loginWithCorrectPasswordReturnsAccount() {
        String username = newUsername();
        User user = createAccount(username, USERTYPE.RESIDENT);

        assertEquals(user, app.login(username, "password123"));
    }

    @Test
    public void loginWithWrongPasswordReturnsNull() {
        String username = newUsername();
        createAccount(username, USERTYPE.RESIDENT);

        assertNull(app.login(username, "wrongPassword"));
    }

    @Test
    public void loginWithUnknownUsernameReturnsNull() {
        assertNull(app.login(newUsername(), "password123"));
    }

    @Test
    public void failedLoginClearsPreviousSession() {
        String username = newUsername();
        createAndLogin(username, USERTYPE.RESIDENT);

        app.login(username, "wrongPassword");

        assertNull(app.getCurrentUser());
    }

    // ----- logout -----

    @Test
    public void logoutWithoutCurrentUserReturnsFalse() {
        assertFalse(app.logout());
    }

    @Test
    public void logoutWithCurrentUserReturnsTrue() {
        createAndLogin(newUsername(), USERTYPE.RESIDENT);

        assertTrue(app.logout());
    }

    @Test
    public void logoutClearsLoggedInUser() {
        createAndLogin(newUsername(), USERTYPE.RESIDENT);

        app.logout();

        assertNull(app.getCurrentUser());
    }

    @Test
    public void logoutMakesUserLoggedOut() {
        createAndLogin(newUsername(), USERTYPE.RESIDENT);

        app.logout();

        assertFalse(app.isLoggedIn());
    }

    @Test
    public void logoutTwiceReturnsFalseSecondTime() {
        createAndLogin(newUsername(), USERTYPE.RESIDENT);
        app.logout();

        assertFalse(app.logout());
    }

    // ----- getUserById -----

    @Test
    public void getUserByIdReturnsCreatedAccount() {
        User user = createAccount(newUsername(), USERTYPE.RESIDENT);

        assertEquals(user, app.getUserById(user.getUserId()));
    }

    // ----- getAssignedRequests -----

    @Test
    public void assignedRequestsAreEmptyWhenLoggedOut() {
        assertTrue(app.getAssignedRequests().isEmpty());
    }

    // ----- getSelectedShelter -----

    @Test
    public void selectedShelterForNullRequestReturnsNull() {
        assertNull(app.getSelectedShelter(null));
    }

    // ----- exportTripInformation -----

    @Test
    public void exportTripInformationWhenLoggedOutReturnsFalse() {
        Path filePath = backupFolder.getRoot().toPath().resolve("trip.txt");

        assertFalse(app.exportTripInformation(
                null, null, filePath.toString()));
    }

    @Test
    public void exportTripInformationWhenLoggedOutCreatesNoFile() {
        Path filePath = backupFolder.getRoot().toPath().resolve("trip.txt");

        app.exportTripInformation(null, null, filePath.toString());

        assertFalse(Files.exists(filePath));
    }

    // ----- getRequestFrom -----

    @Test
    public void requestFromUnknownUsernameReturnsNull() {
        assertNull(app.getRequestFrom(newUsername()));
    }

    // ----- getMyShelter -----

    @Test
    public void myShelterWhenLoggedOutReturnsNull() {
        assertNull(app.getMyShelter());
    }

    @Test
    public void residentHasNoManagedShelter() {
        createAndLogin(newUsername(), USERTYPE.RESIDENT);

        assertNull(app.getMyShelter());
    }

    @Test
    public void volunteerHasNoManagedShelter() {
        createAndLogin(newUsername(), USERTYPE.VOLUNTEER);

        assertNull(app.getMyShelter());
    }

    // ----- checkInToShelter -----

    @Test
    public void shelterCheckInWhenLoggedOutReturnsFalse() {
        ArrayList<String> people = new ArrayList<>();
        ArrayList<String> pets = new ArrayList<>();
        people.add("Test User");

        assertFalse(app.checkInToShelter(people, pets));
    }

    // ----- createHousehold -----

    @Test
    public void createHouseholdWhenLoggedOutReturnsFalse() {
        assertFalse(app.createHousehold("Test Household"));
    }

    // ----- addHouseholdMember -----

    @Test
    public void addHouseholdMemberWhenLoggedOutReturnsFalse() {
        assertFalse(app.addHouseholdMember(
                "Test Household", "Test Member"));
    }

    @Test
    public void addDetailedHouseholdMemberWhenLoggedOutReturnsFalse() {
        assertFalse(app.addHouseholdMember(
                "Test Household",
                "Test Member",
                25,
                null,
                "Needs medication"));
    }

    // ----- getHouseholds -----

    @Test
    public void householdsAreEmptyWhenLoggedOut() {
        assertTrue(app.getHouseholds().isEmpty());
    }
}