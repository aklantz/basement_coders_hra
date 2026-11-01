package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for AidRequestDirectory.
 * Created with AI assistance.
 *
 * +---------------------------------------------------+------------------------------------------------------+
 * | Test                                              | Reasoning                                            |
 * +---------------------------------------------------+------------------------------------------------------+
 * | directory is a singleton                          | callers should share one directory                  |
 * | add request rejects null and accepts a request    | invalid requests should not be stored               |
 * | find request by id and requester id               | requests must be retrievable by their identifiers   |
 * | open requests match county and submitted status   | only open requests in the requested county qualify  |
 * | duplicate matches requester and category          | duplicate requests need the same user and category  |
 * | assigned requests match their volunteer          | volunteers should see only their assigned requests  |
 * | stale requests expire only when submitted         | only old, unassigned requests should expire         |
 * +---------------------------------------------------+------------------------------------------------------+
 */
public class AidRequestDirectoryTest {

    private AidRequestDirectory directory;
    private ArrayList<AidRequest> originalRequests;
    private Resident requester;

    @Before
    public void setUp() {
        directory = AidRequestDirectory.getInstance();
        originalRequests = new ArrayList<AidRequest>(directory.getRequests());
        directory.getRequests().clear();
        requester = new Resident(UUID.randomUUID(), "resident_" + UUID.randomUUID(),
                "password", "Rae Resident", LocalDate.of(1990, 1, 1),
                null, null, null, null);
    }

    @After
    public void tearDown() {
        directory.getRequests().clear();
        directory.getRequests().addAll(originalRequests);
    }

    @Test
    public void getInstanceReturnsTheSameDirectory() {
        assertSame(directory, AidRequestDirectory.getInstance());
    }

    @Test
    public void addRequestRejectsNullAndAddsAValidRequest() {
        AidRequest request = makeRequest(requester, RequestCategory.FOOD, "1 Main St");

        assertFalse(directory.addRequest(null));
        assertTrue(directory.addRequest(request));
        assertSame(request, directory.getRequests().get(0));
    }

    @Test
    public void findRequestByIdAndRequesterId() {
        AidRequest request = makeRequest(requester, RequestCategory.FOOD, "1 Main St");
        directory.addRequest(request);

        assertSame(request, directory.getRequestById(request.getRequestId()));
        assertSame(request, directory.getRequest(requester.getUserId()));
        assertNull(directory.getRequestById(null));
        assertNull(directory.getRequestById(UUID.randomUUID()));
        assertNull(directory.getRequest(null));
    }

    @Test
    public void getOpenRequestsReturnsOnlySubmittedRequestsInCounty() {
        AidRequest openInCounty = makeRequest(requester, RequestCategory.FOOD,
                "1 Main St, Columbia");
        AidRequest closedInCounty = makeRequest(requester, RequestCategory.WATER,
                "2 Main St, Columbia");
        AidRequest openElsewhere = makeRequest(requester, RequestCategory.MEDICAL,
                "3 Main St, Charleston");
        closedInCounty.updateStatus(RequestStatus.ASSIGNED);
        directory.addRequest(openInCounty);
        directory.addRequest(closedInCounty);
        directory.addRequest(openElsewhere);

        ArrayList<AidRequest> results = directory.getOpenRequests("Columbia");

        assertEquals(1, results.size());
        assertSame(openInCounty, results.get(0));
        assertTrue(directory.getOpenRequests(null).isEmpty());
    }

    @Test
    public void findDuplicateMatchesRequesterAndCategory() {
        AidRequest existing = makeRequest(requester, RequestCategory.FOOD, "1 Main St");
        directory.addRequest(existing);
        Resident sameUser = new Resident(requester.getUserId(), "another_username",
                "password", "Rae Resident", LocalDate.of(1990, 1, 1),
                null, null, null, null);

        assertSame(existing, directory.findDuplicate(
                makeRequest(sameUser, RequestCategory.FOOD, "2 Main St")));
        assertNull(directory.findDuplicate(
                makeRequest(sameUser, RequestCategory.WATER, "2 Main St")));
        assertNull(directory.findDuplicate(null));
    }

    @Test
    public void getRequestsAssignedToFindsOnlyTheVolunteersRequests() {
        Volunteer volunteer = new Volunteer(UUID.randomUUID(), "volunteer_" + UUID.randomUUID(),
                "password", "Val Volunteer", LocalDate.of(1990, 1, 1),
                null, null, null, null);
        AidRequest assignedRequest = makeRequest(requester, RequestCategory.FOOD, "1 Main St");
        assignedRequest.addAssignment(new TaskAssignment(
                UUID.randomUUID(), assignedRequest, volunteer, null));
        directory.addRequest(assignedRequest);

        ArrayList<AidRequest> assigned = directory.getRequestsAssignedTo(volunteer);

        assertEquals(1, assigned.size());
        assertSame(assignedRequest, assigned.get(0));
        assertTrue(directory.getRequestsAssignedTo(requester).isEmpty());
        assertTrue(directory.getRequestsAssignedTo(null).isEmpty());
    }

    @Test
    public void expireStaleRequestExpiresOnlyOldSubmittedRequests() {
        AidRequest stale = makeRequest(requester, RequestCategory.FOOD, "1 Main St");
        stale.setCreatedAt(LocalDateTime.now().minusHours(73));
        AidRequest recent = makeRequest(requester, RequestCategory.WATER, "2 Main St");
        AidRequest alreadyAssigned = makeRequest(requester, RequestCategory.SUPPLIES, "3 Main St");
        alreadyAssigned.setCreatedAt(LocalDateTime.now().minusHours(73));
        alreadyAssigned.updateStatus(RequestStatus.ASSIGNED);
        directory.addRequest(stale);
        directory.addRequest(recent);
        directory.addRequest(alreadyAssigned);

        assertEquals(1, directory.expireStaleRequest());
        assertEquals(RequestStatus.EXPIRED, stale.getStatus());
        assertEquals(RequestStatus.SUBMITTED, recent.getStatus());
        assertEquals(RequestStatus.ASSIGNED, alreadyAssigned.getStatus());
    }

    private AidRequest makeRequest(User user, RequestCategory category, String address) {
        return new AidRequest(UUID.randomUUID(), user, category, 1, address);
    }
}
