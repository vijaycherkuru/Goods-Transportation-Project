package com.gdc.ride_management.controller;

import com.gdc.ride_management.dto.request.RideRequestRequestDto;
import com.gdc.ride_management.dto.response.RideRequestResponseDto;
import com.gdc.ride_management.service.RideRequestService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class RideRequestController {

    private final RideRequestService rideRequestService;

    @PostMapping("/ride-requests")
    @Operation(summary = "Create Ride Request", description = "Creates a request for a ride for the authenticated user")
    public ResponseEntity<RideRequestResponseDto> createRideRequest(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody RideRequestRequestDto dto) {

        RideRequestResponseDto created = rideRequestService.createRideRequest(dto, UUID.fromString(userId));
        return ResponseEntity.ok(created);
    }

    @GetMapping("/my")
    @Operation(summary = "Get My Ride Requests", description = "Fetches all ride requests made by the authenticated user")
    public ResponseEntity<List<RideRequestResponseDto>> getMyRideRequests(
            @AuthenticationPrincipal String userId) {

        List<RideRequestResponseDto> myRequests = rideRequestService.getRequestsBySenderId(UUID.fromString(userId));
        return ResponseEntity.ok(myRequests);
    }

    @GetMapping("/driver")
    @Operation(summary = "Get Driver's Assigned Requests", description = "Fetches all ride requests assigned to the authenticated driver")
    public ResponseEntity<List<RideRequestResponseDto>> getRequestsAssignedToDriver(
            @AuthenticationPrincipal String userId) {

        List<RideRequestResponseDto> assigned = rideRequestService.getRequestsForRide(UUID.fromString(userId));
        return ResponseEntity.ok(assigned);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RideRequestResponseDto> getRideRequestById(@PathVariable UUID id) {
        RideRequestResponseDto dto = rideRequestService.getRideRequestById(id);
        return ResponseEntity.ok(dto);
    }

    // ✅ Update Status of Ride Request (e.g., ACCEPTED, REJECTED)
    @PutMapping("/{id}/status")
    public ResponseEntity<RideRequestResponseDto> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        return ResponseEntity.ok(rideRequestService.updateStatus(id, status));
    }


    // ✅ Delete Ride Request
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRideRequest(@PathVariable UUID id) {
        rideRequestService.deleteRideRequest(id);
        return ResponseEntity.noContent().build();
    }
}
