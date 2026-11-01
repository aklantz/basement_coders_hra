package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * Tests for HurricaneDirectory.
 * Created with AI assistance.
 *
 * +---------------------------------------------------+------------------------------------------------------+
 * | Test                                              | Reasoning                                            |
 * +---------------------------------------------------+------------------------------------------------------+
 * | directory is a singleton                          | callers should share one directory                  |
 * | getters return loaded hurricane information       | hurricane details should match saved data           |
 * +---------------------------------------------------+------------------------------------------------------+
 */
public class HurricaneDirectoryTest {

    @Test
    public void getInstanceReturnsTheSameDirectory() {
        assertSame(HurricaneDirectory.getInstance(), HurricaneDirectory.getInstance());
    }

    @Test
    public void gettersReturnTheLoadedHurricaneInformation() {
        HurricaneDirectory directory = HurricaneDirectory.getInstance();

        assertNotNull(directory.getAffectedZipCodes());
        assertEquals(DataLoader.getAffectedZipCodes(), directory.getAffectedZipCodes());
        assertEquals(DataLoader.getHurricaneCategory(), directory.getHurricaneCategory());
        assertEquals(DataLoader.getWindSpeed(), directory.getWindSpeed());
    }
}
