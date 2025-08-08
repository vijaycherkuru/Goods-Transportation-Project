package com.gdc.requests_management.dto.request;

import java.util.UUID;

public class UserIdRequestDTO {
    private UUID userId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}