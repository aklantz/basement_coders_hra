package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for TaskAssignment.
 * The reassign tests add a volunteer to the UserDirectory and remove it afterwards,
 * without saving, so users.json is not changed.
 *
 * +-----------------------------------------------+------------------------------------------------------------------+
 * | Test                                          | Reasoning                                                        |
 * +-----------------------------------------------+------------------------------------------------------------------+
 * | new task is pending                           | a volunteer hasn't said yes yet                                  |
 * | new task has no accepted time                 | it hasn't been accepted                                          |
 * | accept makes task accepted                    | normal case                                                      |
 * | accept records accepted time                  | accepted time should be saved                                    |
 * | accept moves request to in progress           | request status should be updated                                 |
 * | accept twice keeps first accepted time        | accepted time should not change                                  |
 * | accept on completed task does nothing         | finished work can't go back to accepted                          |
 * | accept doesn't revive expired request         | request status should remain expired                             |
 * | accept with no request doesn't crash          | a task loaded without its request must not crash                 |
 * | confirm after accept completes task           | normal case                                                      |
 * | confirm records completed time                | completion time should be saved                                  |
 * | confirm leaves request status alone           | task completion should not change request status                 |
 * | confirm before accept does nothing            | can't finish a task nobody accepted                              |
 * | reassign gives task to new volunteer          | verifies volunteer replacement                                   |
 * | reassign resets task to pending               | the new volunteer has to accept                                  |
 * | reassign clears accepted time                 | accepted time should be reset                                    |
 * | reassign moves request back to assigned       | request should return to assigned status                         |
 * | reassign to unknown id does nothing           | volunteer must exist                                             |
 * | reassign to non-volunteer does nothing        | only volunteers do tasks                                         |
 * | reassign completed task does nothing          | completed tasks should not change                                |
 * | reassign cancelled task does nothing          | cancelled tasks should not change                                |
 * | restore state sets saved status               | loading from JSON must keep the saved status                     |
 * | restore state sets saved times                | loading from JSON must keep the saved times                      |
 * +-----------------------------------------------+------------------------------------------------------------------+
 */
public class TaskAssignmentTest {

    private AidRequest request;
    private Volunteer originalVolunteer;
    private Volunteer newVolunteer;
    private Coordinator coordinator;
    private TaskAssignment task;

    @Before
    public void setUp() {
        LocalDate dob = LocalDate.of(2000, 1, 1);
        originalVolunteer = new Volunteer(UUID.randomUUID(), "test_vol_original", "pw", "Tim Simpson",
                dob, null, null, null, null);
        newVolunteer = new Volunteer(UUID.randomUUID(), "test_vol_replacement_" + UUID.randomUUID(), "pw",
                "Austin Lantz", dob, null, null, null, null);
        coordinator = new Coordinator(UUID.randomUUID(), "test_coord", "pw", "Cora Coord",
                dob, null, null, null, null);
        request = new AidRequest(UUID.randomUUID(), null, RequestCategory.WATER, 4, "10 Main St");
        request.updateStatus(RequestStatus.ASSIGNED);
        task = new TaskAssignment(UUID.fromString("44444444-4444-4444-4444-444444444444"),
                request, originalVolunteer, coordinator);
        UserDirectory.getInstance().addUser(newVolunteer);
    }

    @After
    public void tearDown() {
        UserDirectory.getInstance().deleteUser(newVolunteer.getUserId());
    }

    // ----- constructor -----

    @Test
    public void newTaskIsPending() {
        assertEquals(TaskStatus.PENDING, task.getStatus());
    }

    @Test
    public void newTaskHasNoAcceptedTime() {
        assertNull(task.getAcceptedAt());
    }

    // ----- acceptAssignment -----

    @Test
    public void acceptAssignmentMakesTaskAccepted() {
        task.acceptAssignment();
        assertEquals(TaskStatus.ACCEPTED, task.getStatus());
    }

    @Test
    public void acceptAssignmentRecordsAcceptedTime() {
        task.acceptAssignment();
        assertNotNull(task.getAcceptedAt());
    }

    @Test
    public void acceptAssignmentMovesRequestToInProgress() {
        task.acceptAssignment();
        assertEquals(RequestStatus.IN_PROGRESS, request.getStatus());
    }

    @Test
    public void acceptAssignmentTwiceKeepsFirstAcceptedTime() {
        task.acceptAssignment();
        LocalDateTime firstAccepted = task.getAcceptedAt();
        task.acceptAssignment();
        assertSame(firstAccepted, task.getAcceptedAt());
    }

    @Test
    public void acceptAssignmentDoesNothingOnCompletedTask() {
        task.acceptAssignment();
        task.confirmCompletion();
        task.acceptAssignment();
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }

    @Test
    public void acceptAssignmentDoesNotReviveExpiredRequest() {
        request.expire();
        task.acceptAssignment();
        assertEquals(RequestStatus.EXPIRED, request.getStatus());
    }

    @Test
    public void acceptAssignmentWorksWithoutRequest() {
        TaskAssignment orphan = new TaskAssignment(UUID.randomUUID(), null, originalVolunteer, coordinator);
        orphan.acceptAssignment();
        assertEquals(TaskStatus.ACCEPTED, orphan.getStatus());
    }

    // ----- confirmCompletion -----

    @Test
    public void confirmCompletionAfterAcceptCompletesTask() {
        task.acceptAssignment();
        task.confirmCompletion();
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }

    @Test
    public void confirmCompletionRecordsCompletedTime() {
        task.acceptAssignment();
        task.confirmCompletion();
        assertNotNull(task.getCompletedAt());
    }

    @Test
    public void confirmCompletionLeavesRequestInProgress() {
        task.acceptAssignment();
        task.confirmCompletion();
        assertEquals(RequestStatus.IN_PROGRESS, request.getStatus());
    }

    @Test
    public void confirmCompletionBeforeAcceptDoesNothing() {
        task.confirmCompletion();
        assertEquals(TaskStatus.PENDING, task.getStatus());
    }

    // ----- reassign -----

    @Test
    public void reassignGivesTaskToNewVolunteer() {
        task.reassign(newVolunteer.getUserId());
        assertSame(newVolunteer, task.getVolunteer());
    }

    @Test
    public void reassignResetsTaskToPending() {
        task.acceptAssignment();
        task.reassign(newVolunteer.getUserId());
        assertEquals(TaskStatus.PENDING, task.getStatus());
    }

    @Test
    public void reassignClearsAcceptedTime() {
        task.acceptAssignment();
        task.reassign(newVolunteer.getUserId());
        assertNull(task.getAcceptedAt());
    }

    @Test
    public void reassignMovesRequestBackToAssigned() {
        task.acceptAssignment();
        task.reassign(newVolunteer.getUserId());
        assertEquals(RequestStatus.ASSIGNED, request.getStatus());
    }

    @Test
    public void reassignToUnknownIdDoesNothing() {
        task.reassign(UUID.randomUUID());
        assertSame(originalVolunteer, task.getVolunteer());
    }

    @Test
    public void reassignToNonVolunteerDoesNothing() {
        UserDirectory.getInstance().addUser(coordinator);
        try {
            task.reassign(coordinator.getUserId());
            assertSame(originalVolunteer, task.getVolunteer());
        } finally {
            UserDirectory.getInstance().deleteUser(coordinator.getUserId());
        }
    }

    @Test
    public void reassignDoesNothingOnCompletedTask() {
        task.acceptAssignment();
        task.confirmCompletion();
        task.reassign(newVolunteer.getUserId());
        assertSame(originalVolunteer, task.getVolunteer());
    }

    @Test
    public void reassignDoesNothingOnCancelledTask() {
        task.restoreState(LocalDateTime.of(2026, 10, 1, 9, 0), null, null, TaskStatus.CANCELLED);
        task.reassign(newVolunteer.getUserId());
        assertSame(originalVolunteer, task.getVolunteer());
    }

    // ----- restoreState -----

    @Test
    public void restoreStateSetsSavedStatus() {
        task.restoreState(LocalDateTime.of(2026, 10, 1, 9, 0), LocalDateTime.of(2026, 10, 1, 9, 30),
                null, TaskStatus.ACCEPTED);
        assertEquals(TaskStatus.ACCEPTED, task.getStatus());
    }

    @Test
    public void restoreStateSetsSavedAcceptedTime() {
        task.restoreState(LocalDateTime.of(2026, 10, 1, 9, 0), LocalDateTime.of(2026, 10, 1, 9, 30),
                null, TaskStatus.ACCEPTED);
        assertEquals(LocalDateTime.of(2026, 10, 1, 9, 30), task.getAcceptedAt());
    }
}