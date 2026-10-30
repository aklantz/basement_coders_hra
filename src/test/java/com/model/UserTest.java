package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.location.Address;

/**
 * Tests for User (through Resident) and the user subclasses
 * Admin, Coordinator, Resident, Volunteer and ShelterManager.
 *
 *
 * +-----------------------------------------------+------------------------------------------------------------------+
 * | Test                                          | Reasoning                                                        |
 * +-----------------------------------------------+------------------------------------------------------------------+
 * | new user starts active                        | a new account should be usable right away                        |
 * | new user starts with unknown safety status    | we don't know if someone is safe until they check in             |
 * | new user starts with no households            | households are only created when the user adds one               |
 * | correct password verifies                     | login has to work with the right password                        |
 * | wrong password fails                          | login has to reject a wrong password                             |
 * | password with different case fails            | passwords are case sensitive                                     |
 * | null password fails                           | an empty login form must not crash or let anyone in              |
 * | each subclass reports its own type            | the UI shows different menus for each role                       |
 * | only shelter managers can complete requests   | the requirements say only shelter managers mark requests done    |
 * | notification is addressed to the user         | the message has to say who it is for                             |
 * | formed address is split into parts       | the map tools need street, city, state and zip separately       |
 * | address missing commas returns null           | a bad address should not be sent to the map tools                |
 * | null address returns null                     | a user may not have entered an address yet                       |
 * | address with no zip has blank zip             | state is still usable even if the zip was left off               |
 * | add household succeeds                        | normal case for creating a household                             |
 * | duplicate household name rejected             | the user looks households up by name, so names must be unique    |
 * | blank household name rejected                 | a household needs a name to be found later                       |
 * | null household name rejected                  | a missing name can not crash the app                            |
 * | new household is owned by the user            | the household has to point back to the user who made it          |
 * | find household by name                        | normal lookup case                                               |
 * | find missing household returns null           | error case for lookup                                            |
 * | find household with null name returns null    | a null search must not crash                                     |
 * | add member to household succeeds              | normal case for adding a person                                  |
 * | add member to missing household fails         | can't add someone to a household that doesn't exist              |
 * | add blank member name fails                   | every member needs a name                                        |
 * | add duplicate member fails                    | the same person can't be listed twice                            |
 * | add pet with details succeeds                 | pets are tracked so shelters can plan for them                   |
 * | add member with negative age fails            | an age can't be below zero                                       |
 * | add  member to missing household fails        | can't add someone to a household that doesn't exist              |
 * | check in member records status                | normal check in case                                             |
 * | check in unknown member fails                 | can't check in someone who isn't in the household                |
 * | check in to missing household fails           | can't check in to a household that doesn't exist                 |
 * | check in with null status fails               | a check in has to say whether the person is safe                 |
 * | check in with null member name fails          | a missing name must not crash the app                            |
 * +-----------------------------------------------+------------------------------------------------------------------+
 */
public class UserTest {

    private static final String HOUSEHOLD = "Thiele Home";

    private User resident;
    private PrintStream originalOut;
    private ByteArrayOutputStream printed;

    @Before
    public void setUp() {
        resident = new Resident(UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "cthiele", "Hurricane#1", "Connor Thiele", LocalDate.of(2004, 5, 17),
                "142 Riverbend Dr, Tega Cay, SC 29708", "cthiele@email.sc.edu",
                "803-555-0100", "cthiele");
        originalOut = System.out;
        printed = new ByteArrayOutputStream();
        System.setOut(new PrintStream(printed));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
    }

    private User makeUser(USERTYPE type) {
        UUID id = UUID.randomUUID();
        LocalDate dob = LocalDate.of(1990, 1, 1);
        switch (type) {
            case ADMIN:
                return new Admin(id, "admin1", "pw", "Ada Admin", dob, null, null, null, null);
            case COORDINATOR:
                return new Coordinator(id, "coord1", "pw", "Cora Coord", dob, null, null, null, null);
            case VOLUNTEER:
                return new Volunteer(id, "vol1", "pw", "Val Vol", dob, null, null, null, null);
            case SHELTER_MANAGER:
                return new ShelterManager(id, "mgr1", "pw", "Sam Mgr", dob, null, null, null, null,
                        UUID.randomUUID());
            default:
                return new Resident(id, "res1", "pw", "Rae Res", dob, null, null, null, null);
        }
    }

    // ----- new account defaults -----

    @Test
    public void newUserStartsWithActiveAccount() {
        assertEquals(AccountStatus.ACTIVE, resident.getAccountStatus());
    }

    @Test
    public void newUserStartsWithUnknownSafetyStatus() {
        assertEquals(SafetyStatus.UNKNOWN, resident.getSafetyStatus());
    }

    @Test
    public void newUserStartsWithNoHouseholds() {
        assertTrue(resident.getHouseholds().isEmpty());
    }

    // ----- verifyPassword -----

    @Test
    public void verifyPasswordAcceptsCorrectPassword() {
        assertTrue(resident.verifyPassword("Hurricane#1"));
    }

    @Test
    public void verifyPasswordRejectsWrongPassword() {
        assertFalse(resident.verifyPassword("Tornado#2"));
    }

    @Test
    public void verifyPasswordIsCaseSensitive() {
        assertFalse(resident.verifyPassword("hurricane#1"));
    }

    @Test
    public void verifyPasswordRejectsNullPassword() {
        assertFalse(resident.verifyPassword(null));
    }

    // ----- getType and canCompleteRequests -----

    @Test
    public void adminReportsAdminType() {
        assertEquals(USERTYPE.ADMIN, makeUser(USERTYPE.ADMIN).getType());
    }

    @Test
    public void coordinatorReportsCoordinatorType() {
        assertEquals(USERTYPE.COORDINATOR, makeUser(USERTYPE.COORDINATOR).getType());
    }

    @Test
    public void residentReportsResidentType() {
        assertEquals(USERTYPE.RESIDENT, resident.getType());
    }

    @Test
    public void volunteerReportsVolunteerType() {
        assertEquals(USERTYPE.VOLUNTEER, makeUser(USERTYPE.VOLUNTEER).getType());
    }

    @Test
    public void shelterManagerReportsShelterManagerType() {
        assertEquals(USERTYPE.SHELTER_MANAGER, makeUser(USERTYPE.SHELTER_MANAGER).getType());
    }

    @Test
    public void shelterManagerCanCompleteRequests() {
        assertTrue(makeUser(USERTYPE.SHELTER_MANAGER).canCompleteRequests());
    }

    @Test
    public void residentCannotCompleteRequests() {
        assertFalse(resident.canCompleteRequests());
    }

    @Test
    public void volunteerCannotCompleteRequests() {
        assertFalse(makeUser(USERTYPE.VOLUNTEER).canCompleteRequests());
    }

    // ----- sendNotification -----

    @Test
    public void sendNotificationIsAddressedToUsersFullName() {
        resident.sendNotification("Shelter assigned");
        assertEquals("To Connor Thiele: Shelter assigned", printed.toString().trim());
    }

    // ----- getAddress -----

    @Test
    public void getAddressSplitsStreet() {
        assertEquals("142 Riverbend Dr", resident.getAddress().getStreet());
    }

    @Test
    public void getAddressSplitsCity() {
        assertEquals("Tega Cay", resident.getAddress().getCity());
    }

    @Test
    public void getAddressSplitsState() {
        assertEquals("SC", resident.getAddress().getState());
    }

    @Test
    public void getAddressSplitsZip() {
        assertEquals("29708", resident.getAddress().getZip());
    }

    @Test
    public void getAddressReturnsNullWhenCommasMissing() {
        resident.setHomeAddress("142 Riverbend Dr Tega Cay SC 29708");
        assertNull(resident.getAddress());
    }

    @Test
    public void getAddressReturnsNullWhenAddressIsNull() {
        resident.setHomeAddress(null);
        assertNull(resident.getAddress());
    }

    @Test
    public void getAddressUsesBlankZipWhenZipMissing() {
        resident.setHomeAddress("142 Riverbend Dr, Tega Cay, SC");
        Address address = resident.getAddress();
        assertEquals("", address.getZip());
    }

    // ----- addHousehold / getHousehold -----

    @Test
    public void addHouseholdSucceedsWithNewName() {
        assertTrue(resident.addHousehold(HOUSEHOLD));
    }

    @Test
    public void addHouseholdRejectsDuplicateName() {
        resident.addHousehold(HOUSEHOLD);
        assertFalse(resident.addHousehold(HOUSEHOLD));
    }

    @Test
    public void addHouseholdRejectsBlankName() {
        assertFalse(resident.addHousehold("   "));
    }

    @Test
    public void addHouseholdRejectsNullName() {
        assertFalse(resident.addHousehold(null));
    }

    @Test
    public void addHouseholdSetsUserAsOwner() {
        resident.addHousehold(HOUSEHOLD);
        assertEquals(resident.getUserId(), resident.getHousehold(HOUSEHOLD).getHouseholdOwnerUUID());
    }

    @Test
    public void getHouseholdFindsHouseholdByName() {
        resident.addHousehold(HOUSEHOLD);
        assertNotNull(resident.getHousehold(HOUSEHOLD));
    }

    @Test
    public void getHouseholdReturnsNullForUnknownName() {
        resident.addHousehold(HOUSEHOLD);
        assertNull(resident.getHousehold("Beach House"));
    }

    @Test
    public void getHouseholdReturnsNullForNullName() {
        assertNull(resident.getHousehold(null));
    }

    // ----- addHouseholdMember (name only) -----

    @Test
    public void addHouseholdMemberSucceeds() {
        resident.addHousehold(HOUSEHOLD);
        assertTrue(resident.addHouseholdMember(HOUSEHOLD, "Riley"));
    }

    @Test
    public void addHouseholdMemberFailsForMissingHousehold() {
        assertFalse(resident.addHouseholdMember("Beach House", "Riley"));
    }

    @Test
    public void addHouseholdMemberRejectsBlankName() {
        resident.addHousehold(HOUSEHOLD);
        assertFalse(resident.addHouseholdMember(HOUSEHOLD, " "));
    }

    @Test
    public void addHouseholdMemberRejectsNullName() {
        resident.addHousehold(HOUSEHOLD);
        assertFalse(resident.addHouseholdMember(HOUSEHOLD, null));
    }

    @Test
    public void addHouseholdMemberRejectsDuplicateName() {
        resident.addHousehold(HOUSEHOLD);
        resident.addHouseholdMember(HOUSEHOLD, "Riley");
        assertFalse(resident.addHouseholdMember(HOUSEHOLD, "Riley"));
    }

    // ----- addHouseholdMember (with details) -----

    @Test
    public void addHouseholdMemberWithDetailsAddsPet() {
        resident.addHousehold(HOUSEHOLD);
        resident.addHouseholdMember(HOUSEHOLD, "Biscuit", 0, "dog", null);
        assertTrue(resident.getHousehold(HOUSEHOLD).getMembers().get(0).isPet());
    }

    @Test
    public void addHouseholdMemberWithDetailsRejectsNegativeAge() {
        resident.addHousehold(HOUSEHOLD);
        assertFalse(resident.addHouseholdMember(HOUSEHOLD, "Tim", -1, null, null));
    }

    @Test
    public void addHouseholdMemberWithDetailsFailsForMissingHousehold() {
        assertFalse(resident.addHouseholdMember("Beach House", "Tim", 20, null, null));
    }

    // ----- checkInHouseholdMember -----

    @Test
    public void checkInHouseholdMemberRecordsSafetyStatus() {
        resident.addHousehold(HOUSEHOLD);
        resident.addHouseholdMember(HOUSEHOLD, "Riley");
        resident.checkInHouseholdMember(HOUSEHOLD, "Riley", SafetyStatus.SAFE);
        assertEquals(SafetyStatus.SAFE, resident.getHousehold(HOUSEHOLD).getSafetyStatus("Riley"));
    }

    @Test
    public void checkInHouseholdMemberFailsForUnknownMember() {
        resident.addHousehold(HOUSEHOLD);
        assertFalse(resident.checkInHouseholdMember(HOUSEHOLD, "Austin", SafetyStatus.SAFE));
    }

    @Test
    public void checkInHouseholdMemberFailsForMissingHousehold() {
        assertFalse(resident.checkInHouseholdMember("Beach House", "Riley", SafetyStatus.SAFE));
    }

    @Test
    public void checkInHouseholdMemberRejectsNullStatus() {
        resident.addHousehold(HOUSEHOLD);
        resident.addHouseholdMember(HOUSEHOLD, "Riley");
        assertFalse(resident.checkInHouseholdMember(HOUSEHOLD, "Riley", null));
    }

    @Test
    public void checkInHouseholdMemberRejectsNullName() {
        resident.addHousehold(HOUSEHOLD);
        assertFalse(resident.checkInHouseholdMember(HOUSEHOLD, null, SafetyStatus.SAFE));
    }
}