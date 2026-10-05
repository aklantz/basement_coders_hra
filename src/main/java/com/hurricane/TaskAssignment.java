package com.hurricane;

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

    public void acceptAssignment() {
    }

    public void confirmCompletion() {
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void reassign(UUID volunteerId) {
        
    }
}