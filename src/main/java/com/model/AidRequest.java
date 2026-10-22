package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

/**
 * A request for aid submitted by a user. Keeps track of the request's status
 * and the tasks assigned to volunteers to carry it out.
 *
 * @author Austin Lantz
 */
public class AidRequest {
    private UUID requestId;
    private User requester;
    private RequestCategory category;
    private RequestStatus status;
    private int householdSize;
    private String streetAddress;
    private boolean isDuplicate;
    private LocalDateTime createdAt;

    private ArrayList<TaskAssignment> assignments = new ArrayList<>();

    /**
     * Creates a new request. It starts as SUBMITTED, is not a duplicate,
     * and has no assignments. The creation time is set to now.
     *
     * @param requestId the unique id of the request
     * @param requester the user who made the request
     * @param category the kind of aid needed
     * @param householdSize how many people need aid
     * @param streetAddress where the aid is needed
     */
    public AidRequest(UUID requestId, User requester, RequestCategory category,
                      int householdSize, String streetAddress) {
        this.requestId = requestId;
        this.requester = requester;
        this.category = category;
        this.householdSize = householdSize;
        this.streetAddress = streetAddress;
        this.status = RequestStatus.SUBMITTED;
        this.isDuplicate = false;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Changes the request's status. This does not check who is asking, so a role
     * check (such as who may complete a request) has to be made before calling it.
     *
     * @param status the new status
     */
    public void updateStatus(RequestStatus status) {
        this.status = status;
    }

    /**
     * Marks this request as a duplicate of another request.
     */
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
     * Marks this request as fulfilled. Whether the user is allowed to do this
     * is checked by the directory before this is called.
     */
    public void complete() {
        updateStatus(RequestStatus.FULFILLED);
    }

    /**
     * Checks whether the request's location can be trusted.
     * Stub: always returns true for now.
     *
     * @return true if the location is reliable
     */
    public boolean isLocationReliable() {
        return true;
    }

    /**
     * Gets when this request was created.
     *
     * @return the creation time
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

    /**
     * Replaces the request's task assignments. A null list is treated as no assignments,
     * so the request can always be saved.
     *
     * @param assignments the assignments to use
     */
    public void setAssignments(ArrayList<TaskAssignment> assignments) {
        this.assignments = (assignments == null) ? new ArrayList<>() : assignments;
    }

    /**
     * Adds one task assignment to the request.
     *
     * @param assignment the assignment to add
     */
    public void addAssignment(TaskAssignment assignment) {
        assignments.add(assignment);
    }

    /**
     * Gets the request's id.
     *
     * @return the request id
     */
    public UUID getRequestId() {
        return requestId;
    }

    /**
     * Gets the user who made the request.
     *
     * @return the requester
     */
    public User getRequester() {
        return requester;
    }

    /**
     * Gets the kind of aid requested.
     *
     * @return the category
     */
    public RequestCategory getCategory() {
        return category;
    }

    /**
     * Gets the request's current status.
     *
     * @return the status
     */
    public RequestStatus getStatus() {
        return status;
    }

    /**
     * Gets how many people need aid.
     *
     * @return the household size
     */
    public int getHouseholdSize() {
        return householdSize;
    }

    /**
     * Gets where the aid is needed.
     *
     * @return the street address
     */
    public String getStreetAddress() {
        return streetAddress;
    }

    /**
     * Checks whether this request was marked as a duplicate.
     *
     * @return true if it is a duplicate
     */
    public boolean isDuplicate() {
        return isDuplicate;
    }

    /**
     * Gets the tasks assigned to volunteers for this request.
     *
     * @return the assignments (empty if there are none)
     */
    public ArrayList<TaskAssignment> getAssignments() {
        return assignments;
    }
}