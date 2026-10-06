package com.hurricane;

import java.util.ArrayList;
import java.util.UUID;

public class AidRequestDirectory {

    private static AidRequestDirectory aidRequestDirectory;
    private ArrayList<AidRequest> requests;

    private AidRequestDirectory() {
        requests = new ArrayList<AidRequest>();
    }

    public static AidRequestDirectory getInstance() {
        if (aidRequestDirectory == null) {
            aidRequestDirectory = new AidRequestDirectory();
        }
        return aidRequestDirectory;
    }

    public AidRequest getRequest(UUID residentId) {
        return null;
    }

    public ArrayList<AidRequest> getOpenRequests(String county) {
        return new ArrayList<AidRequest>();
    }

    public boolean addRequest(AidRequest request) {
        return false;
    }

    public AidRequest findDuplicate(AidRequest request) {
        return null;
    }

    public int expireStaleRequest() {
        return 0;
    }

    public boolean save() {
        return false;
    }

    public AidRequest getRequestById(UUID requestId) {
        return null;
    }
}
