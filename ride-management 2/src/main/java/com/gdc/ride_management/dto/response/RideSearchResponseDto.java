package com.gdc.ride_management.dto.response;

import com.gdc.ride_management.enums.RequiredSpaceType;
import com.gdc.ride_management.enums.VehicleType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideSearchResponseDto {
    private UUID rideId;
    private String from;
    private String to;
    private LocalDate date;
    private LocalTime time;
    private VehicleType vehicleType;
    private RequiredSpaceType luggageSpace;
    private double availableSpaceInKg;
    private double estimatedFare;

    private UUID rideUserId;
    private String username;
    private String phone;
    private int goodsQuantity;

}
