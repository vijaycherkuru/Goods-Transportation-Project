package com.gdc.ride_management.dto.request;

import com.gdc.ride_management.enums.RequiredSpaceType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRequestDto {

    @NotBlank(message = "From location is required")
    private String from;

    @NotBlank(message = "To location is required")
    private String to;

    @NotNull(message = "Ride date is required")
    private LocalDate date;

    @NotNull(message = "Ride time is required")
    private LocalTime time;

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;

    @NotNull(message = "Luggage space is required")
    private RequiredSpaceType luggageSpace;

    @NotBlank(message = "Driving license number is required")
    private String drivingLicenseNumber;


    @Column(nullable = false)
    private UUID rideUserId; // Reference to user-service
}
