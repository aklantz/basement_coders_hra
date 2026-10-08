package com.model;

import java.util.ArrayList;
import java.util.UUID;

public class ReliefUI {
	private ReliefApplication reliefApp;

	ReliefUI() {
		reliefApp = ReliefApplication.getInstance();
	}

	public void run() {
		scenario1();
		scenario2();
	}

	/**
	 * Scenario 1: Maria Rivera logs in, views her existing aid requests,
	 * and submits a new request for FOOD.
	 */
	public void scenario1() {
		System.out.println();

		// Replace "password" with the plain-text password that matches Maria's passwordHash
		User user = reliefApp.login("mrivera", "password");
		if (user == null) {
			System.out.println("Sorry, we couldn't log in.");
			return;
		}
		System.out.println("Maria Rivera is now logged in");

		ArrayList<AidRequest> myRequests = reliefApp.getMyRequests();
		if (myRequests == null || myRequests.isEmpty()) {
			System.out.println("You have no requests in the system.");
		} else {
			System.out.println("You have " + myRequests.size() + " request(s) in the system.");
		}

		// Adjust to match the facade 
		if (!reliefApp.submitAidRequest(RequestCategory.FOOD, 4, UrgencyLevel.HIGH, "12 Palmetto Ln, Tega Cay, SC")) {
			System.out.println("Sorry, your aid request could not be submitted.");
		} else {
			System.out.println("Your request for FOOD was successfully submitted.");
		}

		reliefApp.logout();
		System.out.println("Maria Rivera has logged out");
	}

	/**
	 * Scenario 2: Jordan Smith (admin) logs in, checks shelters,
	 * and assigns Maria's WATER request to volunteer Maria Rivera.
	 */
	public void scenario2() {
		System.out.println();

		// Replace "password" with the plain-text password that matches Jordan's passwordHash
		User user = reliefApp.login("jsmith", "password");
		if (user == null) {
			System.out.println("Sorry, we couldn't log in.");
			return;
		}
		System.out.println("Jordan Smith is now logged in");

		ArrayList<Shelter> shelters = reliefApp.getShelterDirectory();
		if (shelters == null || shelters.isEmpty()) {
			System.out.println("There are no shelters currently available.");
		} else {
			System.out.println("There are " + shelters.size() + " shelter(s) in the system.");
		}

		// The WATER request (status SUBMITTED) and volunteer Maria Rivera from the JSON data
		UUID requestId = UUID.fromString("a1b2c3d4-0001-4000-8000-000000000001");
		UUID volunteerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");

		TaskAssignment task = reliefApp.assignTask(requestId, volunteerId);
		if (task == null) {
			System.out.println("Sorry, the WATER request could not be assigned.");
		} else {
			System.out.println("The WATER request was successfully assigned to Maria Rivera.");
			reliefApp.logAction("Assigned task", "AidRequest", requestId);
		}

		reliefApp.logout();
		System.out.println("Jordan Smith has logged out");
	}

	public static void main(String[] args) {
		ReliefUI reliefInterface = new ReliefUI();
		reliefInterface.run();
	}
}