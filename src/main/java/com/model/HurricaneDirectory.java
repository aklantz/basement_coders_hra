package com.model;

import java.util.ArrayList;

public class HurricaneDirectory {

    private static HurricaneDirectory hurricaneDirectory;
    private ArrayList<ZipCode> zipCodes;
    private int windSpeed;
    private HurricaneCategory category;

    /**
     * Loads the hurricane from DataLoader
     */
    private HurricaneDirectory() {
        zipCodes = DataLoader.getAffectedZipCodes();
        windSpeed = DataLoader.getWindSpeed();
        category = DataLoader.getHurricaneCategory();
    }

    /**
     * Singleton
     */
    public static HurricaneDirectory getInstance() {
        if (hurricaneDirectory == null) {
            hurricaneDirectory = new HurricaneDirectory();
        }
        return hurricaneDirectory;
    }

    public ArrayList<ZipCode> getAffectedZipCodes() {
        return zipCodes;
    }

    public HurricaneCategory getHurricaneCategory() {
        return category;
    }
}