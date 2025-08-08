package com.gdc.requests_management.feign.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class DriverResponseDTO {
    private UUID id;
    private String fullName;
    private String phoneNumber;
    private String vehicleDetails;
}

