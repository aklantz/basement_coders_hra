package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for InventoryItem.
 *
 *
 * +-----------------------------------------------+------------------------------------------------------------------+
 * | Test                                          | Reasoning                                                        |
 * +-----------------------------------------------+------------------------------------------------------------------+
 * | negative starting quantity                    | a shelter can't start with less than zero of something           |
 * | negative starting threshold                   | a point below zero makes no sense                                |
 * | zero quantity and threshold allowed           | an empty item with no reorder point is valid                     |
 * | adding stock raises quantity                  | normal case for a delivery                                       |
 * | removing stock lowers quantity                | normal case for handing out supplies                             |
 * | removing more than on hand,can't go negative  | you can't hand out water you don't have                          |
 * | set threshold changes it                      | normal                                                           |
 * | set negative threshold throws                 | error case - same rule as the constructor                        |
 * | above threshold is not low stock              | normal                                                           |
 * | exactly at threshold is low stock             | the comment says "at or below"                                   |
 * | below threshold is low stock                  | normal low stock                                                 |
 * +-----------------------------------------------+------------------------------------------------------------------+
 */
public class InventoryItemTest {

    private InventoryItem water;

    @Before
    public void setUp() {
        water = new InventoryItem(UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Bottled Water", 50, 10);
    }

    // ----- constructor -----

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNegativeQuantity() {
        new InventoryItem(UUID.randomUUID(), "Bottled Water", -1, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNegativeThreshold() {
        new InventoryItem(UUID.randomUUID(), "Bottled Water", 50, -1);
    }

    @Test
    public void constructorAllowsZeroQuantityAndThreshold() {
        InventoryItem empty = new InventoryItem(UUID.randomUUID(), "Tarps", 0, 0);
        assertEquals(0, empty.getQuantity());
    }

    // ----- adjustQuantity -----

    @Test
    public void adjustQuantityAddsStock() {
        water.adjustQuantity(25);
        assertEquals(75, water.getQuantity());
    }

    @Test
    public void adjustQuantityRemovesStock() {
        water.adjustQuantity(-20);
        assertEquals(30, water.getQuantity());
    }

    @Test
    public void adjustQuantityDoesNotGoBelowZero() {
        water.adjustQuantity(-60);
        assertTrue(water.getQuantity() >= 0);
    }

    // ----- setReorderThreshold -----

    @Test
    public void setReorderThresholdChangesThreshold() {
        water.setReorderThreshold(30);
        assertEquals(30, water.getReorderThreshold());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setReorderThresholdRejectsNegative() {
        water.setReorderThreshold(-5);
    }

    // ----- isLowStock -----

    @Test
    public void isLowStockFalseAboveThreshold() {
        assertFalse(water.isLowStock());
    }

    @Test
    public void isLowStockTrueAtThreshold() {
        water.adjustQuantity(-40);
        assertTrue(water.isLowStock());
    }

    @Test
    public void isLowStockTrueBelowThreshold() {
        water.adjustQuantity(-45);
        assertTrue(water.isLowStock());
    }
}