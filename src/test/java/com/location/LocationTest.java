package com.location;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for the display text of Address, Coordinates and MapLocation.
 * +-----------------------------------------------+------------------------------------------------------------------+
 * | Test                                          | Reasoning                                                        |
 * +-----------------------------------------------+------------------------------------------------------------------+
 * | address prints in mailing format              | this string is what gets sent to the map lookup                  |
 * | coordinates print as "lat, long"              | shown to users and used in logs                                  |
 * | map location prints its address               | the UI lists map locations by address                            |
 * +-----------------------------------------------+------------------------------------------------------------------+
 */

public class LocationTest {

    private final Address address = new Address("142 Riverbend Dr", "Tega Cay", "SC", "29708");

    @Test
    public void addressPrintsInMailingFormat() {
        assertEquals("142 Riverbend Dr, Tega Cay, SC 29708", address.toString());
    }

    @Test
    public void coordinatesPrintAsLatitudeCommaLongitude() {
        assertEquals("34.0, -81.0", new Coordinates(34.0, -81.0).toString());
    }

    @Test
    public void mapLocationPrintsItsAddress() {
        MapLocation location = new MapLocation(address, new Coordinates(35.02, -81.01));
        assertEquals("142 Riverbend Dr, Tega Cay, SC 29708", location.toString());
    }
}