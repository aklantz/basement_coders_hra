package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
/**
 * Tests for AidRequest.
 *
 * ┌───────────────────────────────────────────────┬──────────────────────────────────────────────────────┐
 * │ Test                                          │ Reasoning                                            │
 * ├───────────────────────────────────────────────┼──────────────────────────────────────────────────────┤
 * │ new request is submitted                      │ requests start in submitted status                   │
 * │ new request is not a duplicate                │ only a coordinator marks duplicates                  │
 * │ new request has no assignments                │ assignments are added later                          │
 * │ request without note has empty note           │ note should default to an empty string               │
 * │ request with null note has empty note         │ null notes should be handled safely                  │
 * │ medical need found in household               │ medical needs affect request handling                │
 * │ no medical need when nobody has a note        │ normal "no" case                                     │
 * │ no medical need when requester is null        │ a request with no user must not crash                │
 * │ dog found in household                        │ some shelters have pet requirements                  │
 * │ dog found regardless of capital letters       │ capitalization should not matter                     │
 * │ cat is not a dog                              │ only dogs matter for the dog policy                  │
 * │ no dog when requester is null                 │ a request with no user must not crash                │
 * │ update status changes status                  │ normal                                               │
 * │ mark duplicate flags request                  │ normal                                               │
 * │ expire sets expired                           │ normal                                               │
 * │ complete sets fulfilled                       │ normal                                               │
 * │ expired request can't be completed            │ expired requests should stay expired                 │
 * │ fulfilled request can't expire                │ fulfilled requests should stay fulfilled             │
 * │ set null assignments gives empty list         │ avoids null assignment lists                         │
 * │ set assignments replaces list                 │ normal - data loader                                 │
 * │ add assignment adds it                        │ normal                                               │
 * │ add null assignment is ignored                │ prevents invalid assignments from being stored       │
 * │ negative household size rejected              │ a household can't have negative people               │
 * └───────────────────────────────────────────────┴──────────────────────────────────────────────────────┘
 */

public class AidRequestTest {

    private Resident requester;
    private AidRequest request;

    @Before
    public void setUp() {
        requester = new Resident(UUID.randomUUID(), "rhaywood", "pw", "Riley Haywood",
                LocalDate.of(2003, 3, 3), "10 Main St, Columbia, SC 29201", null, null, null);
        requester.addHousehold("Haywood Home");
        request = new AidRequest(UUID.fromString("33333333-3333-3333-3333-333333333333"),
                requester, RequestCategory.FOOD, 3, "10 Main St");
    }

    private TaskAssignment makeTask() {
        return new TaskAssignment(UUID.randomUUID(), request, null, null);
    }

    // ----- constructor defaults -----

    @Test
    public void newRequestIsSubmitted() {
        assertEquals(RequestStatus.SUBMITTED, request.getStatus());
    }

    @Test
    public void newRequestIsNotDuplicate() {
        assertFalse(request.isDuplicate());
    }

    @Test
    public void newRequestHasNoAssignments() {
        assertTrue(request.getAssignments().isEmpty());
    }

    @Test
    public void requestWithoutNoteHasEmptyNote() {
        assertEquals("", request.getNote());
    }

    @Test
    public void requestWithNullNoteHasEmptyNote() {
        AidRequest withNote = new AidRequest(UUID.randomUUID(), requester,
                RequestCategory.WATER, 2, "10 Main St", null);
        assertEquals("", withNote.getNote());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNegativeHouseholdSize() {
        new AidRequest(UUID.randomUUID(), requester, RequestCategory.FOOD, -2, "10 Main St");
    }

    // ----- needsMedicalAssistance -----

    @Test
    public void needsMedicalAssistanceTrueWhenMemberHasMedicalNote() {
        requester.addHouseholdMember("Haywood Home", "Pop Haywood", 80, null, "wheelchair");
        assertTrue(request.needsMedicalAssistance());
    }

    @Test
    public void needsMedicalAssistanceFalseWhenNoMedicalNotes() {
        requester.addHouseholdMember("Haywood Home", "Sis Haywood", 15, null, null);
        assertFalse(request.needsMedicalAssistance());
    }

    @Test
    public void needsMedicalAssistanceFalseWhenRequesterIsNull() {
        AidRequest anonymous = new AidRequest(UUID.randomUUID(), null, RequestCategory.FOOD, 1, "10 Main St");
        assertFalse(anonymous.needsMedicalAssistance());
    }

    // ----- hasDog -----

    @Test
    public void hasDogTrueWhenHouseholdHasDog() {
        requester.addHouseholdMember("Haywood Home", "Biscuit", 0, "dog", null);
        assertTrue(request.hasDog());
    }

    @Test
    public void hasDogIgnoresCapitalLetters() {
        requester.addHouseholdMember("Haywood Home", "Biscuit", 0, "DOG", null);
        assertTrue(request.hasDog());
    }

    @Test
    public void hasDogFalseWhenOnlyCat() {
        requester.addHouseholdMember("Haywood Home", "Whiskers", 0, "cat", null);
        assertFalse(request.hasDog());
    }

    @Test
    public void hasDogFalseWhenRequesterIsNull() {
        AidRequest anonymous = new AidRequest(UUID.randomUUID(), null, RequestCategory.FOOD, 1, "10 Main St");
        assertFalse(anonymous.hasDog());
    }

    // ----- status changes -----

    @Test
    public void updateStatusChangesStatus() {
        request.updateStatus(RequestStatus.ASSIGNED);
        assertEquals(RequestStatus.ASSIGNED, request.getStatus());
    }

    @Test
    public void markDuplicateFlagsRequest() {
        request.markDuplicate();
        assertTrue(request.isDuplicate());
    }

    @Test
    public void expireSetsExpiredStatus() {
        request.expire();
        assertEquals(RequestStatus.EXPIRED, request.getStatus());
    }

    @Test
    public void completeSetsFulfilledStatus() {
        request.complete();
        assertEquals(RequestStatus.FULFILLED, request.getStatus());
    }

    @Test
    public void completeDoesNotChangeExpiredRequest() {
        request.expire();
        request.complete();
        assertEquals(RequestStatus.EXPIRED, request.getStatus());
    }

    @Test
    public void expireDoesNotChangeFulfilledRequest() {
        request.complete();
        request.expire();
        assertEquals(RequestStatus.FULFILLED, request.getStatus());
    }

    // ----- assignments -----

    @Test
    public void setAssignmentsWithNullGivesEmptyList() {
        request.setAssignments(null);
        assertTrue(request.getAssignments().isEmpty());
    }

    @Test
    public void setAssignmentsReplacesList() {
        ArrayList<TaskAssignment> saved = new ArrayList<>();
        saved.add(makeTask());
        saved.add(makeTask());
        request.setAssignments(saved);
        assertEquals(2, request.getAssignments().size());
    }

    @Test
    public void addAssignmentAddsTask() {
        request.addAssignment(makeTask());
        assertEquals(1, request.getAssignments().size());
    }

    @Test
    public void addAssignmentIgnoresNull() {
        request.addAssignment(null);
        assertTrue(request.getAssignments().isEmpty());
    }
}