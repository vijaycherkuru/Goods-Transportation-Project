package com.gdc.requests_management.feign.dto;

import lombok.Builder;
import lombok.Data;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import com.gdc.requests_management.model.enums.RequiredSpaceType;
import com.gdc.requests_management.model.enums.RequestStatus;

@Data
@Builder
public class RideResponseDto {
    private UUID id;
    private String from;
    private String to;
    private LocalDate date;
    private LocalTime time;
    private RequestStatus rideStatus;
    private String vehicleType;
    private RequiredSpaceType luggageSpace;
    private String drivingLicenseNumber;
    private String phone;
    private UUID rideUserId;
    private String displayName;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}