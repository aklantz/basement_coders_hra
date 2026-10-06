package com.hurricane;

import java.util.UUID;

public class InventoryItem {

    private UUID itemId;
    private String supplyType;
    private int quantity;
    private int reorderThreshold;

    public InventoryItem(UUID itemId, String supplyType, int quantity, int reorderThreshold) {
        this.itemId = itemId;
        this.supplyType = supplyType;
        this.quantity = quantity;
        this.reorderThreshold = reorderThreshold;
    }

    /** UML lists both adjustQuanity and adjustQuantity; the first is a typo, so only one is kept. */
    public void adjustQuantity(int amount) {
        // TODO: add amount (can be negative); don't let quantity drop below 0
    }

    public void setReorderThreshold(int threshold) {
        // TODO: validate and set
    }

    public boolean isLowStock() {
        // TODO: quantity <= reorderThreshold
        return false;
    }
}
