package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.location.Coordinates;

/**
 * Tests for Shelter. getCoordinates is only tested in the cases that don't
 * need the internet (cached coordinates, missing address).
 *
 * +-----------------------------------------------+------------------------------------------------------------------+
 * | Test                                          | Reasoning                                                        |
 * +-----------------------------------------------+------------------------------------------------------------------+
 * | shelter with room starts open                 | shelter should be available for check in                         |
 * | shelter at capacity starts full               | boundary case: no spots left                                     |
 * | negative capacity stored as zero              | capacity cannot be negative                                      |
 * | restored shelter keeps saved status           | saved status should be restored                                  |
 * | restored shelter keeps saved time             | saved timestamp should be restored                               |
 * | remaining capacity is capacity - occupancy    | normal case                                                      |
 * | remaining capacity never negative             | remaining capacity should not go below zero                      |
 * | update status changes status                  | verifies status update                                           |
 * | update capacity reopens a full shelter        | increasing capacity creates space                                |
 * | negative capacity update ignored              | invalid input                                                    |
 * | update occupancy to capacity makes full       | boundary case                                                    |
 * | negative occupancy update ignored             | invalid input                                                    |
 * | closed shelter stays closed when people leave | shelter status should not change automatically                   |
 * | full address is complete                      | required for address lookups                                     |
 * | missing zip means no address                  | zip code is required                                             |
 * | address object built from fields              | address object should be created correctly                       |
 * | no address object when field missing          | invalid address data                                             |
 * | cached coordinates returned                   | avoids repeated coordinate lookups                               |
 * | no coordinates without address                | address is required                                              |
 * | check in adds occupants                       | normal case                                                      |
 * | check in raises occupancy                     | occupancy count should increase                                  |
 * | check in pets don't count toward occupancy    | occupancy only tracks people                                     |
 * | check in records pets                         | pet information should be stored                                 |
 * | check in with null pets works                 | pets are optional                                                |
 * | check in null people fails                    | invalid input                                                    |
 * | check in to closed shelter fails              | closed shelters cannot accept check ins                          |
 * | check in more people than spots fails         | shelter cannot exceed capacity                                   |
 * | check in exactly remaining spots works        | boundary case: fills remaining capacity                          |
 * | check in someone already inside fails         | duplicate occupants not allowed                                  |
 * | check in same name twice in one group fails   | duplicate entries not allowed                                    |
 * | failed check in adds nobody                   | check in should be all or nothing                                |
 * | medical staff flag can be set                 | used for shelter matching                                        |
 * | dogs flag can be set                          | used for shelter matching                                        |
 * | toString summarizes availability              | displayed in shelter listings                                    |
 * +-----------------------------------------------+------------------------------------------------------------------+
 */
public class ShelterTest {

    private Shelter shelter;

    @Before
    public void setUp() {
        shelter = new Shelter(UUID.fromString("55555555-5555-5555-5555-555555555555"),
                "Tega Cay High School", 100, 40);
    }

    private ArrayList<String> names(String... people) {
        return new ArrayList<>(Arrays.asList(people));
    }

    private void giveFullAddress() {
        shelter.setAddress("7725 Tega Cay Dr", "Tega Cay", "SC");
        shelter.setZipCode("29708");
    }

    // ----- constructors -----

    @Test
    public void shelterWithRoomStartsOpen() {
        assertEquals(ShelterStatus.OPEN, shelter.getStatus());
    }

    @Test
    public void shelterAtCapacityStartsFull() {
        Shelter packed = new Shelter(UUID.randomUUID(), "Gym", 50, 50);
        assertEquals(ShelterStatus.FULL, packed.getStatus());
    }

    @Test
    public void negativeCapacityStoredAsZero() {
        Shelter broken = new Shelter(UUID.randomUUID(), "Gym", -10, 0);
        assertEquals(0, broken.getCapacity());
    }

    @Test
    public void restoredShelterKeepsSavedStatus() {
        Shelter restored = new Shelter(UUID.randomUUID(), "Gym", 50, 10,
                ShelterStatus.CLOSED, LocalDateTime.of(2026, 10, 1, 8, 0));
        assertEquals(ShelterStatus.CLOSED, restored.getStatus());
    }

    @Test
    public void restoredShelterKeepsSavedTime() {
        Shelter restored = new Shelter(UUID.randomUUID(), "Gym", 50, 10,
                ShelterStatus.OPEN, LocalDateTime.of(2026, 10, 1, 8, 0));
        assertEquals(LocalDateTime.of(2026, 10, 1, 8, 0), restored.getLastUpdated());
    }

    // ----- getRemainingCapacity -----

    @Test
    public void remainingCapacityIsCapacityMinusOccupancy() {
        assertEquals(60, shelter.getRemainingCapacity());
    }

    @Test
    public void remainingCapacityNeverNegative() {
        Shelter overFull = new Shelter(UUID.randomUUID(), "Gym", 50, 55);
        assertEquals(0, overFull.getRemainingCapacity());
    }

    // ----- updateStatus / updateCapacity / updateOccupancy -----

    @Test
    public void updateStatusChangesStatus() {
        shelter.updateStatus(ShelterStatus.CLOSED);
        assertEquals(ShelterStatus.CLOSED, shelter.getStatus());
    }

    @Test
    public void updateCapacityReopensFullShelter() {
        shelter.updateOccupancy(100);
        shelter.updateCapacity(150);
        assertEquals(ShelterStatus.OPEN, shelter.getStatus());
    }

    @Test
    public void updateCapacityIgnoresNegative() {
        shelter.updateCapacity(-1);
        assertEquals(100, shelter.getCapacity());
    }

    @Test
    public void updateOccupancyToCapacityMakesFull() {
        shelter.updateOccupancy(100);
        assertEquals(ShelterStatus.FULL, shelter.getStatus());
    }

    @Test
    public void updateOccupancyIgnoresNegative() {
        shelter.updateOccupancy(-3);
        assertEquals(40, shelter.getOccupancy());
    }

    @Test
    public void closedShelterStaysClosedWhenOccupancyDrops() {
        shelter.updateStatus(ShelterStatus.CLOSED);
        shelter.updateOccupancy(0);
        assertEquals(ShelterStatus.CLOSED, shelter.getStatus());
    }

    // ----- address -----

    @Test
    public void hasAddressTrueWhenAllFieldsSet() {
        giveFullAddress();
        assertTrue(shelter.hasAddress());
    }

    @Test
    public void hasAddressFalseWhenZipMissing() {
        shelter.setAddress("7725 Tega Cay Dr", "Tega Cay", "SC");
        assertFalse(shelter.hasAddress());
    }

    @Test
    public void getAddressBuiltFromFields() {
        giveFullAddress();
        assertEquals("7725 Tega Cay Dr, Tega Cay, SC 29708", shelter.getAddress().toString());
    }

    @Test
    public void getAddressNullWhenFieldMissing() {
        assertNull(shelter.getAddress());
    }

    // ----- getCoordinates (no internet needed) -----

    @Test
    public void getCoordinatesReturnsCachedLatitude() throws Exception {
        shelter.setCoordinates(35.02, -81.01);
        Coordinates coordinates = shelter.getCoordinates();
        assertEquals(35.02, coordinates.getLatitude(), 0.0001);
    }

    @Test
    public void getCoordinatesNullWithoutAddress() throws Exception {
        assertNull(shelter.getCoordinates());
    }

    // ----- checkIn -----

    @Test
    public void checkInAddsOccupants() {
        shelter.checkIn(names("Connor", "Riley"), null);
        assertEquals(names("Connor", "Riley"), shelter.getOccupants());
    }

    @Test
    public void checkInRaisesOccupancy() {
        shelter.checkIn(names("Connor", "Riley"), null);
        assertEquals(42, shelter.getOccupancy());
    }

    @Test
    public void checkInPetsDoNotCountTowardOccupancy() {
        shelter.checkIn(names("Connor"), names("Biscuit", "Whiskers"));
        assertEquals(41, shelter.getOccupancy());
    }

    @Test
    public void checkInRecordsPets() {
        shelter.checkIn(names("Connor"), names("Biscuit"));
        assertEquals(names("Biscuit"), shelter.getPets());
    }

    @Test
    public void checkInWorksWithNullPets() {
        assertTrue(shelter.checkIn(names("Connor"), null));
    }

    @Test
    public void checkInFailsForNullPeople() {
        assertFalse(shelter.checkIn(null, names("Biscuit")));
    }

    @Test
    public void checkInFailsWhenClosed() {
        shelter.updateStatus(ShelterStatus.CLOSED);
        assertFalse(shelter.checkIn(names("Connor"), null));
    }

    @Test
    public void checkInFailsWhenMorePeopleThanSpots() {
        shelter.updateOccupancy(99);
        assertFalse(shelter.checkIn(names("Connor", "Riley"), null));
    }

    @Test
    public void checkInFillsExactlyRemainingSpots() {
        shelter.updateOccupancy(98);
        assertTrue(shelter.checkIn(names("Connor", "Riley"), null));
    }

    @Test
    public void checkInFailsForPersonAlreadyInside() {
        shelter.checkIn(names("Connor"), null);
        assertFalse(shelter.checkIn(names("Tim", "Connor"), null));
    }

    @Test
    public void checkInFailsForSameNameTwiceInOneGroup() {
        assertFalse(shelter.checkIn(names("Austin", "Austin"), null));
    }

    @Test
    public void failedCheckInAddsNobody() {
        shelter.checkIn(names("Connor"), null);
        shelter.checkIn(names("Tim", "Connor"), null);
        assertEquals(names("Connor"), shelter.getOccupants());
    }

    // ----- flags and toString -----

    @Test
    public void setMedicalStaffMarksMedicalAvailable() {
        shelter.setMedicalStaff(true);
        assertTrue(shelter.hasMedicalStaff());
    }

    @Test
    public void setDogsAllowedMarksDogsAccepted() {
        shelter.setDogsAllowed(true);
        assertTrue(shelter.acceptsDogs());
    }

    @Test
    public void toStringSummarizesAvailability() {
        assertEquals("Tega Cay High School | Status: OPEN | Occupancy: 40/100 | Spots left: 60",
                shelter.toString());
    }
}