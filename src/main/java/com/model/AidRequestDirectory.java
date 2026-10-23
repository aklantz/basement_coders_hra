package com.model;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class AidRequestDirectory {

    /** How long a submitted request can go unassigned before it expires. */
    private static final int STALE_HOURS = 72;

    /** A matching request this many minutes or less away from the volunteer is recommended. */
    private static final double NEARBY_MINUTES = 30.0;

    private static AidRequestDirectory aidRequestDirectory;
    private ArrayList<AidRequest> requests;

    /**
     * Loads all aid requests from DataLoader
     */
    private AidRequestDirectory() {
        requests = DataLoader.getAidRequests();
        if (requests == null) {
            requests = new ArrayList<AidRequest>();
        }
    }

    /**
     * Singleton 
     */
    
    public static AidRequestDirectory getInstance() {
        if (aidRequestDirectory == null) {
            aidRequestDirectory = new AidRequestDirectory();
        }
        return aidRequestDirectory;
    }

    /**
     * Creates and saves an aid request.
     *
     * @param requester the user requesting aid
     * @param category the kind of aid needed
     * @param householdSize how many people need aid
     * @param streetAddress where the aid is needed
     * @return the new request, or null if input is invalid or saving fails
     */
    public AidRequest createAidRequest(User requester, RequestCategory category,
                                    int householdSize, String streetAddress) {
        if (requester == null || category == null || householdSize <= 0
                || streetAddress == null || streetAddress.trim().isEmpty()) {
            return null;
        }
        AidRequest request = new AidRequest(
                UUID.randomUUID(), requester, category,
                householdSize, streetAddress.trim());

        if (!addRequest(request)) {
            return null;
        }
        if (!save()) {
            requests.remove(request);
            return null;
        }
        return request;
    }
    
    /**
     * Creates and saves an aid request.
     *
     * @param requester the user requesting aid
     * @param category the kind of aid needed
     * @param householdSize how many people need aid
     * @param streetAddress where the aid is needed
     * @param note a note to leave in the aid request
     * @return the new request, or null if input is invalid or saving fails
     */
    public AidRequest createAidRequest(User requester, RequestCategory category,
                                    int householdSize, String streetAddress, String note) {
        if (requester == null || category == null || householdSize <= 0
                || streetAddress == null || streetAddress.trim().isEmpty()) {
            return null;
        }
        AidRequest request = new AidRequest(
                UUID.randomUUID(), requester, category,
                householdSize, streetAddress.trim(), note);

        if (!addRequest(request)) {
            return null;
        }
        if (!save()) {
            requests.remove(request);
            return null;
        }
        return request;
    }

    /**
     * Returns ALL aid requests,required for DataWriter
     */
    public ArrayList<AidRequest> getRequests() {
        return requests;
    }

    /**
     * Finds a request by resident/user ID
     */
    public AidRequest getRequest(UUID residentId) {
        if (residentId == null) {
            return null;
        }
        for (AidRequest request : requests) {
            if (request.getRequester() != null &&
                residentId.equals(request.getRequester().getUserId())) {
                return request;
            }
        }
        return null;
    }

    /**
     * Returns open requests for a given county
     */
    public ArrayList<AidRequest> getOpenRequests(String county) {
        ArrayList<AidRequest> open = new ArrayList<>();
        if (county == null) {
            return open;
        }
        for (AidRequest request : requests) {
            if (request.getStatus() == RequestStatus.SUBMITTED &&
                request.getStreetAddress() != null &&
                request.getStreetAddress().contains(county)) {
                open.add(request);
            }
        }
        return open;
    }

    /**
     * Gets the open requests for a volunteer, with the recommended ones first and then
     * the closest first. A request is recommended when its category matches the volunteer's
     * specialty and it is within NEARBY_MINUTES of the volunteer's home. Each one carries
     * a sentence explaining why it is or is not recommended.
     *
     * @param user the volunteer looking at the requests
     * @return the open requests, or an empty list if the user is not a volunteer
     * @throws IOException if a map lookup fails
     * @throws InterruptedException if a map request is interrupted
     */
    public ArrayList<RecommendedRequest> getRecommendedRequests(User user)
            throws IOException, InterruptedException {
        ArrayList<RecommendedRequest> result = new ArrayList<RecommendedRequest>();
        if (!(user instanceof Volunteer)) {
            return result;
        }
        Volunteer volunteer = (Volunteer) user;
        ShelterDirectory shelterDirectory = ShelterDirectory.getInstance();

        for (AidRequest request : requests) {
            if (request.getStatus() != RequestStatus.SUBMITTED || request.isDuplicate()) {
                continue;
            }

            double minutes = shelterDirectory.getDrivingMinutes(
                    volunteer.getHomeAddress(), request.getStreetAddress());
            boolean matches = volunteer.getSpecialty() != null
                    && request.getCategory() == volunteer.getSpecialty();
            boolean recommended = matches && minutes <= NEARBY_MINUTES;

            String away = (minutes == Double.MAX_VALUE)
                    ? "its distance is unknown"
                    : "it is " + String.format("%.0f", minutes) + " minutes away";
            String reason;
            if (recommended) {
                reason = "Recommended: it matches your "
                        + volunteer.getSpecialty().name().toLowerCase().replace('_', ' ')
                        + " specialty and " + away;
            } else if (matches) {
                reason = "Matches your specialty, but " + away;
            } else {
                reason = "Open request; " + away;
            }
            result.add(new RecommendedRequest(request, recommended, minutes, reason));
        }

        result.sort((a, b) -> {
            if (a.isRecommended() != b.isRecommended()) {
                return a.isRecommended() ? -1 : 1;
            }
            return Double.compare(a.getDrivingMinutes(), b.getDrivingMinutes());
        });
        return result;
    }

    /**
     * Adds a request to the directory
     */
    public boolean addRequest(AidRequest request) {
        if (request == null) {
            return false;
        }
        requests.add(request);
        return true;
    }

    /**
     * A volunteer takes an open request for themselves. The request becomes ASSIGNED, the
     * shelter they chose is saved with it, and other volunteers can see who is handling it.
     *
     * @param user the volunteer taking the request
     * @param requestId the id of the request
     * @param shelter the shelter the volunteer selected
     * @return true if the request was assigned and saved
     */
    public boolean assignToVolunteer(User user, UUID requestId, Shelter shelter) {
        AidRequest request = getRequestById(requestId);
        if (!(user instanceof Volunteer) || shelter == null || request == null
                || request.getStatus() != RequestStatus.SUBMITTED) {
            return false;
        }
        TaskAssignment task = new TaskAssignment(
                UUID.randomUUID(), request, (Volunteer) user, null);
        request.addAssignment(task);
        request.setShelterId(shelter.getResourceId());
        request.updateStatus(RequestStatus.ASSIGNED);

        if (!save()) {
            request.getAssignments().remove(task);
            request.setShelterId(null);
            request.updateStatus(RequestStatus.SUBMITTED);
            return false;
        }
        return true;
    }

    /**
     * Completes and saves an aid request.
     *
     * @param user the user completing the request
     * @param requestId the id of the request
     * @return true if the request was completed and saved
     * @throws SecurityException if the user's role cannot complete requests
     */
    public boolean completeRequest(User user, UUID requestId) {
        if (user == null) {
            return false;
        }
        // only roles like shelter manager are allowed to complete requests
        if (!user.canCompleteRequests()) {
            throw new SecurityException("Your "
                    + user.getType().name().toLowerCase().replace('_', ' ')
                    + " role does not have permission to complete relief requests.");
        }
        AidRequest request = getRequestById(requestId);
        if (request == null || request.getStatus() == RequestStatus.FULFILLED
                || request.getStatus() == RequestStatus.EXPIRED) {
            return false;
        }
        RequestStatus previousStatus = request.getStatus();
        request.complete();
        if (!save()) {
            request.updateStatus(previousStatus);
            return false;
        }

        return true;
    }

    /**
     * Gets the aid requests assigned to a volunteer.
     *
     * @param user the volunteer whose requests to find
     * @return the assigned requests, or an empty list if none are found
     */
    public ArrayList<AidRequest> getRequestsAssignedTo(User user) {
        ArrayList<AidRequest> assignedRequests = new ArrayList<>();
        if (!(user instanceof Volunteer)) {
            return assignedRequests;
        }
        for (AidRequest request : requests) {
            for (TaskAssignment assignment : request.getAssignments()) {
                Volunteer volunteer = assignment.getVolunteer();
                if (volunteer != null
                        && volunteer.getUserId().equals(user.getUserId())) {
                    assignedRequests.add(request);
                    break;
                }
            }
        }
        return assignedRequests;
    }

    /**
     * Checks if a request is a duplicate
     */
    public AidRequest findDuplicate(AidRequest request) {
        if (request == null) {
            return null;
        }
        for (AidRequest existing : requests) {
            if (existing.getRequester() != null &&
                request.getRequester() != null &&
                existing.getRequester().getUserId().equals(
                    request.getRequester().getUserId()
                ) &&
                existing.getCategory() == request.getCategory()) {
                return existing;
            }
        }
        return null;
    }

    /**
     * Expires submitted requests that are older than STALE_HOURS
     *
     * @return how many requests were expired
     */
    public int expireStaleRequest() {
        int count = 0;
        LocalDateTime cutoff = LocalDateTime.now().minusHours(STALE_HOURS);
        for (AidRequest request : requests) {
            if (request.getStatus() == RequestStatus.SUBMITTED &&
                request.getCreatedAt() != null &&
                request.getCreatedAt().isBefore(cutoff)) {
                request.expire();
                count++;
            }
        }
        return count;
    }

    /**
     * Saves all aid requests to JSON
     */
    public boolean save() {
        return DataWriter.saveAidRequests(requests);
    }

    /**
     * Finds a request by its ID
     */
    public AidRequest getRequestById(UUID requestId) {
        if (requestId == null) {
            return null;
        }
        for (AidRequest request : requests) {
            if (requestId.equals(request.getRequestId())) {
                return request;
            }
        }
        return null;
    }
}