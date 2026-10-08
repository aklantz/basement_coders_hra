package com.model;

import java.time.LocalDateTime;

public class StatusChange {

    private RequestStatus status;
    private LocalDateTime changedAt;

    public RequestStatus getStatus() {
        return status;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
