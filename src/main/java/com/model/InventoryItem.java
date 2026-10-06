package com.model;

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

    public void adjustQuantity(int amount) {
    }

    public void setReorderThreshold(int threshold) {
    }

    public boolean isLowStock() {
        return false;
    }
}