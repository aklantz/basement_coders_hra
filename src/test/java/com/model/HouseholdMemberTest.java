package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for HouseholdMember.
 *
 * +-----------------------------------------------+------------------------------------------------------------------+
 * | Test                                          | Reasoning                                                        |
 * +-----------------------------------------------+------------------------------------------------------------------+
 * | new member has unknown status                 | nobody is known to be safe until they check in                   |
 * | new member is a person                        | members are people unless a pet type is set                      |
 * | new member has no medical note                | no medical need until one is entered                             |
 * | check in records status                       | normal check in case                                             |
 * | check in moves check in time forward          | families need to see the latest check in                         |
 * | positive age is kept                          | normal case                                                      |
 * | negative age becomes zero                     | an age can't be below zero                                       |
 * | pet type makes member a pet                   | shelters treat pets differently                                  |
 * | pet type is trimmed                           | " dog " should match "dog" when shelters check for dogs          |
 * | blank pet type means a person                 | an empty form field shouldn't turn a person into a pet           |
 * | null pet type means a person                  | null is how a person is stored                                   |
 * | medical note is kept                          | medical needs decide which shelter someone goes to               |
 * | medical note is trimmed                       | stray spaces from the form shouldn't be saved                    |
 * | blank medical note means none                 | an empty form field means no medical need                        |
 * +-----------------------------------------------+------------------------------------------------------------------+
 */
public class HouseholdMemberTest {

    private HouseholdMember member;

    @Before
    public void setUp() {
        member = new HouseholdMember("Riley");
    }

    // ----- defaults -----

    @Test
    public void newMemberHasUnknownSafetyStatus() {
        assertEquals(SafetyStatus.UNKNOWN, member.getSafetyStatus());
    }

    @Test
    public void newMemberIsNotAPet() {
        assertFalse(member.isPet());
    }

    @Test
    public void newMemberHasNoMedicalNote() {
        assertNull(member.getMedicalNote());
    }

    // ----- checkIn -----

    @Test
    public void checkInRecordsSafetyStatus() {
        member.checkIn(SafetyStatus.SAFE);
        assertEquals(SafetyStatus.SAFE, member.getSafetyStatus());
    }

    @Test
    public void checkInMovesCheckInTimeForward() {
        LocalDateTime before = LocalDateTime.now();
        member.checkIn(SafetyStatus.SAFE);
        assertFalse(member.getLastCheckIn().isBefore(before));
    }

    // ----- setAge -----

    @Test
    public void setAgeKeepsPositiveAge() {
        member.setAge(21);
        assertEquals(21, member.getAge());
    }

    @Test
    public void setAgeStoresNegativeAgeAsZero() {
        member.setAge(-5);
        assertEquals(0, member.getAge());
    }

    // ----- setPetType -----

    @Test
    public void setPetTypeMakesMemberAPet() {
        member.setPetType("cat");
        assertTrue(member.isPet());
    }

    @Test
    public void setPetTypeTrimsSpaces() {
        member.setPetType("  dog ");
        assertEquals("dog", member.getPetType());
    }

    @Test
    public void setPetTypeBlankMeansPerson() {
        member.setPetType("   ");
        assertFalse(member.isPet());
    }

    @Test
    public void setPetTypeNullMeansPerson() {
        member.setPetType("dog");
        member.setPetType(null);
        assertFalse(member.isPet());
    }

    // ----- setMedicalNote -----

    @Test
    public void setMedicalNoteKeepsNote() {
        member.setMedicalNote("insulin");
        assertEquals("insulin", member.getMedicalNote());
    }

    @Test
    public void setMedicalNoteTrimsSpaces() {
        member.setMedicalNote("  insulin  ");
        assertEquals("insulin", member.getMedicalNote());
    }

    @Test
    public void setMedicalNoteBlankMeansNone() {
        member.setMedicalNote("  ");
        assertNull(member.getMedicalNote());
    }
}