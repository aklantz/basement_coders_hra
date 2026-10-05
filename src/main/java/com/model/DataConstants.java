package com.model;

/**
 * Constants used for reading and writing the JSON files.
 */
public class DataConstants {

      // file names
    public static final String USER_FILE = "json/users.json";
    public static final String AID_REQUEST_FILE = "json/aidrequests.json";
    public static final String RESOURCE_FILE = "json/resources.json";
    public static final String SHELTER_FILE = "json/shelters.json";

      // shelter keys
    public static final String SHELTER_ID = "resourceId";
    public static final String SHELTER_NAME = "name";
    public static final String SHELTER_CAPACITY = "capacity";
    public static final String SHELTER_OCCUPANCY = "occupancy";
    public static final String SHELTER_STATUS = "status";
    public static final String SHELTER_LAST_UPDATED = "lastUpdated";
}