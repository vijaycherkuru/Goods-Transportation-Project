package com.gdc.ride_management.repository;

import com.gdc.ride_management.entity.Ride;
import com.gdc.ride_management.enums.RideStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RideRepository extends JpaRepository<Ride, UUID> {

    @EntityGraph(attributePaths = "rideRequests")
    List<Ride> findByFromIgnoreCaseAndToIgnoreCaseAndDateAndRideStatus(
            String from, String to, LocalDate date, RideStatus rideStatus
    );

    @EntityGraph(attributePaths = "rideRequests")
    List<Ride> findByFromIgnoreCaseAndToIgnoreCaseAndDate(String from, String to, LocalDate date);

    @EntityGraph(attributePaths = "rideRequests")
    List<Ride> findByRideUserId(UUID rideUserId);
}