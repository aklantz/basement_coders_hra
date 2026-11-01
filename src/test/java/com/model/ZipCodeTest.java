package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for ZipCode.
 *
 * +-----------------------------------------------+------------------------------------------------------------------+
 * | Test                                          | Reasoning                                                        |
 * +-----------------------------------------------+------------------------------------------------------------------+
 * | spaces are trimmed                            | extra spaces should not affect matching                          |
 * | null code stays null                          | missing zip codes should be handled safely                       |
 * | matches same text                             | normal case                                                      |
 * | matches text with spaces                      | extra spaces should not affect matching                          |
 * | does not match different zip                  | different zip codes should not match                             |
 * | does not match null text                      | null values should not match                                     |
 * | null zip matches nothing                      | missing zip codes should not match                               |
 * | equal when same code                          | same zip code should be considered equal                         |
 * | not equal when different code                 | different zip codes should not be equal                          |
 * | not equal to a plain string                   | different object types should not be equal                       |
 * | equal to itself                               | basic equals rule                                                |
 * | equal zips have same hash code                | required for HashSet and HashMap usage                            |
 * | toString shows the code                       | string output should show the zip code                           |
 * +-----------------------------------------------+------------------------------------------------------------------+
 */
public class ZipCodeTest {

    @Test
    public void constructorTrimsSpaces() {
        assertEquals("29708", new ZipCode("  29708 ").getCode());
    }

    @Test
    public void constructorKeepsNullCode() {
        assertNull(new ZipCode(null).getCode());
    }

    // ----- matches -----

    @Test
    public void matchesSameText() {
        assertTrue(new ZipCode("29708").matches("29708"));
    }

    @Test
    public void matchesTextWithExtraSpaces() {
        assertTrue(new ZipCode("29708").matches(" 29708  "));
    }

    @Test
    public void doesNotMatchDifferentZip() {
        assertFalse(new ZipCode("29708").matches("29715"));
    }

    @Test
    public void doesNotMatchNullText() {
        assertFalse(new ZipCode("29708").matches(null));
    }

    @Test
    public void nullZipMatchesNothing() {
        assertFalse(new ZipCode(null).matches("29708"));
    }

    // ----- equals / hashCode / toString -----

    @Test
    public void equalWhenSameCode() {
        assertEquals(new ZipCode("29708"), new ZipCode(" 29708"));
    }

    @Test
    public void notEqualWhenDifferentCode() {
        assertNotEquals(new ZipCode("29708"), new ZipCode("29715"));
    }

    @Test
    public void notEqualToPlainString() {
        assertFalse(new ZipCode("29708").equals("29708"));
    }

    @Test
    public void equalToItself() {
        ZipCode zip = new ZipCode("29708");
        assertTrue(zip.equals(zip));
    }

    @Test
    public void equalZipCodesHaveSameHashCode() {
        assertEquals(new ZipCode("29708").hashCode(), new ZipCode("29708 ").hashCode());
    }

    @Test
    public void toStringShowsCode() {
        assertEquals("29708", new ZipCode("29708").toString());
    }
}