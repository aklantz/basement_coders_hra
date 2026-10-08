package com.model;

import java.util.UUID;

public class HouseholdMember {

    private UUID memberUUID;

    public HouseholdMember(UUID memberUUID) {
        this.memberUUID = memberUUID;
    }

    public UUID getMemberUUID() {
        return memberUUID;
    }
}
