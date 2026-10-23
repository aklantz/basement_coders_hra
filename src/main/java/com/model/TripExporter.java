package com.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Writes the printable trip sheet a volunteer uses to pick up a family
 * and take them to a shelter.
 *
 * @author Austin Lantz
 */
public class TripExporter {

    private ShelterDirectory shelterDirectory;

    /**
     * Creates an exporter.
     *
     * @param shelterDirectory used to get directions
     */
    public TripExporter(ShelterDirectory shelterDirectory) {
        this.shelterDirectory = shelterDirectory;
    }

    /**
     * Writes a trip sheet containing household information, contact details,
     * medical needs, driving directions, and shelter information.
     *
     * @param volunteer the volunteer making the trip
     * @param request the request being handled
     * @param shelter the shelter the family is going to
     * @param filePath where to save the file
     * @return true if the file was written, false if required information
     *         is missing, a map lookup fails, or writing fails
     */
    public boolean export(User volunteer, AidRequest request,
                          Shelter shelter, String filePath) {
        if (volunteer == null || request == null || shelter == null
                || filePath == null || request.getRequester() == null) {
            return false;
        }
        if (shelter.getAddress() == null) {
            return false;
        }
        try {
            Files.write(
                    Paths.get(filePath),
                    buildSheet(volunteer, request, shelter),
                    StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Builds the lines of the trip sheet.
     *
     * @param volunteer the volunteer making the trip
     * @param request the request being handled
     * @param shelter the destination shelter
     * @return the trip sheet lines
     * @throws IOException if a map lookup fails
     * @throws InterruptedException if a map request is interrupted
     */
    private ArrayList<String> buildSheet(User volunteer, AidRequest request,
                                         Shelter shelter)
            throws IOException, InterruptedException {
        User requester = request.getRequester();
        ArrayList<HouseholdMember> members = getMembers(requester);
        ArrayList<String> lines = new ArrayList<String>();
        String shelterAddress = shelter.getAddress().toString();
        lines.add("HURRICANE RELIEF TRIP SHEET");
        lines.add("===========================");
        lines.add("Volunteer: " + volunteer.getFullName());
        lines.add("Prepared: " + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")));
        lines.add("Request ID: " + request.getRequestId());
        lines.add("");
        lines.add("PEOPLE AND PETS (" + request.getHouseholdSize()
                + " people need transportation)");
        if (members.isEmpty()) {
            lines.add("No household information on file");
        }
        for (HouseholdMember member : members) {
            if (member.isPet()) {
                lines.add("- " + member.getName()
                        + " (" + member.getPetType() + ")");
            } else {
                lines.add("- " + member.getName()
                        + ", age " + member.getAge());
            }
        }
        lines.add("");
        lines.add("CONTACT INFORMATION");
        lines.add("Name: " + requester.getFullName());
        lines.add("Phone: " + requester.getPhoneNumber());
        lines.add("Email: " + requester.getEmailAddress());
        lines.add("Pickup address: " + request.getStreetAddress());
        lines.add("");
        lines.add("MEDICAL ASSISTANCE");
        boolean anyMedical = false;
        for (HouseholdMember member : members) {
            String note = member.getMedicalNote();
            if (note != null && !note.trim().isEmpty()) {
                lines.add("- " + member.getName() + ": " + note);
                anyMedical = true;
            }
        }
        if (!anyMedical) {
            lines.add(request.getCategory() == RequestCategory.MEDICAL
                    ? "Medical assistance requested (no details given)"
                    : "None reported");
        }
        lines.add("");
        addDirections(lines, "DIRECTIONS: YOUR LOCATION TO THE PICKUP ADDRESS",
                volunteer.getHomeAddress(), request.getStreetAddress());

        addDirections(lines, "DIRECTIONS: PICKUP ADDRESS TO THE SHELTER",
                request.getStreetAddress(), shelterAddress);
        lines.add("SHELTER");
        lines.add("Name: " + shelter.getName());
        lines.add("Address: " + shelterAddress);
        return lines;
    }

    /**
     * Gets everyone in all of a user's households.
     *
     * @param requester the user whose household members to collect
     * @return the household members
     */
    private ArrayList<HouseholdMember> getMembers(User requester) {
        ArrayList<HouseholdMember> members =
                new ArrayList<HouseholdMember>();

        for (Household household : requester.getHouseholds()) {
            members.addAll(household.getMembers());
        }
        return members;
    }

    /**
     * Adds a heading and numbered directions to the sheet.
     *
     * @param lines the trip sheet lines
     * @param heading the heading for these directions
     * @param from the starting address
     * @param to the destination address
     * @throws IOException if a map lookup fails
     * @throws InterruptedException if a map request is interrupted
     */
    private void addDirections(ArrayList<String> lines, String heading,
                               String from, String to)
            throws IOException, InterruptedException {
        lines.add(heading);
        int number = 1;
        for (String step : shelterDirectory.getDirections(from, to)) {
            lines.add(number + ". " + step);
            number++;
        }
        lines.add("");
    }
}