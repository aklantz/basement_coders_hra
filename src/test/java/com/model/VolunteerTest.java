package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for a volunteer's profile.
 * Created with AI assistance.
 *
 * +---------------------------------------------------+------------------------------------------------------+
 * | Test                                              | Reasoning                                            |
 * +---------------------------------------------------+------------------------------------------------------+
 * | new volunteer has no specialty                   | volunteers choose a specialty for their profile     |
 * | specialty can be added to the profile             | request matching uses the volunteer's specialty     |
 * | specialty can be cleared from the profile         | volunteers must be able to update their profile     |
 * | profile keeps the volunteer's personal details    | contact details belong to the volunteer's profile   |
 * +---------------------------------------------------+------------------------------------------------------+
 */
public class VolunteerTest {

    private Volunteer volunteer;

    @Before
    public void setUp() {
        volunteer = new Volunteer(UUID.fromString("66666666-6666-6666-6666-666666666666"),
                "volunteer1", "password", "Val Volunteer", LocalDate.of(1990, 4, 12),
                "10 Main St, Columbia, SC 29201", "val@example.com",
                "803-555-0100", "volunteer1");
    }

    @Test
    public void newVolunteerHasNoSpecialty() {
        assertNull(volunteer.getSpecialty());
    }

    @Test
    public void setSpecialtyUpdatesVolunteerProfile() {
        volunteer.setSpecialty(RequestCategory.MEDICAL);

        assertEquals(RequestCategory.MEDICAL, volunteer.getSpecialty());
    }

    @Test
    public void setNullSpecialtyClearsVolunteerProfile() {
        volunteer.setSpecialty(RequestCategory.FOOD);
        volunteer.setSpecialty(null);

        assertNull(volunteer.getSpecialty());
    }

    @Test
    public void profileReturnsVolunteersPersonalDetails() {
        assertEquals("Val Volunteer", volunteer.getFullName());
        assertEquals("volunteer1", volunteer.getUsername());
        assertEquals(LocalDate.of(1990, 4, 12), volunteer.getDateOfBirth());
        assertEquals("10 Main St, Columbia, SC 29201", volunteer.getHomeAddress());
        assertEquals("val@example.com", volunteer.getEmailAddress());
        assertEquals("803-555-0100", volunteer.getPhoneNumber());
    }
}
