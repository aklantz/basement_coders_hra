package com.model;

import java.util.ArrayList;

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
     * Saves shelters to JSON
     */
    public boolean save() {
        return DataWriter.saveResources(shelters);
    }
}
