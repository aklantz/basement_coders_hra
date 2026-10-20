package com.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A task that a coordinator assigns to a volunteer to help with an aid request.
 */
public class TaskAssignment {

    private UUID taskId;
    private AidRequest request;
    private Volunteer volunteer;
    private Coordinator coordinator;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private TaskStatus status;

    public TaskAssignment(UUID taskId, AidRequest request, Volunteer volunteer, Coordinator coordinator) {
        this.taskId = taskId;
        this.request = request;
        this.volunteer = volunteer;
        this.coordinator = coordinator;
        this.assignedAt = LocalDateTime.now();
        this.status = TaskStatus.PENDING;
    }

    /**
     * Restores saved timestamps and status when loading from JSON.
     */
    public void restoreState(LocalDateTime assignedAt, LocalDateTime acceptedAt,
                             LocalDateTime completedAt, TaskStatus status) {
        this.assignedAt = assignedAt;
        this.acceptedAt = acceptedAt;
        this.completedAt = completedAt;
        this.status = status;
    }

    /**
     * The volunteer accepts the task. Only works while the task is PENDING.
     * The aid request moves to IN_PROGRESS.
     */
    public void acceptAssignment() {
        if (status != TaskStatus.PENDING) {
            return;
        }
        status = TaskStatus.ACCEPTED;
        acceptedAt = LocalDateTime.now();
        if (request != null) {
            request.updateStatus(RequestStatus.IN_PROGRESS);
        }
    }

    /**
     * Marks the task as done. Only works after the volunteer has accepted it.
     * The aid request moves to FULFILLED.
     */
    public void confirmCompletion() {
        if (status != TaskStatus.ACCEPTED) {
            return;
        }
        status = TaskStatus.COMPLETED;
        completedAt = LocalDateTime.now();
        if (request != null) {
            request.updateStatus(RequestStatus.FULFILLED);
        }
    }

    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Gives the task to a different volunteer. The task starts over as PENDING,
     * and the aid request goes back to ASSIGNED.
     *
     * @param volunteerId the id of the new volunteer
     */
    public void reassign(UUID volunteerId) {
        if (status == TaskStatus.COMPLETED || status == TaskStatus.CANCELLED) {
            return;
        }
        User user = UserDirectory.getInstance().getUserById(volunteerId);
        if (!(user instanceof Volunteer)) {
            return;
        }
        volunteer = (Volunteer) user;
        status = TaskStatus.PENDING;
        assignedAt = LocalDateTime.now();
        acceptedAt = null;
        if (request != null) {
            request.updateStatus(RequestStatus.ASSIGNED);
        }
    }
    

    public UUID getTaskId() {
        return taskId;
    }

    public AidRequest getRequest() {
        return request;
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public Coordinator getCoordinator() {
        return coordinator;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}