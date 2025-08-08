package com.gdc.ride_management.repository;

import com.gdc.ride_management.entity.RideRequest;
import com.gdc.ride_management.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RideRequestRepository extends JpaRepository<RideRequest, UUID> {
    List<RideRequest> findBySenderUserId(UUID senderUserId);
    List<RideRequest> findByRideIdIn(List<UUID> rideIds);
    List<RideRequest> findByRideIdAndRequestStatus(UUID rideId, RequestStatus requeststatus);

}