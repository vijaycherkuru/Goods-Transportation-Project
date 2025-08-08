package com.gdc.ride_management.service;

import com.gdc.ride_management.dto.request.RideRequestDto;
import com.gdc.ride_management.dto.response.RideResponseDto;
import com.gdc.ride_management.dto.response.RideSearchResponseDto;
import com.gdc.ride_management.enums.RequiredSpaceType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RideService {
    RideResponseDto createRide(UUID rideUserId, RideRequestDto requestDto);
    RideResponseDto getRideById(UUID id);
    List<RideResponseDto> getAllRides();
    void deleteRide(UUID id);
    List<RideResponseDto> searchAvailableRides(String from, String to, LocalDate date);
    List<RideResponseDto> getRidesByDriverId(UUID userId);

    List<RideSearchResponseDto> searchAvailableRidesWithFare(
            String from,
            String to,
            LocalDate date,
            RequiredSpaceType luggageSpace,
            double goodsWeightInKg,
            int goodsQuantity
    );
}
