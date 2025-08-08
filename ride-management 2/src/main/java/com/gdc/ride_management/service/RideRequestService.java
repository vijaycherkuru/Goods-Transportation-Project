package com.gdc.ride_management.service;

// com.gdc.ride_management.service.RideRequestService (Interface)
import com.gdc.ride_management.dto.request.RideRequestRequestDto;
import com.gdc.ride_management.dto.response.RideRequestResponseDto;

import java.util.List;
import java.util.UUID;

public interface RideRequestService {

    // Modified to take the rideId and the sender's userId
    RideRequestResponseDto createRideRequest(RideRequestRequestDto request, UUID senderUserId);

    List<RideRequestResponseDto> getAllRideRequests(); // Admin view
    RideRequestResponseDto getRideRequestById(UUID id);

    // Update status, ensuring only driver of the associated ride can do so
    RideRequestResponseDto updateRideRequestStatus(UUID id, String status, UUID currentUserId);

    void deleteRideRequest(UUID id);

    List<RideRequestResponseDto> getRequestsBySenderId(UUID senderUserId); // For sender to view their requests

    List<RideRequestResponseDto> getRequestsForRide(UUID rideUserId); // For driver to view requests for their rides

    RideRequestResponseDto updateStatus(UUID id, String status);
}
