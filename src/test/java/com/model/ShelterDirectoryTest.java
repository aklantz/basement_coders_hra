package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ShelterDirectory.
 * Created with AI assistance.
 *
 * +---------------------------------------------------+------------------------------------------------------+
 * | Test                                              | Reasoning                                            |
 * +---------------------------------------------------+------------------------------------------------------+
 * | directory is a singleton                          | callers should share one directory                  |
 * | find shelter by id handles missing ids            | shelters should be retrievable by their identifiers |
 * | active shelters match zip codes and status        | closed shelters and other zip codes are excluded    |
 * | offline shelters contains only closed shelters    | closed shelters should be easy to identify          |
 * +---------------------------------------------------+------------------------------------------------------+
 */
public class ShelterDirectoryTest {

    private ShelterDirectory directory;
    private ArrayList<Shelter> originalShelters;
    private Shelter openShelter;
    private Shelter fullShelter;
    private Shelter closedShelter;

    @Before
    public void setUp() {
        directory = ShelterDirectory.getInstance();
        originalShelters = new ArrayList<Shelter>(directory.getShelters());
        directory.getShelters().clear();

        openShelter = makeShelter("Open shelter", "29708", 10, 4);
        fullShelter = makeShelter("Full shelter", "29708", 10, 10);
        closedShelter = makeShelter("Closed shelter", "29708", 10, 2);
        closedShelter.updateStatus(ShelterStatus.CLOSED);
        directory.getShelters().add(openShelter);
        directory.getShelters().add(fullShelter);
        directory.getShelters().add(closedShelter);
        directory.getShelters().add(makeShelter("Other zip", "29201", 10, 2));
    }

    @After
    public void tearDown() {
        directory.getShelters().clear();
        directory.getShelters().addAll(originalShelters);
    }

    @Test
    public void getInstanceReturnsTheSameDirectory() {
        assertSame(directory, ShelterDirectory.getInstance());
    }

    @Test
    public void getShelterByIdFindsSheltersAndHandlesMissingIds() {
        assertSame(openShelter, directory.getShelterById(openShelter.getResourceId()));
        assertNull(directory.getShelterById(null));
        assertNull(directory.getShelterById(UUID.randomUUID()));
    }

    @Test
    public void getActiveSheltersIncludesOpenAndFullSheltersInRequestedZipCodes() {
        ArrayList<Shelter> active = directory.getActiveShelters(
                new ArrayList<ZipCode>(Arrays.asList(new ZipCode("29708"))));

        assertEquals(2, active.size());
        assertTrue(active.contains(openShelter));
        assertTrue(active.contains(fullShelter));
        assertFalse(active.contains(closedShelter));
        assertTrue(directory.getActiveShelters(null).isEmpty());
    }

    @Test
    public void getOfflineSheltersReturnsOnlyClosedShelters() {
        ArrayList<Shelter> offline = directory.getOfflineShelters();

        assertEquals(1, offline.size());
        assertSame(closedShelter, offline.get(0));
    }

    private Shelter makeShelter(String name, String zipCode, int capacity, int occupancy) {
        Shelter shelter = new Shelter(UUID.randomUUID(), name, capacity, occupancy);
        shelter.setZipCode(zipCode);
        return shelter;
    }
}
