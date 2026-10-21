package com.model;

import java.util.ArrayList;

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