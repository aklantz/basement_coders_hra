package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

/**
 * A request for aid submitted by a user.
 * Stub: only what DataLoader needs for now.
 */
public class AidRequest {
    private UUID requestId;
    private User requester;
    private RequestCategory category;
    private UrgencyLevel urgency;
    private RequestStatus status;
    private int householdSize;
    private String streetAddress;
    private boolean isDuplicate;
    private LocalDateTime createdAt;

    private ArrayList<TaskAssignment> assignments = new ArrayList<>();
    private ArrayList<StatusChange> statusHistory = new ArrayList<>();

    public AidRequest(UUID requestId, User requester, RequestCategory category, UrgencyLevel urgency,
                      int householdSize, String streetAddress) {
        this.requestId = requestId;
        this.requester = requester;
        this.category = category;
        this.urgency = urgency;
        this.householdSize = householdSize;
        this.streetAddress = streetAddress;
        this.status = RequestStatus.SUBMITTED;
        this.isDuplicate = false;
        this.createdAt = LocalDateTime.now();
    }

    public void updateStatus(RequestStatus status) {
        this.status = status;
    }

    public void markDuplicate() {
        this.isDuplicate = true;
    }

    /**
     * Marks this request as expired.
     */
    public void expire() {
        updateStatus(RequestStatus.EXPIRED);
    }

    /**
     * @return when this request was created
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Restores the creation time when loading from JSON.
     *
     * @param createdAt when this request was created
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setAssignments(ArrayList<TaskAssignment> assignments) {
        this.assignments = assignments;
    }

    public void setStatusHistory(ArrayList<StatusChange> statusHistory) {
        this.statusHistory = statusHistory;
    }

    public boolean isLocationReliable() {
        return true;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public User getRequester() {
        return requester;
    }

    public RequestCategory getCategory() {
        return category;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public UrgencyLevel getUrgency() {
        return urgency;
    }

    public int getHouseholdSize() {
        return householdSize;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public boolean isDuplicate() {
        return isDuplicate;
    }

    public ArrayList<TaskAssignment> getAssignments() {
        return assignments;
    }

    public ArrayList<StatusChange> getStatusHistory() {
        return statusHistory;
    }
}