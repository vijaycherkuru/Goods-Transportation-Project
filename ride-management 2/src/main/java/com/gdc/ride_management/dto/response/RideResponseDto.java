package com.gdc.ride_management.dto.response;

// com.gdc.ride_management.dto.response.RideResponseDto

import lombok.Builder;
import lombok.Data;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import com.gdc.ride_management.enums.RequiredSpaceType;
import com.gdc.ride_management.enums.RideStatus;

@Data
@Builder
public class RideResponseDto {
    private UUID id;
    private String from;
    private String to;
    private LocalDate date;
    private LocalTime time;
    private RideStatus rideStatus;
    private String vehicleType;
    private RequiredSpaceType luggageSpace;
    private String drivingLicenseNumber;
    private String phone;
    private UUID rideUserId;
    private String userName;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private Double fromLatitude;
    private Double fromLongitude;
    private Double toLatitude;
    private Double toLongitude;
}
