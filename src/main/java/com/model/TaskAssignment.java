package com.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A task that a coordinator assigns to a volunteer to help with an aid request.
 * A volunteer finishing the task does not complete the request; only a coordinator
 * or admin can do that through the request directory.
 *
 * @author Austin Lantz
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

    /**
     * Creates a task that starts as PENDING, with the assigned time set to now.
     *
     * @param taskId the unique id of the task
     * @param request the aid request the task is for
     * @param volunteer the volunteer who will do the task
     * @param coordinator the coordinator who assigned the task
     */
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
     * The request is not changed.
     *
     * @param assignedAt when the task was assigned
     * @param acceptedAt when the volunteer accepted it, or null
     * @param completedAt when the task was completed, or null
     * @param status the saved status
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
     * The aid request is left as it is: a coordinator or admin completes the
     * request itself through the request directory.
     */
    public void confirmCompletion() {
        if (status != TaskStatus.ACCEPTED) {
            return;
        }
        status = TaskStatus.COMPLETED;
        completedAt = LocalDateTime.now();
    }

    /**
     * Gets the task's current status.
     *
     * @return the status
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Gives the task to a different volunteer. The task starts over as PENDING,
     * and the aid request goes back to ASSIGNED. Does nothing if the task is already
     * COMPLETED or CANCELLED, or if the id is not a volunteer.
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

    /**
     * Gets the task's id.
     *
     * @return the task id
     */
    public UUID getTaskId() {
        return taskId;
    }

    /**
     * Gets the aid request this task is for.
     *
     * @return the request
     */
    public AidRequest getRequest() {
        return request;
    }

    /**
     * Gets the volunteer doing the task.
     *
     * @return the volunteer
     */
    public Volunteer getVolunteer() {
        return volunteer;
    }

    /**
     * Gets the coordinator who assigned the task.
     *
     * @return the coordinator
     */
    public Coordinator getCoordinator() {
        return coordinator;
    }

    /**
     * Gets when the task was assigned.
     *
     * @return the assigned time
     */
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    /**
     * Gets when the volunteer accepted the task.
     *
     * @return the accepted time, or null if not accepted yet
     */
    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    /**
     * Gets when the task was completed.
     *
     * @return the completed time, or null if not completed yet
     */
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}