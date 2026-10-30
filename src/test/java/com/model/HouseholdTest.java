package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for Household.
 *
 *
 * +-----------------------------------------------+------------------------------------------------------------------+
 * | Test                                          | Reasoning                                                        |
 * +-----------------------------------------------+------------------------------------------------------------------+
 * |  new household has no members                  | members are only added on purpose                                |
 * | add member succeeds                           | normal                                                       |
 * | added member starts with unknown status       | nobody is known to be safe until they check in                   |
 * | add duplicate member fails                    | the same person can't be listed twice                            |
 * | add same name with extra spaces fails         | "Riley" and " Riley " are the same person                        |
 * | add blank member fails                        | every member needs a name                                        |
 * | add null member fails                         | a missing name must not crash the app                            |
 * | add member with details keeps age             | shelters need ages to plan for kids and elderly                  |
 * | add member with details keeps medical note    | medical needs decide which shelter someone goes to               |
 * | add member with details keeps pet type        | shelters need to know about pets                                 |
 * | status of unknown member is null              | error case for status lookup                                     |
 * | status lookup with null name returns null     | a null search must not crash                                     |
 * | check in member records status                | normal check in case                                             |
 * | check in member updates check-in time         | families need to see when someone last checked in                |
 * | check in unknown member fails                 | can't check in someone who isn't in the household                |
 * | check in with null name fails                 | a missing name must not crash the app                            |
 * | check in with null status fails               | a check in has to say whether the person is safe                 |
 * | last check in of unknown member is null       | error case for time lookup                                       |
 * | last check in with null name returns null     | a null search must not crash                                     |
 * +-----------------------------------------------+------------------------------------------------------------------+
 */
public class HouseholdTest {

    private Household household;

    @Before
    public void setUp() {
        household = new Household(UUID.fromString("11111111-1111-1111-1111-111111111111"), "Thiele Home");
    }

    // ----- addHouseholdMember -----

    @Test
    public void newHouseholdHasNoMembers() {
        assertTrue(household.getMembers().isEmpty());
    }

    @Test
    public void addHouseholdMemberSucceeds() {
        assertTrue(household.addHouseholdMember("Riley"));
    }

    @Test
    public void addedMemberStartsWithUnknownSafetyStatus() {
        household.addHouseholdMember("Riley");
        assertEquals(SafetyStatus.UNKNOWN, household.getSafetyStatus("Riley"));
    }

    @Test
    public void addHouseholdMemberRejectsDuplicateName() {
        household.addHouseholdMember("Riley");
        assertFalse(household.addHouseholdMember("Riley"));
    }

    @Test
    public void addHouseholdMemberRejectsSameNameWithExtraSpaces() {
        household.addHouseholdMember("Riley");
        assertFalse(household.addHouseholdMember(" Riley "));
    }

    @Test
    public void addHouseholdMemberRejectsBlankName() {
        assertFalse(household.addHouseholdMember("   "));
    }

    @Test
    public void addHouseholdMemberRejectsNullName() {
        assertFalse(household.addHouseholdMember(null));
    }

    @Test
    public void addHouseholdMemberWithDetailsKeepsAge() {
        household.addHouseholdMember("Grandma Thiele", 82, null, "uses oxygen");
        assertEquals(82, household.getMembers().get(0).getAge());
    }

    @Test
    public void addHouseholdMemberWithDetailsKeepsMedicalNote() {
        household.addHouseholdMember("Grandma Thiele", 82, null, "uses oxygen");
        assertEquals("uses oxygen", household.getMembers().get(0).getMedicalNote());
    }

    @Test
    public void addHouseholdMemberWithDetailsKeepsPetType() {
        household.addHouseholdMember("Biscuit", 0, "dog", null);
        assertEquals("dog", household.getMembers().get(0).getPetType());
    }

    // ----- getSafetyStatus -----

    @Test
    public void getSafetyStatusReturnsNullForUnknownMember() {
        household.addHouseholdMember("Riley");
        assertNull(household.getSafetyStatus("Austin"));
    }

    @Test
    public void getSafetyStatusReturnsNullForNullName() {
        household.addHouseholdMember("Riley");
        assertNull(household.getSafetyStatus(null));
    }

    // ----- checkIn -----

    @Test
    public void checkInRecordsSafetyStatus() {
        household.addHouseholdMember("Riley");
        household.checkIn("Riley", SafetyStatus.IN_NEED);
        assertEquals(SafetyStatus.IN_NEED, household.getSafetyStatus("Riley"));
    }

    @Test
    public void checkInUpdatesLastCheckInTime() {
        household.addHouseholdMember("Riley");
        LocalDateTime before = LocalDateTime.now();
        household.checkIn("Riley", SafetyStatus.SAFE);
        assertFalse(household.getLastCheckIn("Riley").isBefore(before));
    }

    @Test
    public void checkInFailsForUnknownMember() {
        household.addHouseholdMember("Riley");
        assertFalse(household.checkIn("Austin", SafetyStatus.SAFE));
    }

    @Test
    public void checkInFailsForNullName() {
        household.addHouseholdMember("Riley");
        assertFalse(household.checkIn(null, SafetyStatus.SAFE));
    }

    @Test
    public void checkInRejectsNullStatus() {
        household.addHouseholdMember("Riley");
        assertFalse(household.checkIn("Riley", null));
    }

    // ----- getLastCheckIn -----

    @Test
    public void getLastCheckInReturnsNullForUnknownMember() {
        household.addHouseholdMember("Riley");
        assertNull(household.getLastCheckIn("Austin"));
    }

    @Test
    public void getLastCheckInReturnsNullForNullName() {
        household.addHouseholdMember("Riley");
        assertNull(household.getLastCheckIn(null));
    }
}