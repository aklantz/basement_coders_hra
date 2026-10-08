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

    protected static final String USER_ID = "userId";
    protected static final String USER_USERNAME = "username";
    protected static final String USER_PASSWORD = "passwordHash";
    protected static final String USER_FULL_NAME = "fullName";
    protected static final String USER_DOB = "dateOfBirth";
    protected static final String USER_ADDRESS = "homeAddress";
    protected static final String USER_EMAIL = "emailAddress";
    protected static final String USER_PHONE = "phoneNumber";
    protected static final String USER_TYPE = "type";
    protected static final String USER_ACCOUNT_STATUS = "accountStatus";
    protected static final String USER_SAFETY_STATUS = "safetyStatus";
}