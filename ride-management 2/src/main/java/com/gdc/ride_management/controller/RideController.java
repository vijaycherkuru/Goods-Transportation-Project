package com.gdc.ride_management.controller;

import com.gdc.ride_management.dto.request.RideRequestDto;
import com.gdc.ride_management.dto.response.RideResponseDto;
import com.gdc.ride_management.dto.response.RideSearchResponseDto;
import com.gdc.ride_management.enums.RequiredSpaceType;
import com.gdc.ride_management.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class RideController {

    private final RideService rideService;

    @PostMapping
    @Operation(summary = "Create Ride", description = "Creates a new ride for the authenticated user")
    public ResponseEntity<RideResponseDto> createRide(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody RideRequestDto dto) {

        RideResponseDto createdRide = rideService.createRide(UUID.fromString(userId), dto);
        return ResponseEntity.ok(createdRide);
    }

    @GetMapping("/my")
    @Operation(summary = "Get My Rides", description = "Retrieves all rides for the authenticated driver")
    public ResponseEntity<List<RideResponseDto>> getMyRides(@AuthenticationPrincipal String userId) {
        List<RideResponseDto> myRides = rideService.getRidesByDriverId(UUID.fromString(userId));
        return ResponseEntity.ok(myRides);
    }

    @GetMapping("/{rideId}")
    @Operation(summary = "Get Ride by ID", description = "Retrieves a ride by its ID")
    public ResponseEntity<RideResponseDto> getRideById(@PathVariable UUID rideId) {
        RideResponseDto ride = rideService.getRideById(rideId);
        return ResponseEntity.ok(ride);
    }

    @GetMapping
    @Operation(summary = "Get All Rides", description = "Retrieves all rides for the global dashboard view")
    public ResponseEntity<List<RideResponseDto>> getAllRides() {
        List<RideResponseDto> rides = rideService.getAllRides();
        return ResponseEntity.ok(rides);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRide(@PathVariable UUID id) {
        rideService.deleteRide(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/search")
    public ResponseEntity<List<RideResponseDto>> searchRides(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<RideResponseDto> results = rideService.searchAvailableRides(from, to, date);
        return ResponseEntity.ok(results);
    }
    // ✅ Search Available Rides with Estimated Fare
    // Add @RequestParam int goodsQuantity to the method parameters
    @GetMapping("/search-with-fare")
    public ResponseEntity<List<RideSearchResponseDto>> searchRidesWithFare(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam RequiredSpaceType luggageSpace,
            @RequestParam double goodsWeightInKg,
            @RequestParam int goodsQuantity // <-- Add this line
    )

    {
        List<RideSearchResponseDto> results = rideService.searchAvailableRidesWithFare(
                from, to, date, luggageSpace, goodsWeightInKg, goodsQuantity // <-- Pass goodsQuantity
        );
        return ResponseEntity.ok(results);
    }
    @PostMapping("/user/{userId}")
    public ResponseEntity<RideResponseDto> createRide(
            @PathVariable UUID userId,
            @Valid @RequestBody RideRequestDto requestDto) {

        // ✅ Pass userId to service layer here
        RideResponseDto ride = rideService.createRide(userId, requestDto);
        return ResponseEntity.ok(ride);
    }

    // ✅ Get all rides created by a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RideResponseDto>> getRidesByUserId(@PathVariable UUID userId) {
        List<RideResponseDto> rides = rideService.getRidesByDriverId(userId);
        return ResponseEntity.ok(rides);
    }
}
