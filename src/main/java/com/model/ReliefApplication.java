package com.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Facade between the UI and the rest of the system. Singleton.
 */
public class ReliefApplication {

    private static ReliefApplication reliefApplication;

    private UserDirectory userDirectory;
    private AidRequestDirectory aidRequestDirectory;
    private ResourceDirectory resourceDirectory;
    private HurricaneDirectory hurricaneDirectory;
    private User currentUser;

    private ReliefApplication() {
        userDirectory = UserDirectory.getInstance();
        aidRequestDirectory = AidRequestDirectory.getInstance();
        resourceDirectory = ResourceDirectory.getInstance();
        hurricaneDirectory = HurricaneDirectory.getInstance();
        currentUser = null;
    }

    public static ReliefApplication getInstance() {
        if (reliefApplication == null) {
            reliefApplication = new ReliefApplication();
        }
        return reliefApplication;
    }

    /**
     * The UML elides the rest of the parameters with "...". The extras below are borrowed
     * from the User constructor (homeAddress, emailAddress, phoneNumber); adjust to match your diagram.
     */
    public User createAccount(String username, String password, String firstName, String lastName,
                              Date dateOfBirth, String homeAddress, String emailAddress, String phoneNumber) {
        // TODO: build User, hash password, userDirectory.addUser(...), return the new user (or null on failure)
        return null;
    }

    public User login(String username, String password) {
        // TODO: userDirectory.getUser(username, password); set currentUser on success
        return null;
    }

    /** UML says "bool"; Java uses boolean. */
    public boolean logout() {
        // TODO: clear currentUser, return true if someone was logged in
        return false;
    }

    /**
     * UML elides the rest of the parameters with "...". urgency and streetAddress come from
     * the AidRequest constructor; adjust as needed.
     */
    public boolean submitAidRequest(RequestCategory category, int householdSize,
                                    UrgencyLevel urgency, String streetAddress) {
        // TODO: build AidRequest for currentUser, aidRequestDirectory.addRequest(...)
        return false;
    }

    public ArrayList<AidRequest> getMyRequests() {
        // TODO: return requests submitted by currentUser
        return new ArrayList<AidRequest>();
    }

    public TaskAssignment assignTask(UUID requestId, UUID volunteerId) {
        // TODO: look up request and volunteer, create TaskAssignment with currentUser as coordinator
        return null;
    }

    /** Named getShelterDirectory but returns a list of shelters, per the UML. */
    public ArrayList<Shelter> getShelterDirectory() {
        // TODO: resourceDirectory.getActiveShelters(hurricaneDirectory.getAffectedZipCodes())
        return new ArrayList<Shelter>();
    }

    public void logAction(String action, String entityType, UUID entityId) {
        // TODO: record who did what to which entity (audit log)
    }
}
