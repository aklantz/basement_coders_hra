package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class AidRequestDirectory {

    /** How long a submitted request can go unassigned before it expires. */
    private static final int STALE_HOURS = 72;

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
     * Singleton accessor
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