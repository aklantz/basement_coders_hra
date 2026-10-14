package com.model;

import java.util.UUID;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class HouseholdMember {

    private HashMap<String, SafetyStatus> memberStatuses;
    private HashMap<String, LocalDateTime> memberCheckInTimes;
    private String householdName;
    private UUID householdOwnerUUID;

    public HouseholdMember(UUID householdOwnerUUID, String householdName) {
        this.householdName = householdName;
        this.householdOwnerUUID = householdOwnerUUID;
        this.memberStatuses = new HashMap<>();
        this.memberCheckInTimes = new HashMap<>();
    }

    public boolean addHouseholdMember(String memberName) {
        memberStatuses.put(memberName, SafetyStatus.UNKNOWN);
        memberCheckInTimes.put(memberName, LocalDateTime.now());
        return true;
    }

    public ArrayList<String> getMembers() {
        ArrayList<String> members = new ArrayList<>(memberStatuses.keySet());
        return members;
    }

    public SafetyStatus getSafetyStatus(String memberName) {
        return memberStatuses.get(memberName);
    }

    public String getHouseHoldName() {
        return householdName;
    }

    public LocalDateTime getLastCheckIn(String memberName) {
        return memberCheckInTimes.get(memberName);
    }

    public boolean checkIn(String memberName, SafetyStatus safetyStatus) {
        memberStatuses.put(memberName, safetyStatus);
        memberCheckInTimes.put(memberName, LocalDateTime.now());
        return true;
    }

    public UUID getHouseholdOwnerUUID() {
        return householdOwnerUUID;
    }
}
