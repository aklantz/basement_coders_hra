package com.model;

import java.util.ArrayList;

public class ResourceDirectory {

    private static ResourceDirectory resourceDirectory;
    private ArrayList<Shelter> shelters;

    /**
     * Loads shelters from DataLoader
     */
    private ResourceDirectory() {
        shelters = DataLoader.getCachedShelters();
        if (shelters == null) {
            shelters = new ArrayList<Shelter>();
        }
    }

    /**
     * Singleton
     */
    public static ResourceDirectory getInstance() {
        if (resourceDirectory == null) {
            resourceDirectory = new ResourceDirectory();
        }
        return resourceDirectory;
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