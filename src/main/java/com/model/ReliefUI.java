package com.model;

import java.util.ArrayList;
import com.model.UserDirectory;
import com.model.AidRequestDirectory;
import com.model.ShelterDirectory;
import com.model.DataWriter;
import com.model.DataLoader;
import com.model.ReliefApplication;
import com.model.User;
import com.model.Shelter;
import com.model.AidRequest;

public class ReliefUI {
    private ReliefApplication reliefApp;

    ReliefUI() {
        reliefApp = ReliefApplication.getInstance();
    }

    public void run() {
        scenario1();
        scenario2();
        scenario3();
        scenario4();

        // Save all data after scenarios
        DataWriter.saveUsers(UserDirectory.getInstance().getUsers());
        DataWriter.saveAidRequests(AidRequestDirectory.getInstance().getRequests());
        DataWriter.saveResources(ShelterDirectory.getInstance().getShelters());

        System.out.println("All data saved to JSON.");
    }

    /**
     * Scenario 1: Maria Rivera (volunteer) logs in and logs out.
     */
    public void scenario1() {
        System.out.println();

        User user = reliefApp.login("mrivera", "password");
        if (user == null) {
            System.out.println("Sorry, Maria Rivera couldn't log in.");
            return;
        }
        System.out.println("Maria Rivera is now logged in");

        if (!reliefApp.logout()) {
            System.out.println("Sorry, Maria Rivera could not be logged out.");
            return;
        }
        System.out.println("Maria Rivera has logged out");
    }

    /**
     * Scenario 2: Jordan Smith (admin) logs in and logs out.
     */
    public void scenario2() {
        System.out.println();

        User user = reliefApp.login("jsmith", "password");
        if (user == null) {
            System.out.println("Sorry, Jordan Smith couldn't log in.");
            return;
        }
        System.out.println("Jordan Smith is now logged in");

        if (!reliefApp.logout()) {
            System.out.println("Sorry, Jordan Smith could not be logged out.");
            return;
        }
        System.out.println("Jordan Smith has logged out");
    }

    /**
     * Scenario 3: Load and display shelters.
     */
    public void scenario3() {
        System.out.println();

        ArrayList<Shelter> shelters = DataLoader.getCachedShelters();
        if (shelters.isEmpty()) {
            System.out.println("Sorry, no shelters were loaded.");
            return;
        }
        System.out.println(shelters.size() + " shelter(s) loaded");
    }

    /**
     * Scenario 4: Load and display aid requests.
     */
    public void scenario4() {
        System.out.println();

        ArrayList<AidRequest> requests = DataLoader.getAidRequests();
        if (requests.isEmpty()) {
            System.out.println("Sorry, no aid requests were loaded.");
            return;
        }
        System.out.println(requests.size() + " aid request(s) loaded");

        for (AidRequest request : requests) {
            String requesterName = (request.getRequester() == null)
                    ? "UNKNOWN USER"
                    : request.getRequester().getUsername();
            System.out.println("- " + request.getCategory() + " request from "
                    + requesterName + " (status: " + request.getStatus() + ")");
        }
    }

    public static void main(String[] args) {
        ReliefUI reliefInterface = new ReliefUI();
        reliefInterface.run();
    }
}
