package com.model;

import java.time.LocalDateTime;
import java.util.UUID;

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

    public void acceptAssignment() {}

    public void confirmCompletion() {}

    public TaskStatus getStatus() {
        return status;
    }

    public void reassign(UUID volunteerId) {}

    public UUID getTaskId() {
        return taskId;
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