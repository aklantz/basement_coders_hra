package com.model;

import java.util.ArrayList;
import java.util.UUID;

public class AidRequestDirectory {

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
     * Expires stale requests (placeholder logic)
     */
    public int expireStaleRequest() {
        int count = 0;
        for (AidRequest request : requests) {
            if (request.getStatus() == RequestStatus.SUBMITTED &&
                request.isDuplicate()) {
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
