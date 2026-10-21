package com.model;

import java.util.UUID;

/**
 * Tracks a supply's available quantity and the threshold for replenishing it.
 */
public class InventoryItem {

    private UUID itemId;
    private String supplyType;
    private int quantity;
    private int reorderThreshold;

    /**
     * Creates an inventory item.
     *
     * @param itemId the item's unique identifier
     * @param supplyType the type of supply
     * @param quantity the initial nonnegative quantity
     * @param reorderThreshold the nonnegative replenishment threshold
     * @throws IllegalArgumentException if quantity or reorderThreshold is negative
     */
    public InventoryItem(UUID itemId, String supplyType, int quantity, int reorderThreshold) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        this.itemId = itemId;
        this.supplyType = supplyType;
        this.quantity = quantity;
        setReorderThreshold(reorderThreshold);
    }

    /**
     * Adjusts the quantity, allowing the resulting stock to be negative.
     *
     * @param amount the quantity to add, or a negative quantity to remove
     */
    public void adjustQuantity(int amount) {
        quantity += amount;
    }

    /**
     * Sets the quantity at or below which stock needs replenishment.
     *
     * @param threshold the nonnegative replenishment threshold
     * @throws IllegalArgumentException if threshold is negative
     */
    public void setReorderThreshold(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Reorder threshold cannot be negative.");
        }
        reorderThreshold = threshold;
    }

    /**
     * Reports whether stock needs replenishment.
     *
     * @return true if quantity is at or below the reorder threshold
     */
    public boolean isLowStock() {
        return quantity <= reorderThreshold;
    }

    /**
     * Returns the item's unique identifier.
     * @return the item identifier
     */
    public UUID getItemId() {
        return itemId;
    }

    /**
     * Returns the type of supply.
     * @return the supply type
     */
    public String getSupplyType() {
        return supplyType;
    }

    /**
     * Returns the available quantity.
     * @return the current quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the replenishment threshold.
     * @return the reorder threshold
     */
    public int getReorderThreshold() {
        return reorderThreshold;
    }
}
