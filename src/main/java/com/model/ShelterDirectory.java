package com.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import com.location.Address;
import com.location.Coordinates;
import com.location.MapTools;

/**
 * Holds shelters and provides shelter searches and driving directions.
 *
 * @author Austin Lantz
 */
public class ShelterDirectory {

    private static ShelterDirectory shelterDirectory;
    private ArrayList<Shelter> shelters;

    /**
     * Loads shelters from storage.
     */
    private ShelterDirectory() {
        shelters = DataLoader.getCachedShelters();

        if (shelters == null) {
            shelters = new ArrayList<Shelter>();
        }
    }

    /**
     * Gets the single shelter directory.
     *
     * @return the shelter directory
     */
    public static ShelterDirectory getInstance() {
        if (shelterDirectory == null) {
            shelterDirectory = new ShelterDirectory();
        }

        return shelterDirectory;
    }

    /**
     * Finds a shelter by its id.
     *
     * @param shelterId the shelter's id
     * @return the shelter, or null if not found
     */
    public Shelter getShelterById(UUID shelterId) {
        if (shelterId == null) {
            return null;
        }

        for (Shelter shelter : shelters) {
            if (shelterId.equals(shelter.getResourceId())) {
                return shelter;
            }
        }

        return null;
    }

    /**
     * Gets all shelters.
     *
     * @return the shelters
     */
    public ArrayList<Shelter> getShelters() {
        return shelters;
    }

    /**
     * Finds shelters that are not closed and are in the given zip codes.
     * Full shelters are included because they are still operating.
     *
     * @param zipCodes the zip codes to search
     * @return the matching active shelters
     */
    public ArrayList<Shelter> getActiveShelters(
            ArrayList<ZipCode> zipCodes) {
        ArrayList<Shelter> active = new ArrayList<Shelter>();

        if (zipCodes == null) {
            return active;
        }

        for (Shelter shelter : shelters) {
            if (shelter.getStatus() != ShelterStatus.CLOSED
                    && isInZipCodes(shelter, zipCodes)) {
                active.add(shelter);
            }
        }

        return active;
    }

    /**
     * Gets shelters that are closed.
     *
     * @return the closed shelters
     */
    public ArrayList<Shelter> getOfflineShelters() {
        ArrayList<Shelter> offline = new ArrayList<Shelter>();

        for (Shelter shelter : shelters) {
            if (shelter.getStatus() == ShelterStatus.CLOSED) {
                offline.add(shelter);
            }
        }

        return offline;
    }

    /**
     * Finds the open shelter with space that has the shortest driving time
     * from the user's home.
     *
     * @param user the user looking for a shelter
     * @return the nearest available shelter, or null if none is found
     *         or the user has no usable address
     * @throws IOException if a map lookup fails
     * @throws InterruptedException if a map request is interrupted
     */
    public Shelter findNearestShelter(User user)
            throws IOException, InterruptedException {
        if (user == null) {
            return null;
        }

        Address homeAddress = user.getAddress();
        if (homeAddress == null) {
            return null;
        }

        MapTools mapTools = MapTools.getInstance();
        Coordinates home = mapTools.getCoordinates(homeAddress);

        Shelter nearest = null;
        double shortestTime = Double.MAX_VALUE;

        for (Shelter shelter : shelters) {
            if (shelter.getStatus() != ShelterStatus.OPEN
                    || shelter.getRemainingCapacity() == 0
                    || !shelter.hasAddress()) {
                continue;
            }

            double drivingTime = mapTools.getDrivingTime(
                    home, shelter.getCoordinates());

            if (drivingTime < shortestTime) {
                shortestTime = drivingTime;
                nearest = shelter;
            }
        }

        return nearest;
    }

    /**
     * Finds the open shelter with space that has the shortest driving time
     * from an address.
     *
     * @param streetAddress the starting address, formatted as street, city, state zip
     * @return the nearest available shelter, or null if the address format
     *         is invalid or no shelter is available
     * @throws IOException if a map lookup fails
     * @throws InterruptedException if a map request is interrupted
     */
    public Shelter findNearestShelter(String streetAddress)
            throws IOException, InterruptedException {
        Address address = parseAddress(streetAddress);

        if (address == null) {
            return null;
        }

        MapTools mapTools = MapTools.getInstance();
        Coordinates start = mapTools.getCoordinates(address);

        Shelter nearest = null;
        double shortestTime = Double.MAX_VALUE;

        for (Shelter shelter : shelters) {
            if (shelter.getStatus() != ShelterStatus.OPEN
                    || shelter.getRemainingCapacity() == 0
                    || !shelter.hasAddress()) {
                continue;
            }

            double drivingTime = mapTools.getDrivingTime(
                    start, shelter.getCoordinates());

            if (drivingTime < shortestTime) {
                shortestTime = drivingTime;
                nearest = shelter;
            }
        }

        return nearest;
    }

    /**
     * Finds the nearest available shelter from an aid request's address.
     * This search checks for space but does not check the whole household's
     * capacity, medical, or pet requirements.
     *
     * @param request the request needing a shelter
     * @return the nearest available shelter, or null if the request is null,
     *         its address format is invalid, or no shelter is available
     * @throws IOException if a map lookup fails
     * @throws InterruptedException if a map request is interrupted
     */
    public Shelter findNearestShelter(AidRequest request)
            throws IOException, InterruptedException {
        if (request == null) {
            return null;
        }

        return findNearestShelter(request.getStreetAddress());
    }

    /**
     * Finds the shelter with the shortest driving time that can accommodate
     * the whole household and meet its medical and dog requirements.
     *
     * @param request the request needing a shelter
     * @return the suitable shelter, or null if the request is invalid
     *         or no shelter meets its requirements
     * @throws IOException if a map lookup fails
     * @throws InterruptedException if a map request is interrupted
     */
    public Shelter findSuitableShelter(AidRequest request)
            throws IOException, InterruptedException {
        if (request == null || request.getHouseholdSize() <= 0) {
            return null;
        }

        Address address = parseAddress(request.getStreetAddress());

        if (address == null) {
            return null;
        }

        boolean needsMedical = request.needsMedicalAssistance();
        boolean hasDog = request.hasDog();

        MapTools mapTools = MapTools.getInstance();
        Coordinates start = mapTools.getCoordinates(address);

        Shelter best = null;
        double shortestTime = Double.MAX_VALUE;

        for (Shelter shelter : shelters) {
            if (shelter.getStatus() != ShelterStatus.OPEN
                    || shelter.getRemainingCapacity() < request.getHouseholdSize()
                    || !shelter.hasAddress()) {
                continue;
            }

            if (needsMedical && !shelter.hasMedicalStaff()) {
                continue;
            }

            if (hasDog && !shelter.acceptsDogs()) {
                continue;
            }

            double drivingTime = mapTools.getDrivingTime(
                    start, shelter.getCoordinates());

            if (drivingTime < shortestTime) {
                shortestTime = drivingTime;
                best = shelter;
            }
        }

        return best;
    }

    /**
     * Gets the driving time between two addresses in minutes.
     *
     * @param fromAddress the starting address, formatted as street, city, state zip
     * @param toAddress the destination address, formatted the same way
     * @return the driving time in minutes, or Double.MAX_VALUE
     *         if either address format is invalid
     * @throws IOException if a map lookup fails
     * @throws InterruptedException if a map request is interrupted
     */
    public double getDrivingMinutes(String fromAddress, String toAddress)
            throws IOException, InterruptedException {
        Address from = parseAddress(fromAddress);
        Address to = parseAddress(toAddress);

        if (from == null || to == null) {
            return Double.MAX_VALUE;
        }

        MapTools mapTools = MapTools.getInstance();

        double seconds = mapTools.getDrivingTime(
                mapTools.getCoordinates(from),
                mapTools.getCoordinates(to));

        return seconds / 60.0;
    }

    /**
     * Gets driving directions between two addresses.
     *
     * @param fromAddress the starting address, formatted as street, city, state zip
     * @param toAddress the destination address, formatted the same way
     * @return one direction per entry, or an empty list if either address
     *         format is invalid
     * @throws IOException if a map lookup fails
     * @throws InterruptedException if a map request is interrupted
     */
    public ArrayList<String> getDirections(
            String fromAddress, String toAddress)
            throws IOException, InterruptedException {
        Address start = parseAddress(fromAddress);
        Address end = parseAddress(toAddress);
        ArrayList<String> directions = new ArrayList<String>();

        if (start == null || end == null) {
            return directions;
        }

        String[] steps = MapTools.getInstance()
                .getDrivingDirections(start, end);

        for (String step : steps) {
            directions.add(step);
        }

        return directions;
    }

    /**
     * Gets driving directions from a user's home to a shelter.
     *
     * @param user the user making the trip
     * @param shelter the destination shelter
     * @return one direction per entry, or an empty array if either
     *         argument is null or has no usable address
     * @throws IOException if a map lookup fails
     * @throws InterruptedException if a map request is interrupted
     */
    public String[] getDirections(User user, Shelter shelter)
            throws IOException, InterruptedException {
        if (user == null || shelter == null) {
            return new String[0];
        }

        Address home = user.getAddress();

        if (home == null || !shelter.hasAddress()) {
            return new String[0];
        }

        MapTools mapTools = MapTools.getInstance();

        return mapTools.getDrivingDirections(
                mapTools.getCoordinates(home),
                shelter.getCoordinates());
    }

    /**
     * Converts an address string into an Address.
     *
     * @param address the address, such as
     *                "142 Riverbend Dr, Tega Cay, SC 29708"
     * @return the address, or null if its format is invalid
     */
    private Address parseAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }

        String[] parts = address.split(",", -1);

        if (parts.length != 3) {
            return null;
        }

        String street = parts[0].trim();
        String city = parts[1].trim();
        String[] stateAndZip = parts[2].trim().split("\\s+");

        if (street.isEmpty() || city.isEmpty()
                || stateAndZip.length != 2) {
            return null;
        }

        return new Address(
                street, city, stateAndZip[0], stateAndZip[1]);
    }

    /**
     * Saves shelters to storage.
     *
     * @return true if saving succeeds
     */
    public boolean save() {
        return DataWriter.saveResources(shelters);
    }

    /**
     * Checks whether a shelter is in one of the given zip codes.
     *
     * @param shelter the shelter to check
     * @param zipCodes the zip codes to search
     * @return true if the shelter's zip code matches
     */
    private boolean isInZipCodes(
            Shelter shelter, ArrayList<ZipCode> zipCodes) {
        if (shelter.getZipCode() == null) {
            return false;
        }

        for (ZipCode zipCode : zipCodes) {
            if (shelter.getZipCode().equals(zipCode.getCode())) {
                return true;
            }
        }

        return false;
    }
}