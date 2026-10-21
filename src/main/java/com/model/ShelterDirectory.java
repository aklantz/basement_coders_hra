package com.model;

import java.io.IOException;
import java.util.ArrayList;

import com.location.Address;
import com.location.Coordinates;
import com.location.MapTools;

/**
 * Holds all shelters in the relief application
 */
public class ShelterDirectory {

    private static ShelterDirectory shelterDirectory;
    private ArrayList<Shelter> shelters;

    /**
     * Loads shelters from DataLoader
     */
    private ShelterDirectory() {
        shelters = DataLoader.getCachedShelters();
        if (shelters == null) {
            shelters = new ArrayList<Shelter>();
        }
    }

    /**
     * Singleton
     */
    public static ShelterDirectory getInstance() {
        if (shelterDirectory == null) {
            shelterDirectory = new ShelterDirectory();
        }
        return shelterDirectory;
    }

    /**
     * Returns ALL shelters, required for DataWriter
     */
    public ArrayList<Shelter> getShelters() {
        return shelters;
    }

    /**
     * Finds shelters that are not closed and are in one of the given zip codes.
     * Full shelters are included, since they are still running.
     *
     * @param zipCodes the zip codes to search, such as the hurricane's affected areas
     * @return the active shelters in those zip codes
     */
    public ArrayList<Shelter> getActiveShelters(ArrayList<ZipCode> zipCodes) {
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
     * Finds shelters that are closed.
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
     * Finds the open shelter with space that is the shortest drive from the user's home.
     * Closed and full shelters are skipped, and so are shelters without an address.
     *
     * @param user the user looking for a shelter
     * @return the nearest shelter, or null if none have space or the user has no address
     * @throws IOException if the map API can't be reached or can't find an address
     * @throws InterruptedException if the map request is interrupted
     */
    public Shelter findNearestShelter(User user) throws IOException, InterruptedException {
        if (user == null || user.getAddress() == null) {
            return null;
        }

        MapTools mapTools = MapTools.getInstance();
        Coordinates home = mapTools.getCoordinates(user.getAddress());

        Shelter nearest = null;
        double shortestTime = Double.MAX_VALUE;

        for (Shelter shelter : shelters) {
            if (shelter.getStatus() == ShelterStatus.CLOSED
                    || shelter.getRemainingCapacity() == 0
                    || !shelter.hasAddress()) {
                continue;
            }
            double drivingTime = mapTools.getDrivingTime(home, shelter.getCoordinates());
            if (drivingTime < shortestTime) {
                shortestTime = drivingTime;
                nearest = shelter;
            }
        }
        return nearest;
    }

    /**
     * Gets turn-by-turn driving directions from the user's home to a shelter.
     *
     * @param user the user who is driving
     * @param shelter the shelter they are driving to
     * @return one direction per step, or an empty list if either has no address
     * @throws IOException if the map API can't be reached
     * @throws InterruptedException if the map request is interrupted
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
        return mapTools.getDrivingDirections(mapTools.getCoordinates(home), shelter.getCoordinates());
    }

    /**
     * Saves shelters to JSON
     */
    public boolean save() {
        return DataWriter.saveResources(shelters);
    }

    /**
     * Checks whether a shelter's zip code is in the list.
     */
    private boolean isInZipCodes(Shelter shelter, ArrayList<ZipCode> zipCodes) {
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